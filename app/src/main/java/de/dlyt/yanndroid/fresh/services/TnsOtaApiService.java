package de.dlyt.yanndroid.fresh.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.dlyt.yanndroid.fresh.utils.SystemProperties;
import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsOta;

public class TnsOtaApiService implements Constants {

    private static final String MANIFEST = "update_manifest.xml";
    public final String TAG = this.getClass().getSimpleName();

    public TnsOtaApiService(Context context, boolean isForeground) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            File manifest = new File(context.getFilesDir().getPath(), MANIFEST);
            if (manifest.exists()) {
                boolean deleted = manifest.delete();
                if (!deleted) Log.e(TAG, "Could not delete manifest file...");
            }

            try {
                InputStream input;

                URL url = new URL(
                        SystemProperties.getProp(context.getResources().getString(R.string.ota_swupdate_prop_api_url)) + "/"
                                + SystemProperties.getDeviceProduct() + "/"
                                + SystemProperties.getProp(context.getResources().getString(R.string.ota_swupdate_prop_branch)) + "/"
                                + SystemProperties.getProp(context.getResources().getString(R.string.ota_swupdate_prop_version)) + "/");

                ////////////////////////////////////////////////////////////////////////////////////
                // TO-DO: REMOVE ON PRODUCTION
                // TODO
                // IMPORTANT
                url = new URL("https://ota.tensevntysevn.cf/fresh/a50xx/beta/21040501"); //todo: remove at release
                // REMOVE ON RELEASE
                // TODO
                // IMPORTANT
                ////////////////////////////////////////////////////////////////////////////////////

                URLConnection connection = url.openConnection();
                connection.connect();
                input = new BufferedInputStream(url.openStream());

                OutputStream output = context.openFileOutput(MANIFEST, Context.MODE_PRIVATE);

                byte[] data = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                TnsOtaParser parser = new TnsOtaParser();
                parser.parse(new File(context.getFilesDir(), MANIFEST), context);

                handler.post(() -> {
                    Intent intent;
                    if (isForeground) {
                        intent = new Intent(MANIFEST_LOADED);
                    } else {
                        intent = new Intent(MANIFEST_CHECK_BACKGROUND);
                    }
                    context.sendBroadcast(intent);
                });
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        });
    }
}

class TnsOtaParser extends DefaultHandler implements Constants {

    public final String TAG = this.getClass().getSimpleName();
    private boolean tagRomName = false;
    private boolean tagVersionName = false;
    private boolean tagVersionNumber = false;
    private boolean tagDirectUrl = false;
    private boolean tagHttpUrl = false;
    private boolean tagMD5 = false;
    private boolean tagLog = false;
    private boolean tagAndroid = false;
    private boolean tagDeveloper = false;
    private boolean tagWebsite = false;
    private boolean tagDonateUrl = false;
    private boolean tagBitCoinUrl = false;
    private boolean tagFileSize = false;
    private boolean tagAddonsCount = false;
    private boolean tagAddonUrl = false;
    private boolean tagSemVersion = false;
    private boolean tagSpl = false;
    private boolean tagReleaseType = false;
    private boolean tagReleaseVersion = false;
    private boolean tagReleaseVariant = false;
    private boolean tagForumUrl = false;
    private boolean tagDiscordUrl = false;
    private boolean tagGitIssues = false;
    private boolean tagGitDiscussion = false;
    private boolean tagAppVersion = false;
    private boolean tagAppUrl = false;

    private StringBuffer value = new StringBuffer();
    private Context mContext;

    public void parse(File xmlFile, Context context) throws IOException {
        mContext = context;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(xmlFile, this);

            TnsOta.setUpdateAvailability(context);

        } catch (ParserConfigurationException | SAXException ex) {
            Log.e(TAG, "", ex);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        value.setLength(0);

        if (qName.equalsIgnoreCase("romname")) {
            tagRomName = true;
        }

        if (qName.equalsIgnoreCase("versionname")) {
            tagVersionName = true;
        }

        if (qName.equalsIgnoreCase("buildversion")) {
            tagVersionNumber = true;
        }

        if (qName.equalsIgnoreCase("directurl")) {
            tagDirectUrl = true;
        }

        if (qName.equalsIgnoreCase("httpurl")) {
            tagHttpUrl = true;
        }

        if (qName.equalsIgnoreCase("android")) {
            tagAndroid = true;
        }

        if (qName.equalsIgnoreCase("checkmd5")) {
            tagMD5 = true;
        }

        if (qName.equalsIgnoreCase("filesize")) {
            tagFileSize = true;
        }

        if (qName.equalsIgnoreCase("developer")) {
            tagDeveloper = true;
        }

        if (qName.equalsIgnoreCase("websiteurl")) {
            tagWebsite = true;
        }

        if (qName.equalsIgnoreCase("donateurl")) {
            tagDonateUrl = true;
        }

        if (qName.equalsIgnoreCase("bitcoinaddress")) {
            tagBitCoinUrl = true;
        }

        if (qName.equalsIgnoreCase("changelog")) {
            tagLog = true;
        }

        if (qName.equalsIgnoreCase("addoncount")) {
            tagAddonsCount = true;
        }

        if (qName.equalsIgnoreCase("addonsmanifest")) {
            tagAddonUrl = true;
        }

        if (qName.equalsIgnoreCase("semversion")) {
            tagSemVersion = true;
        }

        if (qName.equalsIgnoreCase("spl")) {
            tagSpl = true;
        }

        if (qName.equalsIgnoreCase("appversion")) {
            tagAppVersion = true;
        }

        if (qName.equalsIgnoreCase("appurl")) {
            tagAppUrl = true;
        }

        if (qName.equalsIgnoreCase("release")) {
            tagReleaseType = true;
        }

        if (qName.equalsIgnoreCase("releaseversion")) {
            tagReleaseVersion = true;
        }

        if (qName.equalsIgnoreCase("releasevariant")) {
            tagReleaseVariant = true;
        }

        if (qName.equalsIgnoreCase("forumurl")) {
            tagForumUrl = true;
        }

        if (qName.equalsIgnoreCase("discordurl")) {
            tagDiscordUrl = true;
        }

        if (qName.equalsIgnoreCase("gitissues")) {
            tagGitIssues = true;
        }

        if (qName.equalsIgnoreCase("gitdiscussion")) {
            tagGitDiscussion = true;
        }
    }

    @Override
    public void characters(char[] buffer, int start, int length)
            throws SAXException {
        value.append(buffer, start, length);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        String input = value.toString().trim();

        if (tagRomName) {
            TnsOta.setRomName(mContext, input);
            tagRomName = false;
            if (DEBUGGING)
                Log.d(TAG, "Name = " + input);
        }

        if (tagVersionName) {
            TnsOta.setVersionName(mContext, input);
            tagVersionName = false;
            if (DEBUGGING)
                Log.d(TAG, "Version = " + input);
        }

        if (tagVersionNumber) {
            TnsOta.setVersionNumber(mContext, Integer.parseInt(input));
            tagVersionNumber = false;
            if (DEBUGGING)
                Log.d(TAG, "OTA Version = " + input);
        }

        if (tagDirectUrl) {
            if (!input.isEmpty()) {
                TnsOta.setDirectUrl(mContext, input);
            } else {
                TnsOta.setDirectUrl(mContext, "null");
            }
            TnsOta.setDirectUrl(mContext, input);
            tagDirectUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "URL = " + input);
        }

        if (tagHttpUrl) {
            if (!input.isEmpty()) {
                TnsOta.setHttpUrl(mContext, input);
            } else {
                TnsOta.setHttpUrl(mContext, "null");
            }
            tagHttpUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "tagHttpUrl = " + input);
        }

        if (tagAndroid) {
            TnsOta.setAndroidVersion(mContext, input);
            tagAndroid = false;
            if (DEBUGGING)
                Log.d(TAG, "Android Version = " + input);
        }

        if (tagMD5) {
            TnsOta.setMd5(mContext, input);
            tagMD5 = false;
            if (DEBUGGING)
                Log.d(TAG, "MD5 = " + input);
        }

        if (tagFileSize) {
            TnsOta.setFileSize(mContext, Integer.parseInt(input));
            tagFileSize = false;
            if (DEBUGGING)
                Log.d(TAG, "Filesize = " + input);
        }

        if (tagDeveloper) {
            TnsOta.setDeveloper(mContext, input);
            tagDeveloper = false;
            if (DEBUGGING)
                Log.d(TAG, "Developer = " + input);
        }

        if (tagSemVersion) {
            if (!input.isEmpty()) {
                TnsOta.setSesl(mContext, input);
            } else {
                TnsOta.setSesl(mContext, "null");
            }
            tagSemVersion = false;
            if (DEBUGGING)
                Log.d(TAG, "Sesl = " + input);
        }

        if (tagSpl) {
            if (!input.isEmpty()) {
                TnsOta.setSpl(mContext, input);
            } else {
                TnsOta.setSpl(mContext, "null");
            }
            tagSpl = false;
            if (DEBUGGING)
                Log.d(TAG, "Spl = " + input);
        }

        if (tagReleaseType) {
            if (!input.isEmpty()) {
                TnsOta.setReleaseType(mContext, input);
            } else {
                TnsOta.setReleaseType(mContext, "null");
            }
            tagReleaseType = false;
            if (DEBUGGING)
                Log.d(TAG, "RelType = " + input);
        }

        if (tagReleaseVersion) {
            TnsOta.setReleaseVersion(mContext, input);
            tagReleaseVersion = false;
            if (DEBUGGING)
                Log.d(TAG, "RelVer = " + input);
        }

        if (tagReleaseVariant) {
            TnsOta.setReleaseVariant(mContext, input);
            tagReleaseVariant = false;
            if (DEBUGGING)
                Log.d(TAG, "RelVar = " + input);
        }

        if (tagWebsite) {
            if (!input.isEmpty()) {
                TnsOta.setWebsite(mContext, input);
            } else {
                TnsOta.setWebsite(mContext, "null");
            }
            tagWebsite = false;
            if (DEBUGGING)
                Log.d(TAG, "Website = " + input);
        }

        if (tagForumUrl) {
            if (!input.isEmpty()) {
                TnsOta.setForum(mContext, input);
            } else {
                TnsOta.setForum(mContext, "null");
            }
            tagForumUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "Forum = " + input);
        }

        if (tagDiscordUrl) {
            if (!input.isEmpty()) {
                TnsOta.setDiscord(mContext, input);
            } else {
                TnsOta.setDiscord(mContext, "null");
            }
            tagDiscordUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "Discord = " + input);
        }

        if (tagGitIssues) {
            if (!input.isEmpty()) {
                TnsOta.setGitIssues(mContext, input);
            } else {
                TnsOta.setGitIssues(mContext, "null");
            }
            tagGitIssues = false;
            if (DEBUGGING)
                Log.d(TAG, "Git Issues = " + input);
        }

        if (tagGitDiscussion) {
            if (!input.isEmpty()) {
                TnsOta.setGitDiscussion(mContext, input);
            } else {
                TnsOta.setGitDiscussion(mContext, "null");
            }
            tagGitDiscussion = false;
            if (DEBUGGING)
                Log.d(TAG, "Git Discussion = " + input);
        }

        if (tagDonateUrl) {
            if (!input.isEmpty()) {
                TnsOta.setDonateLink(mContext, input);
            } else {
                TnsOta.setDonateLink(mContext, "null");
            }
            tagDonateUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "Donate URL = " + input);
        }

        if (tagBitCoinUrl) {
            if (input.contains("bitcoin:")) {
                TnsOta.setBitCoinLink(mContext, input);
            } else if (input.isEmpty()) {
                TnsOta.setBitCoinLink(mContext, "null");
            } else {
                TnsOta.setBitCoinLink(mContext, "bitcoin:" + input);
            }

            tagBitCoinUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "BitCoin URL = " + input);
        }

        if (tagLog) {
            TnsOta.setChangelog(mContext, input);
            tagLog = false;
            if (DEBUGGING)
                Log.d(TAG, "Changelog = " + input);
        }

        if (tagAddonsCount) {
            TnsOta.setAddonsCount(mContext, Integer.parseInt(input));
            tagAddonsCount = false;
            if (DEBUGGING)
                Log.d(TAG, "Addons Count = " + input);
        }

        if (tagAddonUrl) {
            TnsOta.setAddonsUrl(mContext, input);
            tagAddonUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "Addons URL = " + input);
        }

        if (tagAppUrl) {
            TnsOta.setAppUrl(mContext, input);
            tagAppUrl = false;
        }

        if (tagAppVersion) {
            TnsOta.setAppVersion(mContext, Integer.parseInt(input));
            tagAppVersion = false;
        }
    }
}