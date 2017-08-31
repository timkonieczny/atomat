package com.timkonieczny.rss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import java.io.File;

class Image extends DbRow{
    static int TYPE_INLINE = 0;
    static int TYPE_HEADER = -1;
    static int TYPE_ICON = -2;

    String url;
    String fileName;
    Drawable drawable;
    Palette palette;    // TODO: save palette in Db?
    private ImageTask imageTask;
    int width;
    int height;
    private int type;

    Image(int type){
        this.type = type;
    }
    Image(int type, long dbId, String url, String fileName, int width, int height){
        this.dbId = dbId;
        this.url = url;
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    Drawable getDrawable(Context context, ImageListener imageListener, String fileNameSeed, int index, long parentDbId){
        if(drawable != null){
            return drawable;
        }else if(this.fileName !=null){
            loadFromInternalStorage(context);
            return drawable;
        }else if(url != null){
            if(imageTask == null) {
                imageTask = new ImageTask(context, index, generateFileName(fileNameSeed), parentDbId);
                imageTask.execute(this);
            }
            imageTask.imageListener = imageListener;
        }
        return null;
    }

    private void loadFromInternalStorage(Context context){
        BitmapFactory.Options options = new BitmapFactory.Options();
        if(type != TYPE_ICON) options.inSampleSize = getSampleSize();

        Bitmap bitmap = BitmapFactory.decodeFile(
                (new File(context.getFilesDir(), fileName)).getAbsolutePath(), options);
        drawable = new BitmapDrawable(context.getResources(), bitmap);
        if(type!=TYPE_INLINE) palette = (new Palette.Builder(bitmap)).generate();
    }

    private String generateFileName(String name){
        String fileName = name.replaceAll("[^a-zA-Z_0-9]", "").toLowerCase();
        if(fileName.length()>10) fileName = fileName.substring(0, 10);
        fileName += "_" + System.currentTimeMillis() + ".jpg";
        return fileName;
    }

    private int getSampleSize(){
        int dstWidth = MainActivity.viewWidth;
        int sampleSize = 1;
        while (width / (sampleSize*2) > dstWidth) sampleSize*=2;
        if(width/sampleSize-dstWidth < dstWidth-width/(sampleSize*2)) return sampleSize;
        else return sampleSize*2;
    }

    void destroy(Context context){
        if(fileName != null) context.deleteFile(fileName);
    }
}