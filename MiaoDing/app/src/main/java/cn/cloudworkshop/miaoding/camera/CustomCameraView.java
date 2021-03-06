package cn.cloudworkshop.miaoding.camera;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.cloudworkshop.miaoding.R;


/**
 * Author：binge on 2017/3/10 10:55
 * Email：1993911441@qq.com
 * Describe：相机
 */

public class CustomCameraView extends FrameLayout implements SurfaceHolder.Callback, AutoFocusCallback {

    private View viewFocus;
    private PreviewFrameLayout frameLayout;

    private Context context = null;
    public static Camera camera = null;
    private SurfaceHolder surfaceHolder;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private OnTakeFinish onTakeFinish;

    public void setOnTakeFinish(OnTakeFinish onTakeFinish) {
        this.onTakeFinish = onTakeFinish;
    }

    public CustomCameraView(Context context) {
        super(context);
    }

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.preview_frame, this);
        frameLayout = (PreviewFrameLayout) findViewById(R.id.frame_layout);
        SurfaceView surfaceCamera = (SurfaceView) findViewById(R.id.camera_preview);
        viewFocus = findViewById(R.id.view_focus);
        surfaceHolder = surfaceCamera.getHolder();
        surfaceHolder.addCallback(this);
        frameLayout.setOnTouchListener(onTouchListener);

        if (checkCameraHardware()) {
            camera = getCameraInstance();
        }
    }

    /**
     * 获取最大分辨率
     *
     * @return
     */
    public Integer getMaxZoom() {

        if (camera != null) {
            Camera.Parameters p = camera.getParameters();

            return p.getMaxZoom();
        }

        return null;
    }

    /**
     * 变焦
     *
     * @return
     */
    public void setZoom(Integer value) {

        if (camera != null) {
            Camera.Parameters p = camera.getParameters();
            p.setZoom(value);
            camera.setParameters(p);
        }
    }

    /**
     * 设置分辨率
     *
     * @return
     */
    public void setFocus() {

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);

        camera.cancelAutoFocus();// 如果要实现连续的自动对焦，这一句必须加上
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            camera = getCameraInstance();
            camera.setPreviewDisplay(surfaceHolder);
            updateCameraParameters();
            camera.startPreview();
        } catch (IOException e) {
            System.out.println(e);
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (camera != null) {
            camera.autoFocus(new AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean arg0, Camera arg1) {
                    // TODO Auto-generated method stub
                    if (arg0) {
                        updateCameraParameters();// 实现相机的参数初始化
                    }
                }
            });
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null && holder != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {

        viewWidth = MeasureSpec.getSize(widthSpec);
        viewHeight = MeasureSpec.getSize(heightSpec);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    private boolean checkCameraHardware() {
        return context != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras(); // get cameras number

            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                    try {
                        c = Camera.open(camIdx);
                    } catch (RuntimeException ignored) {
                    }
                }
            }
            if (c == null) {
                c = Camera.open(0); // attempt to get a Camera instance
            }
        } catch (Exception e) {
            // Toast.makeText(context, "摄像头打开失败！", Toast.LENGTH_SHORT);
        }
        return c;
    }

    @SuppressLint("InlinedApi")
    private void updateCameraParameters() {
        if (camera != null) {
            Camera.Parameters p = camera.getParameters();

            List<String> focusModes = p.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                Size previewSize = findBestPreviewSize(p);
                if (previewSize != null) {
                    p.setPreviewSize(previewSize.width, previewSize.height);
                }

                List<Size> supportedPictureSizes = p.getSupportedPictureSizes();
                Size bestPictureSize = findBestPictureSize(supportedPictureSizes, 2632, 1228);
                p.setPictureSize(bestPictureSize.width, bestPictureSize.height);
                p.setPictureFormat(PixelFormat.JPEG);//设置图片格式
                p.setJpegQuality(100); // 设置照片质量

                // Set the preview frame aspect ratio according to the picture size.
                if (previewSize != null) {
                    frameLayout.setAspectRatio((double) previewSize.width
                            / previewSize.height);
                }

                if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    camera.setDisplayOrientation(90);
                }
                if (p.isZoomSupported()) {
                    p.setZoom(10);
                }

                camera.setParameters(p);
                camera.startSmoothZoom(10);
                camera.cancelAutoFocus();// 如果要实现连续的自动对焦，这一句必须加上

            }
        }
    }


    /**
     * @param
     * @return图片大小
     */
    private Size findBestPictureSize(List<Size> pictureSizes, int minWidth, int minHeight) {
        Size size = pictureSizes.get(0);
        for (int i = pictureSizes.size() - 1; i > 0; i--) {
            if (pictureSizes.get(i).width > minWidth && pictureSizes.get(i).height > minHeight) {
                size.width = pictureSizes.get(i).width;
                size.height = pictureSizes.get(i).height;
                break;
            }
        }
        return size;
    }

    /**
     * 找到最合适的显示分辨率 （防止预览图像变形）
     *
     * @param parameters
     * @return
     */
    private Size findBestPreviewSize(Camera.Parameters parameters) {

        // 系统支持的所有预览分辨率
        String previewSizeValueString = parameters.get("preview-size-values");

        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        if (previewSizeValueString == null) { // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
            return camera.new Size(getScreenWH().widthPixels,
                    getScreenWH().heightPixels);
        }
        float bestX = 0;
        float bestY = 0;

        float tmpRadio = 0;
        float viewRadio = 0;

        if (viewWidth != 0 && viewHeight != 0) {
            viewRadio = Math.min((float) viewWidth, (float) viewHeight)
                    / Math.max((float) viewWidth, (float) viewHeight);
        }


        String[] COMMA_PATTERN = previewSizeValueString.split(",");
        for (String prewsizeString : COMMA_PATTERN) {
            prewsizeString = prewsizeString.trim();

            int dimPosition = prewsizeString.indexOf('x');
            if (dimPosition == -1) {
                continue;
            }

            float newX = 0;
            float newY = 0;

            try {
                newX = Float.parseFloat(prewsizeString
                        .substring(0, dimPosition));
                newY = Float.parseFloat(prewsizeString
                        .substring(dimPosition + 1));
            } catch (NumberFormatException e) {
                continue;
            }

            float radio = Math.min(newX, newY) / Math.max(newX, newY);
            if (tmpRadio == 0) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            } else if (Math.abs(radio - viewRadio) < Math.abs(tmpRadio - viewRadio)) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            }
        }

        if (bestX > 0 && bestY > 0) {
            return camera.new Size((int) bestX, (int) bestY);
        }
        return null;
    }

    /**
     * 点击显示焦点区域
     */
    OnTouchListener onTouchListener = new OnTouchListener() {

        @SuppressWarnings("deprecation")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                int width = viewFocus.getWidth();
                int height = viewFocus.getHeight();
                viewFocus.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.ic_focus_focusing));
                viewFocus.setX(event.getX() - (width / 2));
                viewFocus.setY(event.getY() - (height / 2));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                focusOnTouch(event);
            }

            return true;
        }
    };

    /**
     * 设置焦点和测光区域
     *
     * @param event
     */
    public void focusOnTouch(MotionEvent event) {

        int[] location = new int[2];
        frameLayout.getLocationOnScreen(location);

        Rect focusRect = calculateTapArea(viewFocus.getWidth(),
                viewFocus.getHeight(), 1f, event.getRawX(), event.getRawY(),
                location[0], location[0] + frameLayout.getWidth(), location[1],
                location[1] + frameLayout.getHeight());
        Rect meteringRect = calculateTapArea(viewFocus.getWidth(),
                viewFocus.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
                location[0], location[0] + frameLayout.getWidth(), location[1],
                location[1] + frameLayout.getHeight());

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        // System.out.println("CustomCameraView getMaxNumFocusAreas = " +
        // parameters.getMaxNumFocusAreas());
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(focusRect, 1000));

            parameters.setFocusAreas(focusAreas);
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));

            parameters.setMeteringAreas(meteringAreas);
        }

        try {
            camera.setParameters(parameters);
        } catch (Exception ignored) {
        }
        camera.autoFocus(this);
    }

    /**
     * 计算焦点及测光区域
     *
     * @param focusWidth
     * @param focusHeight
     * @param areaMultiple
     * @param x
     * @param y
     * @param previewleft
     * @param previewRight
     * @param previewTop
     * @param previewBottom
     * @return Rect(left, top, right, bottom) : left、top、right、bottom是以显示区域中心为原点的坐标
     */
    public Rect calculateTapArea(int focusWidth, int focusHeight,
                                 float areaMultiple, float x, float y, int previewleft,
                                 int previewRight, int previewTop, int previewBottom) {
        int areaWidth = (int) (focusWidth * areaMultiple);
        int areaHeight = (int) (focusHeight * areaMultiple);
        int centerX = (previewleft + previewRight) / 2;
        int centerY = (previewTop + previewBottom) / 2;
        double unitx = ((double) previewRight - (double) previewleft) / 2000;
        double unity = ((double) previewBottom - (double) previewTop) / 2000;
        int left = clamp((int) (((x - areaWidth / 2) - centerX) / unitx),
                -1000, 1000);
        int top = clamp((int) (((y - areaHeight / 2) - centerY) / unity),
                -1000, 1000);
        int right = clamp((int) (left + areaWidth / unitx), -1000, 1000);
        int bottom = clamp((int) (top + areaHeight / unity), -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    public int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    protected DisplayMetrics getScreenWH() {
        return this.getResources().getDisplayMetrics();
    }


    /**
     * 拍照
     */
    public void takePicture(final String path) {

        if (camera != null) {
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, null, new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    if (camera != null) {
                        camera.startPreview();// 开启预览
                    }

                    BufferedOutputStream bos = null;
                    Bitmap bm = null;

                    try {
                        // 获得图片
                        bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);

                        // 创建新的图片
                        Bitmap newBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                                bm.getHeight(), matrix, true);
                        File file = new File(path);
                        bos = new BufferedOutputStream(new FileOutputStream(file));
                        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
                        onTakeFinish.takeFinish(newBitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (bos != null) {
                                bos.flush();//输出
                                bos.close();//关闭
                                bm.recycle();// 回收bitmap空间
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera _camera) {
        if (success) {
            viewFocus.setBackgroundResource(R.drawable.ic_focus_focused);
        } else {
            viewFocus.setBackgroundResource(R.drawable.ic_focus_failed);
        }

        setFocusView();
    }

    private void setFocusView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewFocus.setBackgroundResource(0);
            }
        }, 1000);
    }


    /**
     * 拍照结束
     */
    public interface OnTakeFinish {
        void takeFinish(Bitmap bitmap);
    }
}

