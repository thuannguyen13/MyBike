package com.example.admin.mybike;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

public class CountdownService extends Service {
    private NotificationCompat.Builder notBuilder;
    private static final int MY_NOTIFICATION_ID = 12345;
    private static final int MY_REQUEST_CODE = 100;

    public CountdownService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // tạo đối tượng notificaiton
    @Override
    public void onCreate() {
        super.onCreate();
        this.notBuilder = new NotificationCompat.Builder(this);
    }

    //Hàm duy trì chạy ngầm hệ thống
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Hàm đếm ngược thời gian bảo dưỡng xe, thời gian quy ước 2 tháng
        new CountDownTimer(11000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Notification("Đã đến thời gian bảo dưỡng xe!!!", "");
                start();
            }
        }
                .start();
        return START_STICKY;
    }

    //Hủy dịch vụ chạy ngầm
    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    //Hàm hiển thị thông báo
    public void Notification(String sTitle, String ticker) {
        notBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notBuilder.setTicker(ticker);

        notBuilder.setWhen(System.currentTimeMillis() + 5 * 10000);
        notBuilder.setContentTitle(sTitle);
        notBuilder.setContentText("Hãy đến nơi bảo dưỡng gần nhất trên bản đồ!!!");

        Intent intent = new Intent(getBaseContext(), com.example.admin.mybike.Notification.class);


        // PendingIntent.getActivity(..) sẽ start mới một Activity và trả về
        // đối tượng PendingIntent.
        // Nó cũng tương đương với gọi Context.startActivity(Intent).
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), MY_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);


        notBuilder.setContentIntent(pendingIntent);

        // Lấy ra dịch vụ thông báo (Một dịch vụ có sẵn của hệ thống).
        NotificationManager notificationService =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Xây dựng thông báo và gửi nó lên hệ thống.

        Notification notification = notBuilder.build();
        notificationService.notify(MY_NOTIFICATION_ID, notification);
    }

}
