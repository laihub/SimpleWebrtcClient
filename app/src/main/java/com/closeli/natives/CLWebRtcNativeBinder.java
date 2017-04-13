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



    /**
     * 初始化服务，连接到服务器
     * @param context   上下文
     */
    public static native void init(Context context);

    /**
     * 连接指定结点前端
     * @param peerID    端id
     * @return  0 成功 其他失败
     */
    private static native int connect(int peerID);

    /**
     * 断开指定结点前端的连接
     * @param peerID    端id
     */
    private static native void disConnect(int peerID);

    /**
     * 断开服务器连接，释放服务资源
     */
    public static native void stop();

    /**
     * 发送视频数据到当前连接的端id
     * @param data      视频数据 RGBA 格式
     * @param width     视频 宽
     * @param height    视频 高
     */
    public static native void sendVideoData(byte[] data, int width, int height);


    /**
     * 视频数据回调
     * @param peerId    当前连接的端id
     * @param pdata     视频数据 RGBA 格式
     * @param width     视频宽
     * @param height    视频高
     */
    private static void onVideoData(int peerId, byte[] pdata, int width, int height) {
        if (null != mCallback)
            mCallback.onVideoData(peerId, pdata, width, height);
    }

    /**
     * 当接收到其他端的连接请求
     * @param peerId    端id
     * @param name      端名称
     */
    private static void onInvite(int peerId, String name) {

        roomPeerId = peerId;
        if (null != mHallCallback)
             mHallCallback.onInvite(peerId, name);
    }

    /**
     * 当接收到服务在线的用户列表
     * @param userList  用户列表
     */
    private static void onUserList(byte[] userList) {

        //testing code
        if (TRICK != roomPeerId) {
            onRemoteDisConnect(roomPeerId);
        }

        if (null != mHallCallback)
             mHallCallback.onUserList(userList);
    }

    /**
     * 当远程端断开连接
     * @param peerId    端id
     */
    private static void onRemoteDisConnect(int peerId) {
        if (null != mCallback) {
            mCallback.onDisconnect(peerId, 0);
        }
    }



    ////////////////////////////////////
    ///////////Testing code///////////////
    ////////////////////////////////////


    private static final int TRICK = -888;
    private static int roomPeerId = TRICK;
    public static int connectToPeer(int peerID) {

        //被邀请的情况
        if (TRICK != roomPeerId) {
            return 0;
        }


        int code = connect(peerID);
        roomPeerId = (0 == code) ? peerID : TRICK;
        return code;
    }


    public static void disConnectToPeer(int peerID) {
        disConnect(peerID);
        roomPeerId = TRICK;
    }




    public interface onRoomCallback {
        void onVideoData(int peerId, byte[] pdata, int width, int height);
        void onDisconnect(int peerId, int code);
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
}
