package com.qiusheng.cast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity  extends Activity {
    private Button buttonHttp;
    private Button buttonYouku;

    
    private Context context;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
        setupUserInterface();

    }

    private void setupUserInterface() {
        buttonHttp = (Button) this.findViewById(R.id.button_http);
        buttonYouku = (Button) this.findViewById(R.id.button_youku);

        buttonHttp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start HTTPInputActivity
                Intent intent = new Intent(context, HttpInputActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        
        buttonYouku.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start HTTPInputActivity
                Intent intent = new Intent(context, YoukuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }
}
