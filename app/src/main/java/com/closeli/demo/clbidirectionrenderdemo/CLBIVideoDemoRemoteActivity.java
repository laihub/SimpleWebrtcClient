package com.closeli.demo.clbidirectionrenderdemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.closeli.library.camera.tools.PixelBuffer;
import com.closeli.natives.CLWebRtcNativeBinder;
import com.closeli.remoteTools.CLNetworkData;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Created by piaovalentin on 2017/4/5.
 */

public class CLBIVideoDemoRemoteActivity extends CLBIVideoDemoActivity implements CLWebRtcNativeBinder.onRoomCallback{

    private CLNetworkData mUser;

    @Override
    protected void onInit(@Nullable Bundle savedInstanceState) {
        super.onInit(savedInstanceState);

        mUser = getIntent().getExtras().getParcelable(CLNetworkData.PASS_DATA_KEY);
        connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CLWebRtcNativeBinder.disConnectToPeer(mUser.peerId);
        CLWebRtcNativeBinder.setCallback(null);
    }

    @Override
    protected void onReadData(byte[] data, int width, int height) {
        final long captureTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
        CLWebRtcNativeBinder.sendVideoData(data, width, height, captureTimeNs, 13);
    }

    @Override
    public void onVideoData(int peerId, byte[] pdata, int width, int height) {
        mRemoteRender.fillData(pdata, false, width, height);
    }

    @Override
    public void onDisconnect(int peerId, int code) {
        Toast.makeText(this, "连接结束！", Toast.LENGTH_SHORT).show();
        finish();
    }


    private void connect() {
        int ret = CLWebRtcNativeBinder.connectToPeer(mUser.peerId);
        CLWebRtcNativeBinder.setCallback(this);
        if (ret < 0)
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("连接到 " + mUser.title +" : " + mUser.peerId + " 失败！");
            dialogBuilder.setMessage("尝试重连?");
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    connect();
                }
            });
            dialogBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            dialogBuilder.create().show();
        }
    }
}
