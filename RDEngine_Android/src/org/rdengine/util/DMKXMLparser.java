package org.rdengine.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.rdengine.util.xml.KXmlParser;
import org.rdengine.util.xml.XML;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author Rainer
 */
public class DMKXMLparser
{
    /**
     * 从数据流解析XML
     * 
     * @param is
     *            传入的数据流
     * @return XML对象
     */
    public static XML parser(InputStream is)
    {
        XML xml = new XML();
        long time = System.currentTimeMillis();
        try
        {
            boolean begin = true;
            KXmlParser xpp = new KXmlParser();
            // XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(is, "UTF-8");
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                // long subtime = System.currentTimeMillis();
                if (eventType == XmlPullParser.START_DOCUMENT)
                {
                    xml = new XML();
                    xml.isRoot = true;
                } else if (eventType == XmlPullParser.END_DOCUMENT)
                {
                    // System.out.println("End document");
                } else if (eventType == XmlPullParser.START_TAG)
                {
                    xml = new XML(xml);
                    xml.setName(xpp.getName());
                    int attributeCount = xpp.getAttributeCount();
                    if (attributeCount > 0)
                    {
                        HashMap<String, String> attributes = xml.getAttributes();
                        for (int i = 0; i < attributeCount; i++)
                        {
                            String key = xpp.getAttributeName(i);
                            String value = xpp.getAttributeValue(i);
                            attributes.put(key, value);
                        }
                    }
                    begin = true;
                } else if (eventType == XmlPullParser.END_TAG)
                {
                    xml = xml.getParent();
                    begin = false;
                } else if (eventType == XmlPullParser.TEXT)
                {
                    if (begin)
                    {
                        xml.setText(xpp.getText());
                    }
                }
                eventType = xpp.next();
                // if ((System.currentTimeMillis() - subtime) > 10)
                // {
                // LOG.d("xmlparser", "subtime:" + (System.currentTimeMillis() - subtime));
                // LOG.d("xmlparser", "type:" + eventType + " name:" + xpp.getName());
                // }

            }
        } catch (Exception ex)
        {
            System.out.println("KXML parser error : " + ex);
        }

        if ((System.currentTimeMillis() - time) > 200)
        {
            // DLOG.d("xmlparser", "usetime:" + (System.currentTimeMillis() - time));
            // printXML(xml.getChild(0));
            // LOG.d("xmlparser", outstr);
        }
        return xml.getChild(0);
    }

    /**
     * 从字节数据解析XML
     * 
     * @param data
     *            字节数组
     * @return XML对象
     */
    public static XML parser(byte[] data)
    {
        if (data == null || data.length == 0)
        {
            return new XML();
        }
        long time = System.currentTimeMillis();
        try
        {
            if ((data[0] & 0xFF) == 0xEF && (data[1] & 0xFF) == 0xBB && (data[2] & 0xFF) == 0xBF)
            {
                byte[] trans = new byte[data.length];
                System.arraycopy(data, 0, trans, 0, data.length);
                data = new byte[data.length - 3];
                System.arraycopy(trans, 3, data, 0, data.length);
                trans = null;
            }

        } catch (Exception ex)
        {
            System.out.println("xml parser error : " + ex);
        }
        // LOG.d("xmlparser", "transtime:" + (System.currentTimeMillis() - time));
        // try {
        // System.out.println(new String(data, "UTF-8"));
        // } catch (Exception ex) {
        //
        // }

        return parser(new ByteArrayInputStream(data));
    }

    static StringBuilder outstr;

    /**
     * 从XML对象生成XML数据
     * 
     * @param xml
     *            XML对象
     * @return XML数据
     */
    public static byte[] outPutXML(XML xml)
    {
        outstr = new StringBuilder();
        outstr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ");
        printXML(xml);
        // System.out.println(outstr);
        byte[] outbyte = new byte[0];
        try
        {
            outbyte = outstr.toString().getBytes("UTF-8");
        } catch (Exception ex)
        {

        }
        return outbyte;
    }

    private static void printXML(XML xml)
    {
        outstr.append("<").append(xml.getName()).append(">");
        if (xml.isNode())
        {
            for (int i = 0; i < xml.getChildrenLength(); i++)
            {
                printXML(xml.getChild(i));
            }
        } else
        {
            outstr.append(removeASCIInull(transformXMLSymbol(xml.getText())));
            // outstr += transformAmp(xml.getText());
            // outstr += (xml.getText());
        }
        outstr.append("</").append(xml.getName()).append(">");
    }

    public static String transformXMLSymbol(String text)
    {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            if (text.charAt(i) == '<')
            {
                out.append("&lt;");
            } else if (text.charAt(i) == '>')
            {
                out.append("&gt;");
            } else if (text.charAt(i) == '&')
            {
                out.append("&amp;");
            } else if (text.charAt(i) == '\'')
            {
                out.append("&apos;");
            } else if (text.charAt(i) == '\"')
            {
                out.append("&quot;");
            } else
            {
                out.append(text.charAt(i));
            }
        }
        return out.toString();
    }

    public static String transformAmp(String text)
    {
        if (text.indexOf("<![CDATA[") >= 0)
        {
            return text;
        }
        if (text.length() == 0)
        {
            return text;
        }
        if (text.indexOf('&') < 0)
        {
            return text;
        }
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < text.length(); i++)
        {
            if (text.charAt(i) == '&' && (text.length() > i + 1 && text.charAt(i + 1) != '#'))
            {
                out.append("&amp;");
            } else
            {
                out.append(text.charAt(i));
            }
        }

        return out.toString();
    }

    /**
     * 删除数据中的ASCII码null
     */
    public static String removeASCIInull(String text)
    {
        if (text.length() == 0)
        {
            return text;
        }
        char char_ascii = 0x00;
        String str_ascii = "" + char_ascii;
        if (text.indexOf(str_ascii) < 0)
        {
            return text;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            if (text.charAt(i) == char_ascii)
            {

            } else
            {
                out.append(text.charAt(i));
            }
        }

        return out.toString();
    }

    /**
     * 删除数据中大于127的ASCII码
     */
    public static String removeASCIIgreaterthan127(String text)
    {
        if (text.length() == 0)
        {
            return text;
        }
        char char_ascii = 0x7F;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            if (text.charAt(i) > char_ascii)
            {

            } else
            {
                out.append(text.charAt(i));
            }
        }

        return out.toString();

    }

}
