package org.rdengine.util;

/**
 * 防止用户多次连续暴力点击
 * @author choices
 *
 */

public class ClickUtil
{
    private static long lastClickTime;

    public static boolean isFastDoubleClick()
    {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 300)
        {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
