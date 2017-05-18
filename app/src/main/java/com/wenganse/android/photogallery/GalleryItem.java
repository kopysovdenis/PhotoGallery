package com.wenganse.android.photogallery;

/**
 * Created by Plus on 14.05.2017.
 */
//Объект модели
public class GalleryItem {
    private String  mCaption;
    private String  mId;
    private String  mUri;

    @Override
    public String toString() {
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = uri;
    }
}
