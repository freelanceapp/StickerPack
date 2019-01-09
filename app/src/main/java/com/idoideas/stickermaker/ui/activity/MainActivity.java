package com.idoideas.stickermaker.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.idoideas.stickermaker.NewUserIntroActivity;
import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.WhatsAppBasedCode.StickerPackListActivity;
import com.idoideas.stickermaker.adapter.CategoryListAdapter;
import com.idoideas.stickermaker.adapter.StickerAdapter;
import com.idoideas.stickermaker.modals.StickerModel;
import com.idoideas.stickermaker.modals.category.StickerCategoryMainModal;
import com.idoideas.stickermaker.modals.category.StickerDatum;
import com.idoideas.stickermaker.retrofit_provider.RetrofitService;
import com.idoideas.stickermaker.retrofit_provider.WebResponse;
import com.idoideas.stickermaker.ui.fragment.Sticker;
import com.idoideas.stickermaker.ui.fragment.StickerPack;
import com.idoideas.stickermaker.utils.Alerts;
import com.idoideas.stickermaker.utils.AppPreference;
import com.idoideas.stickermaker.utils.BaseActivity;
import com.idoideas.stickermaker.utils.ConnectionDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
    public static final String EXTRA_STICKERPACK = "stickerpack";
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String path;
    StickerAdapter adapter;
    ArrayList<StickerPack> stickerPacks = new ArrayList<>();
    List<Sticker> mStickers;
    ArrayList<StickerModel> stickerModels = new ArrayList<>();

    private List<StickerDatum> categoryList = new ArrayList<>();
    private CategoryListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        cd = new ConnectionDetector(mContext);
        retrofitRxClient = RetrofitService.getRxClient();
        retrofitApiClient = RetrofitService.getRetrofit();

        init();
        initNavigationDrawer();
        setCategoryRecyclerView();
    }

    private void initNavigationDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color_a));

        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!AppPreference.getBooleanPreference(mContext, "is_login")) {
            noticeDialog();
        }
        if (toShowIntro()) {
            startActivityForResult(new Intent(this, NewUserIntroActivity.class), 1114);
        }

        ((LinearLayout) findViewById(R.id.llAddSticker)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, StickerPackListActivity.class));
            }
        });
    }

    private void init(){
        stickerPacks = new ArrayList<>();
        path = getFilesDir() + "/" + "stickers_asset";
        mStickers = new ArrayList<>();
        stickerModels = new ArrayList<>();

        getPermissions();
    }

    public static void SaveImage(Bitmap finalBitmap, String name, String identifier) {
        String root = path + "/" + identifier;
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = name;
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveTryImage(Bitmap finalBitmap, String name, String identifier) {
        String root = path + "/" + identifier;
        File myDir = new File(root + "/" + "try");
        myDir.mkdirs();
        String fname = name.replace(".png", "").replace(" ", "_") + ".png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 40, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPermissions() {
        int perm = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (perm != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }
    }

    private void noticeDialog() {
        Dialog dialogReview = new Dialog(mContext);
        dialogReview.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogReview.setContentView(R.layout.dialog_notice);

        dialogReview.setCanceledOnTouchOutside(true);
        dialogReview.setCancelable(true);
        if (dialogReview.getWindow() != null)
            dialogReview.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogReview.findViewById(R.id.tvOk).setOnClickListener(v -> {
            AppPreference.setBooleanPreference(mContext, "is_login", true);
            dialogReview.dismiss();
        });

        Window window = dialogReview.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialogReview.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.setting:
                Toast.makeText(getApplicationContext(), "Setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.recommend:
                Toast.makeText(getApplicationContext(), "Recommended to friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rate_us:
                Toast.makeText(getApplicationContext(), "Rate us", Toast.LENGTH_SHORT).show();
                break;
            case R.id.more_apps:
                Toast.makeText(getApplicationContext(), "More App", Toast.LENGTH_SHORT).show();
                break;
            case R.id.license:
                setIntentData("3", "License");
                break;
            case R.id.notice:
                setIntentData("4", "DMCA Notice");
                break;
            case R.id.privacy:
                setIntentData("2", "Privacy Policy");
                break;
            case R.id.about:
                setIntentData("1", "About us");
                break;
            default:
                return true;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setIntentData(String strId, String strTitle) {
        Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
        intent.putExtra("id", strId);
        intent.putExtra("title", strTitle);
        startActivity(intent);
    }

    private void makeIntroNotRunAgain() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean("isAlreadyShown", false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("isAlreadyShown", false);
            edit.commit();
        }
    }

    private boolean toShowIntro() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return prefs.getBoolean("isAlreadyShown", true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1114) {
            makeIntroNotRunAgain();
        }
    }

    /*
     *  Category api
     * */
    private void setCategoryRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerviewStickerCategory);
        mAdapter = new CategoryListAdapter(this, categoryList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
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
                    mAdapter.notifyDataSetChanged();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relativeItem:
                int position = Integer.parseInt(v.getTag().toString());
                StickerDatum datum = categoryList.get(position);
                if (datum.getSubcategory().size() > 0) {
                    Intent intent = new Intent(mContext, SubCategoryActivity.class);
                    intent.putExtra("sub_category", (Parcelable) datum);
                    startActivity(intent);
                } else {
                    Alerts.show(mContext, "No sticker pack found. Please contact to Admin");
                }
                break;
        }
    }
}
