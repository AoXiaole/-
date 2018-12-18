package com.example.mynotificationlistener;

import android.app.Notification;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;

import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NotificationMonitor extends NotificationListenerService {

    public static final String ACTION_DATA = "android.service.notification.NotificationListenerService";
    private String PAY_URL = ""; //"http://www.longint.cn/api/onepay";//
    private static final int EVENT_NEW_MSG = 1;
    private Handler handler;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.v("NotificationMonitor","create");
        PAY_URL = this.getString(R.string.pay_url);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case EVENT_NEW_MSG:
                        new HttpRequestThread(msg.obj.toString()).start();
                        Log.v("handleMessage",msg.obj.toString());
                        break;
                }
            }
        };
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v("Notification","onDestroy");

    }

    @Override
    public IBinder onBind(Intent intent) {
        // a.equals("b");
        Log.v("Notification","onBind...");
        return super.onBind(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Notification","Notification removed");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        String notificationText = extras.getString(Notification.EXTRA_TEXT);
        String money = "0.00";
        boolean matched = false;
        int payType = -1;

        if(title.equals("支付宝通知")){

            Log.v("onCreate","title 匹配");
            Pattern pattern = Pattern.compile("^成功收款([0-9\\.]+)元.*$");
            Matcher matcher = pattern.matcher(notificationText);
            if (matcher.find()) {
                Log.v("onCreate", "正则表达式匹配" + matcher.group(1));
                // 获取金额
                money = matcher.group(1);
                matched = true;
                payType = 1;
            }
        }
        else if (title.equals("微信支付")){
            Log.v("onCreate","title 匹配");
            Pattern pattern = Pattern.compile("^微信支付收款([0-9\\.]+)元.*$");
            Matcher matcher = pattern.matcher(notificationText);
            if (matcher.find()) {
                Log.v("onCreate", "正则表达式匹配" + matcher.group(1));
                // 获取金额
                money = matcher.group(1);
                matched = true;
                payType = 0;
            }
        }

        if (matched){

            //发送广播 给UI，进行UI显示
            Intent intent = new Intent();
            intent.putExtra("title",title);
            intent.putExtra("msg",notificationText);
            intent.setAction(ACTION_DATA);
            sendBroadcast(intent);

            // 发送消息给handle，进行http 上报
            String payinfo = payinfo2json(money, payType);
            Message message = Message.obtain(handler);
            message.what = EVENT_NEW_MSG;
            message.obj = payinfo;
            message.sendToTarget();

        }


    }


    public String payinfo2json(String money, int type){
        JSONObject jsonMsg = new JSONObject();
        Date now=new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timeString = formatter.format(now);
        try {
            jsonMsg.put("time", timeString );
            jsonMsg.put("type",Integer.toString(type));
            jsonMsg.put("money",money);

        }catch (JSONException e){
            e.printStackTrace();
            return "";
        }

        return jsonMsg.toString();
    }

    public int sendHttpRequest(String jsonString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(PAY_URL);
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方式
            connection.setRequestMethod("POST");
            // 设置编码格式
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // 设置容许输出
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(jsonString);
            wr.flush();
            wr.close();
            Log.v("sendHttpRequest",jsonString);
            // 获取返回数据
            if(connection.getResponseCode() == 200){
                InputStream is = connection.getInputStream();

                is.close();
            }
        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if(connection!=null){
                connection.disconnect();
            }
        }
        return 0;
    }


    private class HttpRequestThread extends Thread {
        private String msg;

        HttpRequestThread(String msg){
            this.msg = msg;

        }

        @Override
        public void run() {
            sendHttpRequest(msg);
        }
    }
}
