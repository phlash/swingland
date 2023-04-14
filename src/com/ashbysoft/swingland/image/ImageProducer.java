package com.ashbysoft.swingland.image;

public interface ImageProducer {
    void addConsumer(ImageConsumer c);
    boolean isConsumer(ImageConsumer c);
    void removeConsumer(ImageConsumer c);
    void startProduction(ImageConsumer c);
}
