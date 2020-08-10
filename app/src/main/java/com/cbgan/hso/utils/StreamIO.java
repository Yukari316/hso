package com.cbgan.hso.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class StreamIO {
    public static boolean save_setu(Bitmap setu, Handler msgHandler, JSONObject setu_json, Context context){
        String StorageState = Environment.getExternalStorageState();//获取外部存储状态
        if (StorageState.equals(Environment.MEDIA_MOUNTED)){
            if(Build.VERSION.SDK_INT<29){//API Level<29
                //安卓10以下的文件系统适配，避免空指针
                //检查图片目录是否存在
                File hsoDir = new File(Environment.getExternalStorageDirectory(), "Pictures/hso");
                Log.i("[API<29]","File System Check, RootStorageDirectory="+hsoDir.toPath());
                if(!hsoDir.exists()) hsoDir.mkdirs();//不存在目录时创建目录
                File setu_file = new File(hsoDir,setu_json.get("pid")+".jpg");
                try{
                    FileOutputStream setu_fileoutput_stream = new FileOutputStream(setu_file);
                    //写入文件流
                    setu.compress(Bitmap.CompressFormat.JPEG, 100, setu_fileoutput_stream);//处理色图
                    setu_fileoutput_stream.flush();
                    setu_fileoutput_stream.close();
                    //发送系统广播
                    Uri uri = Uri.fromFile(setu_file);
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
                }catch (Exception e){
                    Log.e("[Dir]",""+e);
                    msgHandler.obtainMessage(4, "wdnmd冲不出来了(文件系统错误:"+e+")").sendToTarget();//向主线程发送错误信息
                    return false;
                }
            }else {//API Level>=29
                //写入图片信息
                ContentValues setu_img_value = new ContentValues();
                //色图名
                setu_img_value.put(MediaStore.Images.Media.DISPLAY_NAME,setu_json.get("pid").toString());
                //色图信息
                setu_img_value.put(MediaStore.Images.Media.DESCRIPTION,"Title="+setu_json.get("title")+"Author="+setu_json.get("author"));
                //文件格式
                setu_img_value.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
                        /*  色图存储相对路径
                            !----CAUTION----!
                            相对路径语句在API29以下系统不支持*/
                setu_img_value.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/hso");
                //生成Uri
                Uri setu_uri=context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,setu_img_value);
                Log.i("[Get_Uri]",setu_uri.toString());
                try{
                    //写入文件流
                    OutputStream setu_output_stream=context.getContentResolver().openOutputStream(setu_uri);
                    setu.compress(Bitmap.CompressFormat.JPEG, 100, setu_output_stream);//处理色图
                    setu_output_stream.close();
                }catch (Exception e){
                    Log.e("[Dir]",""+e);
                    msgHandler.obtainMessage(4,"wdnmd冲不出来了(文件系统错误:"+e+")").sendToTarget();
                    return false;
                }
            }
            Log.i("[Bitmap]",setu_json.get("pid").toString()+".jpg write successful");
            msgHandler.obtainMessage(5,"冲出来了("+setu_json.get("pid").toString()+".jpg)").sendToTarget();
            return true;
        }
        return false;
    }
}