package one.yukari.hso.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

import one.yukari.hso.resource.MessageStatus;
import one.yukari.hso.utils.APIConfigIO;
import one.yukari.hso.R;
import one.yukari.hso.thread.SetuNetThread;
import one.yukari.hso.utils.StreamIO;
import one.yukari.hso.resource.Values;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.Objects;

import me.gujun.android.taggroup.TagGroup;

public class MainActivity extends AppCompatActivity {
    int 你可真是他娘是个天才;
    //UI组件
    private TextView piclink_pix,piclink_pic,piclink_auth,setu_name;
    private TagGroup tags;
    private LinearLayout INFOUI1,INFOUI2,INFOUI3,TagUI;
    private Bitmap setu;
    private ImageView setu_view;
    private Button hso;
    private Button save;
    private Button R18_SW;
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
        //Dialog

        //按钮UI
        hso=findViewById(R.id.hso);
        save=findViewById(R.id.save);
        Button piclink_pix_net = findViewById(R.id.piclink_pix_net);
        Button piclink_pic_net = findViewById(R.id.piclink_pic_net);
        Button piclink_auth_net = findViewById(R.id.piclink_auth_net);
        R18_SW=findViewById(R.id.R18);

        waitNet=findViewById(R.id.NetStateBar);
        setu_view=findViewById(R.id.setu);
        stopNet=findViewById(R.id.stop);
        tags=findViewById(R.id.tags);

        stopNet.setVisibility(View.GONE);//隐藏中止按钮
        if(Build.VERSION.SDK_INT<29) CheckPrm();//对Android Q以下设备申请权限
        Log.i("[device api level]",Build.VERSION.SDK_INT+"");

        //检查配置文件
        APIConfigIO config = new APIConfigIO(MainActivity.this);
        if(!config.InitData()){
            System.exit(0);
        }

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
                    case R.id.switch_source://切换色图源
                        AlertDialog.Builder builderSource = new AlertDialog.Builder(MainActivity.this);
                        final APIConfigIO ioAction = new APIConfigIO(MainActivity.this);
                        final int oldType = ioAction.GetSourceType();
                        //创建单选列表dialog
                        builderSource.setTitle(R.string.sw_source)
                                .setSingleChoiceItems(R.array.source , oldType,
                                        new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                //切换源
                                                if (ioAction.SwitchSource(which)){
                                                    Log.i("[source sw]","change source to "+Values.source[which]);
                                                    Toast.makeText(MainActivity.this,"成功切换到"+Values.source[which]+"!",Toast.LENGTH_SHORT).show();
                                                }else{//配置文件修改失败
                                                    Log.e("[source sw]","change source to "+Values.source[which]+"failed");
                                                    Toast.makeText(MainActivity.this,"切换源失败",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                )
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {//取消按钮
                                    public void onClick(DialogInterface dialog, int id) {
                                        //点击取消后切换回原来的源
                                        ioAction.SwitchSource(oldType);
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        builderSource.create().show();
                        break;
                    case R.id.change_api_key://修改色图源的APIKEY
                        final APIConfigIO apiConfigIO = new APIConfigIO(MainActivity.this);
                        //获取当前源类型
                        final int apiType = apiConfigIO.GetSourceType();
                        //获取原Key
                        String oldApiKey = apiConfigIO.GetApiKey(apiType);
                        AlertDialog.Builder builderApiKeyInput = new AlertDialog.Builder(MainActivity.this);
                        builderApiKeyInput.setView(R.layout.apikey_dialog)
                                // Add action buttons
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {//确认按钮
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        EditText apiKey = ((AlertDialog)dialog).findViewById(R.id.api_key_edit_text);
                                        if(apiKey == null){//防止某些时候系统抽风找不到组件
                                            Toast.makeText(MainActivity.this,"遇到了未知错误",Toast.LENGTH_SHORT).show();
                                            Log.e("[apikey dialog error]","EditText not found");
                                            dialog.cancel();
                                            return;
                                        }
                                        //获取输入的值
                                        String key = apiKey.getText().toString();
                                        if(apiConfigIO.ChangeApiKey(apiType,key)){//修改Key
                                            if(key.equals("")) Toast.makeText(MainActivity.this,"已清空API KEY",Toast.LENGTH_SHORT).show();
                                            else Toast.makeText(MainActivity.this,"已修改API KEY为:"+key,Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(MainActivity.this,"修改API KEY失败\n(修改配置文件时发生错误)",Toast.LENGTH_SHORT).show();
                                            Log.e("[Change Api key]","can't write to config file");
                                        }
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {//取消按钮
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builderApiKeyInput.create();
                        alert.show();
                        EditText apiKey = alert.findViewById(R.id.api_key_edit_text);
                        if(apiKey == null){//防止某些时候系统抽风找不到组件
                            Toast.makeText(MainActivity.this,"遇到了未知错误",Toast.LENGTH_SHORT).show();
                            Log.e("[apikey dialog error]","EditText not found");
                        }else if(!oldApiKey.equals("")) apiKey.setHint("原Key:"+oldApiKey);
                        break;
                    case R.id.github:
                        Uri github_uri = Uri.parse("https://github.com/CBGan/hso");
                        Intent github_intent = new Intent(Intent.ACTION_VIEW, github_uri);
                        startActivity(github_intent);
                        break;
                    case R.id.goinfo:
                        Intent info_intent = new Intent(MainActivity.this,info_page.class);
                        startActivity(info_intent);
                        break;
                    case R.id.extra_api:
                        Intent extro_intent = new Intent(MainActivity.this,ExtraPage.class);
                        startActivity(extro_intent);
                        break;
                }
                return true;
            }
        });

        hso.setOnClickListener(new View.OnClickListener() {//点击按钮获得色图
            @Override
            public void onClick(View v) {//色图获取按钮监听
                Log.i("[Condig file check]","Try find config");
                APIConfigIO config = new APIConfigIO(MainActivity.this);
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
                //读取API类型
                int apiType = config.GetSourceType();
                //读取API KEY
                String apiKey = config.GetApiKey(apiType);
                Uri.Builder url = Uri.parse(Values.source_url[apiType]).buildUpon();
                if(R18) url.appendQueryParameter("r18","1");
                if(!apiKey.equals("")) url.appendQueryParameter("apikey",apiKey);
                Net=new SetuNetThread(url.toString(),mHandler);
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
                    Toast.makeText(MainActivity.this,"想🍑吃(保存失败)",Toast.LENGTH_SHORT).show();
                }
            }
        });

        R18_SW.setOnClickListener(new View.OnClickListener() {//点击切换R18模式
            @SuppressLint("SetTextI18n")
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
                    Uri pixiv_uri = Uri.parse(Values.pixiv_pic_url +setu_json.get("pid"));
                    Intent pixiv_intent = new Intent(Intent.ACTION_VIEW, pixiv_uri);
                    startActivity(pixiv_intent);
                }
            }
        });

        piclink_pic_net.setOnClickListener(new View.OnClickListener() {//点击按钮跳转pic_cat
            @Override
            public void onClick(View v) {
                if (load_success) {
                    Uri pic_uri = Uri.parse(Objects.requireNonNull(setu_json.get("url")).toString());
                    Intent pic_intent = new Intent(Intent.ACTION_VIEW, pic_uri);
                    startActivity(pic_intent);
                }
            }
        });

        piclink_auth_net.setOnClickListener(new View.OnClickListener() {//点击按钮跳转pixiv画师页
            @Override
            public void onClick(View v) {
                if (load_success) {
                    Uri author_uri = Uri.parse(Values.pixiv_auth_url +setu_json.get("uid"));
                    Intent author_intent = new Intent(Intent.ACTION_VIEW, author_uri);
                    startActivity(author_intent);
                }
            }
        });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what) {
                case MessageStatus.IMG_SUCCESS:
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

                case MessageStatus.FAILURE://线程内部出错
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

                case MessageStatus.GET_JSON_SUCCESS://获取JSON成功
                    setu_json=(JSONObject) msg.obj;
                    INFO_UI_SHOW();
                    piclink_pix.setText(Values.pixiv_pic_url +setu_json.get("pid"));
                    piclink_pic.setText(setu_json.get("url").toString());
                    piclink_auth.setText(Values.pixiv_auth_url +setu_json.get("uid"));
                    setu_name.setText(setu_json.get("title")+"\n"+setu_json.get("author"));
                    Log.i("[JSON_INFO]",""+msg.obj);
                    //将JSON中的tags数据转换为String数组并更新UI
                    String tags_string = Objects.requireNonNull(setu_json.get("tags")).toString();
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

                case MessageStatus.GET_IMG_SIZE://得到图片文件大小
                    hsoSize=(Integer) msg.obj/1024;
                    Log.i("[bitmapSize]",msg.obj.toString());
                    break;

                case MessageStatus.IO_FAILURE://IO错误
                    Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    save.setText("👴死了(重试保存)");
                    break;

                case MessageStatus.TOAST:
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