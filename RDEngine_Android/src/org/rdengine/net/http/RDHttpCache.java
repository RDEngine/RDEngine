package org.rdengine.net.http;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.rdengine.log.DLOG;
import org.rdengine.util.MD5Util;

public class RDHttpCache
{

    private static RDHttpCache instance = null;

    private static String cachePath = "";

    public static RDHttpCache ins()
    {
        if (instance == null)
        {
            instance = new RDHttpCache();
        }

        return instance;
    }

    public void setCachePath(String path)
    {
        cachePath = path;
    }

    public byte[] readCache(String key)
    {
        String path = cachePath + key;
        File file = new File(path);
        if (!file.exists())
        {
            return null;
        }
        DLOG.d("httpcache", "readcache:" + key);
        byte[] buff = null;
        try
        {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            long time = dis.readLong();
            DLOG.d("httpcache", "readcache:" + time);
            if (time < System.currentTimeMillis())
            {
                dis.close();
                fis.close();
                file.delete();
                return null;
            }
            buff = new byte[fis.available()];
            fis.read(buff);
            fis.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return buff;
    }

    public void addCache(String key, byte[] cache, int time)
    {
        String path = cachePath + key;
        File file = new File(path);
        if (file.exists())
        {
            file.delete();
        }
        try
        {
            file.createNewFile();
            long aviliableTime = System.currentTimeMillis() + time * 1000;
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeLong(aviliableTime);
            dos.write(cache);
            dos.close();
            fos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        DLOG.d("httpcache", "addcache:" + key);
    }

    public void deleteCache(String key)
    {
        String path = cachePath + key;
        File file = new File(path);
        if (file.exists())
        {
            file.delete();
        }
    }

    public void cleanCache()
    {
        String path = cachePath;
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                files[i].delete();
            }
        }
    }

    public static String genCacheKey(String url, RDHttpParams urlparams)
    {
        String key;
        if (urlparams == null)
        {
            key = MD5Util.getMd5((url).getBytes());
        } else
        {
            key = MD5Util.getMd5((url + urlparams.toUrlString()).getBytes());

        }
        DLOG.d("httpcache", "genkey:" + key);
        return key;
    }

}
