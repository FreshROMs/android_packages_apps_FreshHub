package de.dlyt.yanndroid.fresh.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class AddonProperties {
    public static Boolean isAddonInstalled(String packageName) {
        File meta;
        File fileList;

        try {
            meta = new File("/system/etc/fresh/addons/"+packageName+".metadata");
        } catch (Exception e) {
            return false;
        }

        try {
            fileList = new File("/system/etc/fresh/addons/files-list/"+packageName+".txt");
        } catch (Exception e) {
            return false;
        }

        return meta.exists() && fileList.exists();
    }

    public static Integer getInstalledAddonVersion(String packageName) {
        Process p;
        int result = 0;
        try {
            p = new ProcessBuilder("/system/bin/cat", "/system/etc/fresh/addons/"+packageName+".metadata").redirectErrorStream(true)
                    .start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                result = Integer.parseInt(line);
            }
            br.close();
        } catch (Exception e) {
            return 0;
        }
        return result;
    }
}
