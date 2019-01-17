package com.proxy.kiwi.image;

import java.io.InputStream;

public class KImage extends javafx.scene.image.Image {
    KMetadata metadata;

    public KImage(String url) {
        super(url);
    }

    public KImage(String url, boolean backgroundLoading) {
        super(url, backgroundLoading);
    }

    public KImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
        super(url, requestedWidth, requestedHeight, preserveRatio, smooth);
    }

    public KImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
        super(url, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading);
    }

    public KImage(InputStream is) {
        super(is);
    }

    public KImage(InputStream is, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
        super(is, requestedWidth, requestedHeight, preserveRatio, smooth);
    }


}
