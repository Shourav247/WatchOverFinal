package com.example.shourav.watchover.Adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.shourav.watchover.R;
import com.peak.salut.SalutDevice;

import java.util.List;

public class ReceiverDeviceAdapter extends BaseQuickAdapter<SalutDevice, BaseViewHolder> {
    public ReceiverDeviceAdapter(@Nullable List<SalutDevice> data) {
        super(R.layout.item_device, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SalutDevice item) {
        helper.setText(R.id.item_device_name, item.deviceName);
    }
}
