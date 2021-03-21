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

package de.dlyt.yanndroid.freshapp.utils;

public class Addon {
    private String mTitle;
    private String mDesc;
    private String mUpdatedOn;
    private int mFilesize;
    private String mDownloadLink;
    private int mId;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String input) {
        mTitle = input;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String input) {
        mDesc = input;
    }

    public String getUpdatedOn() {
        return mUpdatedOn;
    }

    public void setUpdatedOn(String input) {
        mUpdatedOn = input;
    }

    public String getDownloadLink() {
        return mDownloadLink;
    }

    public void setDownloadLink(String input) {
        mDownloadLink = input;
    }

    public int getFilesize() {
        return mFilesize;
    }

    public void setFilesize(int input) {
        mFilesize = input;
    }

    public int getId() {
        return mId;
    }

    public void setId(int input) {
        mId = input;
    }
}