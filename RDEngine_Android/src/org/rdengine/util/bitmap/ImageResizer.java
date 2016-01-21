/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rdengine.util.bitmap;

import java.io.InputStream;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width and height. Useful for when the input images might be too large to simply load directly into memory.
 */
public class ImageResizer
{
    private static final String TAG = "ImageResizer";

    // private static Object lock = new Object();
    // private static Object lock4 = new Object();
    // private static Object lock3 = new Object();
    // private static Object lock2 = new Object();
    // private static Object lock1 = new Object();

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     * 
     * @param filename
     *            The full path of the file to decode
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions that are equal to or greater than the requested width and height
     * @throws Throwable
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) throws Throwable
    {
        // synchronized (lock1)
        {
            int sample = 0;
            try
            {
                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filename, options);

                sample = calculateInSampleSize(options, reqWidth, reqHeight);
                // Calculate inSampleSize
                options.inSampleSize = sample;

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                // DLOG.e("Image", "warn,path="+filename);
                return BitmapFactory.decodeFile(filename, options);
            } catch (Throwable e)
            {
                e.printStackTrace();
                Log.e("Image", "error,samplesize=" + sample + ",path=" + filename);
                if (e instanceof OutOfMemoryError)
                {
                    throw e;
                }
            }
        }
        return null;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates the closest inSampleSize that will result in the final decoded bitmap having a width and height equal to or larger than the requested width and height. This implementation does not ensure a power of 2 is returned for inSampleSize which can be faster when decoding but results in a larger bitmap which isn't as
     * useful for caching purposes.
     * 
     * @param options
     *            An options object with out* params already populated (run through a decode* method with inJustDecodeBounds==true
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqHeight <= 0 || reqWidth <= 0)
        {
            return inSampleSize;
        }

        if (height > reqHeight || width > reqWidth)
        {
            if (width > height)
            {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else
            {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap)
            {
                inSampleSize++;
            }
        }
        return inSampleSize;
        // final int height = options.outHeight;
        // final int width = options.outWidth;
        // int inSampleSize = 1;
        //
        // if (height > reqHeight || width > reqWidth) {
        //
        // // Calculate ratios of height and width to requested height and width
        // final int heightRatio = Math.round((float) height / (float) reqHeight);
        // final int widthRatio = Math.round((float) width / (float) reqWidth);
        //
        // // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
        // // with both dimensions larger than or equal to the requested height and width.
        // inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        //
        // // This offers some additional logic in case the image has a strange
        // // aspect ratio. For example, a panorama may have a much larger
        // // width than height. In these cases the total pixels might still
        // // end up being too large to fit comfortably in memory, so we should
        // // be more aggressive with sample down the image (=larger inSampleSize).
        //
        // final float totalPixels = width * height;
        //
        // // Anything more than 2x the requested pixels we'll sample down further
        // final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        //
        // while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
        // inSampleSize++;
        // }
        // }
        // return inSampleSize;

    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     * 
     * @param res
     *            The resources object containing the image data
     * @param resId
     *            The resource id of the image data
     * @param reqWidth
     *            The requested width of the resulting bitmap
     * @param reqHeight
     *            The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight)
    {
        // synchronized (lock2 )
        {

            /*
             * InputStream is = this.getResources().openRawResource(R.drawable.pic1); BitmapFactory.Options options = new BitmapFactory.Options(); options.inJustDecodeBounds = false; options.inSampleSize = 10; // width，hight设为原来的十分一 Bitmap btp = BitmapFactory.decodeStream(is, null, options);
             */
            Bitmap bitmap = null;
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            InputStream is = null;
            int sample = 0;
            try
            {
                // options.inJustDecodeBounds = true;
                // bitmap = BitmapFactory.decodeResource(res, resId, options);
                // recycle(bitmap);
                // bitmap = null;
                // // Calculate inSampleSize
                // options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                // // Decode bitmap with inSampleSize set
                // options.inJustDecodeBounds = false;
                // bitmap = BitmapFactory.decodeResource(res, resId, options);
                options.inJustDecodeBounds = true;
                is = res.openRawResource(resId);
                bitmap = BitmapFactory.decodeStream(is, null, options);
                recycle(bitmap);
                bitmap = null;
                is.close();
                sample = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inSampleSize = sample;
                options.inJustDecodeBounds = false;
                is = res.openRawResource(resId);
                bitmap = BitmapFactory.decodeStream(is, null, options);
                return bitmap;
            } catch (Throwable e)
            {
                e.printStackTrace();
                DLOG.e("ImageResize", "decode Resource Error!!resId>>" + resId + " sampleSize>>" + sample);
                // System.gc();
            } finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] byteArray, int reqWidth, int reqHeight)
    {
        // synchronized (lock3)
        {
            Bitmap bitmap = null;
            try
            {
                if (byteArray == null || byteArray.length == 0)
                {
                    return null;
                }
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
                recycle(bitmap);
                bitmap = null;
                if (options.outHeight == -1 || options.outWidth == -1)
                {
                    return null;
                }
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inDither = true;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
            } catch (Throwable e)
            {
                // DLOG.e("Image", "error");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 不对色值处理
     * 
     * @param byteArray
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromByteArrayNotColor(byte[] byteArray, int reqWidth, int reqHeight)
    {
        // synchronized (lock4)
        {
            Bitmap bitmap = null;
            try
            {
                if (byteArray == null || byteArray.length == 0)
                {
                    return null;
                }
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
                recycle(bitmap);
                bitmap = null;
                if (options.outHeight == -1 || options.outWidth == -1)
                {
                    return null;
                }
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inDither = true;
                options.inJustDecodeBounds = false;
                // options.inPreferredConfig = Bitmap.Config.RGB_565;
                return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
            } catch (Throwable e)
            {
                // DLOG.e("Image", "error");

            }
        }
        return null;
    }

    public static void recycle(Bitmap bitmap)
    {
        if (bitmap != null)
        {

            // if (!PhoneUtil.hasHoneycombMR1())
            {
                if (RT.DEBUG)
                {
                    DLOG.e("recycle", "true");
                }
                bitmap.recycle();
            }

        }
    }
}
