package com.zoe.player.player.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *@author: cjl
 *@date: 2019/5/15 12:24
 *@desc:
 */
public class StringByteUtils {
    /**
     * Convert ascii string to hexadecimal string
     * @param str ASCII string
     * @return String Each Byte space between space, such as: [61 6c 6b]
     */
    public static String str2HexStr(String str)
    {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * Convert hexadecimal string to ascii string
     * @param hexStr No separators between Byte string (Byte Such as: [6b616c])
     * @return String The corresponding string
     */
    public static String hexStr2Str(String hexStr)
    {
        String str = "0123456789ABCDEF";
        hexStr = hexStr.toUpperCase();
        char[] hexs = hexStr.toCharArray();

        byte[] bytes = new byte[hexStr.length() / 2];
        // judge the input string good or not
        if (hexStr.length() % 2 == 1) {
            return new String();
        }
        // judge the input string good or not
        for(int i = 0; i < hexStr.length(); i++) {
            if((hexs[i] >= '0' && hexs[i] <= '9') || (hexs[i] >= 'A' && hexs[i] <= 'F')) {
            } else {
                return new String();
            }
        }
        int n;

        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


    /**
     * Bytes convert to hexadecimal string
     * @param b byte array
     * @return String Space separated between each Byte value
     */
    public static String byte2HexStr(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    public static String byte2HexStrWithoutSpace(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }


    /**
     * Convert hexadecimal string to byte array
     * @param src Byte string, there is no separator between each Byte
     * @return byte[] The corresponding byte array
     */
    public static byte[] hexStringToByteArray(String src) {
        int len = src.length();
        byte[] data = new byte[len/2];
        src = src.toUpperCase();
        char[] hexs = src.toCharArray();
        // judge the input string good or not
        if (len % 2 == 1) {
            return null;
        }
        // judge the input string good or not
        for(int i = 0; i < len; i++) {
            if((hexs[i] >= '0' && hexs[i] <= '9') || (hexs[i] >= 'A' && hexs[i] <= 'F')) {
            } else {
                return null;
            }
        }
        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(src.charAt(i), 16) << 4) + Character.digit(src.charAt(i+1), 16));
        }

        return data;
    }

    /**
     * ASCII string convert to Byte array
     * @param str ascii string
     * @return byte[]
     */
    public static byte[] Str2Bytes(String str)
    {
        if (str == null) {
            throw new IllegalArgumentException(
                    "Argument str ( String ) is null! ");
        }
        byte[] b = new byte[str.length() / 2];

        try {
            b = str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * bytes array convert to a normal string corresponding character (ASCII)
     *
     * @param bytearray
     *            byte[]
     * @return String
     */
    public static String Byte2String(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }

    /**
     * String into a unicode String
     * @param  strText The Angle of the string
     * @return String No separator between each unicode
     * @throws Exception
     */
    public static String strToUnicode(String strText)
            throws Exception
    {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++)
        {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 00 low in the front
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * Unicode String into a String
     * @param hex Hexadecimal values string (a unicode 2 byte)
     * @return String The Angle of the string
     */
    public static String unicodeToString(String hex)
    {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++)
        {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // Need to catch up on high 00 turn again
            String s1 = s.substring(2, 4) + "00";
            // Low directly
            String s2 = s.substring(4);
            // The hexadecimal string to an int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // To convert an int to characters
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    /**
     * int到字节数组的转换.
     * 方法二：
     * ByteBuffer buffer = ByteBuffer.allocate(4);
     *	buffer.putInt(time);
     *	byte[] array = buffer.array();
     */
    public static byte[] int2Byte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = b.length-1; i > 0; i--) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8;// 向右移8位
        }
        return b;
    }

    /**
     * 字节数组到int的转换.
     * @param b
     * @return
     */
    public static int byte2Int(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;// 最低位
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    /**
     * short到字节数组的转换.
     */
    public static byte[] short2Byte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = b.length-1; i >= 0; i--) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8;// 向右移8位
        }
        return b;
    }

    /**
     * 字节数组到short的转换.
     */
    public static short byte2Short(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * 从左到右为高位-->低位(与c语言相同)
     * @param b
     * @return
     */
    public static float byte2Float(byte[] b) {
        int accum = 0;
        accum = accum|(b[3] & 0xff) << 0;
        accum = accum|(b[2] & 0xff) << 8;
        accum = accum|(b[1] & 0xff) << 16;
        accum = accum|(b[0] & 0xff) << 24;
        return Float.intBitsToFloat(accum);
    }

    /**
     * 从左到右为低位-->高位(与c语言不同)
     * @param b
     * @return
     */
    public static float bytes2Float(byte[] b) {
        int index=0;
        int value;
        value = b[index + 0];
        value &= 0xff;
        value |= ((long) b[index + 1] << 8);
        value &= 0xffff;
        value |= ((long) b[index + 2] << 16);
        value &= 0xffffff;
        value |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(value);
    }

    public static byte[] float2byte(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;
    }



    public static byte[] long2Byte(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }


    public static long bytes2Long(byte[] bytes) {
        long num = 0;
        for (int i = 0; i < 8; ++i) {
            if (i>=bytes.length) break;
            num <<= 8;
            num |= (bytes[i] & 0xff);
        }
        return num;
    }


    public static int[] hex2IntArray(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new int[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        int[] result = new int[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (Integer.parseInt(tmp, 16)&0xFF);
        }
        return result;
    }

    /**
     * 将int[]转为byte[]
     * @param arr
     * @return
     */
    public static byte[] intArray2ByteArray(int[] arr){
        if (arr==null||arr.length==0){
            return new byte[0];
        }
        byte[] byteArray=new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            byteArray[i]= (byte) arr[i];
        }
        return byteArray;
    }


    /**
     * 获取int[]的字符串
     * @param intArray
     * @return
     */
    public static String getIntegerArrayString(int[] intArray) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < intArray.length; i++) {
            int b = intArray[i];
            builder.append(b+" ");
            if (i==intArray.length-1){
                builder.append("]");
            }
        }
        return builder.toString();
    }

}
