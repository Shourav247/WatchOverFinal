package com.example.shourav.watchover.AlarmDialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shourav.watchover.R;


public class NewItemsAdd extends DialogFragment {

    private DialogListener mListener;

    public static NewItemsAdd newInstance() {
        NewItemsAdd fragment = new NewItemsAdd();
        return fragment;
    }

    public NewItemsAdd() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Dialog dialog = new BottomSheetDialog(getActivity(), getTheme());


        View view = LayoutInflater.from(getActivity()).inflate(R.layout.add_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);

        view.findViewById(R.id.new_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mListener.onOptionClick(R.id.new_folder, null);
            }
        });

        view.findViewById(R.id.new_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mListener.onOptionClick(R.id.new_file, null);
            }
        });

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
