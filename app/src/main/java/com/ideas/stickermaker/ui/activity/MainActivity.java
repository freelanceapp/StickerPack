package com.ideas.stickermaker.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ideas.stickermaker.BuildConfig;
import com.ideas.stickermaker.NewUserIntroActivity;
import com.ideas.stickermaker.R;
import com.ideas.stickermaker.WhatsAppBasedCode.StickerPackListActivity;
import com.ideas.stickermaker.adapter.CategoryListAdapter;
import com.ideas.stickermaker.constant.Constant;
import com.ideas.stickermaker.modals.StickerModel;
import com.ideas.stickermaker.modals.category.StickerCategoryMainModal;
import com.ideas.stickermaker.modals.category.StickerDatum;
import com.ideas.stickermaker.retrofit_provider.RetrofitService;
import com.ideas.stickermaker.retrofit_provider.WebResponse;
import com.ideas.stickermaker.ui.fragment.Sticker;
import com.ideas.stickermaker.ui.fragment.StickerPack;
import com.ideas.stickermaker.utils.Alerts;
import com.ideas.stickermaker.utils.AppPreference;
import com.ideas.stickermaker.utils.BaseActivity;
import com.ideas.stickermaker.utils.ConnectionDetector;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String EXTRA_STICKERPACK = "stickerpack";
    private final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String path;

    ArrayList<StickerPack> stickerPacks = new ArrayList<>();
    List<Sticker> mStickers;
    ArrayList<StickerModel> stickerModels = new ArrayList<>();

    private List<StickerDatum> categoryList = new ArrayList<>();
    private CategoryListAdapter mAdapter;
    private String strToken = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        strToken = AppPreference.getStringPreference(mContext, Constant.DEVICE_TOKEN_PREF);
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

    private void init() {
        stickerPacks = new ArrayList<>();
        path = getFilesDir() + "/" + "stickers_asset";
        mStickers = new ArrayList<>();
        stickerModels = new ArrayList<>();

        getPermissions();
    }

    private void getPermissions() {
        int perm = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (perm != PackageManager.PERMISSION_GRANTED) {
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
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            case R.id.recommend:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sticker Maker");
                String shareMessage = "\nLet me recommend you this application\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
                break;
            case R.id.rate_us:
                rateUs();
                break;
            case R.id.more_apps:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/")));
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

    private void rateUs() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
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
                    intent.putExtra("position", position);
                    startActivity(intent);
                } else {
                    Alerts.show(mContext, "No sticker pack found. Please contact to Admin");
                }
                break;
        }
    }
}
