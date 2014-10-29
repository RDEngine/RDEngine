package org.rdengine.runtime.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * 事件分发系统,用于无直接引用实例的对象间传递消息,对逻辑进行解藕
 * 
 * @author yangyu
 */
public class EventManager
{
    private static EventManager manager = null;

    private Hashtable<Integer, ArrayList<ListenerItem>> EventTable = null;

    private LinkedList<ListenerItem> DistributePool = null;

    private Object lock = new Object();

    private EventManager()
    {
        if (EventTable == null)
        {
            EventTable = new Hashtable<Integer, ArrayList<ListenerItem>>();
        } else
        {
            EventTable.clear();
        }

        if (DistributePool == null)
        {
            DistributePool = new LinkedList<EventManager.ListenerItem>();
        } else
        {
            DistributePool.clear();
        }

        synchronized (DistributeThread)
        {
            if (!DistributeThread.isAlive())
            {
                DistributeThread.start();
            }
        }
    }

    public static EventManager ins()
    {
        if (manager == null)
        {
            manager = new EventManager();
        }
        return manager;
    }

    /**
     * 发送消息事件,自定义参数由Listener自已解析
     * 
     * @param what
     *            事件名
     * @param arg1
     *            自定义参数1
     * @param arg2
     *            自定义参数2
     * @param dataobj
     *            自定义参数3,Object类型
     */
    public void sendEvent(int what, int arg1, int arg2, Object dataobj)
    {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = dataobj;
        // Log.d("dmevent", "send what:" + what + " arg1:" + arg1 + " arg2:" + arg2 + " obj:" + dataobj);
        MainHandler.sendMessage(msg);
    }

    private void dealWithEvent(int what, int arg1, int arg2, Object dataobj)
    {
        // Log.d("dmevent", "deal Event what:" + what + " arg1:" + arg1 + " arg2:" + arg2 + " obj:" + dataobj);
        if (EventTable == null)
        {
            return;
        }

        ArrayList<ListenerItem> list = EventTable.get(what);
        if (list == null)
        {
            return;
        }
        ListenerItem item = null, itemsend = null;
        for (int i = 0; i < list.size(); i++)
        {
            item = list.get(i);
            if (item.getWhat() == what)
            {
                if (item.getCallMainThread())
                {
                    item.getListener().handleMessage(what, arg1, arg2, dataobj);
                } else
                {
                    itemsend = new ListenerItem(item.getWhat(), item.getListener(), item.getCallMainThread());
                    itemsend.Arg1 = arg1;
                    itemsend.Arg2 = arg2;
                    itemsend.setData(dataobj);
                    synchronized (DistributePool)
                    {
                        DistributePool.add(itemsend);
                    }

                    try
                    {
                        synchronized (lock)
                        {
                            lock.notify();
                        }

                    } catch (Exception ex)
                    {
                        Log.d("dmevent", "notify error");
                    }
                }
            }
        }
    }

    /**
     * 注册事件监听,*需要在声明对象销毁时调用removeListener*
     * 
     * @param what
     *            事件名
     * @param listener
     *            监听器
     * @param callmainthread
     *            回调是否到主线程
     * @return
     */
    public boolean registListener(int what, EventListener listener, boolean callmainthread)
    {
        ListenerItem item = new ListenerItem(what, listener, callmainthread);

        ArrayList<ListenerItem> list = EventTable.get(what);
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (item.isSameListener(list.get(i)))
                {
                    return false;
                }
            }

            list.add(item);
        } else
        {
            list = new ArrayList<ListenerItem>();
            list.add(item);
            EventTable.put(what, list);
        }

        return true;
    }

    /**
     * 注册事件监听,*需要在声明对象销毁时调用removeListener*
     * 
     * @param what
     *            事件名
     * @param listener
     *            监听器
     * @return
     */
    public boolean registListener(int what, EventListener listener)
    {
        return registListener(what, listener, true);
    }

    /**
     * 注销事件监听器
     * 
     * @param what
     *            事件名
     * @param listener
     *            监听器
     */
    public void removeListener(int what, EventListener listener)
    {
        ArrayList<ListenerItem> list = EventTable.get(what);
        if (list != null)
        {
            ListenerItem item = null;
            for (int i = 0; i < list.size(); i++)
            {
                item = list.get(i);
                if (item.getWhat() == what && item.getListener() == listener)
                {
                    list.remove(i);
                    return;
                }
            }
        }
    }

    /**
     * 注销所有该事件下的监听器
     * 
     * @param what
     *            事件名
     */
    public void reomoveWhat(int what)
    {
        EventTable.remove(what);
    }

    private Thread DistributeThread = new Thread()
    {
        public void run()
        {
            try
            {
                while (true)
                {
                    ListenerItem item = null;
                    while (!DistributePool.isEmpty())
                    {
                        synchronized (DistributePool)
                        {
                            item = DistributePool.getFirst();
                            DistributePool.removeFirst();
                        }
                        item.getListener()
                                .handleMessage(item.getWhat(), item.getArg1(), item.getArg2(), item.getData());
                        item.setData(null);

                    }
                    synchronized (lock)
                    {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    };

    private static Handler MainHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            EventManager.ins().dealWithEvent(msg.what, msg.arg1, msg.arg2, msg.obj);
        }
    };

    private class ListenerItem
    {
        private int What = 0;
        private int Arg1 = 0;
        private int Arg2 = 0;
        private EventListener Listener = null;
        private boolean CallMainThread = false;
        private Object DataObj = null;

        public ListenerItem(int what, EventListener listener, boolean callmainthread)
        {
            What = what;
            Listener = listener;
            CallMainThread = callmainthread;
        }

        public int getWhat()
        {
            return What;
        }

        public int getArg1()
        {
            return Arg1;
        }

        public int getArg2()
        {
            return Arg2;
        }

        public EventListener getListener()
        {
            return Listener;
        }

        public boolean getCallMainThread()
        {
            return CallMainThread;
        }

        private void setData(Object obj)
        {
            DataObj = obj;
        }

        private Object getData()
        {
            return DataObj;
        }

        public boolean isSameListener(ListenerItem item)
        {
            if (item.getWhat() == What && item.getListener() == Listener)
            {
                return true;
            } else
            {
                return false;
            }
        }
    }
}
