package com.ziggeo.androidsdk.helper;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String path;
 
    public ImageItem(Bitmap image, String path) {
        super();
        this.image = image;
        this.path = path;
    }
 
    public Bitmap getImage() { return this.image; }
    
    public String getPath() { return this.path; }
 
    public void setImage(Bitmap image) { this.image = image; }
   
}