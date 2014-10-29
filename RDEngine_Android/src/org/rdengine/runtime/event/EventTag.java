package org.rdengine.runtime.event;

public class EventTag
{

    public static final int CHAT_NOTIFY_ROOM = 0x1001;// 公聊消息拉取刷新
    public static final int CHAT_NOTIFY_CONNECT = 0x1002;// socket连接
    public static final int CHAT_NOTIFY_PRI = 0x1003;// 私聊消息拉取刷新
    public static final int CHAT_NOTIFY_PRI_SHOW_NOTITICATION = 0x1004;// 私聊弹出通知栏
    public static final int CHAT_NOTIFY_REFRESH_SESSION = 0x1005;// 刷现session列表
    public static final int CHAT_NOTIFY_CHOOSE_PHOTO = 0x1006;// 选取图片了
    public static final int CHAT_NOTIFY_FRIEND_SHOW_NOTIFYCATION = 0x1007;// 申请好友弹出通知栏
    public static final int CHAT_NOTIFY_ACTIVITY = 0x1008;// 推送活动
    public static final int CHAT_CANCEL_NOTIFY = 0x1012;// 取消通知栏
    public static final int CHAT_NOTIFY_GROUP = 0x1013;// 群组消息拉取刷新
    public static final int CHAT_NOTIFY_GROUP_SHOW_NOTIFICATION = 0x1014;// 群组消息弹出通知栏
    public static final int CHAT_NOTIFY_POST = 0x1015;
    public static final int CHAT_NOTIFY_POST_SHOW_NOTIFY = 0x1016;
    public static final int CHAT_NOTIFY_SYSTEM = 0x1017;
    public static final int CHAT_NOTIFY_SYSTEM_SHOW_NOTIFY = 0x1018;
    public static final int CHAT_CLOSE_CONNECT = 0x1019;// 断开连接

    public static final int ACCOUNT_LOGIN = 0x2001;// 登录成功
    public static final int ACCOUNT_LOGOUT = 0x2002;// 注销成功
    public static final int ACCOUNT_UPDATE_INFO = 0x2003;// 用户信息修改
    public static final int ACCOUNT_GET_PREUID = 0x2004;// 获取到预注册uid
    public static final int ACCOUNT_REQUEST_FRIEND_UPDATE = 0x2005;// 请求添加好友数量更新
    public static final int ACCOUNT_FRIEND_UPDATE = 0x2006;// 好友列表更新
    public static final int ACCOUNT_BIND_PHONE = 0x2007;// 绑定了手机号码 obj = long[]{_phn,_uid};

    public static final int GAME_BUTTON_UPDATE = 0x3001;// 游戏主页button有更新
    public static final int TOOL_DOWNLOAD = 0X3002;// 工具下载状态更新
    public static final int GAME_ADD_REMOVE = 0x3003;// 游戏更新或卸载
    public static final int FLOAT_UPDATE_NEW = 0x3004;// 浮窗有更新
    public static final int FLOAT_LAST_MESSAGE = 0x3005;// 快捷模式刷新最后一条消息
    public static final int FLOAT_REFRESH_MESSAGE = 0x3006;// 快捷模式刷新消息
    public static final int FLOAT_PUB_CHAT_TIP = 0x3007;// 浮窗聊天提示
    public static final int FLOAT_PUSH_POPUP_SHOW = 0x3008;// 浮窗推送泡泡显示

    public static final int RECOMMEND_ACTIVITY = 0x3009;// 有推荐的活动
    public static final int APP_GUIDE = 0x3010;// 新手引导
    public static final int GUILD_LIST_CHANGED = 0x3011;// 公会列表变更或者信息变更
    public static final int GUILD_INFO_CHANGED = 0x3012;// 公会信息变更
    public static final int GUILD_MEMBER_MENAGE = 0x3013;// 公会成员管理
    public static final int GUILD_SIGN_IN = 0x3014;// 公会成员签到
    public static final int GIFT_LIST_REFRESH = 0x3015;// 礼包列表刷新
    public static final int FEED_LIST_REFRESH = 0x3016;// 论坛列表刷新

    public static final int CHAT_GROUP_ACTIVITY_HIDE = 0x3017;// 隐藏聊天室活动

    public static final int TASK_FINISH_NOTIFY = 0x3018;// 任务消息通知
    public static final int CONVERT_LIST_REFRESH = 0x3019;// 兑换中心列表刷新
    public static final int UPDATE_TAOHAO_CODE = 0x3020;// 刷新淘号礼包码

    public static final int FLOAT_WINDOW_SHOW_PANEL = 0x4001;// 显示主浮窗

    public static final int SCAN_ACTIVITY_DESKTOP = 0x4002;// 切换到桌面
    public static final int SCAN_ACTIVITY_APP = 0x4003;// 切换到应用
    public static final int SCAN_ACTIVITY_SELF = 0x4004;// 切换到自己
}
