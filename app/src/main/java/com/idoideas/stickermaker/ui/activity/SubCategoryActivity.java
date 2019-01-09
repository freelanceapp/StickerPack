package com.idoideas.stickermaker.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.WhatsAppBasedCode.StickerPackListActivity;
import com.idoideas.stickermaker.adapter.StickerAdapter;
import com.idoideas.stickermaker.adapter.SubCategoryListAdapter;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.modals.category.StickerDatum;
import com.idoideas.stickermaker.modals.category.StickerSubcategory;
import com.idoideas.stickermaker.retrofit_provider.RetrofitService;
import com.idoideas.stickermaker.ui.fragment.Sticker;
import com.idoideas.stickermaker.ui.fragment.StickerPack;
import com.idoideas.stickermaker.utils.Alerts;
import com.idoideas.stickermaker.utils.AppPreference;
import com.idoideas.stickermaker.utils.AppProgressDialog;
import com.idoideas.stickermaker.utils.BaseActivity;
import com.idoideas.stickermaker.utils.ConnectionDetector;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubCategoryActivity extends BaseActivity implements View.OnClickListener {

    public static Context mContext;
    private StickerDatum stickerDatum;
    private SubCategoryListAdapter subCategoryListAdapter;
    private List<StickerSubcategory> subCatList = new ArrayList<>();
    public static SubCategoryActivity subCategoryActivity;

    public static String strDownloadPackId = "", strPackName = "";
    public static String strImgUrl = "";
    public static Uri urlURI;
    public static String newId = "";
    public static Dialog dialog;

    List<Sticker> mStickers;
    List<StickerPack> stickerPacks = new ArrayList<>();
    List<String> mEmojis;
    StickerAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

        subCategoryActivity = this;
        mContext = this;
        cd = new ConnectionDetector(mContext);
        retrofitRxClient = RetrofitService.getRxClient();
        retrofitApiClient = RetrofitService.getRetrofit();

        init();
    }

    private void init() {
        if (getIntent() == null)
            return;

        stickerDatum = getIntent().getParcelableExtra("sub_category");
        subCatList.addAll(stickerDatum.getSubcategory());

        ((ImageView) findViewById(R.id.imgBack)).setOnClickListener(this);
        ((TextView) findViewById(R.id.tvTitleToolbar)).setText(stickerDatum.getCategoryName());
        setStickerAdapter();
    }

    private void setStickerAdapter() {
        stickerPacks = new ArrayList<>();
        mStickers = new ArrayList<>();
        mEmojis = new ArrayList<>();
        mEmojis.add("");

        recyclerView = findViewById(R.id.recyclerviewSubCategory);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        subCategoryListAdapter = new SubCategoryListAdapter(mContext, subCatList, this);
        recyclerView.setAdapter(subCategoryListAdapter);
        subCategoryListAdapter.notifyDataSetChanged();
    }

    private void setList() {
        if (subCatList.size() > 0) {
            for (int i = 0; i < subCatList.size(); i++) {
                String strIcon = "";
                if (subCatList.get(i).getSticker().size() > 0) {
                    strIcon = Constant.IMAGE_URL + subCatList.get(i).getSticker().get(0).getStickers();
                } else {
                    strIcon = Constant.IMAGE_URL + stickerDatum.getCategoryImage();
                }
                stickerPacks.add(new StickerPack(
                        subCatList.get(i).getId(),
                        subCatList.get(i).getSubcatName(),
                        "StickerMaker", getLastBitFromUrl(strIcon),
                        "rupesh.infobite@gmail.com", "", "",
                        ""
                ));

                for (int p = 0; p < subCatList.get(i).getSticker().size(); p++) {
                    mStickers.add(new Sticker(
                            getLastBitFromUrl(subCatList.get(i).getSticker().get(p).getStickers().replace(".png", ".webp")
                                    .replace(".jpeg", ".webp").replace(".png", ".webp")), mEmojis));
                }
                Hawk.put(subCatList.get(i).getId(), mStickers);
                stickerPacks.get(i).setStickers(Hawk.get(subCatList.get(i).getId(), new ArrayList<Sticker>()));
                mStickers.clear();
            }
            Hawk.put("sticker_packs", stickerPacks);
            adapter = new StickerAdapter(this, stickerPacks);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private static String getLastBitFromUrl(final String url) {
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.imgAdd:
                int pos = Integer.parseInt(v.getTag().toString());
                newId = UUID.randomUUID().toString();
                StickerSubcategory stickerSubcategory = subCatList.get(pos);
                strDownloadPackId = stickerSubcategory.getId() + stickerSubcategory.getSubcatName();
                strPackName = stickerSubcategory.getSubcatName();
                dialog = new Dialog(mContext);
                AppProgressDialog.show(dialog);
                if (AppPreference.getStringPreference(mContext, strDownloadPackId) != null) {
                    if (AppPreference.getStringPreference(mContext, strDownloadPackId).equalsIgnoreCase(strDownloadPackId)) {
                        Alerts.show(mContext, "Already downloaded!!!");
                        if (dialog != null)
                            AppProgressDialog.hide(dialog);
                    } else {
                        if (stickerSubcategory.getSticker().size() > 0) {
                            AppPreference.setStringPreference(mContext, strDownloadPackId, strDownloadPackId);
                            strImgUrl = Constant.IMAGE_URL + stickerSubcategory.getSticker().get(0).getStickers();
                            try {
                                new MyAsyncTask().execute(strImgUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (dialog != null)
                                AppProgressDialog.hide(dialog);
                            Alerts.show(mContext, "No sticker found. Please contact to Admin!!!");
                        }
                    }
                } else {
                    if (stickerSubcategory.getSticker().size() > 0) {
                        AppPreference.setStringPreference(mContext, strDownloadPackId, strDownloadPackId);
                        strImgUrl = Constant.IMAGE_URL + stickerSubcategory.getSticker().get(0).getStickers();
                        try {
                            new MyAsyncTask().execute(strImgUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (dialog != null)
                            AppProgressDialog.hide(dialog);
                        Alerts.show(mContext, "No sticker found. Please contact to Admin!!!");
                    }
                }
                break;
            case R.id.linearPack:
                int position = Integer.parseInt(v.getTag().toString());
                StickerSubcategory datum = subCatList.get(position);
                if (datum.getSticker().size() > 0) {
                    Intent intent = new Intent(mContext, StickerDetailsActivity.class);
                    intent.putExtra("sub_category", (Parcelable) datum);
                    startActivity(intent);
                } else {
                    Alerts.show(mContext, "No sticker found. Please contact to admin!!!");
                }
                break;
        }
    }

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
            urlURI = convertIconTrayToWebP(result, mContext);

            subCategoryActivity.sendDataIntent(strPackName, newId, urlURI.toString());
        }
    }

    public void sendDataIntent(String strName, String strId, String strUri) {
        Intent intent = new Intent(mContext, StickerPackListActivity.class);
        intent.putExtra("pack_name", strName);
        intent.putExtra("pack_id", strId);
        intent.putExtra("pack_uri", strUri);
        startActivity(intent);
    }

    public static Uri convertIconTrayToWebP(Bitmap bitmap, Context context) {
        dirChecker(context.getFilesDir() + "/" + newId);
        String path = context.getFilesDir() + "/" + newId + "/" + newId + "-" + "trayImage" + ".webp";

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out); //100-best quality
        try {
            out.close();
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

}
