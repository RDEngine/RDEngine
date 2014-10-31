package org.rdengine.log;

/**
 * 日志过滤器,定义过滤规则(tag,key关键字),满足条件的执行注册的callback
 * 
 * @author yangyu
 */
public abstract class DMLogFilter
{

    public String KEYTag = "";

    public String[] KEYs = null;

    public DMLogFilter()
    {

    }

    public DMLogFilter(String tag)
    {
        KEYTag = tag;
    }

    public DMLogFilter(String tag, String[] keys)
    {
        KEYTag = tag;
        KEYs = keys;
    }

    public void setKEYTag(String tag)
    {
        KEYTag = tag;
    }

    public void setKEYs(String[] keys)
    {
        KEYs = keys;
    }

    public abstract void callback(String tag, String log);
}
