package org.rdengine.util.xml;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author rainer 用来存诸XML数据,XML数据以树结构存诸与XML对象中 第一次创建:2007-05-23 rainer
 */
public class XML
{
    /**
     * 子节点对象
     */
    private Vector<XML> children = null;

    private HashMap<String, String> attributes = null;

    /**
     * 父节点对象,如果本节点是根目录,此项为空
     */
    private XML parent = null;

    /**
     * 当前节点是否还有子节点
     */
    private boolean isNode = false;

    /**
     * 当前节点是否为根节点
     */
    public boolean isRoot = false;

    /**
     * 标签名
     */
    private String name;

    /**
     * 标签内容
     */
    private String text;

    /**
     * 标签内属性表
     */

    /**
     * 当前标签深度
     */
    private int level = 0;

    /**
     * 所属XML的最大深度
     */
    private static int maxLevel = 0;

    public XML()
    {

    }

    /**
     * @param p
     *            父节点
     */
    public XML(XML p)
    {
        parent = p;
        p.addChild(this);
        // System.out.println("level : " + level);
    }

    /**
     * @return 返回最大深度
     */
    public static int getmaxLevel()
    {
        return maxLevel;
    }

    /**
     * @return 当前节点还包含子节点返回true
     */
    public boolean isNode()
    {
        return isNode;
    }

    /**
     * @return 返回当前节点的深度
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * @return 返回标签名
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return 返回标签内容
     */
    public String getText()
    {
        if (text == null)
            text = "";
        return text;
    }

    /**
     * 设置标签名
     * 
     * @param n
     *            新的标签名
     */
    public void setName(String n)
    {
        name = n;
    }

    /**
     * 设置标签内容
     * 
     * @param t
     *            新的标签内容
     */
    public void setText(String t)
    {
        text = t;
    }

    /**
     * 返回子节点数量
     * 
     * @return 子节点数量
     */
    public int getChildrenLength()
    {
        if (children == null)
        {
            return 0;
        }
        return children.size();
    }

    /**
     * 添加子节点
     * 
     * @param x
     *            子节点
     */
    public void addChild(XML x)
    {
        isNode = true;
        x.level = level + 1;
        if (x.level > maxLevel)
        {
            maxLevel = x.level;
        }
        if (children == null)
        {
            children = new Vector<XML>();
        }
        children.addElement(x);
    }

    /**
     * 添加子节点
     * 
     * @param tag
     *            标签名
     * @param text
     *            Text内容
     */
    public void addChild(String tag, String text)
    {
        XML x = new XML();
        x.setName(tag);
        x.setText(text);
        isNode = true;
        x.level = level + 1;
        if (x.level > maxLevel)
        {
            maxLevel = x.level;
        }
        if (children == null)
        {
            children = new Vector<XML>();
        }
        children.addElement(x);
    }

    /**
     * 删除子节点
     * 
     * @param index
     *            子节点索引值
     */
    public void deleteChild(int index)
    {
        if (children == null)
        {
            return;
        }
        children.removeElementAt(index);
        if (children.size() == 0)
        {
            isNode = false;
        }
    }

    /**
     * 取得子节点对象
     * 
     * @param index
     *            子节点索引值
     * @return 子节点对象
     */
    public XML getChild(int index)
    {
        if (children == null)
        {
            return null;
        }
        try
        {
            XML xml = (XML) children.elementAt(index);
            return xml;
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取得父节点对象
     * 
     * @return 父节点对象
     */
    public XML getParent()
    {
        return parent;
    }

    /**
     * 按标签名搜索子节点
     * 
     * @param n
     *            标签名
     * @return 子节点集合
     */
    public Vector<XML> findByName(String n)
    {

        Vector<XML> v = new Vector<XML>();
        if (children == null)
        {
            return v;
        }
        for (int i = 0; i < children.size(); i++)
        {
            if (children.elementAt(i).getName().equals(n))
            {
                v.addElement(children.elementAt(i));
            }
        }
        return v;
    }

    /**
     * 按标签名索第一个子节点
     * 
     * @param n
     *            标签名
     * @return 子节点
     */
    public XML find1stByName(String n)
    {
        if (children == null)
        {
            return null;
        }
        for (int i = 0; i < children.size(); i++)
        {
            if (((XML) children.elementAt(i)).getName().equals(n))
            {
                return (XML) children.elementAt(i);
            }
        }
        return null;
    }

    public String findText(String n)
    {
        return findText(n, null);
    }

    public String findText(String n, String defaultStr)
    {
        if (children == null)
        {
            return defaultStr;
        }
        for (int i = 0; i < children.size(); i++)
        {
            if (((XML) children.elementAt(i)).getName().equals(n))
            {
                if (((XML) children.elementAt(i)).getText().equals(""))
                {
                    return defaultStr;
                } else
                {
                    return ((XML) children.elementAt(i)).getText();
                }
            }
        }
        return defaultStr;
    }

    public HashMap<String, String> getAttributes()
    {
        if (attributes == null)
        {
            attributes = new HashMap<String, String>();
        }
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes)
    {
        this.attributes = attributes;
    }

    public String getAttributeValue(String key)
    {
        if (this.attributes == null)
        {
            return null;
        }
        if (this.attributes.containsKey(key))
        {
            return this.attributes.get(key);
        }
        return null;
    }
}
