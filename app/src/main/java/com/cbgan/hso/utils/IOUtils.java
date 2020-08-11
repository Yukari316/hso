package com.cbgan.hso.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.cbgan.hso.resource.Values;
import com.cbgan.hso.ui.MainActivity;

public class IOUtils {
    private final String SourceKeyName = "source";
    private final String LoliconTokenName = "lolicon_token";
    private final String YukariTokenName = "yukari_token";
    private SharedPreferences sharedPreferences;
    private Context context;

    public IOUtils(Context context){
        this.sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        this.context = context;
    }

    /*
    初始化配置文件
     */
    public void InitData(){
        int currentSource=sharedPreferences.getInt(SourceKeyName,-1);
        if(currentSource == -1){
            Log.i("[SharedPreferences_source]","Source config not found,Create new");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(SourceKeyName,0);
            editor.putString(LoliconTokenName,"");
            editor.putString(YukariTokenName,"");
            editor.apply();
        }else {
            Resources res = context.getResources();
            Log.i("[API source check]","Get source type"+Values.source[currentSource]);
        }
        Toast.makeText(context,"当前使用源："+Values.source[currentSource],Toast.LENGTH_SHORT).show();
    }

    public int GetSourceType(){
        int currentSource=sharedPreferences.getInt(SourceKeyName,-1);
        if(currentSource != -1){
            return currentSource;
        }else {
            Log.e("[Unknow error]","get config value error(value:source is null)");
            System.exit(0);
            return -1;
        }
    }

    public boolean SwitchSource(int apiType){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SourceKeyName,apiType);
        return editor.commit();
    }
}