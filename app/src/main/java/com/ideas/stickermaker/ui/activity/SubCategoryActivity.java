package com.ideas.stickermaker.ui.activity;

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

import com.ideas.stickermaker.R;
import com.ideas.stickermaker.WhatsAppBasedCode.StickerPackListActivity;
import com.ideas.stickermaker.adapter.SubCategoryListAdapter;
import com.ideas.stickermaker.constant.Constant;
import com.ideas.stickermaker.modals.category.StickerCategoryMainModal;
import com.ideas.stickermaker.modals.category.StickerDatum;
import com.ideas.stickermaker.modals.category.StickerList;
import com.ideas.stickermaker.modals.category.StickerSubcategory;
import com.ideas.stickermaker.retrofit_provider.RetrofitService;
import com.ideas.stickermaker.retrofit_provider.WebResponse;
import com.ideas.stickermaker.ui.fragment.Sticker;
import com.ideas.stickermaker.ui.fragment.StickerPack;
import com.ideas.stickermaker.utils.Alerts;
import com.ideas.stickermaker.utils.AppPreference;
import com.ideas.stickermaker.utils.AppProgressDialog;
import com.ideas.stickermaker.utils.BaseActivity;
import com.ideas.stickermaker.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

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

import okhttp3.ResponseBody;
import retrofit2.Response;

public class SubCategoryActivity extends BaseActivity implements View.OnClickListener {

    public static Context mContext;
    private StickerDatum stickerDatum;
    private SubCategoryListAdapter subCategoryListAdapter;
    private List<StickerSubcategory> subCatList = new ArrayList<>();
    public static SubCategoryActivity subCategoryActivity;
    public List<StickerList> stickerLists = new ArrayList<>();

    public static String strDownloadPackId = "", strPackName = "";
    public static String strImgUrl = "";
    public static Uri urlURI;
    public static String newId = "";
    public static Dialog dialog;
    private int position = 0;
    private List<StickerDatum> categoryList = new ArrayList<>();
    private String strM_Id = "", strS_Id = "";

    List<Sticker> mStickers;
    List<StickerPack> stickerPacks = new ArrayList<>();
    List<String> mEmojis;
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

        position = getIntent().getIntExtra("position", 0);
        categoryApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        categoryApi();
    }

    private void categoryApi() {
        if (cd.isNetworkAvailable()) {
            RetrofitService.getCategoryData(new Dialog(mContext), retrofitApiClient.categoryList(), new WebResponse() {
                @Override
                public void onResponseSuccess(Response<?> result) {
                    StickerCategoryMainModal categoryMainModal = (StickerCategoryMainModal) result.body();
                    categoryList.clear();
                    if (categoryMainModal == null)
                        return;
                    categoryList.addAll(categoryMainModal.getData());
                    getSubCategory();
                }

                @Override
                public void onResponseFailed(String error) {
                    Alerts.show(mContext, error);
                }
            });
        } else {
            cd.show(mContext);
        }
    }

    private void getSubCategory() {
        subCatList.clear();
        stickerDatum = categoryList.get(position);
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
                strS_Id = stickerSubcategory.getId();
                strM_Id = stickerSubcategory.getMainCatId();
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
                            stickerLists.addAll(stickerSubcategory.getSticker());
                            AppPreference.setStringPreference(mContext, strDownloadPackId, strDownloadPackId);
                            strImgUrl = Constant.IMAGE_URL + stickerSubcategory.getSticker().get(0).getStickers();
                            try {
                                downloadApi();
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
                            downloadApi();
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
                    Intent intent = new Intent(mContext, PackDetailActivity.class);
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
        AppProgressDialog.hide(dialog);
        Intent intent = new Intent(mContext, StickerPackListActivity.class);
        intent.putExtra("pack_name", strName);
        intent.putExtra("pack_id", strId);
        intent.putExtra("pack_uri", strUri);
        intent.putParcelableArrayListExtra("sticker_list", (ArrayList<? extends Parcelable>) stickerLists);
        startActivity(intent);
        finish();
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

    private void downloadApi() {
        if (cd.isNetworkAvailable()) {
            RetrofitService.getContentData(new Dialog(mContext), retrofitApiClient.downloadCount(strM_Id, strS_Id), new WebResponse() {
                @Override
                public void onResponseSuccess(Response<?> result) {
                    ResponseBody responseBody = (ResponseBody) result.body();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        //Alerts.show(mContext, jsonObject.toString());
                        categoryApi();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onResponseFailed(String error) {
                    Alerts.show(mContext, error);
                }
            });
        }
    }
}
