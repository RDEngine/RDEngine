package org.rdengine.util;

public class ByteUtil
{

    /**
     * 整数转1个byte
     * 
     * @param intValue
     * @return
     */
    public static byte intToOneByte(int intValue)
    {
        byte result = (byte) ((intValue & 0x000000FF));
        return result;
    }

    /**
     * 整数转1个byte数组
     * 
     * @param intValue
     * @return
     */
    public static byte[] intToOneByteArr(int intValue)
    {
        byte result = (byte) ((intValue & 0x000000FF));
        return new byte[]
        { result };
    }

    /**
     * 整数转2个byte数组
     * 
     * @param type
     *            类型0为element,1为attribute
     * @param intValue
     *            整数
     * @return
     */
    public static byte[] twoByteFromInt(int type, int intValue)
    {
        byte[] result = new byte[2];

        if (type == 0)
        {
            result[0] = (byte) ((intValue & 0x00007F00) >> 8);
        } else
        {
            result[0] = (byte) (((intValue & 0x00007F00) | 0x00008000) >> 8);
        }
        result[1] = (byte) ((intValue & 0x000000FF));
        return result;
    }

    /**
     * 整数转2个byte
     * 
     * @param intValue
     * @return
     */
    public static byte[] intToTwoByteArr(int intValue)
    {
        byte[] result = new byte[2];
        result[0] = (byte) ((intValue & 0x0000FF00) >> 8);
        result[1] = (byte) ((intValue & 0x000000FF));

        return result;
    }

    /**
     * 整数转3个byte
     * 
     * @param intValue
     * @return
     */
    public static byte[] intToThreeByteArr(int intValue)
    {
        byte[] result = new byte[3];
        result[0] = (byte) ((intValue & 0x00FF0000) >> 16);
        result[1] = (byte) ((intValue & 0x0000FF00) >> 8);
        result[2] = (byte) ((intValue & 0x000000FF));
        return result;
    }

    /**
     * 整数到4字节数组的转换
     */
    public static byte[] intToFourByteArr(int intValue)
    {
        byte[] result = new byte[4];
        result[0] = (byte) ((intValue & 0xFF000000) >> 24);
        result[1] = (byte) ((intValue & 0x00FF0000) >> 16);
        result[2] = (byte) ((intValue & 0x0000FF00) >> 8);
        result[3] = (byte) ((intValue & 0x000000FF));
        return result;
    }

    /**
     * 字节数组到整数的转换
     * 
     * @param byteVal
     * @return
     */
    public static int byteToInt(byte... byteVal)
    {
        int result = 0;
        if (byteVal.length == 4)
        {
            for (int i = 0; i < byteVal.length; i++)
            {
                int tmpVal = (byteVal[i] << (8 * (3 - i)));
                switch (i)
                {
                case 0 :
                    tmpVal = tmpVal & 0xFF000000;
                    break;
                case 1 :
                    tmpVal = tmpVal & 0x00FF0000;
                    break;
                case 2 :
                    tmpVal = tmpVal & 0x0000FF00;
                    break;
                case 3 :
                    tmpVal = tmpVal & 0x000000FF;
                    break;
                }
                result = result | tmpVal;
            }
        } else if (byteVal.length == 3)
        {
            for (int i = 0; i < byteVal.length; i++)
            {
                int tmpVal = (byteVal[i] << (8 * (2 - i)));
                switch (i)
                {
                case 0 :
                    tmpVal = tmpVal & 0x00FF0000;
                    break;
                case 1 :
                    tmpVal = tmpVal & 0x0000FF00;
                    break;
                case 2 :
                    tmpVal = tmpVal & 0x000000FF;
                    break;
                }
                result = result | tmpVal;
            }
        } else if (byteVal.length == 2)
        {
            for (int i = 0; i < byteVal.length; i++)
            {
                int tmpVal = (byteVal[i] << (8 * (1 - i)));
                switch (i)
                {
                case 0 :
                    tmpVal = tmpVal & 0x0000FF00;
                    break;
                case 1 :
                    tmpVal = tmpVal & 0x000000FF;
                    break;
                }
                result = result | tmpVal;
            }
        } else if (byteVal.length == 1)
        {
            for (int i = 0; i < byteVal.length; i++)
            {
                int tmpVal = byteVal[i];
                switch (i)
                {

                case 0 :
                    tmpVal = tmpVal & 0x000000FF;
                    break;
                }
                result = result | tmpVal;
            }
        }

        return result;
    }

    /**
     * a[0]的低7个bit作为高位，a[1]作为低位，转成整数
     * 
     * @param a
     * @return
     */
    public static int[] toIntFromTwoByte(byte a[])
    {
        int ret[] = new int[2];
        // 取类型值，最高位
        ret[0] = (int) ((a[0] & 0xFF) >> 7);
        // a[0]的低7个bit作为高位，a[1]作为低位，转成整数
        ret[1] = (((a[0] & 0xFF) & 0x7F) << 8) | (a[1] & 0xFF);
        return ret;
    }

    /**
     * 字符到字节转换
     * 
     * @param ch
     * @return
     */
    public static byte[] charToByte(char ch)
    {
        int temp = (int) ch;
        byte[] b = new byte[2];
        for (int i = b.length - 1; i > -1; i--)
        {
            b[i] = new Integer(temp & 0xff).byteValue(); // 将最高位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    /**
     * 字节到字符转换
     * 
     * @param b
     * @return
     */
    public static char byteToChar(byte[] b)
    {
        int s = 0;
        if (b[0] > 0)
            s += b[0];
        else s += 256 + b[0];
        s *= 256;
        if (b[1] > 0)
            s += b[1];
        else s += 256 + b[1];
        char ch = (char) s;
        return ch;
    }

    /**
     * 字节到字符转换
     * 
     * @param b
     * @return
     */
    public static char byteToChar(byte[] b, int offSet)
    {
        int s = 0;
        if (b[0] > 0)
            s += b[0];
        else s += 256 + b[offSet];
        s *= 256;
        if (b[1] > 0)
            s += b[1];
        else s += 256 + b[offSet + 1];
        char ch = (char) s;
        return ch;
    }

    /**
     * 浮点到字节转换
     * 
     * @param d
     * @return
     */
    public static byte[] doubleToByte(double d)
    {
        byte[] b = new byte[8];
        long l = Double.doubleToLongBits(d);
        for (int i = 0; i < b.length; i++)
        {
            b[i] = new Long(l).byteValue();
            l = l >> 8;
        }
        return b;
    }

    /**
     * 字节到浮点转换
     * 
     * @param b
     * @return
     */
    public static double byteToDouble(byte[] b)
    {
        long l;

        l = b[0];
        l &= 0xff;
        l |= ((long) b[1] << 8);
        l &= 0xffff;
        l |= ((long) b[2] << 16);
        l &= 0xffffff;
        l |= ((long) b[3] << 24);
        l &= 0xffffffffl;
        l |= ((long) b[4] << 32);
        l &= 0xffffffffffl;

        l |= ((long) b[5] << 40);
        l &= 0xffffffffffffl;
        l |= ((long) b[6] << 48);
        l &= 0xffffffffffffffl;
        l |= ((long) b[7] << 56);
        return Double.longBitsToDouble(l);
    }
}
