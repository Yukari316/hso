package com.cbgan.hso.utils;
import android.graphics.Bitmap;
import android.util.Log;
import android.os.Handler;
import org.json.simple.JSONObject;

import java.util.Objects;

public class SetuNetThread extends Thread {//获取色图的线程
    public JSONObject setu_info;
    private String setu_PATH,setu_bitmap_url;
    private Bitmap setu_thread;
    private Handler mHandler;
    private static final int MSG_SUCCESS = 0;//获取图片成功标识

    //传入外部变量
    public SetuNetThread(String setu_PATH, Handler msgHandler) {
        this.setu_PATH = setu_PATH;
        this.mHandler = msgHandler;
    }

    @Override
    public void run(){
        Log.i("[Thread]URL",setu_PATH);
        try {
            setu_info = NET.GET_JSON(setu_PATH,mHandler);
            Log.i("[setu_url]",""+setu_bitmap_url);
            NET.GET_IMG(
                    Objects.requireNonNull(setu_info.get("url")).toString(),
                    mHandler);
        } catch (Exception e) {
            Log.e("[ThreadError]", "" + e);
        }
    }
}
