/*
 * Copyright (C) 2017 Nicholas Chum (nicholaschum) and Matt Booth (Kryten2k35).
 *
 * Licensed under the Attribution-NonCommercial-ShareAlike 4.0 International
 * (the "License") you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dlyt.yanndroid.freshapp.tasks;

import android.content.Context;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.dlyt.yanndroid.freshapp.utils.Constants;
import de.dlyt.yanndroid.freshapp.utils.RomUpdate;
import de.dlyt.yanndroid.freshapp.utils.Utils;

class RomXmlParser extends DefaultHandler implements Constants {

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
    private boolean tagRomHut = false;
    private boolean tagAddonsCount = false;
    private boolean tagAddonUrl = false;
    private StringBuffer value = new StringBuffer();
    private Context mContext;

    public void parse(File xmlFile, Context context) throws IOException {
        mContext = context;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(xmlFile, this);

            Utils.setUpdateAvailability(context);

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

        if (qName.equalsIgnoreCase("versionnumber")) {
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

        if (qName.equalsIgnoreCase("addonsurl")) {
            tagAddonUrl = true;
        }

        if (qName.equalsIgnoreCase("romhut")) {
            tagRomHut = true;
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
            RomUpdate.setRomName(mContext, input);
            tagRomName = false;
            if (DEBUGGING)
                Log.d(TAG, "Name = " + input);
        }

        if (tagVersionName) {
            RomUpdate.setVersionName(mContext, input);
            tagVersionName = false;
            if (DEBUGGING)
                Log.d(TAG, "Version = " + input);
        }

        if (tagVersionNumber) {
            RomUpdate.setVersionNumber(mContext, Integer.parseInt(input));
            tagVersionNumber = false;
            if (DEBUGGING)
                Log.d(TAG, "OTA Version = " + input);
        }

        if (tagDirectUrl) {
            if (!input.isEmpty()) {
                RomUpdate.setDirectUrl(mContext, input);
            } else {
                RomUpdate.setDirectUrl(mContext, "null");
            }
            RomUpdate.setDirectUrl(mContext, input);
            tagDirectUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "URL = " + input);
        }

        if (tagHttpUrl) {
            if (!input.isEmpty()) {
                RomUpdate.setHttpUrl(mContext, input);
            } else {
                RomUpdate.setHttpUrl(mContext, "null");
            }
            tagHttpUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "tagHttpUrl = " + input);
        }

        if (tagAndroid) {
            RomUpdate.setAndroidVersion(mContext, input);
            tagAndroid = false;
            if (DEBUGGING)
                Log.d(TAG, "Android Version = " + input);
        }

        if (tagMD5) {
            RomUpdate.setMd5(mContext, input);
            tagMD5 = false;
            if (DEBUGGING)
                Log.d(TAG, "MD5 = " + input);
        }

        if (tagFileSize) {
            RomUpdate.setFileSize(mContext, Integer.parseInt(input));
            tagFileSize = false;
            if (DEBUGGING)
                Log.d(TAG, "Filesize = " + input);
        }

        if (tagDeveloper) {
            RomUpdate.setDeveloper(mContext, input);
            tagDeveloper = false;
            if (DEBUGGING)
                Log.d(TAG, "Developer = " + input);
        }

        if (tagWebsite) {
            if (!input.isEmpty()) {
                RomUpdate.setWebsite(mContext, input);
            } else {
                RomUpdate.setWebsite(mContext, "null");
            }
            tagWebsite = false;
            if (DEBUGGING)
                Log.d(TAG, "Website = " + input);
        }

        if (tagDonateUrl) {
            if (!input.isEmpty()) {
                RomUpdate.setDonateLink(mContext, input);
            } else {
                RomUpdate.setDonateLink(mContext, "null");
            }
            tagDonateUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "Donate URL = " + input);
        }

        if (tagBitCoinUrl) {
            if (input.contains("bitcoin:")) {
                RomUpdate.setBitCoinLink(mContext, input);
            } else if (input.isEmpty()) {
                RomUpdate.setBitCoinLink(mContext, "null");
            } else {
                RomUpdate.setBitCoinLink(mContext, "bitcoin:" + input);
            }

            tagBitCoinUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "BitCoin URL = " + input);
        }

        if (tagLog) {
            RomUpdate.setChangelog(mContext, input);
            tagLog = false;
            if (DEBUGGING)
                Log.d(TAG, "Changelog = " + input);
        }

        if (tagAddonsCount) {
            RomUpdate.setAddonsCount(mContext, Integer.parseInt(input));
            tagAddonsCount = false;
            if (DEBUGGING)
                Log.d(TAG, "Addons Count = " + input);
        }

        if (tagAddonUrl) {
            RomUpdate.setAddonsUrl(mContext, input);
            tagAddonUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "Addons URL = " + input);
        }

        if (tagRomHut) {
            RomUpdate.setRomHut(mContext, input);
            tagRomHut = false;
            if (DEBUGGING)
                Log.d(TAG, "Romhut? = " + input);
        }
    }
}