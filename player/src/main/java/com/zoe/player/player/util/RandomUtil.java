package com.zoe.player.player.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 *@author: cjl
 *@date: 2019/5/14 16:54
 *@desc:
 */
public class RandomUtil {

    public static final String TAG= RandomUtil.class.getSimpleName();

    public static int[] getRandomArray(int[] array){
        if (array==null){
            Log.e(TAG, "array can't be null!");
            return null;
        }
        int seed=0;//种子数
        int max=array.length;
        for (int i : array) {
            seed+=i;
        }
        return getRandomArray(seed,max);
    }



    /**
     * 用种子seed产生不大于max的不重复数列,要求max大于等于16
     * @param seed
     * @param max 解密后的字节数组长度
     * @return
     */
    public static int[] getRandomArray(int seed,int max){
        int[] result = new int[16];
        Map<Integer,Boolean> map = new HashMap<Integer,Boolean>();
        int a = 23;
        int c = 7;
        int seed1 = seed;
        int i = 0;
        while(i<16) {
            int t = (seed1 * a + c)%max;
            if(!map.containsKey(t)) {
                map.put(t, true);
                result[i] = t;
                seed1 = t;
                i++;
            }else {
                seed1++;
            }
        }
        return result;
    }

}
