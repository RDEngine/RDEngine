package org.rdengine.log;

import java.util.Vector;

public class DMLogFilterManager
{

    /**
     * 日志过滤器规则定义类
     */
    public DMLogFilterManager()
    {
        if (Filters == null)
        {
            Filters = new Vector<DMLogFilter>();
        }
        loadAllFilter();
    }

    public Vector<DMLogFilter> Filters = null;

    static DMLogFilterManager manager = null;

    public static DMLogFilterManager instance()
    {
        if (manager == null)
        {
            manager = new DMLogFilterManager();
        }
        return manager;
    }

    void loadAllFilter()
    {
        if (Filters == null)
        {
            Filters = new Vector<DMLogFilter>();
        }

        Filters.clear();
        // 实例并增加filter
        DMLogFilter filter = new BILogFilter();
        Filters.add(filter);
    }

    /*----------------------------------------------------------------------------------------------------*/

    class BILogFilter extends DMLogFilter
    {

        public BILogFilter()
        {
            this.KEYTag = "BI";
            this.KEYs = null;
        }

        public void callback(String tag, String log)
        {
            sendBILog(log);
        }

        void sendBILog(String log)
        {

        }

    }
}
