package com.androidNewLab.baiduMap.Map;

/**
 * Created by Administrator on 2016/7/28.
 */
import com.google.gson.Gson; //导入google gson包

public class GsonService {

    public static <T> T parseJson(String jsonString, Class<T> clazz) {
        T t = null;
        try {
            Gson gson = new Gson();
            t = gson.fromJson(jsonString, clazz);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("解析json失败");
        }
        return t;

    }
}