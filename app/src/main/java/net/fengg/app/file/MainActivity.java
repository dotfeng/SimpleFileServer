package net.fengg.app.file;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import fi.iki.elonen.SimpleWebServer;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    protected ToggleButton tb_server;

    protected TextView tv_tips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tb_server = (ToggleButton) findViewById(R.id.tb_server);
        tv_tips = (TextView) findViewById(R.id.tv_tips);
        tb_server.setOnCheckedChangeListener(this);
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
            SimpleWebServer.startServer(ip, port,
                    Environment.getExternalStorageDirectory().getPath());
            tv_tips.setVisibility(View.VISIBLE);
            tv_tips.setText(getString(R.string.input) + "\n" + "http://" + ip + ":" + port);
        }else{
            SimpleWebServer.stopServer();
            tv_tips.setVisibility(View.INVISIBLE);
        }
    }
}
