package com.example.shourav.watchover.Adapter;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.shourav.watchover.R;

import java.util.List;

public class AppAdapter extends BaseQuickAdapter<UsageStats, BaseViewHolder> {
    private Context mContext;
    public AppAdapter(@Nullable List<UsageStats> data, Context context) {
        super(R.layout.item_app, data);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, UsageStats item) {
        helper.setText(R.id.item_app_name, getApplicationLabel(mContext, item.getPackageName()));
        helper.setImageDrawable(R.id.item_app_icon, getAppIcon(mContext, item.getPackageName()));
//        helper.addOnClickListener(R.id.item_app_kill);
    }

    private static String getApplicationLabel(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return  (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    private static Drawable getAppIcon(Context context, String packageName) {
        try
        {
            return context.getPackageManager().getApplicationIcon(packageName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }


}
