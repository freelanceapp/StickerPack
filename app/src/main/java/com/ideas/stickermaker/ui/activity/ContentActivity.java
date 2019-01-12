package com.ideas.stickermaker.ui.activity;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.TextView;

import com.ideas.stickermaker.R;
import com.ideas.stickermaker.retrofit_provider.RetrofitService;
import com.ideas.stickermaker.retrofit_provider.WebResponse;
import com.ideas.stickermaker.utils.Alerts;
import com.ideas.stickermaker.utils.BaseActivity;
import com.ideas.stickermaker.utils.ConnectionDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class ContentActivity extends BaseActivity {

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        mContext = this;
        cd = new ConnectionDetector(mContext);
        retrofitRxClient = RetrofitService.getRxClient();
        retrofitApiClient = RetrofitService.getRetrofit();

        init();
    }

    private void init() {
        if (getIntent() == null)
            return;
        textView = (TextView) findViewById(R.id.tvContent);
        String strId = getIntent().getStringExtra("id");
        String strTitle = getIntent().getStringExtra("title");

        ((TextView) findViewById(R.id.tvTitleToolbar)).setText(strTitle);
        findViewById(R.id.imgBack).setOnClickListener(v -> {
            finish();
        });
        contentApi(strId);
    }

    private void contentApi(String id) {
        if (cd.isNetworkAvailable()) {
            RetrofitService.getContentData(new Dialog(mContext), retrofitApiClient.contentData(id), new WebResponse() {
                @Override
                public void onResponseSuccess(Response<?> result) {
                    ResponseBody responseBody = (ResponseBody) result.body();
                    try {
                        if (responseBody != null) {
                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            if (jsonArray.length() > 0) {
                                JSONObject object = jsonArray.getJSONObject(0);
                                String strData = object.getString("contentdata");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    textView.setText(Html.fromHtml(strData, Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    textView.setText(Html.fromHtml(strData));
                                }
                            } else {
                                //textView.setText(Html.fromHtml(strData));
                                Alerts.show(mContext, "No data found");
                            }
                        } else {
                            Alerts.show(mContext, "No data found!!!");
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
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
}
