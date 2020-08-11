package one.yukari.hso.thread;

import android.os.Handler;
import android.util.Log;

import one.yukari.hso.utils.NET;

public class SetuUploadThread extends Thread {
    private String Pid;
    private String Token;
    private Handler MsgHandler;

    //传入外部变量
    public SetuUploadThread(String pid, String token, Handler msgHandler) {
        this.Pid = pid;
        this.Token = token;
        this.MsgHandler = msgHandler;
    }

    @Override
    public void run(){
        Log.i("[Upload Thread]","Try send pid to server");
        try{
            NET.UPLOAD_IMG(Pid,Token,MsgHandler);
        }catch (Exception e){
            Log.e("[ThreadError]", e.toString());
        }
    }
}
