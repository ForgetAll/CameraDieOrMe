package com.xiasuhuei321.cameradieorme.camera;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.xiasuhuei321.cameradieorme.R;

/**
 * Created by xiasuhuei321 on 2017/8/22.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 */

public class CameraActivity extends AppCompatActivity {

    private Camera camera;
    private FrameLayout preview;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View iv_take = findViewById(R.id.iv_take);

        final ObjectAnimator scaleX = ObjectAnimator.ofFloat(iv_take, "scaleX", 1f, 0.8f);
        final ObjectAnimator scaleY = ObjectAnimator.ofFloat(iv_take, "scaleY", 1f, 0.8f);


        iv_take.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setScaleX(0.9f);
                        v.setScaleY(0.9f);
                        scaleX.start();
                        scaleY.start();
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        scaleX.reverse();
                        scaleY.reverse();
                        break;
                }
                return false;
            }
        });
        iv_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.takePhoto();
            }
        });

        mPreview = new CameraPreview(this);
        CameraUtil.getInstance().init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            // 已有权限
            startCameraPre();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 如果权限申请成功
            startCameraPre();
        } else {
            Toast.makeText(this, "您已拒绝打开相机，想要使用此功能请手动打开相机权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCameraPre() {
        if (CameraUtil.checkCameraHardware(this)) {
            camera = CameraUtil.getInstance().getCameraInstance();
        }
        mPreview.setCamera(camera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        if (preview.getChildCount() == 0)
            preview.addView(mPreview);
    }
}
