package com.closeli.natives;

import android.content.Context;

/**
 * Created by piaovalentin on 2017/4/5.
 */

public class CLWebRtcNativeBinder {


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public interface onRoomCallback {
        void onVideoData(int peerId, byte[] pdata, int width, int height);
    }

    public interface onHallCallback {
        void onInvite(int peerId, String name);
        void onUserList(byte[] userList);
    }


    //temp tools
    private static onRoomCallback mCallback;
    public static void setCallback(onRoomCallback callback) {
        mCallback = callback;
    }

    private static onHallCallback mHallCallback;
    public static void setHallCallback(onHallCallback hallCallback) {
        mHallCallback = hallCallback;
    }


    public static native void init(Context context);
    public static native int connect(int peerID);
    public static native void disConnect(int peerID);


    public static native void sendVideoData(byte[] data, int width, int height);
    public static native void stop();

    public static void onVideoData(int peerId, byte[] pdata, int width, int height) {
        if (null != mCallback)
            mCallback.onVideoData(peerId, pdata, width, height);
    }

    public static void onInvite(int peerId, String name) {
        if (null != mHallCallback)
             mHallCallback.onInvite(peerId, name);
    }

    public static void onUserList(byte[] userList) {
        if (null != mHallCallback)
             mHallCallback.onUserList(userList);
    }

}
