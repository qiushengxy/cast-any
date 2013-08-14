package com.qiusheng.cast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class HttpInputActivity extends Activity {

    private Button buttonConnect;
    private EditText editServer;
    private EditText editUsername;
    private EditText editPassword;

    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_input);
        context = this.getApplicationContext();
        setupUserInterface();

    }

    private void setupUserInterface() {
        buttonConnect = (Button) this.findViewById(R.id.button_connect);
        editServer = (EditText) this.findViewById(R.id.editText_http_server);
        editUsername = (EditText) this.findViewById(R.id.editText_http_username);
        editPassword = (EditText) this.findViewById(R.id.editText_http_password);

        buttonConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String server = editServer.getText().toString();
                if (server.length() < 1) {
                    Toast.makeText(context, "Server address is mandotory", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // start HTTPActivity
                    Intent intent = new Intent(context, HttpActivity.class);
                    intent.putExtra("HTTP_SERVER_URL", server);
                    intent.putExtra("HTTP_SERVER_USERNAME", editUsername.getText().toString());
                    intent.putExtra("HTTP_SERVER_PASSWORD", editPassword.getText().toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }

        });
    }
}
