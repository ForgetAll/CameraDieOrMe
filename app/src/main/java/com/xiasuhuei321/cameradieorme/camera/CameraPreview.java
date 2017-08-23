package com.xiasuhuei321.cameradieorme.camera;

/**
 * Created by xiasuhuei321 on 2017/8/22.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 */

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback, Camera.PictureCallback {
    public static final String TAG = "CameraPreview";
    public static final String DIRNAME = "MyCamera";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean canTake = false;
    private Context context;

    public CameraPreview(Context context) {
        super(context);
        this.context = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Log.i(TAG, "CameraPreview被创建 " + this.hashCode());
    }

    /**
     * surface在很多情况下都会被销毁，这个时候相机也会被释放。
     * 而这个类的camera就无法再使用了，所以需要外部再传入一个
     * 正确的Camera实例
     *
     * @param mCamera Camera实例
     */
    public void setCamera(Camera mCamera) {
        this.mCamera = mCamera;
        mHolder.addCallback(this);
        surfaceCreated(getHolder());
        Log.i(TAG, "serCamera" + " release = " + CameraUtil.getInstance().isRelease());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // surface创建完毕，camera设置预览
        Log.i(TAG, "surface view被创建");
        if (CameraUtil.getInstance().isRelease()) return;
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 在这里可以释放相机资源
        // 也可以在Activity中释放
        Log.i(TAG, "surface view被销毁 ");
        holder.removeCallback(this);
        // 停止回调，以防释放的相机再被使用导致异常
        mCamera.setPreviewCallback(null);
        // 停止预览
        mCamera.stopPreview();
        mCamera.lock();
        // 释放相机资源
        CameraUtil.getInstance().releaseCamera();
        mCamera = null;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

    }

    /**
     * 给外部调用，用来拍照的方法
     */
    public void takePhoto() {
        // 因为设置了聚焦，这里又设置了回调对象，所以重新开始预览之后
        // 需要一个标志判断是否是拍照的聚焦回调
        canTake = true;
        // 首先聚焦
        mCamera.autoFocus(this);
//        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onAutoFocus(boolean success, final Camera camera) {
        Log.i(TAG, "聚焦： " + canTake);
        // 不管聚焦成功与否，都开始拍照
        if (canTake) {
            camera.takePicture(null, null, CameraPreview.this);
        }
        canTake = false;
        // 延时一秒，重新开始预览
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.startPreview();
            }
        }, 1000);
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        Log.i(TAG, "onPictureTaken");
        // 在子线程中进行io操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveToSd(data);
            }
        }).start();
    }


    /**
     * 将照片保存至sd卡
     */
    private void saveToSd(byte[] data) {
        // 创建位图，这一步在图片比较大的时候可能会抛oom异常，所以跳过这一步，直接将byte[]
        // 数据写入文件，而且如果有进行图片处理的需求，尽量不要另外再申请内存，不然很容易
        // oom。所以尽量避免在这里处理图片
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        // 系统时间
        long dateTaken = System.currentTimeMillis();
        // 图像名称
        String fileName = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString() + ".jpg";

        FileOutputStream fos = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String filePath = Environment.getExternalStorageDirectory() + File.separator +
                    DIRNAME + File.separator + fileName;
            Log.i(TAG, "文件路径：" + filePath);
            File imgFile = new File(filePath);
            if (!imgFile.getParentFile().exists()) {
                imgFile.getParentFile().mkdirs();
            }
            try {
                if (!imgFile.exists()) {
                    imgFile.createNewFile();
                }

                fos = new FileOutputStream(imgFile);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                fos.write(data);
                fos.flush();
                insertIntoMediaPic();
            } catch (Exception e) {

            } finally {
                try {
                    if (fos != null) {
                        fos.close();//关闭
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            // sd卡状态异常，直接插入系统相册
            // 暂时是空实现
            insertIntoMediaPic();
        }

    }

    private void insertIntoMediaPic() {

    }
}
