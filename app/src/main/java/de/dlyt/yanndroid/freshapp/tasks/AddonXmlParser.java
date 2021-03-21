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

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.dlyt.yanndroid.freshapp.utils.Addon;
import de.dlyt.yanndroid.freshapp.utils.Constants;

public class AddonXmlParser extends DefaultHandler implements Constants {

    private final String TAG = this.getClass().getSimpleName();
    private boolean tagAddon = false;
    private boolean tagId = false;
    private boolean tagTitle = false;
    private boolean tagDesc = false;
    private boolean tagUpdatedAt = false;
    private boolean tagSize = false;
    private boolean tagDownloadLink = false;
    private ArrayList<Addon> mAddons = new ArrayList<>();
    private Addon mAddon;
    private StringBuffer value = new StringBuffer();
    private int id;

    public ArrayList<Addon> parse(File xmlFile) throws IOException {

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
            mAddon = new Addon();
            tagAddon = true;
        }

        if (qName.equalsIgnoreCase("id")) {
            tagId = true;
        }

        if (qName.equalsIgnoreCase("name")) {
            tagTitle = true;
        }

        if (qName.equalsIgnoreCase("description")) {
            tagDesc = true;
        }

        if (qName.equalsIgnoreCase("updated-at")) {
            tagUpdatedAt = true;
        }

        if (qName.equalsIgnoreCase("size")) {
            tagSize = true;
        }

        if (qName.equalsIgnoreCase("download-link")) {
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

            if (tagUpdatedAt) {
                String[] splitInput = input.split("T");
                mAddon.setUpdatedOn(splitInput[0]);
                tagUpdatedAt = false;
                if (DEBUGGING) {
                    Log.d(TAG, "Updated Date = " + splitInput[0]);
                }
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