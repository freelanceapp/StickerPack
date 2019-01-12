package com.ideas.stickermaker.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ideas.stickermaker.BuildConfig;
import com.ideas.stickermaker.R;
import com.ideas.stickermaker.utils.Alerts;
import com.ideas.stickermaker.utils.BaseActivity;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    ImageView backBtn;
    TextView aboutUsBtn, policyBtn, moreApplicationBtn, friendsBtn, versionBtn, updateBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        aboutUsBtn = (TextView) findViewById(R.id.aboutUsBtn);
        policyBtn = (TextView) findViewById(R.id.policyBtn);
        moreApplicationBtn = (TextView) findViewById(R.id.moreApplicationBtn);
        friendsBtn = (TextView) findViewById(R.id.friendsBtn);
        versionBtn = (TextView) findViewById(R.id.versionBtn);
        updateBtn = (TextView) findViewById(R.id.updateBtn);
        aboutUsBtn.setOnClickListener(this);
        policyBtn.setOnClickListener(this);
        moreApplicationBtn.setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.llRateUs)).setOnClickListener(this);
        friendsBtn.setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.llRecommend)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.llFeedback)).setOnClickListener(this);
        versionBtn.setOnClickListener(this);
        updateBtn.setOnClickListener(this);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.aboutUsBtn:
                setIntentData("1", "About us");
                break;
            case R.id.policyBtn:
                setIntentData("2", "Privacy Policy");
                break;
            case R.id.moreApplicationBtn:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/")));
                break;
            case R.id.llRateUs:
                rateUs();
                break;
            case R.id.llRecommend:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sticker Maker");
                String shareMessage = "\nLet me recommend you this application\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
                break;
            case R.id.llFeedback:
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    //intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"example.yahoo.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Sticker Maker 1.0");
                    intent.putExtra(Intent.EXTRA_TEXT, "Please write your problem to us we will try our best to solve it..");
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Alerts.show(mContext, "There are no email client installed on your device.");
                }
                break;
            case R.id.versionBtn:
                break;
            case R.id.updateBtn:
                rateUs();
                break;
        }
    }

    private void setIntentData(String strId, String strTitle) {
        Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
        intent.putExtra("id", strId);
        intent.putExtra("title", strTitle);
        startActivity(intent);
    }

    private void rateUs() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
