package org.rdengine.view.manager;

import org.rdengine.util.StringUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class ViewParam implements Parcelable
{
    public static final int OBJECT_STRING = 0;// 字符串类型
    public static final int OBJECT_MSGOBJ = 1;// msgobj

    public ViewParam()
    {

    }

    public ViewParam(ViewParam parent)
    {
        super();
    }

    public ViewParam(String title)
    {
        super();
        this.title = title;
    }

    public ViewParam setData(Object data)
    {
        this.data = data;
        return this;
    }

    public ViewParam setData1(Object data1)
    {
        this.data1 = data1;
        return this;
    }

    public ViewParam(String title, String type)
    {
        super();
        this.title = title;
        this.type = type;
    }

    public String title;// 标题
    public String type = "";// 类型标识
    public String road_ids = "";

    public int objectType = 0;
    public Object data;
    public Object data1;// 附属数据

    public int index;

    public static ViewParam EMPTY = new ViewParam();

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof ViewParam)
        {
            ViewParam vp = (ViewParam) o;

            if (!StringUtil.isEmpty(type) && !StringUtil.isEmpty(vp.type) && type.equals(vp.type))
            {
                return true;
            }

        }

        return false;
    }

    @Override
    public int describeContents()
    {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

        dest.writeString(title);
        dest.writeString(type);
        dest.writeInt(objectType);
        if (data instanceof Parcelable)
        {
            dest.writeParcelable((Parcelable) data, flags);
        } else if (data instanceof String)
        {
            dest.writeString(data.toString());
        }
    }

    public static final Parcelable.Creator<ViewParam> CREATOR = new Creator<ViewParam>()
    {

        @Override
        public ViewParam[] newArray(int size)
        {

            return null;
        }

        @Override
        public ViewParam createFromParcel(Parcel source)
        {
            ViewParam vp = new ViewParam();
            vp.title = source.readString();
            vp.type = source.readString();
            vp.objectType = source.readInt();
            switch (vp.objectType)
            {
            case OBJECT_STRING :
                vp.data = source.readString();
                break;
            case OBJECT_MSGOBJ :
                break;
            }
            return vp;
        }
    };

    @Override
    public String toString()
    {
        return "ViewParam [title=" + title + ", type=" + type + ", data=" + data + "]";
    }

}
