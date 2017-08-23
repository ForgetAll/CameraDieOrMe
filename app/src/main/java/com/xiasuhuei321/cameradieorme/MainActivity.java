package com.xiasuhuei321.cameradieorme;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    public static MainActivity instance;

    static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Log.e("MainActivity", "onCreate");
        instance = this;
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
//        startActivity(new Intent(this, SecondActivity.class));
//        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.type = WindowManager.LayoutParams.TYPE_PHONE;
//        params.format = PixelFormat.RGB_888;
//        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        params.gravity = Gravity.TOP | Gravity.LEFT;
//        params.width = 500;
//        params.height = 500;
//        View v = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
//
//        wm.addView(v, params);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.e("MainActivity", "onDestroy");
    }
}
