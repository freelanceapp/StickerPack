package com.idoideas.stickermaker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idoideas.stickermaker.R;
import com.idoideas.stickermaker.utils.BaseActivity;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    ImageView backBtn;
    TextView aboutUsBtn, policyBtn, moreApplicationBtn, ratingBtn, friendsBtn, feedbackBtn, versionBtn , updateBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        aboutUsBtn = (TextView)findViewById(R.id.aboutUsBtn);
        policyBtn = (TextView)findViewById(R.id.policyBtn);
        moreApplicationBtn = (TextView)findViewById(R.id.moreApplicationBtn);
        ratingBtn = (TextView)findViewById(R.id.ratingBtn);
        friendsBtn = (TextView)findViewById(R.id.friendsBtn);
        feedbackBtn = (TextView)findViewById(R.id.feedbackBtn);
        versionBtn = (TextView)findViewById(R.id.versionBtn);
        updateBtn = (TextView)findViewById(R.id.updateBtn);
        aboutUsBtn.setOnClickListener(this);
        policyBtn.setOnClickListener(this);
        moreApplicationBtn.setOnClickListener(this);
        ratingBtn.setOnClickListener(this);
        friendsBtn.setOnClickListener(this);
        feedbackBtn.setOnClickListener(this);
        versionBtn.setOnClickListener(this);
        updateBtn.setOnClickListener(this);



        backBtn = (ImageView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.aboutUsBtn :
                setIntentData("1", "About us");
                break;
            case R.id.policyBtn :
                setIntentData("2", "Privacy Policy");
                break;
            case R.id.moreApplicationBtn :

                break;
            case R.id.ratingBtn :

                break;
            case R.id.friendsBtn :

                break;
            case R.id.versionBtn :

                break;
            case R.id.updateBtn :

                break;
        }
    }

    private void setIntentData(String strId, String strTitle) {
        Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
        intent.putExtra("id", strId);
        intent.putExtra("title", strTitle);
        startActivity(intent);
    }

}
