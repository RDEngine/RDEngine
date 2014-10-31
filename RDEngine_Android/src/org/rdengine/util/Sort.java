package org.rdengine.util;

import java.util.Comparator;
import java.util.HashMap;

public class Sort<T> implements Comparator<T>
{

    public Sort()
    {

    }

    HashMap<Object, String> pyCache = new HashMap<Object, String>();

    public String getSortString(T obj)
    {
        return "";
    }

    public String getPinYin(T obj)
    {
        String py = pyCache.get(obj);
        if (StringUtil.isEmpty(py))
        {
            CollatorPinyin ctpy = CollatorPinyin.getInstance();
            py = ctpy.GetFirstPinyin(getSortString(obj));
            pyCache.put(obj, py);
        }
        return py;

    }

    public void clear()
    {
        pyCache.clear();
    }

    String c1 = "", c2 = "", c11 = "", c21 = "";

    @Override
    public int compare(T obj1, T obj2)
    {
        // LOG.d("Sort", ">>" + obj1 + "," + obj2);
        if (obj1 == null || obj2 == null)
        {
            return 0;
        }

        if (obj1 == obj2)
        {
            return 0;
        }

        c1 = getPinYin(obj1);
        c2 = getPinYin(obj2);
        c11 = getSortString(obj1);
        c21 = getSortString(obj2);

        if (!isInLetter(c1) && !isInLetter(c2))
        {
            return 0;
        }

        if (!isInLetter(c1))
        {
            return 1;
        }
        if (!isInLetter(c2))
        {
            return -1;
        }
        int result = 0;
        for (int i = 0; i < c1.length(); i++)
        {
            if (i >= c2.length())
            {
                return 1;
            }

            result = c1.charAt(i) - c2.charAt(i);
            if (result != 0)
            {
                return result;
            }
        }

        for (int i = 0; i < c11.length(); i++)
        {
            if (i >= c21.length())
            {
                return 1;
            }

            result = c11.charAt(i) - c21.charAt(i);
            if (result != 0)
            {
                return result;
            }
        }

        return 0;
    }

    public static boolean isInLetter(String src)
    {
        if (src == null || src.length() < 1)
        {
            return false;
        }
        char c = src.charAt(0);
        // LOG.d("Sort", ">>" + c);
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
        {
            return true;
        }
        return false;
    }
}
