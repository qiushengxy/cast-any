package com.qiusheng.cast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    private Button buttonHttp;
    private Button buttonQQ;
    private Button buttonYouku;
    private Button buttonTudou;
    private Button buttonSohu;

    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
        setupUserInterface();

    }

    private void setupUserInterface() {
        buttonHttp = (Button) this.findViewById(R.id.button_http);
        buttonQQ = (Button) this.findViewById(R.id.button_qq);
        buttonYouku = (Button) this.findViewById(R.id.button_youku);
        buttonTudou = (Button) this.findViewById(R.id.button_tudou);
        buttonSohu = (Button) this.findViewById(R.id.button_sohu);

        buttonHttp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start HTTPInputActivity
                Intent intent = new Intent(context, HttpInputActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        buttonQQ.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start WebActivity
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("WEB_URL", "http://v.qq.com/");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        buttonYouku.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start WebActivity
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("WEB_URL", "http://www.youku.com/");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        buttonTudou.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start WebActivity
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("WEB_URL", "http://www.tudou.com/");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        buttonSohu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start WebActivity
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("WEB_URL", "http://tv.sohu.com/");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }
}
