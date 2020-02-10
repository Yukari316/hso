package com.cbgan.hso;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    int 你可真是他娘是个天才;
    //UI组件
    private TextView piclink_pix,piclink_pic,piclink_auth,setu_name;
    private LinearLayout INFOUI1,INFOUI2,INFOUI3;
    private ConstraintLayout setu_Layout;
    private Bitmap setu;
    private ImageView setu_view;
    private Button hso,save,piclink_pix_net,piclink_pic_net,piclink_auth_net,R18_SW;
    private ProgressBar waitNet;
    //网络线程
    private Thread Net;
    //色图相关信息
    private int hsoSize;//色图的大小
    private JSONObject setu_json;
    private boolean R18 = false;
    private boolean load_success = false;//加载成功标识
    //URL信息
    private String setu_path="https://api.lolicon.app/setu/?r18=0";
    private String setu_path_r18="https://api.lolicon.app/setu/?r18=1";
    private String pixiv_auth_path="https://www.pixiv.net/users/";
    private String pixiv_pic_path="https://www.pixiv.net/artworks/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //文本UI
        piclink_pix=findViewById(R.id.piclink_pix);
        piclink_pic=findViewById(R.id.piclink_pic);
        piclink_auth=findViewById(R.id.piclink_auth);
        setu_name=findViewById(R.id.setu_name);
        //布局UI
        INFOUI1=findViewById(R.id.INFOUI1);
        INFOUI2=findViewById(R.id.INFOUI2);
        INFOUI3=findViewById(R.id.INFOUI3);
        setu_Layout=findViewById(R.id.setu_layout);
        //按钮UI
        hso=findViewById(R.id.hso);
        save=findViewById(R.id.save);
        piclink_pix_net=findViewById(R.id.piclink_pix_net);
        piclink_pic_net=findViewById(R.id.piclink_pic_net);
        piclink_auth_net=findViewById(R.id.piclink_auth_net);
        R18_SW=findViewById(R.id.R18);

        waitNet=findViewById(R.id.NetStateBar);
        setu_view=findViewById(R.id.setu);
        你可真是他娘是个天才=1;
        if(Build.VERSION.SDK_INT<29) CheckPrm();//对Android Q以下设备申请权限
        Log.i("[device api level]",Build.VERSION.SDK_INT+"");

        //禁用保存按钮
        save.setEnabled(false);
        save.setBackgroundColor(Color.GRAY);

        hso.setOnClickListener(new View.OnClickListener() {//点击按钮获得色图
            @Override
            public void onClick(View v) {//色图获取按钮监听
                load_success=false;
                INFO_UI_CLEAR();
                //禁用色图按钮
                hso.setEnabled(false);
                hso.setBackgroundColor(Color.GRAY);
                //清空上一个信息文本和图片
                setu_view.setImageBitmap(null);
                if(setu!=null) setu.recycle();//清空Bitmap
                //禁用保存按钮
                save.setEnabled(false);
                save.setBackgroundColor(Color.GRAY);
                Toast.makeText(getApplication(), "开冲", Toast.LENGTH_SHORT).show();
                //开启网络线程
                waitNet.setVisibility(View.VISIBLE);
                if(R18) Net=new SetuNetThread(setu_path_r18);
                else Net=new SetuNetThread(setu_path);
                Net.start();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {//点击按钮保存色图
            @Override
            public void onClick(View v) {//保存按钮监听
                String StorageState = Environment.getExternalStorageState();//获取外部存储状态
                if (StorageState.equals(Environment.MEDIA_MOUNTED)){
                    Uri setu_uri=null;
                    File setu_file=null;
                    if(Build.VERSION.SDK_INT<29){//API Level<29
                        //安卓10以下的文件系统适配，避免空指针
                        //检查图片目录是否存在
                        File hsoDir = new File(Environment.getExternalStorageDirectory(), "Pictures/hso");
                        Log.i("[API<29]","File System Check, RootStorageDirectory="+hsoDir.toPath());
                        if(!hsoDir.exists()) hsoDir.mkdirs();//不存在目录时创建目录
                        setu_file = new File(hsoDir,setu_json.get("pid")+".jpg");
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
                        setu_uri=MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,setu_img_value);
                        Log.i("[Get_Uri]",setu_uri.toString());
                    }
                    OutputStream setu_output_stream=null;
                    FileOutputStream setu_fileoutput_stream =null;
                    try{
                        //存入Pictur文件夹
                        if(Build.VERSION.SDK_INT<29){//API Level<29
                            //写入文件流
                            setu_fileoutput_stream = new FileOutputStream(setu_file);
                            setu.compress(Bitmap.CompressFormat.JPEG, 100, setu_fileoutput_stream);//处理色图
                            setu_fileoutput_stream.flush();
                            setu_fileoutput_stream.close();
                            //发送系统广播
                            Uri uri = Uri.fromFile(setu_file);
                            MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
                        }
                        else {//API Level>=29
                            //写入文件流
                            setu_output_stream=MainActivity.this.getContentResolver().openOutputStream(setu_uri);
                            setu.compress(Bitmap.CompressFormat.JPEG, 100, setu_output_stream);//处理色图
                            setu_output_stream.close();
                        }
                        Log.i("[Bitmap]",setu_json.get("pid").toString()+".jpg write successful");
                        Toast.makeText(getApplication(), "冲出来了("+setu_json.get("pid").toString()+".jpg)", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Log.e("[Dir]",""+e);
                        Toast.makeText(getApplication(), "wdnmd冲不出来了(文件系统错误:"+e+")", Toast.LENGTH_SHORT).show();
                    }
                    //禁用保存按钮
                    save.setEnabled(false);
                    save.setBackgroundColor(Color.GRAY);
                }
            }
        });

        R18_SW.setOnClickListener(new View.OnClickListener() {//点击切换R18模式
            @Override
            public void onClick(View v) {
                if(R18){
                    R18=false;
                    R18_SW.setText("R18 OFF");
                    R18_SW.setTextColor(Color.parseColor("#68C976"));
                    R18_SW.setBackgroundColor(Color.parseColor("#27000000"));
                }else{
                    R18=true;
                    R18_SW.setText("R18 ON!");
                    R18_SW.setTextColor(Color.parseColor("#D81B60"));
                    R18_SW.setBackgroundColor(Color.parseColor("#99F14343"));
                }
            }
        });

        piclink_pix_net.setOnClickListener(new View.OnClickListener() {//点击按钮跳转pixiv
            @Override
            public void onClick(View v) {
                if (load_success) {
                    Uri pixiv_uri = Uri.parse(pixiv_pic_path+setu_json.get("pid"));
                    Intent pixiv_intent = new Intent(Intent.ACTION_VIEW, pixiv_uri);
                    startActivity(pixiv_intent);
                }
            }
        });

        piclink_pic_net.setOnClickListener(new View.OnClickListener() {//点击按钮跳转pic_cat
            @Override
            public void onClick(View v) {
                if (load_success) {
                    Uri pic_uri = Uri.parse(setu_json.get("url").toString());
                    Intent pic_intent = new Intent(Intent.ACTION_VIEW, pic_uri);
                    startActivity(pic_intent);
                }
            }
        });

        piclink_auth_net.setOnClickListener(new View.OnClickListener() {//点击按钮跳转pixiv画师页
            @Override
            public void onClick(View v) {
                if (load_success) {
                    Uri author_uri = Uri.parse(pixiv_auth_path+setu_json.get("uid"));
                    Intent author_intent = new Intent(Intent.ACTION_VIEW, author_uri);
                    startActivity(author_intent);
                }
            }
        });
    }

    public class SetuNetThread extends Thread {//获取色图的线程
        private String setu_PATH,setu_bitmap_url;
        private JSONObject setu_info=null;

        //传入外部变量
        public SetuNetThread(String setu_PATH) {
            this.setu_PATH = setu_PATH;
        }

        @Override
        public void run(){
            Log.i("[Thread]URL",setu_PATH);
            try {
                setu_info=net.GET_JSON(setu_PATH,mHandler);
                setu_bitmap_url=setu_info.get("url").toString();
                Log.i("[setu_url]",""+setu_bitmap_url);
                setu=net.GET_IMG(setu_bitmap_url,mHandler);
                mHandler.obtainMessage(MSG_SUCCESS,setu).sendToTarget();//向主线程发送JSON数据
            } catch (Exception e) {
                Log.e("[ThreadError]", "" + e);
            }
        }
    }

    private static final int MSG_SUCCESS = 0;//获取图片成功标识
    private static final int FAILURE = 1;//失败标识
    private static final int GET_JSON_SUCCESS = 2;//获取到JSON标识
    private static final int GET_IMG_SIZE = 3;//获取到图片大小标识

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what) {
                case MSG_SUCCESS:
                    waitNet.setVisibility(View.GONE);
                    setu = (Bitmap)msg.obj;
                    Bitmap show = null;//缩略图
                    //对预览图进行压缩,避免图片过大内存溢出
                    try {
                        if(setu.getWidth()>1080){
                            float Scale_Ratio=(float) 1080/setu.getWidth();//计算缩放比例
                            Matrix matrix = new Matrix();
                            matrix.postScale(Scale_Ratio,Scale_Ratio);//设置缩放比例
                            show = Bitmap.createBitmap(setu,0,0,setu.getWidth(),setu.getHeight(),matrix,true);
                        }else{
                            show=setu;
                        }
                        Log.i("[CompressBitmap]","setu Bitmap size="+setu.getWidth()+"x"+setu.getHeight());
                        Log.i("[CompressBitmap]","show Bitmap size="+show.getWidth()+"x"+show.getHeight());
                        setu_view.setImageBitmap(show);
                        Toast.makeText(getApplication(), "色图下载成功("+hsoSize+"KB)", Toast.LENGTH_SHORT).show();
                        load_success=true;
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this,"wdnmd冲不出来了("+e+")",Toast.LENGTH_SHORT).show();
                        Log.e("[IMG ERROR]",e.toString());
                        save.setEnabled(false);
                        save.setBackgroundColor(Color.parseColor("#F16090"));
                        INFO_UI_CLEAR();
                    }

                    //启用保存按钮
                    save.setEnabled(true);
                    save.setBackgroundColor(Color.parseColor("#F16090"));
                    //启用手冲按钮
                    hso.setEnabled(true);
                    hso.setBackgroundColor(Color.parseColor("#F16090"));
                    hso.setText("再给👴整一个");
                    break;

                case FAILURE://线程内部出错
                    Net.interrupt();//终止线程
                    if(setu!=null) setu.recycle();//清空Bitmap
                    load_success=false;
                    waitNet.setVisibility(View.GONE);
                    Toast.makeText(getApplication(),(String)msg.obj, Toast.LENGTH_SHORT).show();
                    //禁用保存按钮
                    save.setEnabled(false);
                    save.setBackgroundColor(Color.GRAY);
                    //启用手冲按钮
                    hso.setEnabled(true);
                    hso.setBackgroundColor(Color.parseColor("#F16090"));
                    INFO_UI_CLEAR();
                    INFO_UI_HIDE();
                    hso.setText("再给👴整一个");
                    break;

                case GET_JSON_SUCCESS://获取JSON成功
                    setu_json=(JSONObject) msg.obj;
                    Toast.makeText(getApplication(), "色图信息获取成功", Toast.LENGTH_SHORT).show();
                    INFO_UI_SHOW();
                    piclink_pix.setText(pixiv_pic_path+setu_json.get("pid"));
                    piclink_pic.setText(setu_json.get("url").toString());
                    piclink_auth.setText(pixiv_auth_path+setu_json.get("uid"));
                    setu_name.setText(setu_json.get("title")+"\n"+setu_json.get("author"));
                    Log.i("[JSON_INFO]",""+msg.obj);
                    break;

                case GET_IMG_SIZE://得到图片文件大小
                    hsoSize=(Integer) msg.obj/1024;
                    Log.i("[bitmapSize]",""+msg.obj);
                    break;
            }
        }
    };

    private static final int PERMISSION_REQUEST_CODE = 1;//权限应答码
    public void CheckPrm(){//存储权限检查函数
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {//检查是否有存储读写权限
            //申请权限
            Log.d("[permission]", "permission denied to EXTERNAL_STORAGE - requesting it");
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,//外部写入
                    Manifest.permission.READ_EXTERNAL_STORAGE//外部读取
            };
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    public void INFO_UI_HIDE(){
        INFOUI1.setVisibility(View.GONE);
        INFOUI2.setVisibility(View.GONE);
        INFOUI3.setVisibility(View.GONE);
    }

    public void INFO_UI_SHOW(){
        INFOUI1.setVisibility(View.VISIBLE);
        INFOUI2.setVisibility(View.VISIBLE);
        INFOUI3.setVisibility(View.VISIBLE);
    }

    public void INFO_UI_CLEAR(){
        piclink_pix.setText("");
        piclink_pic.setText("");
        piclink_auth.setText("");
        setu_name.setText("N/A");
    }
}