package com.moreno.fartbomb.network;

import android.app.*;
import android.content.*;
import android.os.*;

import com.moreno.fartbomb.*;

public class NotificationService extends Service {
    public static final int NOTIFY_ID = 6;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SyncHandler.BROADCAST_FRIEND_REQUEST_RECEIVED)) {
                pushMessageNotification();
            }
        }
    };

    public static Handler notificationHandler = null;

    public static boolean isRunning = false;

    private void addBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncHandler.BROADCAST_FRIEND_REQUEST_RECEIVED);
        registerReceiver(receiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addBroadcastReceiver();
        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    private void pushMessageNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification note = new Notification(R.drawable.fartbomb_icon, "You have a new friend request!", System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ProfileActivity.class), 0);
        note.setLatestEventInfo(this, "FartBomb Message", "You have a new friend request!", pendingIntent);
        notificationManager.notify(NOTIFY_ID, note);
    }
}
