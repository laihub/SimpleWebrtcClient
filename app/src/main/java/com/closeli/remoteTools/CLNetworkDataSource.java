package com.closeli.remoteTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piaovalentin on 2017/4/6.
 */

public class CLNetworkDataSource {


    private List<CLNetworkData> mDataSource;

    public CLNetworkDataSource() {
        mDataSource = new ArrayList<>();
    }

    public void setDataSource(List<CLNetworkData> dataSource) {
        mDataSource = dataSource;
    }

    public CLNetworkData itemAtPosition(int position) {

        int count = mDataSource.size();
        if (position < count) {
            return mDataSource.get(position);
        }


        //测试代码
        CLNetworkData data = new CLNetworkData();
        data.title = (0 == position) ? "双向渲染摄像头数据" : "渲染远程数据";
        data.peerId = (0 == position) ? -1 : -2;
        return data;
    }

    public int countOfItems() {

        int count = mDataSource.size();

        //测试代码
        if (0 == count) {
            return 2;
        }


        return count;

    }
}
