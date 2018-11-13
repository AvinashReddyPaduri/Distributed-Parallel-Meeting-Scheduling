package com.example.project_samples;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class GcmMessageHandler extends IntentService{

	static String PROJECT_NUMBER = "GCM project number";
     String title;
     String msg;
     private Handler handler;
    public GcmMessageHandler(){
        super(PROJECT_NUMBER);
    }

    @Override
    public void onCreate(){
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }
    @Override
    protected void onHandleIntent(Intent intent){
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);//indicates if the message is regular or special. This is used to check(only for testing purpose) the status of the message.

       title = extras.getString("title");
       msg = extras.getString("message");
       showNotification();
       Log.i("GCM", "Received : (" +messageType+")  "+extras.getString("title"));

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void showNotification(){
        handler.post(new Runnable(){
            public void run(){
            	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            	Intent i = new Intent(getBaseContext(), Welcome.class);
            	i.putExtra("message", msg);
            	i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            	Notification notify = new Notification(R.drawable.dpms, msg, System.currentTimeMillis());
            	notify.setLatestEventInfo(getApplicationContext(), "DPMS Notification", title+": "+msg, pi);
            	notify.defaults = Notification.DEFAULT_SOUND;
            	notify.defaults = Notification.DEFAULT_VIBRATE;
            	nm.notify(1, notify);
                Toast.makeText(getApplicationContext(),msg , Toast.LENGTH_LONG).show();
            }
         });
    }
}