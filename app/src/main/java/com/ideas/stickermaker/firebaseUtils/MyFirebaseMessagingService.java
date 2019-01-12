package com.ideas.stickermaker.firebaseUtils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ideas.stickermaker.R;
import com.ideas.stickermaker.WhatsAppBasedCode.StickerPackDetailsActivity;
import com.ideas.stickermaker.constant.Constant;
import com.ideas.stickermaker.utils.AppPreference;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        AppPreference.setStringPreference(this, Constant.DEVICE_TOKEN_PREF, s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                sendNotification(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void sendNotification(JSONObject json) {
        JSONObject data = null;
        Intent intent;
        try {
            data = json.getJSONObject("message");
           /* String title = data.getString("title");
            String body = data.getString("body");
            String strId = json.getString("job_id");*/

            intent = new Intent(this, StickerPackDetailsActivity.class);
           /* intent.putExtra("job_id", strId);
            intent.putExtra("from", "notification");*/
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            createNotification("Sticker Maker", "New pack update");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createNotification(String title, String message) {
        Intent resultIntent = new Intent(this, StickerPackDetailsActivity.class);
        resultIntent.putExtra("sticker_id", "sticker_id");
        resultIntent.putExtra("from", "notification");
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.icon_fire);
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }
}