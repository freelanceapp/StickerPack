package com.ideas.stickermaker.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ideas.stickermaker.R;
import com.ideas.stickermaker.adapter.StickerListAdapter;
import com.ideas.stickermaker.constant.Constant;
import com.ideas.stickermaker.modals.category.StickerList;
import com.ideas.stickermaker.modals.category.StickerSubcategory;
import com.ideas.stickermaker.retrofit_provider.RetrofitService;
import com.ideas.stickermaker.retrofit_provider.WebResponse;
import com.ideas.stickermaker.utils.Alerts;
import com.ideas.stickermaker.utils.AppPreference;
import com.ideas.stickermaker.utils.BaseActivity;
import com.ideas.stickermaker.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class PackDetailActivity extends BaseActivity implements View.OnClickListener {

    private StickerSubcategory stickerSubcategory;
    private StickerListAdapter stickerListAdapter;
    private List<StickerList> stickerLists = new ArrayList<>();
    private String strM_Id = "", strS_Id = "";
    private String strLikeId = "", strTotalLike = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack_detail);

        mContext = this;
        cd = new ConnectionDetector(mContext);
        retrofitRxClient = RetrofitService.getRxClient();
        retrofitApiClient = RetrofitService.getRetrofit();
        init();
    }

    private void init() {
        if (getIntent() == null)
            return;
        ((ImageView) findViewById(R.id.imgLike)).setOnClickListener(this);
        stickerSubcategory = getIntent().getParcelableExtra("sub_category");
        strS_Id = stickerSubcategory.getId();
        stickerPackApi();
    }

    private void setSubCategoryRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerviewSticker);
        stickerListAdapter = new StickerListAdapter(this, stickerLists, this, "vertical");
        RecyclerView.LayoutManager mLayoutManager = (new GridLayoutManager(mContext, 5));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(stickerListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.imgLike:
                strLikeId = AppPreference.getStringPreference(mContext, strM_Id + strS_Id);
                if (!strLikeId.equals(strM_Id + strS_Id)) {
                    int likeCount = Integer.parseInt(strTotalLike);
                    likeCount = likeCount + 1;
                    ((TextView) findViewById(R.id.tvLikesCount)).setText(likeCount + " " + "Likes");
                    AppPreference.setStringPreference(mContext, strM_Id + strS_Id, strM_Id + strS_Id);
                    ((ImageView) findViewById(R.id.imgLike)).setImageResource((R.drawable.ic_favorite_fill));
                    likesApi();
                }
                break;
        }
    }

    private void likesApi() {
        if (cd.isNetworkAvailable()) {
            RetrofitService.getContentData(new Dialog(mContext), retrofitApiClient.sendLike(strM_Id, strS_Id), new WebResponse() {
                @Override
                public void onResponseSuccess(Response<?> result) {
                    ResponseBody responseBody = (ResponseBody) result.body();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        //Alerts.show(mContext, jsonObject.toString());
                        stickerPackApi();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
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

    private void stickerPackApi() {
        if (cd.isNetworkAvailable()) {
            RetrofitService.getStickerPackData(new Dialog(mContext), retrofitApiClient.stickerPackData(strS_Id), new WebResponse() {
                @Override
                public void onResponseSuccess(Response<?> result) {
                    stickerSubcategory = (StickerSubcategory) result.body();
                    if (stickerSubcategory == null)
                        return;
                    getInit();
                }

                @Override
                public void onResponseFailed(String error) {
                    Alerts.show(mContext, error);
                }
            });
        }
    }

    private void getInit() {
        stickerLists.clear();
        stickerLists.addAll(stickerSubcategory.getSticker());
        strM_Id = stickerSubcategory.getMainCatId();
        strS_Id = stickerSubcategory.getId();
        strTotalLike = stickerSubcategory.getLikes();
        String strDownload = stickerSubcategory.getDownloads();
        if (strTotalLike.isEmpty()) {
            strTotalLike = "0";
        }

        if (strDownload == null || strDownload.isEmpty()) {
            strDownload = "0";
        }

        ((TextView) findViewById(R.id.tvDownloadCount)).setText(strDownload + " " + "downloads");
        ((TextView) findViewById(R.id.tvLikesCount)).setText(strTotalLike + " " + "Likes");

        strLikeId = AppPreference.getStringPreference(mContext, strM_Id + strS_Id);
        if (strLikeId.equals(strM_Id + strS_Id)) {
            ((ImageView) findViewById(R.id.imgLike)).setImageResource((R.drawable.ic_favorite_fill));
        } else {
            ((ImageView) findViewById(R.id.imgLike)).setImageResource((R.drawable.ic_favorite_outline));
        }

        Glide.with(mContext)
                .load(Constant.IMAGE_URL + stickerSubcategory.getSticker().get(0).getStickers())
                .into(((ImageView) findViewById(R.id.imgIcon)));

        ((ImageView) findViewById(R.id.imgBack)).setOnClickListener(this);
        ((TextView) findViewById(R.id.tvTitleToolbar)).setText(stickerSubcategory.getSubcatName());
        ((TextView) findViewById(R.id.tvTitle)).setText(stickerSubcategory.getSubcatName());
        setSubCategoryRecyclerView();
    }
}
