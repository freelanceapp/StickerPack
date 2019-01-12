/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.ideas.stickermaker.WhatsAppBasedCode;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.ideas.stickermaker.ImageManipulation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StickerPackModal implements Parcelable {

    private Uri trayImageUri;
    private Uri downloadTrayImageUri;
    public String identifier;
    public String downloadedIdentifier;
    public String name;
    public String publisher;
    public String trayImageFile;
    final String publisherEmail;
    final String publisherWebsite;
    final String privacyPolicyWebsite;
    final String licenseAgreementWebsite;

    String iosAppStoreLink;
    private List<StickerMainModal> stickers;
    private List<StickerMainModal> downloadStickers;
    private long totalSize;
    String androidPlayStoreLink;
    private boolean isWhitelisted;
    private int stickersAddedIndex = 0;

    public StickerPackModal(String identifier, String name, String publisher, Uri trayImageUri, String publisherEmail,
                            String publisherWebsite, String privacyPolicyWebsite, String licenseAgreementWebsite, Context context) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = "trayimage";
        this.trayImageUri = ImageManipulation.convertIconTrayToWebP(trayImageUri, this.identifier, "trayImage", context);
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        this.stickers = new ArrayList<>();
    }

/*
    public StickerPackModal(String downloadedIdentifier, String name, String publisher, Uri downloadTrayImageUri,Context context) {
        this.downloadedIdentifier = downloadedIdentifier;
        this.identifier = downloadedIdentifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = "trayimage";
        this.downloadTrayImageUri = downloadTrayImageUri;
        this.trayImageUri = ImageManipulation.convertIconTrayToWebP(downloadTrayImageUri, this.identifier, "trayImage", context);
        this.publisherEmail = "";
        this.publisherWebsite = "";
        this.privacyPolicyWebsite = "";
        this.licenseAgreementWebsite = "";
        this.downloadStickers = new ArrayList<>();
        this.stickers = new ArrayList<>();
    }
*/

    public void setIsWhitelisted(boolean isWhitelisted) {
        this.isWhitelisted = isWhitelisted;
    }

    boolean getIsWhitelisted() {
        return isWhitelisted;
    }

    protected StickerPackModal(Parcel in) {
        identifier = in.readString();
        downloadedIdentifier = in.readString();
        name = in.readString();
        publisher = in.readString();
        trayImageFile = in.readString();
        publisherEmail = in.readString();
        publisherWebsite = in.readString();
        privacyPolicyWebsite = in.readString();
        licenseAgreementWebsite = in.readString();
        iosAppStoreLink = in.readString();
        stickers = in.createTypedArrayList(StickerMainModal.CREATOR);
        downloadStickers = in.createTypedArrayList(StickerMainModal.CREATOR);
        totalSize = in.readLong();
        androidPlayStoreLink = in.readString();
        isWhitelisted = in.readByte() != 0;
    }

    public static final Creator<StickerPackModal> CREATOR = new Creator<StickerPackModal>() {
        @Override
        public StickerPackModal createFromParcel(Parcel in) {
            return new StickerPackModal(in);
        }

        @Override
        public StickerPackModal[] newArray(int size) {
            return new StickerPackModal[size];
        }
    };

    public void addSticker(Uri uri, Context context) {
        String index = String.valueOf(stickersAddedIndex);
        this.stickers.add(new StickerMainModal(index, ImageManipulation.convertImageToWebP(uri, this.identifier, index, context),
                new ArrayList<String>()));
        stickersAddedIndex++;
    }

    public void addDownloadedSticker(Uri uri, Context context) {
        String index = String.valueOf(stickersAddedIndex);
        this.downloadStickers.add(new StickerMainModal(index, ImageManipulation.convertImageToWebP(uri, this.downloadedIdentifier, index, context),
                new ArrayList<String>()));
        stickersAddedIndex++;
    }

    public void deleteSticker(StickerMainModal sticker) {
        new File(sticker.getUri().getPath()).delete();
        this.stickers.remove(sticker);
    }

    public void deleteDownloadedSticker(StickerMainModal sticker) {
        new File(sticker.getUri().getPath()).delete();
        this.downloadStickers.remove(sticker);
    }

    public StickerMainModal getSticker(int index) {
        return this.stickers.get(index);
    }

    public StickerMainModal getDownloadSticker(int index) {
        return this.downloadStickers.get(index);
    }

    public StickerMainModal getStickerById(int index) {
        for (StickerMainModal s : this.stickers) {
            if (s.getImageFileName().equals(String.valueOf(index))) {
                return s;
            }
        }
        return null;
    }

    public StickerMainModal getDownloadedStickerById(int index) {
        for (StickerMainModal s : this.downloadStickers) {
            if (s.getImageFileName().equals(String.valueOf(index))) {
                return s;
            }
        }
        return null;
    }

    public void setAndroidPlayStoreLink(String androidPlayStoreLink) {
        this.androidPlayStoreLink = androidPlayStoreLink;
    }

    public void setIosAppStoreLink(String iosAppStoreLink) {
        this.iosAppStoreLink = iosAppStoreLink;
    }

    public List<StickerMainModal> getStickers() {
        return stickers;
    }

    public List<StickerMainModal> getDownloadedStickers() {
        return downloadStickers;
    }

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(downloadedIdentifier);
        dest.writeString(name);
        dest.writeString(publisher);
        dest.writeString(trayImageFile);
        dest.writeString(publisherEmail);
        dest.writeString(publisherWebsite);
        dest.writeString(privacyPolicyWebsite);
        dest.writeString(licenseAgreementWebsite);
        dest.writeString(iosAppStoreLink);
        dest.writeTypedList(stickers);
        dest.writeTypedList(downloadStickers);
        dest.writeLong(totalSize);
        dest.writeString(androidPlayStoreLink);
        dest.writeByte((byte) (isWhitelisted ? 1 : 0));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getDownloadedIdentifier() {
        return this.downloadedIdentifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setDownloadedIdentifier(String downloadedIdentifier) {
        this.downloadedIdentifier = downloadedIdentifier;
    }

    public Uri getTrayImageUri() {
        return trayImageUri;
    }

    public Uri getDownloadTrayImageUri() {
        return downloadTrayImageUri;
    }
}
