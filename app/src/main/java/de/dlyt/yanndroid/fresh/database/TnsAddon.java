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

package de.dlyt.yanndroid.fresh.database;

public class TnsAddon {
    private String mTitle;
    private String mDesc;
    private long mFileSize;
    private String mDownloadLink;
    private int mId;

    private String mVersionName;
    private String mFullInfo;
    private int mVersionNumber;
    private String mImageUrl;
    private String mPackageName;

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

    public String getFullInfo() {
        return mFullInfo;
    }

    public void setFullInfo(String input) {
        mFullInfo = input;
    }

    public Integer getVersionNumber() {
        return mVersionNumber;
    }

    public void setVersionNumber(Integer input) {
        mVersionNumber = input;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public void setVersionName(String input) {
        mVersionName = input;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String input) {
        mImageUrl = input;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String input) {
        mPackageName = input;
    }

    public String getDownloadLink() {
        return mDownloadLink;
    }

    public void setDownloadLink(String input) {
        mDownloadLink = input;
    }

    public long getFilesize() {
        return mFileSize;
    }

    public void setFilesize(long input) {
        mFileSize = input;
    }

    public int getId() {
        return mId;
    }

    public void setId(int input) {
        mId = input;
    }
}