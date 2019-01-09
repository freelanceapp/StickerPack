/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.idoideas.stickermaker.WhatsAppBasedCode;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class StickerMainModal implements Parcelable {
    public String imageFileName;
    public List<String> emojis;
    Uri uri;
    long size;

    public StickerMainModal(String imageFileName, List<String> emojis) {
        this.imageFileName = imageFileName;
        this.emojis = emojis;
    }

    public StickerMainModal(String imageFileName, Uri uri, List<String> emojis) {
        this.imageFileName = imageFileName;
        this.emojis = emojis;
        this.uri = uri;
    }

    public StickerMainModal(Parcel in) {
        imageFileName = in.readString();
        emojis = in.createStringArrayList();
        size = in.readLong();
    }


    public static final Creator<StickerMainModal> CREATOR = new Creator<StickerMainModal>() {
        @Override
        public StickerMainModal createFromParcel(Parcel in) {
            return new StickerMainModal(in);
        }

        @Override
        public StickerMainModal[] newArray(int size) {
            return new StickerMainModal[size];
        }
    };

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageFileName);
        dest.writeStringList(emojis);
        dest.writeLong(size);
    }

    public Uri getUri() {
        return uri;
    }

    public String getImageFileName() {
        return imageFileName;
    }
}
