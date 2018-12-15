package com.example.shourav.watchover.AlarmDialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shourav.watchover.R;
import com.example.shourav.watchover.utils.PreferenceGetter;
import com.shourav.storage.ProvideFile;

import java.io.File;

public class Updates extends DialogFragment {

    private final static String PATH = "path";
    private DialogListener mListener;

    public static Updates newInstance(String path) {
        Updates fragment = new Updates();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public Updates() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Dialog dialog = new BottomSheetDialog(getActivity(), getTheme());
        final String path = getArguments().getString(PATH);
        boolean isDirectory = new ProvideFile(getActivity()).getFile(path).isDirectory();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.update_item_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);

        // title
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(isDirectory ? getString(R.string.folder_options) : getString(R.string.file_options));

        View rename = view.findViewById(R.id.rename);
        View send = view.findViewById(R.id.send);
        View delete = view.findViewById(R.id.delete);
        View move = view.findViewById(R.id.move);
        View copy = view.findViewById(R.id.copy);
        boolean decryptMode = false;
        View encrypt = view.findViewById(R.id.encrypt_holder);
        TextView encryptTitle = view.findViewById(R.id.encrypt_text);
        ImageView encryptIcon = view.findViewById(R.id.encrypt_icon);
        Uri pathUri = Uri.fromFile(new File(path));
        if (MimeTypeMap.getFileExtensionFromUrl(pathUri.toString()).equals(PreferenceGetter.WATCHOVER_EXTENSION)) {
            encryptTitle.setText("Decrypt");
            encryptIcon.setImageResource(R.drawable.ic_decrypt_24dp);
            decryptMode = true;
        }
        final boolean isDecrypt = decryptMode;

        encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (isDecrypt) {
                    mListener.onOptionClick(R.id.decrypt, path);
                } else {
                    mListener.onOptionClick(R.id.encrypt, path);
                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mListener.onOptionClick(R.id.send, path);
            }
        });

        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mListener.onOptionClick(R.id.rename, path);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mListener.onOptionClick(R.id.delete, path);
            }
        });

        if (!isDirectory) {
            move.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    mListener.onOptionClick(R.id.move, path);
                }
            });

            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    mListener.onOptionClick(R.id.copy, path);
                }
            });
        } else {
            move.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
        }

        // control dialog width on different devices
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogINterface) {
                int width = (int) getResources().getDimension(R.dimen.bottom_sheet_dialog_width);
                dialog.getWindow().setLayout(
                        width == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : width,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });

        return dialog;
    }

    public interface DialogListener {
        void onOptionClick(int which, String path);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
