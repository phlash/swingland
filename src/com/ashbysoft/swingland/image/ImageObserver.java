package com.ashbysoft.swingland.image;

import com.ashbysoft.swingland.Image;

public interface ImageObserver {
    boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height);
}
