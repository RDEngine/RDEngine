package org.rdengine.runtime;

import org.rdengine.util.bitmap.BitmapCache;

import android.app.Application;
import android.os.Environment;

/**
 * The Class DMRT.
 */
public class RT
{

    // --应用配置
    /** The Constant DEBUG. */
    // #define debugenabled='${debug.enabled}'
    // #ifdef debugenabled
    // #expand public static final boolean DEBUG = %debugenabled%;
    // #else
    public static final boolean DEBUG = true;
    // #endif

    public static final boolean PUBLISH = true;

    public static final String ONLINE_CACHE = null;
    public static boolean WriteLog = false;
    private static RT self = null;

    public static Application application = null;

    /** The m local external path. */
    public static String mLocalExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    // 应用的根目录
    /** The Constant ROOT. */
    public static String ROOT = "mofang";

    // 缺省根目录
    /** The default root path. */
    public static String defaultRootPath = mLocalExternalPath.concat("/").concat(ROOT);

    public static boolean hasGetHome = false;

    /**
     * Instantiates a new dmrt.
     * 
     * @param ctx
     *            the ctx
     */
    private RT()
    {

    }

    public static String dealUrl = null;

    /**
     * Ins.
     * 
     * @param ctx
     *            the ctx
     * @return the dmrt
     */
    public static synchronized RT ins()
    {
        if (self == null)
        {
            self = new RT();
        }
        return self;
    }

    // 是否初始化完
    /** The m is init. */
    public static boolean mIsInit = false;

    /**
     * 初始化 Inits the.
     */
    public void init()
    {
        BitmapCache.CACHE_PATH = defaultRootPath + "cache/";
    }
}