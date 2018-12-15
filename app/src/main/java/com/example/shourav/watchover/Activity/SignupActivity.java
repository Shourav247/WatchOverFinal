package com.example.shourav.watchover.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.shourav.watchover.R;
import com.example.shourav.watchover.utils.InputHelper;
import com.example.shourav.watchover.utils.PreferenceGetter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.signup_password)
    TextInputEditText password;
    @BindView(R.id.re_signup_password)
    TextInputEditText rePassword;
    @BindView(R.id.enable_password_button)
    MaterialButton enableButton;
    @BindView(R.id.signup_progress)
    MaterialProgressBar mProgress;
    private CompositeDisposable compositeDisposable;
    private static final String TAG = "SignupActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        enableButton.setOnClickListener(v -> {
            if (isPasswordValid()) {
                PreferenceGetter.startKeyGeneratorAsync(password.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                mProgress.setVisibility(View.VISIBLE);
                                addDisposable(d);
                            }

                            @Override
                            public void onComplete() {
                                mProgress.setVisibility(View.INVISIBLE);
                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                startActivity(intent);
                                PreferenceGetter.setIsPasswordEnabled(true);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mProgress.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "Unable to set password", Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });

    }



    private boolean isPasswordValid() {
        if (InputHelper.isEmpty(password)) {
            password.setError("Password can't be empty");
            return false;
        } else {
            if (password.getText().toString().length() < 4) {
                password.setError("Password must be at least 4 characters long");
                return false;
            }
        }
        if (InputHelper.isEmpty(rePassword)) {
            rePassword.setError("Retype your password");
            return false;
        }
        if (!rePassword.getText().toString().equals(password.getText().toString())) {
            password.setError("Passwords don't match");
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
