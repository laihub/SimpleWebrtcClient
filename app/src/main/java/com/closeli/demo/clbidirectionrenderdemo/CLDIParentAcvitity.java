package com.closeli.demo.clbidirectionrenderdemo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.closeli.library.camera.tools.CLLoger;

import butterknife.ButterKnife;

/**
 * Created by piaovalentin on 2017/4/6.
 */

abstract class CLDIParentAcvitity extends AppCompatActivity {

    private final String TAG = "CLDIParentAcvitity";
    abstract protected int contenViewLayout();

    abstract protected void onInit(@Nullable Bundle savedInstanceState);



    protected void onPrepareCreate(@Nullable Bundle savedInstanceState) {
        //TO Override
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CLLoger.trace(TAG, "onCreate");

        onPrepareCreate(savedInstanceState);

        int layoutId = contenViewLayout();
        if (0 != layoutId) {
            setContentView(layoutId);
            ButterKnife.bind(this);
        }

        onInit(savedInstanceState);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
