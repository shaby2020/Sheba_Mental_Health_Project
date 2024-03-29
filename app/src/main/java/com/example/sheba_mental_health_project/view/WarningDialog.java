package com.example.sheba_mental_health_project.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sheba_mental_health_project.R;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class WarningDialog extends Dialog {

    private TextView mTitleWarningTv;
    private TextView mPromptTv;

    private final String TAG = "WarningDialog";


    public interface WarningDialogActionInterface {
        void onYesBtnClicked();

        void onNoBtnClicked();
    }

    private WarningDialogActionInterface listener;

    public void setOnActionListener(WarningDialogActionInterface warningDialogActionInterface) {
        this.listener = warningDialogActionInterface;
    }

    public WarningDialog(@NonNull Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        final View rootView = LayoutInflater.from(getContext())
                .inflate(R.layout.warning_dialog, null);

        mTitleWarningTv = rootView.findViewById(R.id.title_warning_tv);
        mPromptTv = rootView.findViewById(R.id.prompt_tv);

        final MaterialButton yesBtn = rootView.findViewById(R.id.yes_btn);
        final MaterialButton noBtn = rootView.findViewById(R.id.no_btn);

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onYesBtnClicked();
                }
                dismiss();
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onNoBtnClicked();
                }
                dismiss();
            }
        });

        setCancelable(false);
        setContentView(rootView);
    }

    public void setTitleWarningText(final String title) {
        if (mTitleWarningTv != null) {
            mTitleWarningTv.setText(title);
        }
    }

    public void setPromptText(final String prompt) {
        if (mPromptTv != null) {
            mPromptTv.setText(prompt);
        }
    }

    @Override
    public void show() {
        super.show();

        mTitleWarningTv.setVisibility(mTitleWarningTv.getText().toString().trim().isEmpty() ?
                View.GONE : View.VISIBLE);

        final Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }
}
