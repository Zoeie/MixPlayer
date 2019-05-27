package com.zoe.player.player.util;


import android.util.Log;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * -----------------------------------------------------------------
 算法/模式/填充                16字节加密后数据长度        不满16字节加密后长度
 AES/CBC/NoPadding             16                          不支持
 AES/CBC/PKCS5Padding          32                          16
 AES/CBC/ISO10126Padding       32                          16
 AES/CFB/NoPadding             16                          原始数据长度
 AES/CFB/PKCS5Padding          32                          16
 AES/CFB/ISO10126Padding       32                          16
 AES/ECB/NoPadding             16                          不支持
 AES/ECB/PKCS5Padding          32                          16
 AES/ECB/ISO10126Padding       32                          16
 AES/OFB/NoPadding             16                          原始数据长度
 AES/OFB/PKCS5Padding          32                          16
 AES/OFB/ISO10126Padding       32                          16
 AES/PCBC/NoPadding            16                          不支持
 AES/PCBC/PKCS5Padding         32                          16
 AES/PCBC/ISO10126Padding      32                          16
 * -----------------------------------------------------------------
 */
public class AESCryptUtil {


    public static final String TAG= AESCryptUtil.class.getSimpleName();
    /** 算法/模式/填充 **/
    private static final String CipherMode = "AES/CBC/PKCS7Padding";

    /**
     * 创建密钥, 长度为128位(16bytes), 且转成字节格式，要求key必须是16位长度
     * @param key
     * @return
     */

    private static SecretKeySpec createKey(String key) {

        byte[] data = null;

        if (key == null) {
            key = "";
        }
        StringBuffer sb = new StringBuffer(16);
        sb.append(key);
        while (sb.length() < 16) {
            sb.append("0");
        }
        if (sb.length() > 16) {
            sb.setLength(16);
        }
        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "createKey: 异常："+e.getMessage());
            e.printStackTrace();
        }

        return new SecretKeySpec(data, "AES");
    }

    // 创建初始化向量, 长度为16bytes, 向量的作用其实就是salt
    private static IvParameterSpec createIV(String iv) {

        byte[] data = null;

        if (iv == null) {
            iv = "";
        }
        StringBuffer sb = new StringBuffer(16);
        sb.append(iv);
        while (sb.length() < 16) {
            sb.append("0");
        }
        if (sb.length() > 16) {
            sb.setLength(16);
        }
        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new IvParameterSpec(data);
    }

    /****************************************************************************/

    // 加密字节数据, 被加密的数据需要提前转化成字节格式
    private static byte[] encrypt(byte[] content, String key, String iv) {

        try {
            SecretKeySpec secretKeySpec = createKey(key);
            IvParameterSpec ivParameterSpec = createIV(iv);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] result = cipher.doFinal(content); // 加密
            return result;
        } catch (Exception e) {
            Log.e(TAG, "encrypt: 异常："+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 加密字符串数据, 返回的字节数据还需转化成16进制字符串
    public static String encrypt(String content, String key, String iv) {

        byte[] data = null;
        try {
            data = content.getBytes("UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "encrypt: 异常："+e.getMessage());
            e.printStackTrace();
        }

        data = encrypt(data, key, iv);
        //        String encoded = Base64.encodeToString(data, Base64.NO_WRAP);
        //        return encoded;
        return byte2hex(data);
    }

    /****************************************************************************/

    // 解密字节数组
    public static byte[] decrypt(byte[] content, String key, String iv) {

        try {
            SecretKeySpec secretKeySpec = createKey(key);
            IvParameterSpec ivParameterSpec = createIV(iv);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "decrypt: 异常："+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 解密(输出结果为字符串), 密文为16进制的字符串
    public static String decrypt(String content, String password, String iv) {
//        byte[] data = Base64.decode(content, Base64.NO_WRAP);
        byte[] data = hex2byte(content);

        data = decrypt(data, password, iv);
        if (data == null) {
            return null;
        }

        String result = null;
        try {
            result = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 解密
     * @param content 内容的字节数组
     * @param password 密码
     * @param iv   偏移量的字节数组
     * @return
     */
    public static String decrypt(byte[] content, String password, byte[] iv) {
        String result = null;
        try {
            SecretKeySpec secretKeySpec = createKey(password);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] data = cipher.doFinal(content);
            if (data == null) {
                return null;
            }
            result = new String(data, "UTF-8");
            return result;
        } catch (Exception e) {
            Log.e(TAG, "decrypt: 异常："+e.getMessage());
            e.printStackTrace();
        }
        return result;
    }






    /****************************************************************************/

    // 字节数组转成16进制大写字符串
    private static String byte2hex(byte[] b) {

        String tmp = "";
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int n = 0; n < b.length; n++) {
            tmp = (Integer.toHexString(b[n] & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase();
    }

    // 将16进制字符串转换成字节数组
    public static byte[] hex2byte(String inputString) {

        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }


    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }


}
