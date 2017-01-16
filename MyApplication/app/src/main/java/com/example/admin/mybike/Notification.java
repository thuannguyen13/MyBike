package com.example.admin.mybike;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class Notification extends AppCompatActivity {
    boolean chkStatus;
    TextView lblAsk, lblStatus;
    Switch btnSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        chkStatus = isMyServiceRunning(CountdownService.class);
        lblAsk = (TextView) findViewById(R.id.lblAsk);
        lblStatus = (TextView) findViewById(R.id.lblStatus);
        btnSwitch=(Switch)findViewById(R.id.btnSwitch);

        if (chkStatus == true) {
            lblStatus.setText("Đã kích hoạt");
            btnSwitch.setChecked(true);
            btnSwitch.setText("Yes");
        } else {
            lblStatus.setText("Chưa kích hoạt");
            btnSwitch.setChecked(false);
            btnSwitch.setText("No");
        }

        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked==true){
                    lblStatus.setText("Đã kích hoạt");
                    btnSwitch.setText("Yes");
                    play();
                }
                else{
                    btnSwitch.setText("No");
                    lblStatus.setText("Chưa kích hoạt");
                    stop();
                }
            }
        });
    }

    //Hàm này sẽ tự động chạy khi chương trình đc kích hoạt lần đầu tiên
    public void play() {
        // Tạo ra một đối tượng Intent cho một dịch vụ (PlaySongService).
        Intent myIntent = new Intent(Notification.this, CountdownService.class);
        // Gọi phương thức startService (Truyền vào đối tượng Intent)
        this.startService(myIntent);
    }

    // Method này được gọi khi người dùng Click vào nút Stop.
    public void stop() {
        // Tạo ra một đối tượng Intent.
        Intent myIntent = new Intent(Notification.this, CountdownService.class);
        this.stopService(myIntent);
    }

    //Kiểm tra trạng thái chức năng
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
