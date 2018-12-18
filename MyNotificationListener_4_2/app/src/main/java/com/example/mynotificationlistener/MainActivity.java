package com.example.mynotificationlistener;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.IntentFilter;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    private MyReceiver receiver;
    private static final int EVENT_NEW_MSG = 1;
    private boolean isEnabledNLS = false;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private Handler handler;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.tv);


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case EVENT_NEW_MSG:
                        textView.append(msg.obj.toString() + "\n");
                        break;
                }
            }
        };


        MainActivity.this.startService(new Intent(MainActivity.this, NotificationMonitor.class));
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.service.notification.NotificationListenerService");
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onDestroy() {
        //结束服务
        stopService(new Intent(MainActivity.this, NotificationMonitor.class));
        unregisterReceiver(receiver);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        isEnabledNLS = isEnabled();
        Log.v("MainActivity","isEnabledNLS = " + isEnabledNLS);
        if (!isEnabledNLS) {
            showConfirmDialog();
        }
    }

    private boolean isEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void openNotificationAccess() {
        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Please enable NotificationMonitor access")
                .setTitle("Notification Access")
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                openNotificationAccess();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // do nothing
                            }
                        })
                .create().show();
    }


    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent){
            Bundle bundle = intent.getExtras();
            String title = bundle.getString("title");
            String msg = bundle.getString("msg");

            String str = title + "&" + msg;

            Message message = Message.obtain(handler);
            message.what = EVENT_NEW_MSG;
            message.obj = str;
            message.sendToTarget();

        }
    }


}