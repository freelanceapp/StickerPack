/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.ideas.stickermaker.WhatsAppBasedCode;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ideas.stickermaker.BuildConfig;
import com.ideas.stickermaker.StickerBook;
import com.ideas.stickermaker.constant.Constant;
import com.ideas.stickermaker.ui.fragment.Sticker;
import com.ideas.stickermaker.ui.fragment.StickerPack;
import com.ideas.stickermaker.utils.AppPreference;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CustomStickerContentProvider extends ContentProvider {

    /**
     * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
     */
    public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
    public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
    public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
    public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
    public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
    public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
    public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
    public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
    public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
    public static final String LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website";

    public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";

    public static final String CONTENT_SCHEME = "content";
    public static Uri AUTHORITY_URI = new Uri.Builder().scheme(CustomStickerContentProvider.CONTENT_SCHEME)
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(CustomStickerContentProvider.METADATA).build();

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static final String METADATA = "metadata";
    private static final int METADATA_CODE = 1;

    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;

    static final String STICKERS = "stickers";
    private static final int STICKERS_CODE = 3;

    static final String STICKERS_ASSET = "stickers_asset";
    private static final int STICKERS_ASSET_CODE = 4;

    private static final int STICKER_PACK_TRAY_ICON_CODE = 5;

    private List<StickerPackModal> stickerPackList;

    /*********************************************************************************/

    public static final String CONTENT_FILE_NAME = "contents.json";

    private static final String TAG = CustomStickerContentProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        final String authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY;
        if (!authority.startsWith(Objects.requireNonNull(getContext()).getPackageName())) {
            throw new IllegalStateException("your authority (" + authority + ") for the content provider should start with your package name: " + getContext().getPackageName());
        }

        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(authority, METADATA, METADATA_CODE);

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);

        //gets the list of stickers for a sticker pack, * respresent the identifier.
        MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE);

        if (AppPreference.getStringPreference(getContext(), Constant.DownloadPack).equals("")) {
            for (StickerPackModal stickerPack : getStickerPackList()) {
                MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + stickerPack.trayImageFile, STICKER_PACK_TRAY_ICON_CODE);
                for (StickerMainModal sticker : stickerPack.getStickers()) {
                    MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + sticker.imageFileName, STICKERS_ASSET_CODE);
                }
            }
        } else {
            Hawk.init(getContext()).build();
            for (StickerPack stickerPack : getDownloadStickerPackList()) {
                Log.e(TAG, "onCreate: " + stickerPack.identifier);
                MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + stickerPack.trayImageFile, STICKER_PACK_TRAY_ICON_CODE);
                if (stickerPack.getStickers() != null) {
                    for (Sticker sticker : stickerPack.getStickers()) {
                        MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + sticker.imageFileName, STICKERS_ASSET_CODE);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int code = MATCHER.match(uri);
        if (AppPreference.getStringPreference(getContext(), Constant.DownloadPack).equals("")) {
            if (StickerBook.getAllStickerPacks().isEmpty()) {
                StickerBook.init(getContext());
            }
            if (code == METADATA_CODE) {
                return getPackForAllStickerPacks(uri);
            } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
                return getCursorForSingleStickerPack(uri);
            } else if (code == STICKERS_CODE) {
                return getStickersForAStickerPack(uri);
            } else {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        } else {
            if (code == METADATA_CODE) {
                return getDownloadPackForAllStickerPacks(uri);
            } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
                return getDownloadCursorForSingleStickerPack(uri);
            } else if (code == STICKERS_CODE) {
                return getDownloadStickersForAStickerPack(uri);
            } else {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) {
        final int matchCode = MATCHER.match(uri);
        final List<String> pathSegments = uri.getPathSegments();
        if (AppPreference.getStringPreference(getContext(), Constant.DownloadPack).equals("")) {
            StickerPackModal csp = StickerBook.getStickerPackByIdWithContext(pathSegments.get(pathSegments.size() - 2), getContext());
            if (csp != null) {
                String filename = pathSegments.get(pathSegments.size() - 1);
                ParcelFileDescriptor pfd = null;
                try {
                    if (filename.equals("trayimage") && csp.getTrayImageUri() != null) {
                        pfd = Objects.requireNonNull(getContext()).getContentResolver().openFileDescriptor(
                                csp.getTrayImageUri(), "r");
                        Log.w("ASSETFILE ACTUAL URI", String.valueOf(csp.getTrayImageUri()) + "");
                    } else {
                        try {
                            pfd = Objects.requireNonNull(getContext()).getContentResolver().openFileDescriptor(
                                    csp.getStickerById(Integer.valueOf(filename)).getUri(), "r");
                        } catch (NullPointerException e) {
                            Log.e("StickerMaker", "WhatsApp tried to access a non existent sticker, id: " + filename);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
            }
        } else {
            return getDownloadImageAsset(uri);
        }
        return getDownloadImageAsset(uri);
    }

    private AssetFileDescriptor getDownloadImageAsset(Uri uri) throws IllegalArgumentException {
        AssetManager am = Objects.requireNonNull(getContext()).getAssets();
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 3) {
            throw new IllegalArgumentException("path segments should be 3, uri is: " + uri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);
        final String identifier = pathSegments.get(pathSegments.size() - 2);
        if (TextUtils.isEmpty(identifier)) {
            throw new IllegalArgumentException("identifier is empty, uri: " + uri);
        }
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("file name is empty, uri: " + uri);
        }
        //making sure the file that is trying to be fetched is in the list of stickers.
        for (StickerPack stickerPack : getDownloadStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                if (fileName.equals(stickerPack.trayImageFile)) {
                    return fetchDownloadFile(uri, am, fileName, identifier);
                } else {
                    for (Sticker sticker : stickerPack.getStickers()) {
                        if (fileName.equals(sticker.imageFileName)) {
                            return fetchDownloadFile(uri, am, fileName, identifier);
                        }
                    }
                }
            }
        }
        return null;
    }

    private AssetFileDescriptor fetchDownloadFile(@NonNull Uri uri, @NonNull AssetManager am, @NonNull String fileName, @NonNull String identifier) {
        try {
            File file;
            if (fileName.endsWith(".png")) {
                file = new File(getContext().getFilesDir() + "/" + "stickers_asset" + "/" + identifier + "/try/", fileName);
            } else {
                file = new File(getContext().getFilesDir() + "/" + "stickers_asset" + "/" + identifier + "/", fileName);
            }
            if (!file.exists()) {
                Log.d("fetFile", "StickerPack dir not found");
            }
            Log.d("fetchFile", "StickerPack " + file.getPath());
            return new AssetFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), 0L, -1L);
        } catch (IOException e) {
            Log.e(Objects.requireNonNull(getContext()).getPackageName(), "IOException when getting asset file, uri:" + uri, e);
            return null;
        }
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        Log.w("TESTING THIS", "IS IT OPENING FILE REGULARLY?");
        return super.openFile(uri, mode);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int matchCode = MATCHER.match(uri);
        switch (matchCode) {
            case METADATA_CODE:
                return "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case METADATA_CODE_FOR_SINGLE_PACK:
                return "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case STICKERS_CODE:
                return "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS;
            case STICKERS_ASSET_CODE:
                return "image/webp";
            case STICKER_PACK_TRAY_ICON_CODE:
                return "image/png";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    public List<StickerPackModal> getStickerPackList() {
        stickerPackList = StickerBook.getAllStickerPacks();
        return stickerPackList;
    }

    public List<StickerPack> getDownloadStickerPackList() {
        return (List) Hawk.get("sticker_packs", new ArrayList<StickerPack>());
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        return getStickerPackInfo(uri, getStickerPackList());
    }

    private Cursor getDownloadPackForAllStickerPacks(@NonNull Uri uri) {
        return getDownloadStickerPackInfo(uri, getDownloadStickerPackList());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        for (StickerPackModal stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }
        return getStickerPackInfo(uri, new ArrayList<StickerPackModal>());
    }

    private Cursor getDownloadCursorForSingleStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        for (StickerPack stickerPack : getDownloadStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                return getDownloadStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }

        return getDownloadStickerPackInfo(uri, new ArrayList<StickerPack>());
    }

    @NonNull
    private Cursor getStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPackModal> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        STICKER_PACK_IDENTIFIER_IN_QUERY,
                        STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY,
                        STICKER_PACK_ICON_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                        IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREENMENT_WEBSITE
                });
        for (StickerPackModal stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.identifier);
            builder.add(stickerPack.name);
            builder.add(stickerPack.publisher);
            builder.add(stickerPack.trayImageFile);
            builder.add(stickerPack.androidPlayStoreLink);
            builder.add(stickerPack.iosAppStoreLink);
            builder.add(stickerPack.publisherEmail);
            builder.add(stickerPack.publisherWebsite);
            builder.add(stickerPack.privacyPolicyWebsite);
            builder.add(stickerPack.licenseAgreementWebsite);
        }
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    private Cursor getDownloadStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        STICKER_PACK_IDENTIFIER_IN_QUERY,
                        STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY,
                        STICKER_PACK_ICON_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                        IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREENMENT_WEBSITE
                });
        for (StickerPack stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.identifier);
            builder.add(stickerPack.name);
            builder.add(stickerPack.publisher);
            builder.add(stickerPack.trayImageFile);
            builder.add(stickerPack.androidPlayStoreLink);
            builder.add(stickerPack.iosAppStoreLink);
            builder.add(stickerPack.publisherEmail);
            builder.add(stickerPack.publisherWebsite);
            builder.add(stickerPack.privacyPolicyWebsite);
            builder.add(stickerPack.licenseAgreementWebsite);
        }
        Log.d(TAG, "getStickerPackInfo: " + stickerPackList.size());
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    private Cursor getStickersForAStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(new String[]{STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY});
        for (StickerPackModal stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                for (StickerMainModal sticker : stickerPack.getStickers()) {
                    cursor.addRow(new Object[]{sticker.imageFileName, TextUtils.join(",", sticker.emojis)});
                }
            }
        }
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    private Cursor getDownloadStickersForAStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(new String[]{STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY});
        for (StickerPack stickerPack : getDownloadStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    cursor.addRow(new Object[]{sticker.imageFileName, TextUtils.join(",", sticker.emojis)});
                }
            }
        }
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported");
    }
}
