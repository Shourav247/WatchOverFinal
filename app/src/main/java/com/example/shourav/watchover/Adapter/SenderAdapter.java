package com.example.shourav.watchover.Adapter;

import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.shourav.watchover.R;
import com.peak.salut.SalutDevice;

import java.util.List;

public class SenderAdapter extends BaseQuickAdapter<SalutDevice, BaseViewHolder> {
    private boolean isDiscovered;
    public SenderAdapter(@Nullable List<SalutDevice> data, boolean isDiscovered) {
        super(R.layout.item_device, data);
        this.isDiscovered = isDiscovered;
    }

    @Override
    protected void convert(BaseViewHolder helper, SalutDevice item) {
        helper.setText(R.id.item_device_name, item.readableName);
        helper.setVisible(R.id.item_device_connect, true);
        helper.addOnClickListener(R.id.item_device_connect);
        if (!isDiscovered) {
            MaterialButton button = helper.getView(R.id.item_device_connect);
            button.setText("Send");
        }
    }
}
