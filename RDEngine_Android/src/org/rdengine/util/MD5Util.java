package org.rdengine.util;

import java.security.MessageDigest;

public class MD5Util
{
    public static String getMd5(byte[] bytes)
    {
        try
        {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(bytes);
            String str = bytesToHexString(localMessageDigest.digest());
            return str;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytesToHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1)
            {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
