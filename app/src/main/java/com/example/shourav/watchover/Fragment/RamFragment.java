package com.example.shourav.watchover.fragment;


import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.shourav.watchover.Adapter.AppAdapter;
import com.example.shourav.watchover.Adapter.MemoryAdapter;
import com.example.shourav.watchover.Adapter.RamAdapter;
import com.example.shourav.watchover.Pojo.Memory;
import com.example.shourav.watchover.Pojo.Ram;
import com.example.shourav.watchover.R;
import com.example.shourav.watchover.WatchOver;
import com.github.mikephil.charting.charts.PieChart;
import com.ram.speed.booster.RAMBooster;
import com.ram.speed.booster.interfaces.CleanListener;
import com.ram.speed.booster.interfaces.ScanListener;
import com.ram.speed.booster.utils.ProcessInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.USAGE_STATS_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class RamFragment extends Fragment {

    private static String TAG = "booster.test";
    RAMBooster booster;

    public TextView tvRam,tvUsedRam,tvAvailRam;
    public PieChart pieChart;
//    public Button btnClear;
    public ListView ram;
    public List<String> process;
    public RecyclerView recyclerView;


    public float tRam,aRam,usedRam;

    public Memory memory;
    public  List<Ram> listProcess = new ArrayList<Ram>();


    public RamFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ram, container, false);

//        btnClear = (Button) v.findViewById(R.id.btnClear);

        recyclerView = (RecyclerView) v.findViewById(R.id.processList);
        ram= (ListView) v.findViewById(R.id.ram);
        AppAdapter appAdapter = new AppAdapter(getProcessName(), getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(appAdapter);
        appAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                UsageStats usageStats = (UsageStats) adapter.getData().get(position);
                killAppBypackage(usageStats.getPackageName());
                adapter.replaceData(getProcessName());
            }
        });


        if (booster==null)
            booster=null;
        booster = new RAMBooster(getContext());
        booster.setDebug(true);
        booster.setScanListener(new ScanListener() {
            @Override
            public void onStarted() {
                Log.d(TAG, "Scan started");
            }

            @Override
            public void onFinished(long availableRam, long totalRam, List<ProcessInfo> appsToClean) {

                aRam = availableRam;
                tRam = totalRam;
                usedRam = totalRam-availableRam;



                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<Memory> memoryDataSource = new ArrayList<Memory>();

                        Memory ramInfo = new Memory("Ram Information",String.valueOf(usedRam),String.valueOf(aRam),String.valueOf(tRam));
                        memoryDataSource.add(ramInfo);
                        MemoryAdapter memoryAdapter = new MemoryAdapter(getContext(), memoryDataSource);
                        ram.setAdapter(memoryAdapter);

                    }
                });



                Log.e(TAG,"Ram : "+aRam);

                Log.d(TAG, String.format(Locale.US,
                        "Scan finished, available RAM: %dMB, total RAM: %dMB",
                        availableRam,totalRam));

                for (ProcessInfo info:appsToClean) {

                    Context context = getContext();
                    String packageName = info.getProcessName();
                    PackageManager packageManager = context.getPackageManager();
                    ApplicationInfo applicationInfo = null;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                    } catch (final PackageManager.NameNotFoundException e) {
                    }
                    final String title = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                    Drawable appIcon = applicationInfo.loadIcon(packageManager);

                    listProcess.add(new Ram(appIcon,title));
                }

//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        RamAdapter recyclerViewAdapter = new RamAdapter(getContext(),(List<Ram>) listProcess);
//
//                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                        recyclerView.setLayoutManager(layoutManager);
//                        recyclerView.setAdapter(recyclerViewAdapter);
//                    }
//                });
                booster.startClean();




            }
        });

//        btnClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                try{
//                    booster.setCleanListener(new CleanListener() {
//                        @Override
//                        public void onStarted() {
//                            Log.d(TAG, "Clean started");
//                        }
//
//
//                        @Override
//                        public void onFinished(long availableRam, long totalRam) {
//
//                            aRam = availableRam;
//                            tRam = totalRam;
//                            usedRam = totalRam-availableRam;
//
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    List<Memory> memoryDataSource = new ArrayList<Memory>();
//
//                                    Memory ramInfo = new Memory("Ram Information",String.valueOf(usedRam),String.valueOf(aRam),String.valueOf(tRam));
//                                    memoryDataSource.add(ramInfo);
//                                    MemoryAdapter memoryAdapter = new MemoryAdapter(getContext(), memoryDataSource);
//                                    ram.setAdapter(memoryAdapter);
//                                    recyclerView.setAdapter(null);
////call the invalidate()
//
//                                }
//                            });
//
//
//                            Log.d(TAG, String.format(Locale.US,
//                                    "Clean finished, available RAM: %dMB, total RAM: %dMB",
//                                    availableRam,totalRam));
//                            booster = null;
//
//                        }
//
//                    });
//                    booster.startScan(true);
//
//                }catch (Exception e)
//                {
//                    System.out.println("Error " + e.getMessage());
//                }
//            }
//        });


        booster.startScan(true);
        Log.e(TAG, "onCreateView: "+getProcessName() );

        return v;
    }
    private List<UsageStats> getProcessName() {
        String foregroundProcess = "";
        ActivityManager activityManager = (ActivityManager) WatchOver.getInstance().getSystemService(ACTIVITY_SERVICE);
        // Process running
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager)WatchOver.getInstance().getSystemService(USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            // We get usage stats for the last 10 seconds
//            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*10, time);
            return mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*10, time);
            // Sort the stats by the last time used
//            if(stats != null) {
//                SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
//                for (UsageStats usageStats : stats) {
//                    mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
//                    Log.d("RunningAppProcessInfo","Package name : "+usageStats.getPackageName());
//                }
//
//            }
        }
        return new ArrayList<>();
    }

    private void killAppBypackage(String packageTokill){

        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getActivity().getPackageManager();
        //get a list of installed apps.
        packages = pm.getInstalledApplications(0);


        ActivityManager mActivityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        String myPackage = getActivity().getPackageName();

        for (ApplicationInfo packageInfo : packages) {

            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1) {
                if (packageInfo.packageName.equals(packageTokill)) {
                    Toast.makeText(getActivity(), "Couldn't kill system app", Toast.LENGTH_SHORT).show();
                }
                continue;
            }
            if(packageInfo.packageName.equals(myPackage)) {
                if (packageInfo.packageName.equals(packageTokill)) {
                    Toast.makeText(getActivity(), "Couldn't kill current app", Toast.LENGTH_SHORT).show();
                }
                continue;
            }
            if(packageInfo.packageName.equals(packageTokill)) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
                Toast.makeText(getActivity(), "App killed, results will update after one minute", Toast.LENGTH_SHORT).show();
            }

        }

    }

}
