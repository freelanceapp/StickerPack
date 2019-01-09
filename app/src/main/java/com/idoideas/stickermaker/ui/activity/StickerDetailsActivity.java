package com.idoideas.stickermaker.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.idoideas.stickermaker.BuildConfig;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.adapter.StickerDetailsAdapter;
import com.idoideas.stickermaker.constant.Constant;
import com.idoideas.stickermaker.ui.fragment.Sticker;
import com.idoideas.stickermaker.ui.fragment.StickerPack;
import com.idoideas.stickermaker.utils.AppPreference;
import com.idoideas.stickermaker.utils.BaseActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.idoideas.stickermaker.ui.activity.MainActivity.EXTRA_STICKER_PACK_AUTHORITY;
import static com.idoideas.stickermaker.ui.activity.MainActivity.EXTRA_STICKER_PACK_ID;
import static com.idoideas.stickermaker.ui.activity.MainActivity.EXTRA_STICKER_PACK_NAME;

public class StickerDetailsActivity extends BaseActivity implements View.OnClickListener {

    private static final int ADD_PACK = 200;
    private static final String TAG = StickerDetailsActivity.class.getSimpleName();
    StickerPack stickerPack;
    StickerDetailsAdapter adapter;
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<Sticker> stickers;
    ArrayList<String> strings;
    public static String path;
    Button addtowhatsapp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_details);
        stickerPack = getIntent().getParcelableExtra(MainActivity.EXTRA_STICKERPACK);
        toolbar = findViewById(R.id.toolbar);
        addtowhatsapp = findViewById(R.id.add_to_whatsapp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(stickerPack.name);
        getSupportActionBar().setSubtitle(stickerPack.publisher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.recyclerView);
        stickers = stickerPack.getStickers();
        strings = new ArrayList<>();
        path = getFilesDir() + "/" + "stickers_asset" + "/" + stickerPack.identifier + "/";
        File file = new File(path + stickers.get(0).imageFileName);

        for (Sticker s : stickers) {
            if (!file.exists()) {
                strings.add(s.imageFileName);
            } else {
                strings.add(path + s.imageFileName);
            }
        }
        adapter = new StickerDetailsAdapter(strings, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        addtowhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppPreference.setStringPreference(getApplicationContext(), Constant.DownloadPack, "download");
                Intent intent = new Intent();
                intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
                intent.putExtra(EXTRA_STICKER_PACK_ID, stickerPack.identifier);
                intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY);
                intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPack.name);
                try {
                    startActivityForResult(intent, ADD_PACK);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(StickerDetailsActivity.this, "error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.linearPack:

                break;
        }
    }
}
