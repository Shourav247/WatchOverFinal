package com.example.shourav.watchover.AlarmDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.shourav.watchover.R;
import com.shourav.storage.ProvideFile;


import java.io.File;

public class DeleteConfirmation extends DialogFragment {

    private final static String PATH = "path";
    private String mPath;

    public static DeleteConfirmation newInstance(String path) {
        DeleteConfirmation fragment = new DeleteConfirmation();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String msg;
        final String path = getArguments().getString(PATH);
        ProvideFile provideFile = new ProvideFile(getActivity());
        File file = provideFile.getFile(path);
        if (file.isDirectory()) {
            msg = "You are about to delete the folder with all it's content for real.";
        } else {
            msg = "You are about to delete the file";
        }
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ConfirmListener) getActivity()).onConfirmDelete(path);
            }
        });
        builder.setNegativeButton(R.string.label_cancel, null);
        return builder.create();
    }

    public interface ConfirmListener {
        void onConfirmDelete(String path);
    }
}
