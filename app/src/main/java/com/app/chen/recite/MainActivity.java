package com.app.chen.recite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    String path = "";
    String answer = "";
    boolean ifFilter = false;
    ArrayList<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取导入文件
        Intent intent = getIntent();
        if(intent.getData() != null){
            getFile(intent.getData().getPath());
        }

        //获取默认路径
        SharedPreferences sharedPreferences = getSharedPreferences("default_value", MODE_PRIVATE);
        String path_path = sharedPreferences.getString("path_path", "");
        if (path_path != "") {
            this.path = path_path;
            setTitle(path);
        }

        //是否过滤已抽
        Switch filter = findViewById(R.id.filter);
        filter.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ifFilter = true;
            } else {
                ifFilter = false;
            }
            arrayList = read(path);
        });

        arrayList = read(path);
        System.out.println(arrayList.size());

        //权限检查
        if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager() == false){
                Intent per = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                per.setData(Uri.parse("package:" + getPackageName()));
                startActivity(per);
            }
        }
    }


    public void Answer(View v) {
        TextView answer = findViewById(R.id.answer);
        answer.setText(this.answer);
    }

    public void getFile(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getSharedPreferences("default_value", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("path_path", text);
        edit.commit();
        System.out.println(sharedPreferences.getString("default_value","path_path"));
        this.path = text;
        read(path);
    }

    public void Random(View v) {
        TextView question = findViewById(R.id.question);
        TextView answer = findViewById(R.id.answer);
        int r = 0;

        if (path != "" && arrayList.size() > 0) {
            System.out.println(arrayList.size());
            r = new Random().nextInt(arrayList.size());
            if (r >= 0) {
                question.setText(getQuestion(arrayList.get(r)));
                answer.setText("/////");
                this.answer = getAnswer(arrayList.get(r));

                //移除抽过
                if (ifFilter) {
                    arrayList.remove(arrayList.get(r));
                    System.out.println(arrayList.size());
                }
            }
        }
    }

    public ArrayList<String> read(String path) {
        ArrayList<String> arrayList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String s = null;
            while ((s = bufferedReader.readLine()) != null) {
                arrayList.add(s);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public static String getQuestion(String text) {
        String question = "";
        if (text.indexOf(" ") != -1) {
            question = text.substring(0, text.indexOf(" "));
        } else {
            question = text;
        }

        return question;
    }

    public static String getAnswer(String text) {
        String answer = "";
        if (text.indexOf(" ") != -1) {
            answer = text.substring(text.indexOf(" "));
        } else {
            answer = "无答案";
        }

        return answer;
    }
}