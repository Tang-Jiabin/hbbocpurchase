package com.zykj.purchase.common;

import java.util.Random;

/**
 * 随机数
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-22
 */

public class RandomUtil {

    private RandomUtil() {
        random = new Random();
    }

    private static RandomUtil randomUtil;

    private Random random;

    public static RandomUtil getInstance() {
        if (randomUtil == null) {
            synchronized (RandomUtil.class){
                if(randomUtil == null){
                    randomUtil = new RandomUtil();
                }
            }
        }
        return randomUtil;
    }

    public int nextInt(int bound){
        return random.nextInt(bound);
    }


}
