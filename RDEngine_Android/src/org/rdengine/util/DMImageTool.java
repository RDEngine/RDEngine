package org.rdengine.util;

import java.io.InputStream;

import org.rdengine.log.DLOG;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;

public class DMImageTool
{
    private static Object lock = new Object();

    /**
     * 非安全，没有回收原图
     * 
     * @param hole
     *            the hole
     * @param src
     *            the src
     * @return the bitmap
     */
    private static Bitmap digImage(Bitmap hole, Bitmap src)
    {
        int[] pp = null;
        int[] ppp = null;
        // synchronized (lock)
        {
            // int[] dstarray = null;
            if (hole == null || src == null)
            {
                // DLOG.e("Image", "Error  >> null");
                return null;
            }
            int bw = hole.getWidth();
            int bh = hole.getHeight();
            try
            {
                pp = new int[bw * bh];
                ppp = new int[bw * bh];
                // dstarray = new int[bw * bh];
            } catch (Throwable e)
            {
                // memory error
                return null;
            }
            Bitmap dst;
            int p, alpha = 0;
            int srcc = 0;
            src.getPixels(pp, 0, bw, 0, 0, bw, bh);
            hole.getPixels(ppp, 0, bw, 0, 0, bw, bh);
            for (int i = 0; i < pp.length; i++)
            {
                alpha = ppp[i];
                srcc = pp[i];
                if (alpha == 1)
                {
                    p = srcc;
                } else if (alpha == 0)
                {
                    // continue;
                    p = 0x00000000;
                } else
                {
                    p = ((alpha) | (srcc & 0xffffff));
                    // p = ((alpha) | (srcc & 0x000000));
                }
                // dstarray[i] = p;
                pp[i] = p;
            }
            // dst = Bitmap.createBitmap(dstarray, bw, bh, Config.ARGB_8888);
            ppp = null;
            dst = Bitmap.createBitmap(pp, bw, bh, Config.ARGB_8888);
            pp = null;
            return dst;
        }
    }

    // public static Drawable digHole(InputStream holeStream, BitmapDrawable srcDrawable)
    // {
    // Bitmap hole = null;
    // Bitmap src = null;
    // Bitmap dst = null;
    // synchronized (lock)
    // {
    // try
    // {
    // hole = BitmapFactory.decodeStream(holeStream);
    // src = srcDrawable.getBitmap();
    //
    // src = Bitmap.createScaledBitmap(src, hole.getWidth(), hole.getHeight(), true);
    // dst = digImage(hole, src);
    //
    // holeStream.close();
    // } catch (Exception e)
    // {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (Error ex)
    // {
    // ex.printStackTrace();
    // // DLOG.e("Image", "Error");
    // } finally
    // {
    //
    // try
    // {
    // // if (!PhoneUtil.hasHoneycombMR1())
    // {
    // hole.recycle();
    // src.recycle();
    // }
    // } catch (Exception e)
    // {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // hole = null;
    // src = null;
    // }
    // }
    // // System.gc();
    // return new BitmapDrawable(RT.application.getResources(), dst);
    // }

    public static Bitmap digHole(InputStream holeStream, Bitmap src)
    {
        // DLOG.i("Image", ">>打洞");
        Bitmap hole = null;
        Bitmap dst = null;
        Bitmap tmp = null;
        synchronized (lock)
        {

            try
            {
                hole = BitmapFactory.decodeStream(holeStream);
                tmp = Bitmap.createScaledBitmap(src, hole.getWidth(), hole.getHeight(), true);
                dst = digImage(hole, tmp);
            } catch (Throwable e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    holeStream.close();
                } catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                try
                {
                    // if (!PhoneUtil.hasHoneycombMR1())
                    {
                        if (hole != null && dst != hole)
                            hole.recycle();
                        if (src != null && dst != src)
                            src.recycle();
                        if (tmp != null && dst != tmp)
                            tmp.recycle();
                    }
                } catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                hole = null;
                src = null;
                tmp = null;
                // System.gc();
            }
        }
        return dst;
    }

    public static Bitmap cutImage(Bitmap src)
    {

        int bit_x, bit_y, bit_width, bit_height;

        bit_y = 2;
        bit_height = src.getHeight() - 2 * bit_y;
        bit_width = bit_height * 10 / 16;
        bit_x = (src.getWidth() - bit_width) / 2 + 1;
        Bitmap ret = null;
        synchronized (lock)
        {

            try
            {
                ret = Bitmap.createBitmap(src, bit_x, bit_y, bit_width, bit_height);
                if (ret != src)
                {
                    if (src != null && !src.isRecycled())
                    {
                        src.recycle();
                        DLOG.e("dmimage", "recyle old bitmap!!");
                    }
                }
            } catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
        return ret;

    }

    public static int getRotationForImage(String path)
    {
        int rotation = 0;

        try
        {
            ExifInterface exif = new ExifInterface(path);
            rotation = (int) exifOrientationToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return rotation;
    }

    public static float exifOrientationToDegrees(int exifOrientation)
    {

        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
        {

            return 90;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
        {

            return 180;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
        {

            return 270;

        }

        return 0;

    }

    /**
     * 柔化效果(高斯模糊)(优化后比上面快三倍)
     * 
     * @param bmp
     * @return
     */
    public static Bitmap blurImageAmeliorate(Bitmap bmp)
    {
        long start = System.currentTimeMillis();
        // 高斯矩阵
        int[] gauss = new int[]
        { 1, 2, 1, 2, 4, 2, 1, 2, 1 };

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;

        int delta = 16; // 值越小图片会越亮，越大则越暗

        int idx = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++)
        {
            for (int k = 1, len = width - 1; k < len; k++)
            {
                idx = 0;
                for (int m = -1; m <= 1; m++)
                {
                    for (int n = -1; n <= 1; n++)
                    {
                        pixColor = pixels[(i + m) * width + k + n];
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);

                        newR = newR + (int) (pixR * gauss[idx]);
                        newG = newG + (int) (pixG * gauss[idx]);
                        newB = newB + (int) (pixB * gauss[idx]);
                        idx++;
                    }
                }

                newR /= delta;
                newG /= delta;
                newB /= delta;

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixels[i * width + k] = Color.argb(255, newR, newG, newB);

                newR = 0;
                newG = 0;
                newB = 0;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        return bitmap;
    }

    /** 水平方向模糊度 */
    private static float hRadius = 10;
    /** 竖直方向模糊度 */
    private static float vRadius = 10;
    /** 模糊迭代度 */
    private static int iterations = 7;

    /**
     * 高斯模糊
     */
    public static Bitmap BoxBlurFilter(Bitmap bmp)
    {
        long start = System.currentTimeMillis();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < iterations; i++)
        {
            blur(inPixels, outPixels, width, height, hRadius);
            blur(outPixels, inPixels, height, width, vRadius);
        }
        blurFractional(inPixels, outPixels, width, height, hRadius);
        blurFractional(outPixels, inPixels, height, width, vRadius);
        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
        
        long end = System.currentTimeMillis();
        DLOG.d("blur", "cost:"+(end - start));
        
        return bitmap;
    }

    public static void blur(int[] in, int[] out, int width, int height, float radius)
    {
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = 2 * r + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        for (int y = 0; y < height; y++)
        {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -r; i <= r; i++)
            {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++)
            {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + r + 1;
                if (i1 > widthMinus1)
                    i1 = widthMinus1;
                int i2 = x - r;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    public static void blurFractional(int[] in, int[] out, int width, int height, float radius)
    {
        radius -= (int) radius;
        float f = 1.0f / (1 + 2 * radius);
        int inIndex = 0;

        for (int y = 0; y < height; y++)
        {
            int outIndex = y;

            out[outIndex] = in[0];
            outIndex += height;
            for (int x = 1; x < width - 1; x++)
            {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];

                int a1 = (rgb1 >> 24) & 0xff;
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int a2 = (rgb2 >> 24) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                int a3 = (rgb3 >> 24) & 0xff;
                int r3 = (rgb3 >> 16) & 0xff;
                int g3 = (rgb3 >> 8) & 0xff;
                int b3 = rgb3 & 0xff;
                a1 = a2 + (int) ((a1 + a3) * radius);
                r1 = r2 + (int) ((r1 + r3) * radius);
                g1 = g2 + (int) ((g1 + g3) * radius);
                b1 = b2 + (int) ((b1 + b3) * radius);
                a1 *= f;
                r1 *= f;
                g1 *= f;
                b1 *= f;
                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    public static int clamp(int x, int a, int b)
    {
        return (x < a) ? a : (x > b) ? b : x;
    }

}
