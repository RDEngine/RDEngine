package org.rdengine.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.rdengine.log.DLOG;

public class SecurityTools
{

    /** DES加密解密 */
    public static class FlowOrderDES
    {
        /**
         * DES加密实现
         * 
         * @param datasource
         * @param password
         * @return
         */
        public static byte[] desCrypto(byte[] datasource, String password)
        {
            try
            {
                SecureRandom random = new SecureRandom();
                DESKeySpec desKey = new DESKeySpec(password.getBytes());
                // 创建一个密匙工厂，然后用它把DESKeySpec转换成
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
                SecretKey securekey = keyFactory.generateSecret(desKey);
                // Cipher对象实际完成加密操作
                Cipher cipher = Cipher.getInstance("DES");
                // 用密匙初始化Cipher对象
                cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
                // 现在，获取数据并加密
                // 正式执行加密操作
                int length = datasource.length;
                byte[] temp = new byte[(length / 8 + ((length % 8 == 0) ? 0 : 1)) * 8];
                for (int i = 0; i < length; i++)
                {
                    temp[i] = datasource[i];
                }
                for (int i = length; i < temp.length; i++)
                {
                    temp[i] = '\0';
                }
                return cipher.doFinal(temp);
            } catch (Throwable e)
            {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * DES解密
         * 
         * @param src
         * @param password
         * @return
         * @throws Exception
         */
        public static byte[] desDecrypt(byte[] src, String password, boolean padding) throws Exception
        {
            // DES算法要求有一个可信任的随机数源
            SecureRandom random = new SecureRandom();
            // 创建一个DESKeySpec对象
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 将DESKeySpec对象转换成SecretKey对象
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = null;
            if (padding)
            {
                cipher = Cipher.getInstance("DES");
            } else
            {// c加密的一般没有填充
                cipher = Cipher.getInstance("DES/ECB/NoPadding");
            }

            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, random);
            // 真正开始解密操作
            return cipher.doFinal(src);
        }

    }

    /** 循环异或加密解密 */
    public static class RecyclXOR
    {
        /** 循环异或 加密解密都调这个方法 */
        public static byte[] RecyclingXOR(byte[] data, String key)
        {
            if (data == null || key == null || data.length == 0 || key.length() == 0)
            {
                return null;
            }
            byte[] ret = new byte[data.length];
            try
            {
                char[] miarry = key.toCharArray();
                int length = data.length;
                for (int i = 0; i < length; i++)
                {
                    int num = i % miarry.length;
                    ret[i] = (byte) (data[i] ^ miarry[num]);
                }
            } catch (Exception e)
            {
                ret = null;
            }
            return ret;
        }

        /**
         * 循环异或-加密 默认字符串编码UTF-8
         * 
         * @param content
         *            需要加密的原文
         * @param key
         *            密钥
         * @return 数据加密后转成16进制字符串
         */
        public static String XORCrypto(String content, String key)
        {
            if (content == null || key == null || content.length() == 0 || key.length() == 0)
                return null;
            String ret = null;
            byte[] a = RecyclingXOR(content.getBytes(), key);
            if (a != null)
            {
                ret = bytesToHexString(a).toUpperCase();
            }
            return ret;
        }

        /**
         * 循环异或-解密 默认字符串编码UTF-8
         * 
         * @param content
         *            16进制字符串形式的加密内容
         * @param key
         *            密钥
         * @return 数据解密后的原文字符串
         */
        public static String XORDecrypt(String content, String key)
        {
            if (content == null || key == null || content.length() == 0 || key.length() == 0)
                return null;
            String ret = null;
            byte[] a = hexStringToBytes(content);
            if (a != null)
            {
                ret = new String(RecyclingXOR(a, key));
            }
            return ret;
        }
    }

    /**
     * Convert char to byte
     * 
     * @param c
     *            char
     * @return byte
     */
    private static byte charToByte(char c)
    {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Convert hex string to byte[]
     * 
     * @param hexString
     *            the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString)
    {
        if (hexString == null || hexString.equals(""))
        {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++)
        {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 将byte型数组,转正hexString
     * 
     * @param src
     *            byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src)
    {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0)
        {
            return null;
        }
        for (int i = 0; i < src.length; i++)
        {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2)
            {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toLowerCase();
    }

    // _dm_rsa_func(const char*inputStr,const char*modules,unsigned int publicExponent)
    public static String rsaCrypto(String inputStr, String modules, int publicExponent)
    {
        String secrect = "";
        RSAPublicKey publicKey;
        // DLOG.e("rsaCrypto","input>>"+inputStr+" | "+modules+" "+publicExponent);
        // DLOG.e("rsaCrypto","atom>>"+SDKHelper.ins().getSession().getAtom());
        // DLOG.e("rsaCrypto","atom lc>>"+RT.AppInfo.channelCode+" "+RT.AppInfo.licenceid);
        try
        {
            KeyFactory keyFac = null;
            try
            {
                keyFac = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException ex)
            {
                throw new Exception(ex.getMessage());
            }
            BigInteger n = new BigInteger(modules, 16);
            BigInteger e = new BigInteger(String.valueOf(publicExponent), 10);
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(n, e);
            try
            {
                publicKey = (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
            } catch (InvalidKeySpecException ex)
            {
                throw new Exception(ex.getMessage());
            }
            if (publicKey != null)
            {
                try
                {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    byte[] result = cipher.doFinal(inputStr.toString().getBytes());
                    secrect = bytesToHexString(result);
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        DLOG.e("rsaCrypto", "return>>" + secrect);
        return secrect;
    }

    
    public static String rsaDec(String content,String modules, int publicExponent)
    {
        String secrect = "";
        RSAPrivateKey privateKey;
        try
        {
            KeyFactory keyFac = null;
            try
            {
                keyFac = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException ex)
            {
                throw new Exception(ex.getMessage());
            }
            BigInteger n = new BigInteger(modules, 16);
            BigInteger e = new BigInteger(String.valueOf(publicExponent), 10);
            RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(n, e);
            try
            {
                privateKey = (RSAPrivateKey) keyFac.generatePublic(priKeySpec);
            } catch (InvalidKeySpecException ex)
            {
                throw new Exception(ex.getMessage());
            }
            if (privateKey != null)
            {
                try
                {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    InputStream ins = new ByteArrayInputStream(Base64.decode(content, Base64.DEFAULT));
                    ByteArrayOutputStream writer = new ByteArrayOutputStream();
                    byte[] buf = new byte[128];
                    int bufl;
                    while ((bufl = ins.read(buf)) != -1)
                    {
                        byte[] block = null;
                        if (buf.length == bufl)
                        {
                            block = buf;
                        } else
                        {
                            block = new byte[bufl];
                            for (int i = 0; i < bufl; i++)
                            {
                                block[i] = buf[i];
                            }
                        }
                        writer.write(cipher.doFinal(block));
                    }
                    return new String(writer.toByteArray(), "utf-8");
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        DLOG.e("rsaCrypto", "return>>" + secrect);
        return secrect;
    }
    
    public static String rsaCrypto(String input, String key)
    {
        String secrect = "";
        PublicKey publicKey = null;
        try
        {
            publicKey = getPublicKey(key);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (publicKey != null)
        {
            try
            {
                Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                byte[] result = cipher.doFinal(input.toString().getBytes());
                secrect = bytesToHexString(result);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        DLOG.d("http", "secrect:" + secrect);
        return secrect;
    }

    public static String ras(String input,String key)
    {
        String str = "";
        // 实例化加解密类
        Cipher cipher;
        try
        {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(key));
            byte[] plainText = input.getBytes();
            // 将明文转化为根据公钥加密的密文，为byte数组格式
            byte[] enBytes = cipher.doFinal(plainText);
            // 为了方便传输我们可以将byte数组转化为base64的编码
            str = Base64.encodeToString(enBytes, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return str;
    }

    public static PublicKey getPublicKey(String key) throws Exception
    {
        byte[] keyBytes;
        // keyBytes = base64Dec(key);
        keyBytes = Base64.decode(key, Base64.DEFAULT);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

}
