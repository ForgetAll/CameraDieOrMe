package com.xiasuhuei321.cameradieorme.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by xiasuhuei321 on 2017/8/21.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 */

public class CameraUtil {
    public static final String TAG = "CameraUtil";

    private Camera camera;
    private int cameraId;

    private int mScreenWidth;
    private int mScreenHeight;


    //    private Callback callback;
    private boolean release = false;
    private Camera.Parameters params;

    private CameraUtil() {
    }


    private static class CameraUtilHolder {
        private static CameraUtil instance = new CameraUtil();
    }

    public static CameraUtil getInstance() {
        return CameraUtilHolder.instance;
    }

    public void init(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);
        mScreenWidth = p.x;
        mScreenHeight = p.y;
    }

    /**
     * 检查是否拥有相机
     *
     * @return 如果有返回true，没有返回false
     */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // 有相机
            return true;
        } else {
            // 没有相机
            return false;
        }
    }

    /**
     * 获取前置相机实例，注意6.0以上的系统需要动态申请权限（如果
     * target >= 23）则必须动态申请，否则无法打开相机
     *
     * @return 打开成功则返回相机实例，失败则返回null
     */
    public Camera getCameraInstance() {
        if (camera != null) {
            Log.i(TAG, "camera已经打开过，返回前一个值");
            return camera;
        }
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } catch (Exception e) {
            e.printStackTrace();
            // 相机正在使用或者不存在
            Log.i(TAG, "相机打开失败，正在使用或者不存在，或者，没有权限？");
            return null;
        }
        initParam();
        release = false;
        return camera;
    }

    public void initParam() {
        if (camera == null) {
            return;
        }
        if (params != null) {
            camera.setParameters(params);
        } else {
            camera.setParameters(generateDefaultParams(camera));
        }
    }

    /**
     * 允许从外部设置相机参数
     *
     * @param params 相机参数
     */
    public void setParams(Camera.Parameters params) {
        this.params = params;
    }

    /**
     * 生成默认的相机参数
     *
     * @param camera 使用该参数的相机
     * @return 生成的参数
     */
    public Camera.Parameters generateDefaultParams(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        // 设置聚焦
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }
        camera.cancelAutoFocus();//自动对焦。
        // 设置图片格式
        parameters.setPictureFormat(PixelFormat.JPEG);
        // 设置照片质量
        parameters.setJpegQuality(100);
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            // 默认打开前置摄像头，旋转90度即可
            camera.setDisplayOrientation(90);
            parameters.setRotation(90);
        } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // 打开后置摄像头，旋转270，这个待验证
            camera.setDisplayOrientation(270);
            parameters.setRotation(180);
        }

        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> picSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : picSizeList) {
            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size picSize = getProperSize(picSizeList, ((float) mScreenHeight / mScreenWidth));
        parameters.setPictureSize(picSize.width, picSize.height);

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : previewSizeList) {
            Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) mScreenHeight) / mScreenWidth);
        Log.i(TAG, "final size is: " + picSize.width + " " + picSize.height);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        return parameters;
    }

    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (camera != null) {
            camera.release();
        }
        camera = null;
        release = true;
    }

    /**
     * 现在是否处于释放状态
     *
     * @return true释放，false没释放
     */
    public boolean isRelease() {
        return release;
    }

}
