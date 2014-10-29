package org.rdengine.runtime.event;

/**
 * 事件监听,用于接收响应事件分发系统发出的事件
 * 
 * @author yangyu
 */
public interface EventListener
{
    /**
     * @param what
     *            事件名
     * @param arg1
     *            自定义参数1
     * @param arg2
     *            自定义参数2
     * @param dataobj
     *            自定义参数3,Object类型
     */
    public void handleMessage(int what, int arg1, int arg2, Object dataobj);
}
