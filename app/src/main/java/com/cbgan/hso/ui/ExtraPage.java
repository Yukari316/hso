package com.cbgan.hso.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cbgan.hso.R;
import com.cbgan.hso.resource.MessageStatus;
import com.cbgan.hso.thread.SetuUploadThread;
import com.dandan.jsonhandleview.library.JsonViewLayout;

import org.json.simple.JSONObject;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class ExtraPage extends AppCompatActivity {

    private EditText PID_Editor;
    private EditText Token_Editor;
    private ProgressBar WaitNet;
    private FrameLayout JsonFramelayout;
    private Thread Net;

    private JsonViewLayout jsonViewLayout;
    private int JsonViewIndex = 0;

    private boolean isStoped = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_api_page);

        PID_Editor = findViewById(R.id.pid);
        Token_Editor = findViewById(R.id.token);
        WaitNet = findViewById(R.id.net_uploading);
        JsonFramelayout = findViewById(R.id.json_framelayout);
        Button uploadBtn = findViewById(R.id.upload_pic);

        uploadBtn.setOnClickListener(new View.OnClickListener() {//点击按钮获得色图
            @Override
            public void onClick(View v) {//色图获取按钮监听
                String token = Token_Editor.getText().toString();
                String pid = PID_Editor.getText().toString();
                if(token.equals("")||pid.equals("")){
                    Toast.makeText(ExtraPage.this,"Pid/Token为空",Toast.LENGTH_SHORT).show();
                }else{
                    //删除原来的json视图
                    JsonFramelayout.removeAllViews();
                    Net=new SetuUploadThread(pid,token,mHandler);
                    isStoped=false;
                    Net.start();
                    WaitNet.setVisibility(View.VISIBLE);
                    PID_Editor.setEnabled(false);
                    Token_Editor.setEnabled(false);
                }
            }
        });
    }

    private JsonViewLayout createJsonView() {
        JsonViewLayout jsonView = new JsonViewLayout(this);
        jsonView.setId(JsonViewIndex++);
        return jsonView;
    }

    private Handler mHandler = new Handler(){
        public void handleMessage (Message msg) {//此方法在ui线程运行
            WaitNet.setVisibility(View.GONE);
            PID_Editor.setEnabled(true);
            Token_Editor.setEnabled(true);
            switch (msg.what){
                case MessageStatus.GET_JSON_SUCCESS:
                    isStoped=true;
                    JSONObject serverRespons = (JSONObject) msg.obj;
                    JsonViewLayout currJson = createJsonView();
                    JsonFramelayout.addView(currJson);
                    currJson.bindJson(serverRespons.toJSONString());
                    currJson.expandAll();
                    String code= Objects.requireNonNull(serverRespons.get("code")).toString();
                    if(code.equals("0")){
                        String count= Objects.requireNonNull(serverRespons.get("count")).toString();
                        Toast.makeText(ExtraPage.this,"上传成功！\n图片数量"+count,Toast.LENGTH_SHORT).show();
                        Log.i("[pic upload]","upload success");
                    }else{
                        String message= Objects.requireNonNull(serverRespons.get("message")).toString();
                        Toast.makeText(ExtraPage.this,"上传失败\n("+message+")",Toast.LENGTH_SHORT).show();
                        Log.i("[pic upload]","upload filed");
                    }
                    break;
                case MessageStatus.FAILURE:
                    Net.interrupt();
                    Log.e("[pic upload]","upload filed");
                    Toast.makeText(ExtraPage.this,"上传失败\n("+msg.obj+")",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
