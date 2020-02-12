package com.cbgan.hso.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.cbgan.hso.R;
import com.cbgan.hso.StreamIO;
import com.cbgan.hso.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import me.gujun.android.taggroup.TagGroup;

public class MainActivity extends AppCompatActivity {
    int 你可真是他娘是个天才;
    //UI组件
    private TextView piclink_pix,piclink_pic,piclink_auth,setu_name;
    private TagGroup tags;
    private LinearLayout INFOUI1,INFOUI2,INFOUI3,TagUI;
    private ConstraintLayout setu_Layout;
    private Bitmap setu;
    private ImageView setu_view;
    private Button hso,save,piclink_pix_net,piclink_pic_net,piclink_auth_net,R18_SW;
    private ProgressBar waitNet;
    private ActionMenuItemView stopNet;
    //网络线程
    private Thread Net;
    private boolean isStoped = true;//线程停止标识
    //色图相关信息
    private int hsoSize;//色图的大小
    private JSONObject setu_json;
    private boolean R18 = false;
    private boolean load_success = false;//json加载成功标识
    //URL信息
    private String setu_path="https://api.lolicon.app/setu/?r18=0";
    private String setu_path_r18="https://api.lolicon.app/setu/?r18=1";
    private String pixiv_auth_path="https://www.pixiv.net/users/";
    private String pixiv_pic_path="https://www.pixiv.net/artworks/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar title = findViewById(R.id.Title);
        title.inflateMenu(R.menu.items);
        //文本UI
        piclink_pix=findViewById(R.id.piclink_pix);
        piclink_pic=findViewById(R.id.piclink_pic);
        piclink_auth=findViewById(R.id.piclink_auth);
        setu_name=findViewById(R.id.setu_name);
        //布局UI
        INFOUI1=findViewById(R.id.INFOUI1);
        INFOUI2=findViewById(R.id.INFOUI2);
        INFOUI3=findViewById(R.id.INFOUI3);
        TagUI=findViewById(R.id.TagUI);
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
        stopNet=findViewById(R.id.stop);
        tags=findViewById(R.id.tags);
        你可真是他娘是个天才=1;

        stopNet.setVisibility(View.GONE);//隐藏中止按钮
        if(Build.VERSION.SDK_INT<29) CheckPrm();//对Android Q以下设备申请权限
        Log.i("[device api level]",Build.VERSION.SDK_INT+"");

        tags.setTags(new String[]{"Tag1", "Tag2", "Tag3"});
        tags.setTags(new  String[]{});

        //禁用保存按钮
        save.setEnabled(false);
        save.setBackgroundColor(Color.GRAY);

        title.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {//ToolBar按钮监听
                switch (item.getItemId()){
                    case R.id.share_setu://分享图片
                        if(setu!=null){
                            //Bitmap转为Uri
                            Uri setu_uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),setu,null,null));
                            Intent share_setu = new Intent();
                            share_setu.setAction(Intent.ACTION_SEND);//设置Intent动作类型
                            share_setu.setType("image/*");//设置发送类型
                            share_setu.putExtra(Intent.EXTRA_STREAM,setu_uri);//写入Uri
                            share_setu = Intent.createChooser(share_setu,"👴要开车");//创建分享Dialog
                            startActivity(share_setu);
                        }else {
                            Toast.makeText(MainActivity.this,"你分享个🔨(未加载任何图片）",Toast.LENGTH_SHORT).show();
                        }
                        Log.i("[fuck]","fuck");
                        break;
                    case R.id.stop://图片停止加载
                        Net.interrupt();
                        isStoped=true;
                        Toast.makeText(MainActivity.this,"不要停下来啊（指加载色图）\n[色图下载被中止]",Toast.LENGTH_SHORT).show();
                        if(setu!=null) {//清空Bitmap
                            setu.recycle();//回收Bitmap
                            setu=null;//置空
                        }
                        if (!load_success){
                            INFO_UI_CLEAR();
                            INFO_UI_HIDE();
                        }
                        //load_success=false;
                        waitNet.setVisibility(View.GONE);
                        stopNet.setVisibility(View.GONE);//隐藏中止按钮
                        //启用手冲按钮
                        hso.setEnabled(true);
                        hso.setBackgroundColor(Color.parseColor("#F16090"));
                        hso.setText("再给👴整一个");
                        break;
                    case R.id.github:
                        Uri github_uri = Uri.parse("https://github.com/CBGan/hso");
                        Intent github_intent = new Intent(Intent.ACTION_VIEW, github_uri);
                        startActivity(github_intent);
                        break;
                    case R.id.goinfo:
                        Intent info_intent = new Intent(MainActivity.this,info_page.class);
                        startActivity(info_intent);
                }
                return true;
            }
        });

        hso.setOnClickListener(new View.OnClickListener() {//点击按钮获得色图
            @Override
            public void onClick(View v) {//色图获取按钮监听
                stopNet.setVisibility(View.VISIBLE);//显示停止按钮
                load_success=false;
                INFO_UI_CLEAR();
                //禁用色图按钮
                hso.setEnabled(false);
                hso.setBackgroundColor(Color.GRAY);
                //清空上一个信息文本和图片
                setu_view.setImageBitmap(null);
                if(setu!=null) {
                    setu.recycle();//清空Bitmap
                    setu=null;//置空
                }
                //禁用保存按钮
                save.setEnabled(false);
                save.setBackgroundColor(Color.GRAY);
                Toast.makeText(getApplication(), "开冲", Toast.LENGTH_SHORT).show();
                //开启网络线程
                waitNet.setVisibility(View.VISIBLE);
                if(R18) Net=new SetuNetThread(setu_path_r18);
                else Net=new SetuNetThread(setu_path);
                isStoped=false;
                Net.start();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {//点击按钮保存色图
            @Override
            public void onClick(View v) {//保存按钮监听
                if(StreamIO.save_setu(setu,mHandler,setu_json,MainActivity.this)){
                    //禁用保存按钮
                    save.setEnabled(false);
                    save.setBackgroundColor(Color.GRAY);
                }else{
                    Toast.makeText(MainActivity.this,"想🍑吃(保存失败)",Toast.LENGTH_SHORT);
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
        private Bitmap setu_thread;

        //传入外部变量
        public SetuNetThread(String setu_PATH) {
            this.setu_PATH = setu_PATH;
        }

        @Override
        public void run(){
            Log.i("[Thread]URL",setu_PATH);
            try {
                setu_info= net.GET_JSON(setu_PATH,mHandler);
                setu_bitmap_url=setu_info.get("url").toString();
                Log.i("[setu_url]",""+setu_bitmap_url);
                setu_thread=net.GET_IMG(setu_bitmap_url,mHandler);
                mHandler.obtainMessage(MSG_SUCCESS,setu_thread).sendToTarget();//向主线程发送JSON数据
            } catch (Exception e) {
                Log.e("[ThreadError]", "" + e);
            }
        }
    }

    private static final int MSG_SUCCESS = 0;//获取图片成功标识
    private static final int FAILURE = 1;//失败标识
    private static final int GET_JSON_SUCCESS = 2;//获取到JSON标识
    private static final int GET_IMG_SIZE = 3;//获取到图片大小标识
    private static final int IO_FAILURE = 4;//IO错误标识
    private static final int GET_TOAST_MSG = 5;//得到需要Toast显示的消息

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what) {
                case MSG_SUCCESS:
                    if(!isStoped){
                        isStoped=true;
                        stopNet.setVisibility(View.GONE);//隐藏中止按钮
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
                        }catch (Exception e){
                            Toast.makeText(MainActivity.this,"wdnmd冲不出来了("+e+")",Toast.LENGTH_SHORT).show();
                            Log.e("[IMG ERROR]",e.toString());
                            save.setEnabled(false);
                            save.setBackgroundColor(Color.parseColor("#F16090"));
                            INFO_UI_CLEAR();
                            load_success=false;
                        }
                        //启用保存按钮
                        save.setEnabled(true);
                        save.setBackgroundColor(Color.parseColor("#F16090"));
                        //启用手冲按钮
                        hso.setEnabled(true);
                        hso.setBackgroundColor(Color.parseColor("#F16090"));
                        hso.setText("再给👴整一个");
                    }
                    break;

                case FAILURE://线程内部出错
                    if(!isStoped){
                        Net.interrupt();//终止线程
                        isStoped=true;
                        if(setu!=null) {//清空Bitmap
                            setu.recycle();//回收Bitmap
                            setu=null;//置空
                        }
                        load_success=false;
                        waitNet.setVisibility(View.GONE);
                        stopNet.setVisibility(View.GONE);//隐藏中止按钮
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
                    }
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
                    //将JSON中的tags数据转换为String数组并更新UI
                    String tags_string = setu_json.get("tags").toString();
                    try {
                        JSONArray param = new JSONArray(tags_string);
                        String[] tags_array = new String[param.length()];
                        for (int i=0;i<param.length();i++){
                            tags_array[i] = param.get(i).toString();
                        }
                        tags.setTags(tags_array);
                    } catch (JSONException e) {
                        Log.e("[Tag Parse Err]",e.toString());
                    }
                    load_success=true;
                    break;

                case GET_IMG_SIZE://得到图片文件大小
                    hsoSize=(Integer) msg.obj/1024;
                    Log.i("[bitmapSize]",msg.obj.toString());
                    break;

                case IO_FAILURE://IO错误
                    Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    save.setText("👴死了(重试保存)");
                    break;

                case GET_TOAST_MSG:
                    Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
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
        stopNet.setVisibility(View.GONE);
        INFOUI1.setVisibility(View.GONE);
        INFOUI2.setVisibility(View.GONE);
        INFOUI3.setVisibility(View.GONE);
        TagUI.setVisibility(View.GONE);
    }

    public void INFO_UI_SHOW(){
        INFOUI1.setVisibility(View.VISIBLE);
        INFOUI2.setVisibility(View.VISIBLE);
        INFOUI3.setVisibility(View.VISIBLE);
        TagUI.setVisibility(View.VISIBLE);
    }

    public void INFO_UI_CLEAR(){
        piclink_pix.setText("");
        piclink_pic.setText("");
        piclink_auth.setText("");
        save.setText("给👴拿下（保存）");
        setu_name.setText("N/A");
        tags.setTags(new String[]{});
    }
}