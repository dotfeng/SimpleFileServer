package net.fengg.app.file;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.reflect.Method;

import fi.iki.elonen.SimpleWebServer;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    protected ToggleButton tb_server;
    protected TextView tv_tips;

    private static final String MOUNT_PATH = "MOUNT_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tb_server = (ToggleButton) findViewById(R.id.tb_server);
        tv_tips = (TextView) findViewById(R.id.tv_tips);
        tb_server.setOnCheckedChangeListener(this);
        detectionUSB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tb_server.setChecked(true);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            String ip = SimpleWebServer.getIp();
            if(TextUtils.isEmpty(ip)) {
                tv_tips.setVisibility(View.VISIBLE);
                tv_tips.setText(R.string.error);
                return;
            }
            int port = 9999;
            SimpleWebServer.startServer(ip, port, getSdPath());
           //         Environment.getExternalStorageDirectory().getPath());

            tv_tips.setVisibility(View.VISIBLE);
            tv_tips.setText(getString(R.string.input) + "\n" + "http://" + ip + ":" + port);
        }else{
            SimpleWebServer.stopServer();
            tv_tips.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 获取所有外置存储器的目录
     * @return
     */
    private String[] getSdPath(){
        String[] paths = null;
        StorageManager manager = (StorageManager) this.getSystemService(STORAGE_SERVICE);
        try {
            Method methodGetPaths = manager.getClass().getMethod("getVolumePaths");
            paths = (String[]) methodGetPaths.invoke(manager);
            Log.i("path", paths.toString());
            return paths;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void detectionUSB() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(1000);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentFilter.addDataScheme("file");
        registerReceiver(usbReceiver, intentFilter);
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            mHandler.removeMessages(0);
            Message msg = new Message();
            msg.what = 0;
            if (action.equals(Intent.ACTION_MEDIA_REMOVED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                //设备卸载成功
                msg.arg1 = 0;
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)){
                //设备挂载成功
                msg.arg1 = 1;
            }
            Bundle bundle = new Bundle();
            bundle.putString(MOUNT_PATH, intent.getData().getPath());
            msg.setData(bundle);
            mHandler.sendMessageDelayed(msg, 1000);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0){
                //卸载成功
                if (msg.arg1 == 0){
                    SimpleWebServer.removeDir(msg.getData().getString(MOUNT_PATH));
                }else {//挂载成功
                    SimpleWebServer.addWwwRootDir(msg.getData().getString(MOUNT_PATH));
                }
            }
            super.handleMessage(msg);
        }
    };
}
