package de.dlyt.yanndroid.fresh.services;

import android.app.Dialog;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.dlyt.yanndroid.fresh.hub.AddonActivity;
import de.dlyt.yanndroid.fresh.Constants;
import de.dlyt.yanndroid.fresh.database.TnsAddon;
import de.dlyt.yanndroid.fresh.database.TnsOta;

public class TnsAddonApiService extends ArrayList<TnsAddon> {

    private static final String MANIFEST = "addon_manifest.xml";
    public final String TAG = this.getClass().getSimpleName();
    private final Context mContext;
    private ArrayList<TnsAddon> mResult;

    public TnsAddonApiService(AddonActivity activity, Context context, Dialog dialog) {
        mContext = context;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            File manifest = new File(mContext.getFilesDir().getPath(), MANIFEST);
            TnsAddonParser parser = new TnsAddonParser();

            try {
                ArrayList<TnsAddon> oldData = parser.parse(new File(context.getFilesDir(), MANIFEST));
                AddonActivity.runOnUI(() -> {
                    if (oldData != null) {
                        activity.setupListView(oldData);
                    }
                });
            } catch (IOException e) {
                // Fail
            }

            if (manifest.exists()) {
                boolean deleted = manifest.delete();
                if (!deleted) Log.e(TAG, "Unable to delete manifest file...");
            }

            mResult = getAddonData(mContext);

            handler.postDelayed(() -> {
                AddonActivity.runOnUI(() -> {
                    if (mResult != null) {
                        activity.setupListView(mResult);
                        dialog.cancel();
                    }
                });
            }, 1500);
        });
    }

    private ArrayList<TnsAddon> getAddonData(Context context) {

        try {
            InputStream input;

            URL url = new URL(TnsOta.getAddonsUrl(context));
            URLConnection connection = url.openConnection();
            connection.connect();
            // download the file
            input = new BufferedInputStream(url.openStream());

            OutputStream output = context.openFileOutput(
                    MANIFEST, Context.MODE_PRIVATE);

            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            // file finished downloading, parse it!
            TnsAddonParser parser = new TnsAddonParser();
            return parser.parse(new File(context.getFilesDir(), MANIFEST));
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
        return null;
    }
}

class TnsAddonParser extends DefaultHandler implements Constants {
    private final String TAG = this.getClass().getSimpleName();
    private boolean tagAddon = false;
    private boolean tagId = false;
    private boolean tagTitle = false;
    private boolean tagDesc = false;
    private boolean tagSize = false;
    private boolean tagVersionName = false;
    private boolean tagFullInfo = false;
    private boolean tagVersionNumber = false;
    private boolean tagImage = false;
    private boolean tagPackageName = false;
    private boolean tagDownloadLink = false;

    private ArrayList<TnsAddon> mAddons = new ArrayList<>();
    private TnsAddon mAddon;
    private StringBuffer value = new StringBuffer();
    private int id;

    public ArrayList<TnsAddon> parse(File xmlFile) throws IOException {

        try {
            id = 1;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(xmlFile, this);

            return mAddons;

        } catch (ParserConfigurationException | SAXException ex) {
            Log.e(TAG, "", ex);
        }

        return null;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        value.setLength(0);

        if (qName.equalsIgnoreCase("addon")) {
            mAddon = new TnsAddon();
            tagAddon = true;
        }

        if (qName.equalsIgnoreCase("id")) {
            tagId = true;
        }

        if (qName.equalsIgnoreCase("thumbnail")) {
            tagImage = true;
        }

        if (qName.equalsIgnoreCase("name")) {
            tagTitle = true;
        }

        if (qName.equalsIgnoreCase("description")) {
            tagDesc = true;
        }

        if (qName.equalsIgnoreCase("versionname")) {
            tagVersionName = true;
        }

        if (qName.equalsIgnoreCase("versionnumber")) {
            tagVersionNumber = true;
        }

        if (qName.equalsIgnoreCase("fullinfo")) {
            tagFullInfo = true;
        }

        if (qName.equalsIgnoreCase("filesize")) {
            tagSize = true;
        }

        if (qName.equalsIgnoreCase("packagename")) {
            tagPackageName = true;
        }

        if (qName.equalsIgnoreCase("directurl")) {
            tagDownloadLink = true;
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

        if (tagAddon) {
            mAddons.add(mAddon);
            tagAddon = false;
        } else {

            if (tagId) {
                mAddon.setId(id);
                tagId = false;
                if (DEBUGGING) {
                    Log.d(TAG, "Id = " + id);
                }
                id++;
            }

            if (tagTitle) {
                mAddon.setTitle(input);
                tagTitle = false;
                if (DEBUGGING)
                    Log.d(TAG, "Title = " + input);
            }

            if (tagDesc) {
                mAddon.setDesc(input);
                tagDesc = false;
                if (DEBUGGING)
                    Log.d(TAG, "Description = " + input);
            }

            if (tagVersionNumber) {
                mAddon.setVersionNumber(Integer.parseInt(input));
                tagVersionNumber = false;
            }

            if (tagImage) {
                mAddon.setImageUrl(input);
                tagImage = false;
            }

            if (tagPackageName) {
                mAddon.setPackageName(input);
                tagPackageName = false;
            }

            if (tagVersionName) {
                mAddon.setVersionName(input);
                tagVersionName = false;
            }

            if (tagFullInfo) {
                mAddon.setFullInfo(input);
                tagFullInfo = false;
            }

            if (tagSize) {
                mAddon.setFilesize(Integer.parseInt(input));
                tagSize = false;
                if (DEBUGGING)
                    Log.d(TAG, "Filesize " + Integer.parseInt(input));
            }

            if (tagDownloadLink) {
                mAddon.setDownloadLink(input);
                tagDownloadLink = false;
                if (DEBUGGING)
                    Log.d(TAG, "Download Link = " + input);
            }
        }
    }
}