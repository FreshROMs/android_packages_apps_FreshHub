package de.dlyt.yanndroid.fresh.services;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.utils.Tools;

public class QSTile extends TileService {

    private AlertDialog.Builder mRebootDialog;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onClick() {

        mRebootDialog = new AlertDialog.Builder(this, R.style.DialogStyle);

        mRebootDialog.setTitle(R.string.are_you_sure)
                .setMessage(R.string.available_reboot_confirm)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Tools.rebootUpdate(getBaseContext());
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        showDialog(mRebootDialog.create());

    }


}
