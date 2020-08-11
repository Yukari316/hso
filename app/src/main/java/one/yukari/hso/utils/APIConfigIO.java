package one.yukari.hso.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import one.yukari.hso.resource.Values;

public class APIConfigIO {
    private final String SourceKeyName = "source";
    private SharedPreferences sharedPreferences;
    private Context context;

    public APIConfigIO(Context context){
        this.sharedPreferences = context.getSharedPreferences("api_config", Context.MODE_PRIVATE);
        this.context = context;
    }

    /*
    初始化配置文件
     */
    public boolean InitData(){
        //检查是否存在初始值
        int currentSource=sharedPreferences.getInt(SourceKeyName,-1);
        if(currentSource == -1){//不存在值
            Log.i("[SharedPreferences_source]","Source config not found,Create new");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //写入所用源
            editor.putInt(SourceKeyName,0);
            //写入初始化APIKEY
            for (String key_name :
                    Values.apikey_names) {
                editor.putString(key_name, "");
            }
            //提交修改
            return editor.commit();
        }else {//存在值
            Log.i("[API source check]","Get source type"+ Values.source[currentSource]);
            //显示选定的源
            Toast.makeText(context,"当前使用源："+Values.source[currentSource],Toast.LENGTH_SHORT).show();
            return true;
        }
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

    public String GetApiKey(int sourceType){
        return sharedPreferences.getString(Values.apikey_names[sourceType],"");
    }

    public boolean ChangeApiKey(int sourceType, String newApiKey){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Values.apikey_names[sourceType],newApiKey);
        return editor.commit();
    }

    public boolean SwitchSource(int apiType){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SourceKeyName,apiType);
        return editor.commit();
    }
}