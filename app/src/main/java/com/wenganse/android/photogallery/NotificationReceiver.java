package com.wenganse.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.w3c.dom.Text;

/**
 * Created by Plus on 03.06.2017.
 */
// Реализация получателя результата
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context c, Intent i) {
        Log.i( TAG, "received result: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK){
            //активити переднего плана отменила рассылку.
            return;
        }
        int requestCode = i.getIntExtra( PollService.REQUEST_CODE, 0 );
        Notification notification = (Notification) i.getParcelableExtra( PollService.NOTIFICATION );
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from( c );
        notificationManager.notify( requestCode, notification );
    }
}
