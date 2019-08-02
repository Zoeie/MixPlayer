package com.zoe.player.player.util;

import android.text.TextUtils;

import java.util.Arrays;

/**
 *@author: cjl
 *@date: 2019/5/15 14:57
 *@desc: 解析m3u8加密数据
 */
public class M3u8ParseUtil {

    public static String parse(String data) {

        if (TextUtils.isEmpty(data)){
            return null;
        }
        int[] hexBytes = StringByteUtils.hex2IntArray(data);
//        String hexByteStr = StringByteUtils.getIntegerArrayString(hexBytes);
//        LogUtil.d("hexBytes："+hexByteStr);

        //1.计算种子数，获取随机数组
        int seed=0;
        if (hexBytes!=null){
            for (int b : hexBytes) {
                seed+=b;
            }
        }
        if(hexBytes == null) return "";
        int[] randomArray = RandomUtil.getRandomArray(seed, hexBytes.length);
//        String randomArrayStr = StringByteUtils.getIntegerArrayString(randomArray);
//        LogUtil.d("randomArrayStr:"+randomArrayStr);

        //2.获取秘钥
        int[] keyArrray=new int[randomArray.length];
        for (int i = 0; i < randomArray.length; i++) {
            keyArrray[i]=hexBytes[randomArray[i]];
        }
        StringBuilder keyBuilder=new StringBuilder();
        for (int i : keyArrray) {
            keyBuilder.append((char) i);
        }
        String key=keyBuilder.toString();
//        LogUtil.d("key："+key);

        //3.去除随机数，获取加密数据
                //先排序需要去除的下标，然后逐段复制数组
        Arrays.sort(randomArray);
//        LogUtil.d("randomArray----order"+StringByteUtils.getIntegerArrayString(randomArray));
        int[] dataArray=new int[hexBytes.length-randomArray.length];
        int preIndex=-1;//前一个需要去除的index
        int index=0;//当前需要去除的index
        int preDestIndex=0;//目标数组的前一个index
        int copyLength=0;//需要复制的数据长度
        for (int i = 0; i < randomArray.length; i++) {
            index=randomArray[i];
            copyLength=index-(preIndex+1);
            if (copyLength!=0){
                System.arraycopy(hexBytes,preIndex+1,dataArray,preDestIndex,copyLength);
            }
            preIndex = index;
            preDestIndex = preIndex-i;
            //copy最后一段数据
            if (i==randomArray.length-1){
                copyLength=hexBytes.length-(preIndex+1);
                System.arraycopy(hexBytes,preIndex+1,dataArray,preDestIndex,copyLength);
            }
        }
//        LogUtil.d("dataArray:"+StringByteUtils.getIntegerArrayString(dataArray));

        //4.获取偏移量
        int[] ivArray=new int[16];
        System.arraycopy(dataArray,0,ivArray,0,16);
        byte[] ivByteArray = StringByteUtils.intArray2ByteArray(ivArray);
//        String str = StringByteUtils.byte2HexStrWithoutSpace(ivByteArray);
//        LogUtil.d("iv:"+str);

        //5.获取待解密数据并解密
        int[] original = new int[dataArray.length - 16];
        System.arraycopy(dataArray, 16, original,0,dataArray.length - 16);
//        LogUtil.d("original:"+StringByteUtils.getIntegerArrayString(original));
        byte[] encrypt = StringByteUtils.intArray2ByteArray(original);
        String decrypt = AESCryptUtil.decrypt(encrypt, key, ivByteArray);
//        LogUtil.d("decrypt："+decrypt);
        return decrypt;
    }
    
    
    
    
    
    
}
