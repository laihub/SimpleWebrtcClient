package com.closeli.demo.clbidirectionrenderdemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.closeli.library.camera.tools.CLLoger;
import com.closeli.natives.CLWebRtcNativeBinder;
import com.closeli.remoteTools.CLNetworkData;
import com.closeli.remoteTools.CLNetworkDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


public class MainActivity extends CLDIParentAcvitity implements CLWebRtcNativeBinder.onHallCallback{

    private final String TAG = "MainActivity";

    @BindView(R.id.recycles)
    RecyclerView mRecycleView;


    @BindView(R.id.nameField)
    EditText etName;
    @BindView(R.id.loginBtn)
    Button btnLogin;
    @BindView(R.id.loginLayout)
    LinearLayout mLoginLayout;

    private UsersAdapter mAdapter;
    private CLNetworkDataSource mDataSource;
    private CLNetworkData mInvitation;

    @Override
    protected int contenViewLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onInit(@Nullable Bundle savedInstanceState) {

        CLLoger.LOG_ENABLE = false;
        needRequestPermission();

        CLWebRtcNativeBinder.setHallCallback(this);
        CLWebRtcNativeBinder.init(getApplicationContext());
        mDataSource = new CLNetworkDataSource();

        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UsersAdapter(this, R.layout.cell_main);
        mRecycleView.setAdapter(mAdapter);


        mAdapter.setDelegate(new UsersAdapter.delegateCallback() {
            @Override
            public void onClick(int position) {
                startVideo(position);
            }

            @Override
            public String onDisplayView(int position) {
                CLNetworkData data = mDataSource.itemAtPosition(position);
                return data.title + " : " + data.peerId;
            }

            @Override
            public int itemCount() {
                return mDataSource.countOfItems();
            }
        });

        etName.setText("VIP:" + System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (View.GONE !=  mLoginLayout.getVisibility()) {
            etName.setSelected(true);
            etName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CLWebRtcNativeBinder.stop();
        CLWebRtcNativeBinder.setHallCallback(null);
    }

    public void startVideo(int position) {
        CLNetworkData data = mDataSource.itemAtPosition(position);
        startVideo(data);
    }

    private void startVideo(CLNetworkData data) {

        CLLoger.trace(TAG, "startVideo with: " + data.title + " peerId: " + data.peerId);

        if (needRequestPermission()) {
            mInvitation = null;
            return;
        }

        Intent intent;
        //测试代码
        if (-1 == data.peerId) {
            intent = new Intent(this, CLBIVideoDemoActivity.class);
        }
        else {
            intent = new Intent(this, CLBIVideoDemoRemoteActivity.class);
            intent.putExtra(CLNetworkData.PASS_DATA_KEY, data);
        }
        startActivityForResult(intent, 888);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mInvitation = null;
    }

    @Override
    public void onInvite(final int peerId, final String name) {

        if (null != mInvitation) {
            return;
        }

        mInvitation = new CLNetworkData();
        mInvitation.title = name;
        mInvitation.peerId = peerId;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setTitle("接收到 " + name +" : " + peerId + " 的邀请");
                dialogBuilder.setMessage("是否行视频通话?");
                dialogBuilder.setCancelable(false);
                dialogBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startVideo(mInvitation);
                    }
                });

                dialogBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mInvitation = null;
                    }
                });
                dialogBuilder.create().show();
            }
        });

    }

    @Override
    public void onUserList(byte[] userList) {

        String utf8 = new String(userList);

        try {
            JSONObject jo = new JSONObject(utf8);
            JSONArray array = jo.getJSONArray("data");

            if (null != array) {
                int count = array.length();
                final ArrayList<CLNetworkData> dataArray = new ArrayList<>();

                for (int i=0; i < count; i++) {
                    CLNetworkData unit = new CLNetworkData();
                    JSONObject jsonUnit = array.optJSONObject(i);
                    unit.title = jsonUnit.optString("title");
                    unit.peerId = jsonUnit.optInt("peerId");
                    dataArray.add(unit);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataSource.setDataSource(dataArray);
                        mAdapter.updateRecycleView();
                    }
                });
            }

        }catch (JSONException exp) {
            exp.printStackTrace();
            CLLoger.trace(TAG, exp.toString());
        }

    }

    @OnClick(R.id.loginBtn)
    public void login() {
        String name = etName.getText().toString();
        if (null != name && name.length() > 0) {
            CLWebRtcNativeBinder.startLogin("101.37.17.13", 8888,name);
            mLoginLayout.setVisibility(View.GONE);
        }
        else {
            Toast.makeText(this, "无效名称", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean needRequestPermission() {
        //Camera 权限提前判断！！！
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            int resultAudio = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int resultCamera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (resultAudio != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (resultCamera != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions.size() > 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), 1);
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (View.GONE ==  mLoginLayout.getVisibility()) {
            mAdapter.notifySelectedIndexForDirection(keyCode);
        }

        return super.onKeyDown(keyCode, event);
    }
}
