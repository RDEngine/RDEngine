package org.rdengine.net.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import org.json.JSONObject;

public class RDHttpParams extends JSONObject
{

    public String toUrlString()
    {
        Iterator<String> iter = this.keys();
        StringBuilder sb = new StringBuilder();
        String key, value;
        while (iter.hasNext())
        {
            key = iter.next();
            value = this.optString(key);
            if (value != null)
            {
                if (sb.length() > 0)
                {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                try
                {
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}
