package com.wenganse.android.photogallery;

import android.net.Uri;

/**
 * Created by Plus on 14.05.2017.
 */
//Объект модели
public class GalleryItem {
    private String  mCaption;
    private String  mId;
    private String  mUri;
    private String mOwner;

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

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }
    //для построения страницы.
    public Uri getPhotoPageUri(){
        return Uri.parse( "http://www.flikr.com/photos/" )
                .buildUpon()
                .appendPath( mOwner )
                .appendPath(mId )
                .build();
    }
}
