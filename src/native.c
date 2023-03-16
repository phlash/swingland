// Implementations of com.ashbysoft.wayland.Native methods
#include <com_ashbysoft_wayland_Native.h>
#include <sys/mman.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>

/*
 * Class:     com_ashbysoft_wayland_Native
 * Method:    createSHM
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_ashbysoft_wayland_Native_createSHM
  (JNIEnv *env, jclass native, jstring name, jint size) {
    const char *shname = (*env)->GetStringUTFChars(env, name, NULL);
    int fd = shm_open(shname, O_CREAT | O_EXCL | O_RDWR, 0600);
    if (fd>=0) {
        if (ftruncate(fd, size)<0) {
            perror("Native: failed to set shared memory size");
            close(fd);
            fd = -1;
        }
        shm_unlink(shname);
    } else {
        perror("Native: failed to open shared memory");
    }
    (*env)->ReleaseStringUTFChars(env, name, shname);
    return fd;
}

/*
 * Class:     com_ashbysoft_wayland_Native
 * Method:    mapSHM
 * Signature: (II)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_com_ashbysoft_wayland_Native_mapSHM
  (JNIEnv *env, jclass native, jint fd, jint size) {
    void *addr = mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (!addr) {
        perror("Native: failed to map shared memory");
        return NULL;
    }
    return (*env)->NewDirectByteBuffer(env, addr, size);
}

/*
 * Class:     com_ashbysoft_wayland_Native
 * Method:    releaseSHM
 * Signature: (ILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_com_ashbysoft_wayland_Native_releaseSHM
  (JNIEnv *env, jclass native, jint fd, jobject bb) {
    void* addr = (*env)->GetDirectBufferAddress(env, bb);
    jlong size = (*env)->GetDirectBufferCapacity(env, bb);
    if (addr) {
        munmap(addr, size);
    }
    if (fd>=0)
        close(fd);
}

static jint getSFD(JNIEnv* env, jclass sclass, jobject sock) {
    // some fugly rummaging to get the  socket file descriptor out of Java..
    jfieldID fdID = (*env)->GetFieldID(env, sclass, "fdVal", "I");
    if (!fdID) {
        fprintf(stderr, "Native: failed to find fdVal field in SocketChannel\n");
        return -1;
    }
    jint sfd = (*env)->GetIntField(env, sock, fdID);
    if (sfd<0) {
        fprintf(stderr, "Native: failed to find valid file descriptor in SocketChannel\n");
        return -1;
    }
    return sfd;
}

/*
 * Class:     com_ashbysoft_wayland_Native
 * Method:    available
 * Signature: (Ljava/lang/Class;Ljava/nio/channels/SocketChannel;)I
 */
JNIEXPORT jint JNICALL Java_com_ashbysoft_wayland_Native_available
  (JNIEnv *env, jclass native, jclass sclass, jobject sock) {
    jint sfd = getSFD(env, sclass, sock);
    jint rv = 0;
    if (ioctl(sfd, FIONREAD, &rv)<0) {
        perror("Native: reading available bytes");
        return -1;
    }
    return rv;
}

/*
 * Class:     com_ashbysoft_wayland_Native
 * Method:    sendFD
 * Signature: (Ljava/nio/channels/SocketChannel;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ashbysoft_wayland_Native_sendFD
  (JNIEnv *env, jclass native, jclass sclass, jobject sock, jobject msg, jint fd) {
    jint sfd = getSFD(env, sclass, sock);
    if (sfd<0)
        return JNI_FALSE;
    // now some sendmsg() wrangling to write the message with ancilliary data holding the fd
    jbyte *mb = (*env)->GetByteArrayElements(env, msg, NULL);
    jsize ml = (*env)->GetArrayLength(env, msg);
    struct iovec iov = {
        .iov_base = mb,
        .iov_len = ml,
    };
    unsigned char cmsg[sizeof(struct cmsghdr)+sizeof(int)];
    struct cmsghdr *cmh = (struct cmsghdr *)cmsg;
    cmh->cmsg_len = CMSG_LEN(sizeof(int));
    cmh->cmsg_level = SOL_SOCKET;
    cmh->cmsg_type = SCM_RIGHTS;
    int* pfd = (int*) CMSG_DATA(cmh);
    *pfd = fd;
    struct msghdr hdr = {
        .msg_name = NULL,
        .msg_namelen = 0,
        .msg_iov = &iov,
        .msg_iovlen = 1,
        .msg_control = cmsg,
        .msg_controllen = cmh->cmsg_len,
        .msg_flags = 0,
    };
    jboolean rv = JNI_TRUE;
    if (sendmsg(sfd, &hdr, 0)!=ml) {
        perror("Native: failed to send message + file descriptor");
        rv = JNI_FALSE;
    }
    (*env)->ReleaseByteArrayElements(env, msg, mb, JNI_ABORT);
    return rv;
}
