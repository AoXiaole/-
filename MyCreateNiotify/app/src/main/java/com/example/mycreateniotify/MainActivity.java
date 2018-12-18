package com.example.mycreateniotify;

import android.app.NotificationManager;
import android.content.Context;

import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {

    public EditText et_msg ;
    public EditText et_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_title = (EditText)findViewById(R.id.et_title_id);
        et_msg = (EditText)findViewById(R.id.et_msg_id);

    }


    public void buttonOnClicked(View view) {
        createNotification(this);
    }

    private void createNotification(Context context) {

        String title = et_title.getText().toString();
        String msg = et_msg.getText().toString();

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncBuilder = new NotificationCompat.Builder(context,"default");
        ncBuilder.setContentTitle(title);
        ncBuilder.setContentText(msg);
        ncBuilder.setTicker("Notification Listener Service Example");
        ncBuilder.setSmallIcon(R.mipmap.ic_launcher);
        ncBuilder.setAutoCancel(true);
        manager.notify((int)System.currentTimeMillis(),ncBuilder.build());
        Log.v("createNotification", title + "&" + msg);
    }
}
