package com.cbgan.hso.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NET {
    public static JSONObject GET_JSON(String path, Handler msgHandler) {//从服务器获取JSON
        try {
            URL setu_url = new URL(path);
            HttpURLConnection connection=(HttpURLConnection) setu_url.openConnection();
            connection.setConnectTimeout(5000);//设置超时
            connection.setRequestMethod("GET");//设置模式
            int responseCODE = connection.getResponseCode();
            if(responseCODE == 200){//请求成功
                InputStream response = connection.getInputStream();
                Log.i("[JSON]",""+response);
                //处理获取数据流
                JSONParser jsonParser = new JSONParser();
                JSONObject setu_json=(JSONObject)jsonParser.parse(new InputStreamReader(response,"UTF-8"));//转换为JSON
                String data=setu_json.get("data").toString();//获取data键值并转换为String
                data=data.substring(1,data.length()-1);//处理文本数据
                Log.i("data",data);
                JSONObject setu_json_data=(JSONObject)jsonParser.parse(data);//将文本数据转换为JSONObj
                msgHandler.obtainMessage(2,setu_json_data).sendToTarget();//向主线程发送JSON数据
                return setu_json_data;
            }else{
                msgHandler.obtainMessage(1, "wdnmd冲不出来了(CODE:"+responseCODE+")").sendToTarget();//向主线程发送网络错误信息
                Log.e("[NetworkError]","CODE:"+responseCODE);
            }
        } catch (Exception e) {
            msgHandler.obtainMessage(1, "wdnmd冲不出来了("+e+")").sendToTarget();//向主线程发送错误信息
            Log.e("[ThreadError]",""+e);
        }
        return null;
    }
    public static void GET_IMG(String path, Handler msgHandler) {//从服务器获取JSON
        try {
            Image setu = null;
            URL setu_url = new URL(path);
            HttpURLConnection connection=(HttpURLConnection) setu_url.openConnection();
            connection.setConnectTimeout(5000);//设置超时
            connection.setRequestMethod("GET");//设置模式
            int responseCODE = connection.getResponseCode();
            if(responseCODE == 200){//请求成功
                msgHandler.obtainMessage(3,connection.getContentLength()).sendToTarget();//得到图片大小
                InputStream response=connection.getInputStream();
                Bitmap setu_bitmap= BitmapFactory.decodeStream(response);//处理流为Bitmap
                msgHandler.obtainMessage(0,setu_bitmap).sendToTarget();//向主线程发送JSON数据
            }else{
                msgHandler.obtainMessage(1, "wdnmd冲不出来了(CODE:"+responseCODE+")").sendToTarget();//向主线程发送网络错误信息
                Log.e("[NetworkError]","CODE:"+responseCODE);
            }
        } catch (Exception e) {
            msgHandler.obtainMessage(1, "wdnmd冲不出来了("+e+")").sendToTarget();//向主线程发送错误信息
            Log.e("[ThreadError]",""+e);
        }
    }
}
