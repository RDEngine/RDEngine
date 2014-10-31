package org.rdengine.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.rdengine.log.DLOG;

public class RSA
{
    public static final String RSA_PUBLICE = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDnhJDsKgEa8aP8UWyVNbeyyOJ8S9mct+wCNLkWmR26QXjpktB6LfcN6UyUOEQQo6k77PsKFoAmMRZEwxqMkdPLEJXKMoY0//Za4K+XKH9UDWAGH0cONV0qz1DtTABu/JHairnFic1EdAW4cImCYHbKcMqLEun7+zxtdAE5V35TyQIDAQAB";
    public static final String RSA_PRIVATE = "MIICXAIBAAKBgQDnhJDsKgEa8aP8UWyVNbeyyOJ8S9mct+wCNLkWmR26QXjpktB6LfN6UyUOEQQo6k77PsKFoAmMRZEwxqMkdPLEJXKMoY0//Za4K+XKH9UDWAGH0cONV0qz1DtTABu/JHairnFic1dAW4cImCYHbKcMqLEun7+zxtdAE5V35TyQIDAQABAoGAUF4GaNTT+WR0hnxFcy5ojFmLtZYm/m4iMUnYDm4skaF0tbBPSnp2To/0E2fMyPHnJ3RGWOQirB9SGaCYsL9Wg3gyl4Ki3WX//RoKe0Gl/ffC6h3V0fB7/aUA1U8JjSp5TdvDGiViPaOqZbEoEn09xJ3NpfL0301+aDftJqa+8/kCQQD6MPls7RFskPZ+G/I8rJ4KG8VVe+iFLb+9yBOa8HoNsGf1gRocj+PqMbpqL6iJswiaX/FTuzYqQB7f3f87B8u7AkEA7OSa4UDTt28rbF8OibrnjNUwmS6xCfAb3Ef8a0keUgjZ2T6idszcUdWB47e5r7E22TRAqcsSCdCxzTf4drysSwJAdKczFQuwLRumIoSrPIkLoxDxwHrmnnoHRdis83geoJVY04pff2PD0+Vd0rbn8VRNtZT4c579kVOWUbEzmPV4kwJABpJI4ZrExL/munjglF2E8tkvIfCzpIzumOu+StOabbglIuuj4hvIuNRtZ/2+vurxlJDogk1J0M/jPsggslqFhQJBAKKSC025Q8aBFljJtj/vQsURXFbe4UdzQgJQQ8z+H3truhovAdJOd6Ci767qHtNRaDoUYitNXqWj+TAcZiPOjdA=";
    private static final String ALGORITHM = "RSA";

    /**
     * 得到公钥
     * 
     * @param algorithm
     * @param bysKey
     * @return
     */
    private static PublicKey getPublicKeyFromX509(String algorithm, String bysKey) throws NoSuchAlgorithmException,
            Exception
    {
        byte[] decodedKey = Base64.decode(bysKey, Base64.DEFAULT);
        X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(x509);
    }

    /**
     * 使用公钥加密
     * 
     * @param content
     * @param key
     * @return
     */
    public static String encryptByPublic(String content)
    {
        try
        {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);

            byte plaintext[] = content.getBytes("UTF-8");
            byte[] output = cipher.doFinal(plaintext);

            String s = new String(Base64.encode(output, Base64.DEFAULT));

            return s;

        } catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 使用公钥解密
     * 
     * @param content
     *            密文
     * @param key
     *            商户私钥
     * @return 解密后的字符串
     */
    public static String decryptByPublic(String content)
    {
        try
        {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pubkey);
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
        } catch (Exception e)
        {
            return null;
        }
    }
}
