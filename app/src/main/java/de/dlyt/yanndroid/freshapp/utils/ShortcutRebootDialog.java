package de.dlyt.yanndroid.freshapp.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.freshapp.R;


public class ShortcutRebootDialog extends AppCompatActivity {

    private AlertDialog.Builder mRebootDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRebootDialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle);

        mRebootDialog.setTitle(R.string.are_you_sure)
                .setMessage(R.string.available_reboot_confirm)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Tools.recovery(getBaseContext());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }
}
