package com.closeli.demo.clbidirectionrenderdemo;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.closeli.library.camera.textureRender.CLCameraRender;
import com.closeli.library.camera.textureRender.CLSimpleRender;
import com.closeli.library.camera.textureRender.CLTextureViewGLRenderCallback;
import com.closeli.library.camera.tools.CLCameraManager;
import com.closeli.library.camera.tools.CLLoger;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by piaovalentin on 2017/3/31.
 */

public class CLBIVideoDemoActivity extends CLDIParentAcvitity {

    private final String TAG = getClass().getSimpleName();


    @BindView(R.id.localView)
    TextureView mLocalView;

    @BindView(R.id.remoteView)
    TextureView mRemoteView;

    @BindView(R.id.mainLayout)
    RelativeLayout mMainLayout;

    @BindView(R.id.funcLayout)
    RelativeLayout mFuncLayout;

    @BindView(R.id.btn_mic)
    ImageButton btnMic;
    @BindView(R.id.btn_sound)
    ImageButton btnSound;
    @BindView(R.id.btn_switch)
    ImageButton btnSwitch;
    @BindView(R.id.btn_close)
    ImageButton btnClose;


    CLCameraRender mCameraRender;
    CLSimpleRender mRemoteRender;

    private byte[] previewBuffer;


    private boolean isMicOff = false;
    private boolean isSoundOff = false;
    private boolean isFlowOnTop = true;

    @Override
    protected int contenViewLayout() {

        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        return R.layout.activity_bivideodemo;
    }

    @Override
    protected void onInit(@Nullable Bundle savedInstanceState) {
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(true);

        setUpCamera();
        setUpLocalView();
        setUpRemoteView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraRender.onResume();
        mRemoteRender.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        CLCameraManager.sharedCameraManager().closeCamera();
        mCameraRender.onPause();
        mRemoteRender.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraRender.halt();
        mRemoteRender.halt();
    }



    private void setUpCamera() {
        try {
            CLCameraManager.init(this);
            CLCameraManager.sharedCameraManager().setSwapAble(true);
            CLCameraManager.sharedCameraManager().config(null);

        } catch (Exception e) {
            e.printStackTrace();
            CLLoger.trace(TAG, "init camera error!");
        }
    }


    private void setUpLocalView() {

        final Camera.Size previewSize = CLCameraManager.sharedCameraManager().getCameraPreviewSize();
        mCameraRender = new CLCameraRender(this, previewSize, new CLTextureViewGLRenderCallback() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
                try {
                    Camera camera = CLCameraManager.sharedCameraManager().getCameraInstance();
                    int bufferSize = previewSize.width * previewSize.height;
                    int bits = (ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat()));
                    bufferSize = Math.round(bufferSize * 1.f * bits / 8.f);
                    previewBuffer = new byte[bufferSize];
                    camera.addCallbackBuffer(previewBuffer);
                    camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            if (null == camera) {
                                return;
                            }
                            camera.addCallbackBuffer(previewBuffer);
                            onReadData(data, previewSize.width, previewSize.height);
                        }
                    });


                    int format = camera.getParameters().getPreviewFormat();
                    camera.setPreviewTexture(st);
                    int degree = CLCameraManager.sharedCameraManager().adjustCameraDisplayOrientation();
                    CLLoger.trace(TAG, "degree :  " + degree);
                    camera.startPreview();
                }catch (Exception exp) {

                    exp.printStackTrace();
                    CLLoger.trace(TAG, "startPlaying error!");
                }
            }
        });
        mCameraRender.start();
        mLocalView.setSurfaceTextureListener(mCameraRender);
    }

    private void setUpRemoteView() {
        mRemoteRender = new CLSimpleRender(this, new CLTextureViewGLRenderCallback() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {

            }
        });
        mRemoteRender.start();
        mRemoteView.setSurfaceTextureListener(mRemoteRender);
    }


    protected void onReadData(byte[] data, int width, int height) {

//        byte[] rgba = new byte[width * height * 4];
//        CLWebRtcNativeBinder.convertI420ToRGBA(data, width, height,rgba);
//
//        mRemoteRender.fillData(rgba, false, width, height);

    }



    @OnClick(R.id.btn_switch)
    public void switchPage() {

        View flowView, mainView;
        if (isFlowOnTop) {
            flowView = mRemoteView;
            mainView = mLocalView;
        }
        else {
            flowView = mLocalView;
            mainView = mRemoteView;
        }
        isFlowOnTop = false;

        int mainLeft = mainView.getLeft();
        int mainRight = mainView.getRight();
        int mainTop = mainView.getTop();
        int mainBottom = mainView.getBottom();

        int flowLeft = flowView.getLeft();
        int flowRight = flowView.getRight();
        int flowTop = flowView.getTop();
        int flowBottom = flowView.getBottom();

        flowView.layout(mainLeft, mainTop, mainRight, mainBottom);
        mainView.layout(flowLeft, flowTop, flowRight, flowBottom);
    }


    @OnClick(R.id.btn_close)
    public void close() {
        finish();
    }

    @OnClick(R.id.btn_mic)
    public void switchMicrophone(ImageButton button) {

        isMicOff = !isMicOff;
        button.setImageResource(isMicOff ? R.drawable.btn_mic_off : R.drawable.btn_mic_on);

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        am.setMicrophoneMute(isMicOff);
    }

    @OnClick(R.id.btn_sound)
    public void switchSound(ImageButton button) {

        isSoundOff = !isSoundOff;
        button.setImageResource(isSoundOff ? R.drawable.btn_sound_off : R.drawable.btn_sound_on);

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, isSoundOff ? 0 : 100, AudioManager.FLAG_PLAY_SOUND);
    }


//    @BindView(R.id.flowImage)
//    ImageView mIV;

    protected void rgbaImageTest(byte[] bytes, int width, int height) {
        //Test Code

//        final Bitmap rgbaImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        rgbaImage.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
//        CLBIVideoDemoActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mIV.setImageBitmap(rgbaImage);
//            }
//        });
    }
}
