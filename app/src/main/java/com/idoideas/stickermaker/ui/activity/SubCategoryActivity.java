package com.idoideas.stickermaker.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.adapter.StickerAdapter;
import com.idoideas.stickermaker.adapter.SubCategoryListAdapter;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.modals.category.StickerDatum;
import com.idoideas.stickermaker.modals.category.StickerSubcategory;
import com.idoideas.stickermaker.retrofit_provider.RetrofitService;
import com.idoideas.stickermaker.ui.fragment.Sticker;
import com.idoideas.stickermaker.ui.fragment.StickerPack;
import com.idoideas.stickermaker.utils.Alerts;
import com.idoideas.stickermaker.utils.BaseActivity;
import com.idoideas.stickermaker.utils.ConnectionDetector;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryActivity extends BaseActivity implements View.OnClickListener {

    public static Context mContext;
    private StickerDatum stickerDatum;
    private SubCategoryListAdapter subCategoryListAdapter;
    private List<StickerSubcategory> subCatList = new ArrayList<>();
    public static SubCategoryActivity subCategoryActivity;

    List<Sticker> mStickers;
    List<StickerPack> stickerPacks = new ArrayList<>();
    List<String> mEmojis;
    StickerAdapter adapter;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerviewSubCategory);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);

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
}
