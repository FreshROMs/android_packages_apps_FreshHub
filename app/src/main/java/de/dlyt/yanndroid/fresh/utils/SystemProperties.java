package de.dlyt.yanndroid.fresh.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.tensevntysevn.fresh.ExperienceUtils;

public class SystemProperties {
    public static Boolean doesPropExist(String propName) {
        boolean valid = false;

        try {
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("[" + propName + "]")) {
                    valid = true;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return valid;
    }

    public static String getDeviceCodename() {
        return Build.DEVICE;
    }

    public static String getDeviceProduct() {
        if (!(ExperienceUtils.getProp("ro.fresh.device.product").equals(""))) {
            return ExperienceUtils.getProp("ro.fresh.device.product");
        } else if (!(ExperienceUtils.getProp("ro.product.vendor.name").equals(""))) {
            return ExperienceUtils.getProp("ro.product.vendor.name");
        } else if (!(ExperienceUtils.getProp("ro.product.odm.name").equals(""))) {
            return ExperienceUtils.getProp("ro.product.odm.name");
        } else {
            return Build.PRODUCT;
        }
    }
}
