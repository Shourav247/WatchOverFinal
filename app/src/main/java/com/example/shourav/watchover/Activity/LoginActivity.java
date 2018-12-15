package com.example.shourav.watchover.activity;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.shourav.watchover.R;
import com.example.shourav.watchover.utils.InputHelper;
import com.example.shourav.watchover.utils.PreferenceGetter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static android.support.v4.app.AppOpsManagerCompat.MODE_ALLOWED;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_password)
    TextInputEditText password;
    @BindView(R.id.login_error)
    TextView error;
    @BindView(R.id.login_button)
    MaterialButton loginButton;
    @BindView(R.id.login_progress)
    MaterialProgressBar mProgress;
    private static final String TAG = "LoginActivity";
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginButton.setOnClickListener(v -> {
            if (validate()) {
                String pass = password.getText().toString();
                PreferenceGetter.authenticateUser(pass)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                addDisposable(d);
                                mProgress.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onSuccess(Boolean isAuthenticated) {
                                mProgress.setVisibility(View.INVISIBLE);
                                if (isAuthenticated) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    error.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                mProgress.setVisibility(View.INVISIBLE);
                                Log.e(TAG, "onError: ", e);
                                error.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });
        if (!checkForPermission(this)) {
            MaterialDialog dialog =  new MaterialDialog.Builder(this)
                    .title("Usage access")
                    .content("Usage access needed. Please open settings and grant usage access.")
                    .positiveText("Open settings")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                            dialog.dismiss();
                        }
                    }).build();
            dialog.show();
        }

    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }
    private boolean validate() {
        if (InputHelper.isEmpty(password)) {
            password.setError("Enter your password");
            return false;
        }
        return true;
    }

    private void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }
}
