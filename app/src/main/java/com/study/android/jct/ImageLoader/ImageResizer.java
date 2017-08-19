package com.study.android.jct.ImageLoader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**实现图片的压缩功能
 * Created by 10764 on 2017/8/18.
 */

public class ImageResizer {
    private static final String TAG = "ImageResizer";

    public ImageResizer(){}

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resid, int reqWidth, int reqHeight){
        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resid,options);

        //Calculate inSampleSize
        options.inSampleSize = calculateInsampleSize(options,reqWidth,reqHeight);

        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,resid,options);
    }

    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fileDescriptor,int reqWidth, int reqHeight){
        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);

        //Calculate inSampleSize
        options.inSampleSize = calculateInsampleSize(options,reqWidth,reqHeight);

        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);
    }

    private int calculateInsampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (reqHeight == 0 || reqWidth == 0){
            return 1;
        }

        //Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(TAG, "origin, w = " + width + " h = " + height);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqHeight){
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //Calculate the largest inSampleSize value that is a power of 2 and keeps both
            //height and width largest than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, "sampleSize:" + inSampleSize);
        return inSampleSize;
    }
}
