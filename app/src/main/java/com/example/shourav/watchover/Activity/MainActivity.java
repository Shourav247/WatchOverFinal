package com.example.shourav.watchover.activity;

import android.Manifest;
import android.accounts.AuthenticatorException;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.shourav.watchover.Adapter.FolderAdapter;
import com.example.shourav.watchover.AlarmDialog.CreateFolder;
import com.example.shourav.watchover.AlarmDialog.CreateTextFile;
import com.example.shourav.watchover.AlarmDialog.DeleteConfirmation;
import com.example.shourav.watchover.AlarmDialog.NewItemsAdd;
import com.example.shourav.watchover.AlarmDialog.RenameFile;
import com.example.shourav.watchover.AlarmDialog.Updates;
import com.example.shourav.watchover.R;
import com.example.shourav.watchover.SnackberView;
import com.example.shourav.watchover.UserPreferences;
import com.example.shourav.watchover.fragment.BatteryFragment;
import com.example.shourav.watchover.fragment.MemoryFragment;
import com.example.shourav.watchover.fragment.RamFragment;
import com.example.shourav.watchover.sender.SHAREthemActivity;
import com.example.shourav.watchover.sender.SHAREthemService;
import com.example.shourav.watchover.utils.InputHelper;
import com.example.shourav.watchover.utils.PreferenceGetter;
import com.jaredrummler.android.device.DeviceName;
import com.ram.speed.booster.RAMBooster;
import com.shourav.storage.Order;
import com.shourav.storage.ProvideFile;
import com.shourav.storage.Unit;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.example.shourav.watchover.SnackberView.fileExt;


public class MainActivity extends AppCompatActivity implements
        FolderAdapter.OnFileItemListener,
        NewItemsAdd.DialogListener,
        Updates.DialogListener,
        CreateFolder.DialogListener,
        CreateTextFile.DialogListener,
        DeleteConfirmation.ConfirmListener,
        RenameFile.DialogListener,
        NavigationView.OnNavigationItemSelectedListener{

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private RecyclerView mRecyclerView;
    private FolderAdapter mFilesAdapter;
    private ProvideFile mProvideFile;
    private TextView mPathView;
    private TextView mMovingText;
    private FloatingActionButton btnNewFile;
    private FloatingActionButton newFile;
    private boolean mCopy;
    private View mMovingLayout;
    private int mTreeSteps = 0;
    private String mMovingPath;
    private boolean mInternal = false;

    private RAMBooster booster;
    private static String TAG = "booster.test";


    private UserPreferences userPreferences;

    private FrameLayout frameLayout;
    private ConstraintLayout constraintLayout;
    private RelativeLayout relativeLayout;

    private boolean isSwitched = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mProvideFile = new ProvideFile(getApplicationContext());

        userPreferences = new UserPreferences(this);
        booster = new RAMBooster(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        frameLayout = (FrameLayout) findViewById(R.id.fragment_container);
        constraintLayout = (ConstraintLayout) findViewById(R.id.fileList);
        relativeLayout = (RelativeLayout) findViewById(R.id.rl);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mPathView = (TextView) findViewById(R.id.path);
        mMovingLayout = findViewById(R.id.moving_layout);
        mMovingText = (TextView) mMovingLayout.findViewById(R.id.moving_text);

        mMovingLayout.findViewById(R.id.accept_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMovingLayout.setVisibility(View.GONE);
                if (mMovingPath != null) {

                    if (!mCopy) {
                        String toPath = getCurrentPath() + File.separator + mProvideFile.getFile(mMovingPath).getName();
                        if (!mMovingPath.equals(toPath)) {
                            mProvideFile.move(mMovingPath, toPath);
                            SnackberView.showSnackbar("Moved", mRecyclerView);
                            showFiles(getCurrentPath());
                        } else {
                            SnackberView.showSnackbar("The file is already here", mRecyclerView);
                        }
                    } else {
                        String toPath = getCurrentPath() + File.separator + "copy " + mProvideFile.getFile(mMovingPath)
                                .getName();
                        mProvideFile.copy(mMovingPath, toPath);
                        SnackberView.showSnackbar("Copied", mRecyclerView);
                        showFiles(getCurrentPath());
                    }
                    mMovingPath = null;
                }
            }
        });

        mMovingLayout.findViewById(R.id.decline_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMovingLayout.setVisibility(View.GONE);
                mMovingPath = null;
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mFilesAdapter = new FolderAdapter(getApplicationContext());
        mFilesAdapter.setListener(this);
        mRecyclerView.setAdapter(mFilesAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        btnNewFile =(FloatingActionButton) findViewById(R.id.btnNewfile);
        btnNewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewItemsAdd.newInstance().show(getFragmentManager(), "add_items");
            }
        });

        mPathView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPathMenu();
            }
        });

        // load files
        showFiles(mProvideFile.getExternalStorageDirectory());

        checkPermission();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        MenuItem viewChange =menu.findItem(R.id.switchView);

        if (isSwitched)
        {
            viewChange.setTitle("Grid View");
        }else {
            viewChange.setTitle("List View");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.newFile:
                NewItemsAdd.newInstance().show(getFragmentManager(), "add_items");
                break;
            case R.id.itemHome:
                frameLayout.setVisibility(View.GONE);
                constraintLayout.setVisibility(View.VISIBLE);
                relativeLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.switchView:
                supportInvalidateOptionsMenu();
                isSwitched = mFilesAdapter.toggleItemViewType();
                mRecyclerView.setLayoutManager(isSwitched ? new LinearLayoutManager(this) : new GridLayoutManager(this, 4));
                mFilesAdapter.notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);

    }
    private void showPathMenu() {
        PopupMenu popupmenu = new PopupMenu(this, mPathView);
        MenuInflater inflater = popupmenu.getMenuInflater();
        inflater.inflate(R.menu.path_menu, popupmenu.getMenu());

        popupmenu.getMenu().findItem(R.id.go_internal).setVisible(!mInternal);
        popupmenu.getMenu().findItem(R.id.go_external).setVisible(mInternal);

        popupmenu.show();

        popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.go_up:
                        String previousPath = getPreviousPath();
                        mTreeSteps = 0;
                        showFiles(previousPath);
                        break;
                    case R.id.go_internal:
                        showFiles(mProvideFile.getInternalFilesDirectory());
                        mInternal = true;
                        break;
                    case R.id.go_external:
                        showFiles(mProvideFile.getExternalStorageDirectory());
                        mInternal = false;
                        break;
                }
                return true;
            }
        });
    }

    private void showFiles(String path) {
        Log.e(TAG, "showFiles: called");
        mPathView.setText(path);
        List<File> files = mProvideFile.getFiles(path);
//        if (path != null && path.equals(mProvideFile.getExternalStorageDirectory())) {
//            Log.e(TAG, "showFiles: Pog bro");
//            if (files != null) {
//                File watchify = new File(mProvideFile.getExternalStorageDirectory()+"/.WatchOver/");
//                modifiableFiles.add(watchify);
//            }
//        }
        if (files != null) {
//            modifiableFiles.addAll(files);
            Collections.sort(files, Order.NAME.getComparator());
        }
        mFilesAdapter.setFiles(files);
        mFilesAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(File file) {
        if (file.isDirectory()) {
            mTreeSteps++;
            String path = file.getAbsolutePath();
            showFiles(path);
        } else {

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String mimeType =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt(file.getAbsolutePath()));
                Uri apkURI = FileProvider.getUriForFile(
                        this,
                        getApplicationContext()
                                .getPackageName() + ".provider", file);
                intent.setDataAndType(apkURI, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                if (mProvideFile.getSize(file, Unit.KB) > 500) {
                    SnackberView.showSnackbar("The file is too big for preview", mRecyclerView);
                    return;
                }
                Intent intent = new Intent(this, ViewTextActivity.class);
                intent.putExtra(ViewTextActivity.EXTRA_FILE_NAME, file.getName());
                intent.putExtra(ViewTextActivity.EXTRA_FILE_PATH, file.getAbsolutePath());
                startActivity(intent);
            }

        }
    }

    @Override
    public void onLongClick(File file) {
        Updates.newInstance(file.getAbsolutePath()).show(getFragmentManager(), "update_item");
    }

    @Override
    public void onBackPressed() {
        if (mTreeSteps > 0) {
            String path = getPreviousPath();
            mTreeSteps--;
            showFiles(path);
            return;
        }
        super.onBackPressed();
    }

    private String getCurrentPath() {
        return mPathView.getText().toString();
    }

    private String getPreviousPath() {
        String path = getCurrentPath();
        int lastIndexOf = path.lastIndexOf(File.separator);
        if (lastIndexOf < 0) {
            SnackberView.showSnackbar("Can't go anymore", mRecyclerView);
            return getCurrentPath();
        }
        return path.substring(0, lastIndexOf);
    }

    @Override
    public void onOptionClick(int which, String path) {
        switch (which) {
            case R.id.encrypt:
                showEncryptionDialog(path);
                break;
            case R.id.decrypt:
                showDecryptionDialog(path);
                break;
            case R.id.send:
                Intent intent = new Intent(this, SHAREthemActivity.class);
                intent.putExtra(SHAREthemService.EXTRA_FILE_PATHS, new String[]{path}); // mandatory
                intent.putExtra(SHAREthemService.EXTRA_PORT, 52287); //optional but preferred. PORT value is hardcoded for Oreo and above since it's not possible to set SSID with which port info can be extracted on Receiver side.
                intent.putExtra(SHAREthemService.EXTRA_SENDER_NAME, DeviceName.getDeviceName()); //optional. Sender name can't be relayed to receiver for Oreo & above
                startActivity(intent);
                break;
            case R.id.new_file:
                CreateTextFile.newInstance().show(getFragmentManager(), "new_file_dialog");
                break;
            case R.id.new_folder:
                CreateFolder.newInstance().show(getFragmentManager(), "new_folder_dialog");
                break;
            case R.id.delete:
                DeleteConfirmation.newInstance(path).show(getFragmentManager(), "confirm_delete");
                break;
            case R.id.rename:
                RenameFile.newInstance(path).show(getSupportFragmentManager(), "rename");
                break;
            case R.id.move:
                mMovingText.setText(getString(R.string.moving_file, mProvideFile.getFile(path).getName()));
                mMovingPath = path;
                mCopy = false;
                mMovingLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.copy:
                mMovingText.setText(getString(R.string.copy_file, mProvideFile.getFile(path).getName()));
                mMovingPath = path;
                mCopy = true;
                mMovingLayout.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void showEncryptionDialog(String path) {
        List<Boolean> booleanList = new ArrayList<>();
        booleanList.add(true);
        Uri fileUri = Uri.fromFile(new File(path));
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Encrypt file")
                .content("Are you sure you want to encrypt "+fileUri.getLastPathSegment()+ "?")
                .positiveText("Yes")
                .negativeText("No")
                .checkBoxPrompt("Delete source file", true, (buttonView, isChecked) -> {
                    booleanList.set(0, isChecked);
                })
                .onPositive((dialog12, which) -> {
                    dialog12.dismiss();
                    startEncryption(path, fileUri, booleanList.get(0));
                })
                .onNegative((dialog1, which) -> {
                    dialog1.dismiss();
                })
                .build();
        dialog.show();
    }

    private void startEncryption(String path, Uri fileUri, boolean deleteSource) {
        EncryptViewHolder viewHolder = new EncryptViewHolder(fileUri, path, deleteSource);
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_encrypt, false)
                .title("Encrypting")
                .negativeText("Cancel")
                .onNegative((dialog1, which) -> {
                    viewHolder.onNegativeClick(dialog1);
                })
                .cancelable(false)
                .autoDismiss(false)
                .build();
        dialog.show();
        dialog.setOnDismissListener(dialog12 -> viewHolder.dispose());
        viewHolder.bindView(dialog.getCustomView(), dialog);

    }

    private void showDecryptionDialog(String path) {
        Uri fileUri = Uri.fromFile(new File(path));
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Decrypt file")
                .content("Are you sure you want to decrypt "+fileUri.getLastPathSegment()+ "?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive((dialog12, which) -> {
                    dialog12.dismiss();
                    startDecryption(path, fileUri);
                })
                .onNegative((dialog1, which) -> {
                    dialog1.dismiss();
                })
                .build();
        dialog.show();
    }


    private void startDecryption(String path, Uri fileUri) {
        DecryptViewHolder viewHolder = new DecryptViewHolder(fileUri, path);
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_decrypt, false)
                .title("Decrypting")
                .positiveText("Continue")
                .negativeText("Cancel")
                .onNegative((dialog1, which) -> {
                    viewHolder.onNegativeClick(dialog1);
                })
                .onPositive((dialog13, which) -> viewHolder.onPositiveClick(dialog13))
                .cancelable(false)
                .autoDismiss(false)
                .build();
        dialog.show();
        dialog.setOnDismissListener(dialog12 -> viewHolder.dispose());
        viewHolder.bindView(dialog.getCustomView(), dialog);

    }


    @Override
    public void onNewFolder(String name) {
        String currentPath = getCurrentPath();
        String folderPath = currentPath + File.separator + name;
        boolean created = mProvideFile.createDirectory(folderPath);
        if (created) {
            showFiles(currentPath);
            SnackberView.showSnackbar("New folder created: " + name, mRecyclerView);
        } else {
            SnackberView.showSnackbar("Failed create folder: " + name, mRecyclerView);
        }
    }

    @Override
    public void onNewFile(String name, String content) {
        String currentPath = getCurrentPath();
        String folderPath = currentPath + File.separator + name;
        mProvideFile.createFile(folderPath, content);
        showFiles(currentPath);
        SnackberView.showSnackbar("New file created: " + name, mRecyclerView);
    }

    @Override
    public void onConfirmDelete(String path) {
        if (mProvideFile.getFile(path).isDirectory()) {
            mProvideFile.deleteDirectory(path);
            SnackberView.showSnackbar("Folder was deleted", mRecyclerView);
        } else {
            mProvideFile.deleteFile(path);
            SnackberView.showSnackbar("File was deleted", mRecyclerView);
        }
        showFiles(getCurrentPath());
    }

    @Override
    public void onRename(String fromPath, String toPath) {
        mProvideFile.rename(fromPath, toPath);
        showFiles(getCurrentPath());
        SnackberView.showSnackbar("Renamed", mRecyclerView);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                .PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showFiles(mProvideFile.getExternalStorageDirectory());
        } else {
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_memory) {
            /*Intent intent = new Intent(getApplicationContext(),MemoryActivity.class);
            startActivity(intent);*/
            constraintLayout.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            fragment = new MemoryFragment();

        } else if (id == R.id.nav_ram) {
            /*Intent intent = new Intent(getApplicationContext(),RamActivity.class);
            startActivity(intent);*/
            constraintLayout.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            fragment = new RamFragment();

        }else if(id == R.id.nav_battery)
        {
            constraintLayout.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            fragment = new BatteryFragment();
        }
        else if (id == R.id.nav_junk) {

        } else if (id == R.id.nav_receive) {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivity(intent);
        }

        if(fragment !=null)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.fragment_container,fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class EncryptViewHolder implements EncryptionDialogListener {
        private View mView;
        private Uri mFileUri;
        private String mPath;
        private MaterialDialog mDialog;
        private Disposable mDisposable;
        private boolean deleteSource;
        @BindView(R.id.dialog_encrypt_content)
        TextView mContent;
        @BindView(R.id.dialog_encrypt_progress)
        ProgressBar mProgress;

        public EncryptViewHolder(Uri mFileUri, String mPath, boolean deleteSource) {
            this.mFileUri = mFileUri;
            this.mPath = mPath;
            this.deleteSource = deleteSource;
        }

        void bindView(View view, MaterialDialog dialog) {
            this.mView = view;
            this.mDialog = dialog;
            ButterKnife.bind(this, view);
            setupView();
        }

        private void setupView() {
            mContent.setText(mFileUri.getLastPathSegment());
            mProgress.setIndeterminate(true);
            Single.fromCallable(() -> PreferenceGetter.encryptFile(mPath))
                    .flatMap(password -> PreferenceGetter.encryptStringAsync(password, PreferenceGetter.getPublicKey()))
                    .flatMapCompletable(encryptedPass ->
                            Single.fromCallable(() ->
                                    PreferenceGetter.calculateMD5(new File(PreferenceGetter.watchify(mPath))))
                                    .flatMapCompletable(md5 ->
                                            Completable.fromAction(() ->
                                                    PreferenceGetter.storeNewEncryptedPassword(encryptedPass, md5))))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mDisposable = d;
                        }

                        @Override
                        public void onComplete() {
                            dismiss();
                            if (deleteSource) {
                                mProvideFile.deleteFile(mPath);
                            }
                            SnackberView.showSnackbar("File encrypted", mRecyclerView);
                            showFiles(getCurrentPath());
                        }

                        @Override
                        public void onError(Throwable e) {
                            dismiss();
                            Log.e(TAG, "onError: ", e);
                        }
                    });

        }

        private void dismiss() {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }

        void dispose() {
            Log.e(TAG, "dispose: called");
            if (mDisposable != null && !mDisposable.isDisposed()) {
                mDisposable.dispose();
                Log.e(TAG, "dispose: disposed");
            }
        }

        @Override
        public void onNegativeClick(MaterialDialog dialog) {
            dialog.dismiss();
        }
    }

    class DecryptViewHolder implements DecryptionDialogListener {
        private View mView;
        private Uri mFileUri;
        private String mPath;
        private MaterialDialog mDialog;
        private CompositeDisposable mDisposable;
        @BindView(R.id.dialog_decrypt_content)
        TextView mContent;
        @BindView(R.id.dialog_decrypt_progress)
        ProgressBar mProgress;
        @BindView(R.id.dialog_decrypt_password_box)
        TextInputEditText mPassword;
        @BindView(R.id.dialog_decrypt_error) TextView mError;
        DecryptViewHolder(Uri mFileUri, String mPath) {
            this.mFileUri = mFileUri;
            this.mPath = mPath;
        }

        void bindView(View view, MaterialDialog dialog) {
            this.mView = view;
            this.mDialog = dialog;
            ButterKnife.bind(this, view);
            setupView();
        }

        private void setupView() {
            mContent.setText(mFileUri.getLastPathSegment());
            mProgress.setIndeterminate(true);
        }

        private void dismiss() {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }

        private boolean validate() {
            if (InputHelper.isEmpty(mPassword)) {
                mPassword.setError("Enter your password");
                return false;
            }
            return true;
        }

        void dispose() {
            Log.e(TAG, "dispose: called");
            if (mDisposable != null && !mDisposable.isDisposed()) {
                mDisposable.dispose();
                Log.e(TAG, "dispose: disposed");
            }
        }

        @Override
        public void onPositiveClick(MaterialDialog dialog) {
            if (validate()) {
                PreferenceGetter
                        .authenticateUser(mPassword.getText().toString())
                        .doOnSubscribe(disposable -> {
                            addDisposable(disposable);
                            runOnUiThread(() -> {
                                mPassword.setVisibility(View.GONE);
                                mDialog.setTitle("Authenticating");
                            });

                        })
                        .doOnError(throwable -> {
                            runOnUiThread(() -> {
                                Log.e(TAG, "onPositiveClick: ", throwable);
                                setError("Wrong password! try again");
                                mDialog.setTitle("Decrypting");
                            });
                        })
                        .doOnSuccess(aBoolean -> {
                            runOnUiThread(() -> {
                                if (aBoolean) {
                                    dialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.GONE);
                                }
                                mPassword.setVisibility(View.GONE);
                                mProgress.setVisibility(View.VISIBLE);
                                mDialog.setTitle("Decrypting");
                            });
                        })
                        .flatMapCompletable(isAuthenticated ->
                                Completable.fromAction(() ->
                                        PreferenceGetter
                                                .findAndDecryptFile(isAuthenticated, mPath, mPassword.getText().toString())))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                addDisposable(d);
                            }

                            @Override
                            public void onComplete() {
                                SnackberView.showSnackbar("File decrypted", mRecyclerView);
                                showFiles(getCurrentPath());
                                dismiss();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: ", e);
                                String message = "";
                                if (e instanceof AuthenticatorException) {
                                    message = "Wrong password! try again.";

                                } else if (e instanceof IllegalAccessError) {
                                    message = "File password not found!";
                                } else {
                                    message = "Unable to decrypt file!";
                                }
                                setError(message);
                                SnackberView.showSnackbar(message, mRecyclerView);
                                dismiss();
                            }
                        });
            }
        }
        private void addDisposable(Disposable disposable) {
            if (mDisposable == null) {
                mDisposable = new CompositeDisposable();
            }
            mDisposable.add(disposable);
        }

        private void setError(String message) {
            if (mError.getVisibility() == View.GONE) {
                mError.setVisibility(View.VISIBLE);
                mError.setText(message);
            }
        }

        @Override
        public void onNegativeClick(MaterialDialog dialog) {
            dialog.dismiss();
        }
    }

    public interface EncryptionDialogListener {
        void onNegativeClick(MaterialDialog dialog);
    }

    public interface DecryptionDialogListener {
        void onPositiveClick(MaterialDialog dialog);
        void onNegativeClick(MaterialDialog dialog);
    }
}