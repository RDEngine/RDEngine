package org.rdengine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ID3V2
{
    private String TAG = "ID3V2";
    public String Title = "";
    public String Artist = "";
    public String Album = "";
    public String Style = "";
    public String Compose = "";
    public String Duration = "0.0";
    public String Filename = "";
    public String path = "";
    public String Size = "";
    public byte[] imgData = null;
    public String comments = "";

    public boolean ID3V2loaded = false;

    public boolean loadID3V2(String path, boolean loadimg)
    {

        FileInputStream fis = null;
        try
        {
            // System.gc();
            File f = new File(path);
            if (!f.exists() || !f.isFile())
            {
                return false;
            }
            try
            {
                fis = new FileInputStream(f);
                Size = fis.available() + "";
                Filename = f.getName();
                if (f.getParentFile().isDirectory())
                {
                    this.path = f.getParent();
                }
            } catch (Exception e)
            {
                // e.printStackTrace();
                if (fis != null)
                {
                    fis.close();
                    fis = null;
                }
                return false;
            }
            byte[] buff = new byte[3];
            fis.read(buff);
            if (!new String(buff).equals("ID3"))
            {
                fis.close();
                fis = null;
                // return false;
                // throw new Exception("no ID3 head");
            }
            char ver = (char) fis.read();// ID3V2版本号
            char revision = (char) fis.read();// 副版本号
            char flag = (char) fis.read();// 标志
            System.out.println(ver + ":" + revision + ":" + flag);
            buff = new byte[4];
            fis.read(buff);
            int headLength = ((buff[0] & 0x7F) << 21) + ((buff[1] & 0x7F) << 14) + ((buff[2] & 0x7F) << 7)
                    + (buff[3] & 0x7F);// ID3头长度
            byte[] head = new byte[headLength];

            fis.read(head);

            fis.close();
            fis = null;

            int offset = 0;

            String frameID = "";
            int length = 0;
            byte[] content = null;
            byte[] imgData = null;
            byte[] strdata = null;
            while (true)
            {
                buff = new byte[4];
                System.arraycopy(head, offset, buff, 0, 4);
                frameID = new String(buff);
                System.arraycopy(head, offset + 4, buff, 0, 4);
                length = ((buff[0] & 0xFF) << 24) + ((buff[1] & 0xFF) << 16) + ((buff[2] & 0xFF) << 8)
                        + (buff[3] & 0xFF);
                try
                {
                    if (length > 1024 * 5)
                    {
                        // DLOG.d(TAG, path);
                        // DLOG.d(TAG, length + "");
                        return false;
                    }
                    content = new byte[length];
                } catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                }
                // System.out.println("head: " + head.length + " offset: "
                // + offset + " cotent: " + content.length);
                System.arraycopy(head, offset + 10, content, 0, length);
                // if (content[0] == 0x01) {
                // try {
                // if (frameID.equals("COMM")){
                // LOG.d("scandisk", "(utf-16)frameID:"+frameID+" content:"+new
                // String(content,8,content.length-8,"utf-16"));
                // }else{
                // LOG.d("scandisk", "(utf-16)frameID:"+frameID+" content:"+new
                // String(content,1,content.length-1,"utf-16"));
                // }
                //
                // } catch (Exception ex) {
                // }
                // } else if (content[0] == 0x00) {
                // try {
                // LOG.d("scandisk", "(GBK)frameID:"+frameID+" content:"+new String(content,1,content.length-1,"GBK"));
                // } catch (Exception ex) {
                // }
                // }

                if (frameID.equals("TIT2"))// 标题
                {
                    if (!ID3V2loaded)
                    {
                        strdata = new byte[content.length - 1];
                        System.arraycopy(content, 1, strdata, 0, strdata.length);
                        if (content[0] == 0x01)
                        {
                            try
                            {
                                Title = new String(strdata, "UTF-16").trim();
                            } catch (Exception ex)
                            {
                            }
                        } else if (content[0] == 0x00)
                        {
                            Title = new String(strdata, "GBK").trim();
                        }
                    }
                } else if (frameID.equals("TPE1"))// 作者
                {
                    if (!ID3V2loaded)
                    {
                        strdata = new byte[content.length - 1];
                        System.arraycopy(content, 1, strdata, 0, strdata.length);
                        if (content[0] == 0x01)
                        {
                            try
                            {
                                Artist = new String(strdata, "UTF-16").trim();
                            } catch (Exception ex)
                            {
                            }
                        } else if (content[0] == 0x00)
                        {
                            Artist = new String(strdata, "GBK").trim();
                        }
                    }
                } else if (frameID.equals("TALB"))// 专辑
                {
                    if (!ID3V2loaded)
                    {
                        strdata = new byte[content.length - 1];
                        System.arraycopy(content, 1, strdata, 0, strdata.length);
                        if (content[0] == 0x01)
                        {
                            try
                            {
                                Album = new String(strdata, "UTF-16").trim();
                            } catch (Exception ex)
                            {
                            }
                        } else if (content[0] == 0x00)
                        {
                            Album = new String(strdata, "GBK").trim();
                        }
                    }
                } else if (frameID.equals("TCOM"))// 作曲
                {
                    if (!ID3V2loaded)
                    {
                        strdata = new byte[content.length - 1];
                        System.arraycopy(content, 1, strdata, 0, strdata.length);
                        if (content[0] == 0x01)
                        {
                            try
                            {
                                Compose = new String(strdata, "UTF-16").trim();
                            } catch (Exception ex)
                            {
                            }
                        } else if (content[0] == 0x00)
                        {
                            Compose = new String(strdata, "GBK").trim();
                        }
                    }
                } else if (frameID.equals("TCON"))// 类型
                {
                    if (!ID3V2loaded)
                    {
                        Style = new String(content).trim();
                    }
                } else if (frameID.equals("TDLY"))// 播放延时
                {
                    if (!ID3V2loaded)
                    {
                        Duration = new String(content).trim();
                    }
                } else if (frameID.equals("TSIZ"))// 音频长度
                {
                    if (!ID3V2loaded)
                    {
                        Size = new String(content).trim();
                    }
                } else if (frameID.equals("APIC"))// 图片
                {
                    if (loadimg)
                    {
                        try
                        {
                            int picoff = 0, zerocont = 0;
                            for (int i = 0; i < content.length; i++)
                            {
                                if (content[i] == 0)
                                {
                                    System.out.println(i);
                                    zerocont++;
                                }
                                if (zerocont == 3)
                                {
                                    picoff = i + 1;
                                    break;
                                }
                            }
                            imgData = new byte[content.length - picoff];
                            System.arraycopy(content, picoff, imgData, 0, imgData.length);
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                } else if ("COMM".equals(frameID))
                {
                    if (!ID3V2loaded)
                    {
                        strdata = new byte[content.length - 4];
                        System.arraycopy(content, 4, strdata, 0, strdata.length);
                        if (content[0] == 0x01)
                        {
                            try
                            {
                                comments = new String(strdata, "UTF-16").trim();
                            } catch (Exception ex)
                            {
                            }
                        } else if (content[0] == 0x00)
                        {
                            comments = new String(strdata, "GBK").trim();
                        }
                    }
                }

                offset += (10 + length);
                if (offset >= head.length - 10)
                {
                    break;
                }
            }

            if (!ID3V2loaded)
            {
                ID3V2loaded = true;
            }
        } catch (Exception ex)
        {
            // ex.printStackTrace();
            if (fis != null)
            {
                try
                {
                    fis.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                fis = null;
            }
        }
        if (Title == null || Title.equals("") || Title.equals("unKnown"))
        {
            Title = Filename.substring(0, Filename.indexOf('.'));
        }
        return true;
    }

    public void clear()
    {
        Title = "";
        Artist = "";
        Album = "";
        Style = "";
        Compose = "";
        Duration = "0.0";
        Filename = "";
        path = "";
        Size = "";
        imgData = null;
        ID3V2loaded = false;
        comments = "";
    }
}
