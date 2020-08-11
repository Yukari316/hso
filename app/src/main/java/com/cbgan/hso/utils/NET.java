package com.cbgan.hso.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.UrlQuerySanitizer;
import android.os.Handler;
import android.util.Log;

import com.cbgan.hso.resource.MessageStatus;
import com.cbgan.hso.resource.Values;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
                JSONObject setu_json=(JSONObject)jsonParser.parse(new InputStreamReader(response, StandardCharsets.UTF_8));//转换为JSON
                String data= Objects.requireNonNull(setu_json.get("data")).toString();//获取data键值并转换为String
                data=data.substring(1,data.length()-1);//处理文本数据
                Log.i("data",data);
                JSONObject setu_json_data=(JSONObject)jsonParser.parse(data);//将文本数据转换为JSONObj
                msgHandler.obtainMessage(MessageStatus.GET_JSON_SUCCESS,setu_json_data).sendToTarget();//向主线程发送JSON数据
                return setu_json_data;
            }else{
                msgHandler.obtainMessage(MessageStatus.FAILURE, "wdnmd冲不出来了(CODE:"+responseCODE+")").sendToTarget();//向主线程发送网络错误信息
                Log.e("[NetworkError]","CODE:"+responseCODE);
            }
        } catch (Exception e) {
            msgHandler.obtainMessage(MessageStatus.FAILURE, "wdnmd冲不出来了("+e+")").sendToTarget();//向主线程发送错误信息
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
                msgHandler.obtainMessage(MessageStatus.GET_IMG_SIZE,connection.getContentLength()).sendToTarget();//得到图片大小
                InputStream response=connection.getInputStream();
                Bitmap setu_bitmap= BitmapFactory.decodeStream(response);//处理流为Bitmap
                msgHandler.obtainMessage(MessageStatus.IMG_SUCCESS,setu_bitmap).sendToTarget();//向主线程发送图片数据
            }else{
                msgHandler.obtainMessage(MessageStatus.FAILURE, "wdnmd冲不出来了(CODE:"+responseCODE+")").sendToTarget();//向主线程发送网络错误信息
                Log.e("[NetworkError]","CODE:"+responseCODE);
            }
        } catch (Exception e) {
            msgHandler.obtainMessage(MessageStatus.FAILURE, "wdnmd冲不出来了("+e+")").sendToTarget();//向主线程发送错误信息
            Log.e("[ThreadError]",""+e);
        }
    }

    public static void UPLOAD_IMG(String pid,String token, Handler msgHandler) {//向服务器上传色图
        try{
            URL yukariUploadUrl = new URL(Values.yukari_upload_url+"?pid="+pid+"&token="+token);
            HttpURLConnection connection=(HttpURLConnection) yukariUploadUrl.openConnection();
            connection.setConnectTimeout(5000);//设置超时
            connection.setRequestMethod("GET");//设置模式
            int responseCODE = connection.getResponseCode();
            if(responseCODE == 200){//请求成功
                InputStream response = connection.getInputStream();
                //处理获取数据流
                JSONParser jsonParser = new JSONParser();
                JSONObject setu_json=(JSONObject)jsonParser.parse(new InputStreamReader(response, StandardCharsets.UTF_8));//转换为JSON
                Log.i("[JSON]",setu_json+"");
                msgHandler.obtainMessage(MessageStatus.GET_JSON_SUCCESS,setu_json).sendToTarget();//发送数据到主线程
            }else{
                msgHandler.obtainMessage(MessageStatus.FAILURE, "网裂开(CODE:"+responseCODE+")").sendToTarget();//向主线程发送网络错误信息
                Log.e("[NetworkError]","CODE:"+responseCODE);
            }
        }catch (Exception e){
            msgHandler.obtainMessage(MessageStatus.FAILURE, "坏了，传不上去("+e+")").sendToTarget();//向主线程发送错误信息
            Log.e("[ThreadError]",""+e);
        }
    }
}
