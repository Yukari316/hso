package com.cbgan.hso.thread;
import android.util.Log;
import android.os.Handler;

import com.cbgan.hso.resource.MessageStatus;
import com.cbgan.hso.utils.NET;

import org.json.simple.JSONObject;

import java.util.Objects;

public class SetuNetThread extends Thread {//获取色图的线程
    public JSONObject Setu_info;
    private String Setu_Path;
    private Handler MsgHandler;

    //传入外部变量
    public SetuNetThread(String setu_PATH, Handler msgHandler) {
        this.Setu_Path = setu_PATH;
        this.MsgHandler = msgHandler;
    }

    @Override
    public void run(){
        Log.i("[Thread]URL", Setu_Path);
        try {
            Setu_info = NET.GET_JSON(Setu_Path, MsgHandler);
            if (Setu_info == null){
                Log.e("[Unknow error]","info is null");
                MsgHandler.obtainMessage(MessageStatus.FAILURE, "wdnmd冲不出来了(未知错误)").sendToTarget();//向主线程发送错误信息
                return;
            }
            String setu_bitmap_url = Objects.requireNonNull(Setu_info.get("url")).toString();
            Log.i("[setu_url]",""+setu_bitmap_url);
            NET.GET_IMG(setu_bitmap_url, MsgHandler);
        } catch (Exception e) {
            Log.e("[ThreadError]", e.toString());
        }
    }
}
