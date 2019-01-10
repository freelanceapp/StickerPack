/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.idoideas.stickermaker.WhatsAppBasedCode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idoideas.stickermaker.BuildConfig;
import com.idoideas.stickermaker.DataArchiver;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.StickerBook;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.modals.category.StickerList;
import com.idoideas.stickermaker.utils.AppPreference;
import com.idoideas.stickermaker.utils.AppProgressDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StickerPackDetailsActivity extends BaseActivity {

    /**
     * Do not change below values of below 3 lines as this is also used by WhatsApp
     */
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";

    public static final int ADD_PACK = 200;
    public static final String EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website";
    public static final String EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email";
    public static final String EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy";
    public static final String EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon";
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    private static final String TAG = "StickerPackDetails";

    public static StickerPackDetailsActivity stickerPackDetailsActivity;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private int numColumns;
    private View addButton;
    private View alreadyAddedText;
    public StickerPackModal stickerPack;
    private View divider;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private LinearLayout shareButton;
    private LinearLayout deleteButton;
    public static Uri urlURI;
    public static Context mContext;
    public List<StickerList> stickerLists = new ArrayList<>();
    public static Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sticker_pack_details);
        stickerPackDetailsActivity = this;
        mContext = this;
        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        init();
        getStickerIntent();
    }

    private void init() {
        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false);
        String strPackId = getIntent().getStringExtra(EXTRA_STICKER_PACK_DATA);
        stickerPack = StickerBook.getStickerPackById(strPackId);
        TextView packNameTextView = findViewById(R.id.pack_name);
        TextView packPublisherTextView = findViewById(R.id.author);
        ImageView packTrayIcon = findViewById(R.id.tray_image);

        addButton = findViewById(R.id.add_to_whatsapp_button);
        shareButton = findViewById(R.id.share_pack_button);
        deleteButton = findViewById(R.id.delete_pack_button);
        alreadyAddedText = findViewById(R.id.already_added_text);
        layoutManager = new GridLayoutManager(this, 4);
        recyclerView = findViewById(R.id.sticker_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
        recyclerView.addOnScrollListener(dividerScrollListener);
        divider = findViewById(R.id.divider);
        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size), getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack);
            recyclerView.setAdapter(stickerPreviewAdapter);
        }
        packNameTextView.setText(stickerPack.name);
        packPublisherTextView.setText(stickerPack.publisher);
        packTrayIcon.setImageURI(stickerPack.getTrayImageUri());
        findViewById(R.id.add_sticker_to_pack).setVisibility(View.VISIBLE);
        findViewById(R.id.add_sticker_to_pack).setOnClickListener(view -> openFile());

        addButton.setOnClickListener(v -> {
            AppPreference.setStringPreference(getApplicationContext(), Constant.DownloadPack, "");
            if (stickerPack.getStickers().size() >= 3) {
                StickerPackDetailsActivity.this.addStickerPackToWhatsApp(stickerPack);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(StickerPackDetailsActivity.this)
                        .setNegativeButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).create();
                alertDialog.setTitle("Invalid Action");
                alertDialog.setMessage("In order to be applied to WhatsApp, the sticker pack must have at least 3 stickers. Please add more stickers first.");
                alertDialog.show();
            }
        });

        shareButton.setOnClickListener(view -> DataArchiver.createZipFileFromStickerPack(stickerPack, StickerPackDetailsActivity.this));

        deleteButton.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(StickerPackDetailsActivity.this)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            StickerBook.deleteStickerPackById(stickerPack.getIdentifier());
                            finish();
                            Intent intent = new Intent(StickerPackDetailsActivity.this, StickerPackListActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(StickerPackDetailsActivity.this, "StickerList Pack deleted", Toast.LENGTH_SHORT).show();
                        }
                    }).create();
            alertDialog.setTitle("Are you sure?");
            alertDialog.setMessage("Deleting this package will also remove it from your WhatsApp app.");
            alertDialog.show();
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setTitle(showUpButton ? R.string.title_activity_sticker_pack_details_multiple_pack : R.string.title_activity_sticker_pack_details_single_pack);
        }
    }

    private void getStickerIntent() {
        if (getIntent().getStringExtra("from") != null) {
            if (getIntent().getStringExtra("from").equals("download")) {
                dialog = new Dialog(mContext);
                AppProgressDialog.show(dialog);
                stickerLists = getIntent().getParcelableArrayListExtra("sticker_list");
                for (int i = 0; i < stickerLists.size(); i++) {
                    String strImgUrl = Constant.IMAGE_URL + stickerLists.get(i).getStickers();
                    new MyAsyncTask().execute(strImgUrl);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppProgressDialog.hide(dialog);
                        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
                        recyclerView.addOnScrollListener(dividerScrollListener);
                        stickerPreviewAdapter.notifyDataSetChanged();
                        init();
                    }
                }, 500);
            }
        }
    }

    /************************************************************************************/
    private static class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            urlURI = convertBitmapToUri(result, mContext);
            stickerPackDetailsActivity.stickerPack.addSticker(urlURI, mContext);
        }
    }

    public static Uri convertBitmapToUri(Bitmap bitmap, Context context) {
        String newId = UUID.randomUUID().toString();
        dirChecker(context.getFilesDir() + "/" + newId);
        String path = context.getFilesDir() + "/" + newId + "/" + newId + ".webp";
        int quality = 100;
        FileOutputStream outs = null;
        try {
            outs = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, 420, 420, true);
        int byteArrayLength = 100000;
        ByteArrayOutputStream bos = null;

        while ((byteArrayLength / 1000) >= 100) {
            bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, quality, bos);
            byteArrayLength = bos.toByteArray().length;
            quality -= 10;
        }
        try {
            outs.write(bos.toByteArray());
            outs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(new File(path));
    }

    private static void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        Log.e("StickerPath:-", f + "");
    }

    /************************************************************************************/
    private void launchInfoActivity(String publisherWebsite, String publisherEmail, String privacyPolicyWebsite, String trayIconUriString) {
        Intent intent = new Intent(StickerPackDetailsActivity.this, StickerPackInfoActivity.class);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, stickerPack.identifier);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_WEBSITE, publisherWebsite);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_EMAIL, publisherEmail);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_PRIVACY_POLICY, privacyPolicyWebsite);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_TRAY_ICON, stickerPack.getTrayImageUri().toString());
        startActivity(intent);
    }

    private void openFile() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setType("image/*");
        startActivityForResult(i, 3000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info && stickerPack != null) {
            final String publisherWebsite = stickerPack.publisherWebsite;
            final String publisherEmail = stickerPack.publisherEmail;
            final String privacyPolicyWebsite = stickerPack.privacyPolicyWebsite;
            Uri trayIconUri = stickerPack.getTrayImageUri();
            launchInfoActivity(publisherWebsite, publisherEmail, privacyPolicyWebsite, trayIconUri.toString());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addStickerPackToWhatsApp(StickerPackModal sp) {
        Intent intent = new Intent();
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        Log.w("IS IT A NEW IDENTIFIER?", sp.getIdentifier());
        intent.putExtra(EXTRA_STICKER_PACK_ID, sp.getIdentifier());
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        intent.putExtra(EXTRA_STICKER_PACK_NAME, sp.getName());
        try {
            startActivityForResult(intent, 200);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED && data != null) {
                final String validationError = data.getStringExtra("validation_error");
                if (validationError != null) {
                    if (BuildConfig.DEBUG) {
                        //validation error should be shown to developer only, not users.
                        MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(getSupportFragmentManager(), "validation error");
                    }
                    Log.e(TAG, "Validation failed:" + validationError);
                }
            }
        } else if (requestCode == 3000) {
            if (data != null) {
                if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item path = clipData.getItemAt(i);
                        Uri uri = path.getUri();
                        stickerPack.addSticker(uri, this);
                    }
                } else {
                    Uri uri = data.getData();
                    stickerPack.addSticker(uri, this);
                }
                finish();
                startActivity(getIntent());
            }
        }
    }

    private final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setNumColumns(recyclerView.getWidth() / recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
        }
    };

    private void setNumColumns(int numColumns) {
        if (this.numColumns != numColumns) {
            layoutManager.setSpanCount(numColumns);
            this.numColumns = numColumns;
            if (stickerPreviewAdapter != null) {
                stickerPreviewAdapter.notifyDataSetChanged();
            }
        }
    }

    private final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            updateDivider(recyclerView);
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateDivider(recyclerView);
        }

        private void updateDivider(RecyclerView recyclerView) {
            boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
            if (divider != null) {
                divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPack);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void updateAddUI(Boolean isWhitelisted) {
        if (isWhitelisted) {
            addButton.setVisibility(View.GONE);
            alreadyAddedText.setVisibility(View.VISIBLE);
        } else {
            addButton.setVisibility(View.VISIBLE);
            alreadyAddedText.setVisibility(View.GONE);
        }
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPackModal, Void, Boolean> {
        private final WeakReference<StickerPackDetailsActivity> stickerPackDetailsActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackDetailsActivity stickerPackListActivity) {
            this.stickerPackDetailsActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final Boolean doInBackground(StickerPackModal... stickerPacks) {
            StickerPackModal stickerPack = stickerPacks[0];
            final StickerPackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            //noinspection SimplifiableIfStatement
            if (stickerPackDetailsActivity == null) {
                return false;
            }
            return WhitelistCheck.isWhitelisted(stickerPackDetailsActivity, stickerPack.identifier);
        }

        @Override
        protected void onPostExecute(Boolean isWhitelisted) {
            final StickerPackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            if (stickerPackDetailsActivity != null) {
                stickerPackDetailsActivity.updateAddUI(isWhitelisted);
            }
        }
    }
}
