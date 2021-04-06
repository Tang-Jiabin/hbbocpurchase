package com.zykj.purchase.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体工具
 *
 * @author : J.Tang
 * @version : v1.0
 * @date : 2018/12/5
 * @email : seven_tjb@163.com
 **/
@Slf4j
public class BeanUtil {

    public static boolean isEmpty(Object model) {

        Field[] field = model.getClass().getDeclaredFields();

        // 获取属性的名字
        String[] modelName = new String[field.length];
        String[] modelType = new String[field.length];

        for (int i = 0; i < field.length; i++) {
            // 获取属性的名字
            String name = field[i].getName();
            modelName[i] = name;
            // 获取属性类型
            String type = field[i].getGenericType().toString();
            modelType[i] = type;
            //可访问私有变量
            field[i].setAccessible(true);
            //将属性的首字母大写
            name = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
            Object value = null;
            try {
                Method m = model.getClass().getMethod("get" + name);
                //调用getter方法获取属性值
                value = m.invoke(model);
            } catch (Exception e) {
                log.info("获取属性值失败");
                e.printStackTrace();
            }

            if (value == null) {
                return true;
            }

        }

        return false;
    }


    public static List copyList(List<? extends Object> poList, Class voClass) {

        List voList = new ArrayList();

        Object voObj = null;
        for (Object poObj : poList) {
            try {
                voObj = voClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(poObj, voObj);

            voList.add(voObj);
        }
        return voList;
    }

    public static String isNullChar(String str){
        if(!StringUtils.isEmpty(str)){
            return str;
        }else {
            return "";
        }

    }
}
