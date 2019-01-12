package com.ideas.stickermaker.firebaseUtils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.ideas.stickermaker.constant.Constant;
import com.ideas.stickermaker.retrofit_provider.RetrofitApiClient;
import com.ideas.stickermaker.retrofit_provider.RetrofitService;
import com.ideas.stickermaker.utils.AppPreference;
import com.ideas.stickermaker.utils.ConnectionDetector;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private RetrofitApiClient retrofitApiClient;
    Context context;
    private ConnectionDetector cd;

    @Override
    public void onTokenRefresh() {

        context = this;

        cd = new ConnectionDetector(context);
        retrofitApiClient = RetrofitService.getRetrofit();

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        AppPreference.setStringPreference(this, Constant.DEVICE_TOKEN_PREF, refreshedToken);
    }
}