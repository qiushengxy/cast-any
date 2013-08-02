package com.qiusheng.cast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class YoukuActivity extends Activity {
        private final static String LOG_TAG = "YoukuActivity";
        private Context context;
        private WebView webView;
        private Button buttonCast; 
        
        @SuppressLint("SetJavaScriptEnabled")
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = this.getApplicationContext();
            setContentView(R.layout.activity_youku);
            setupUserInterface();
        }

        @SuppressLint("SetJavaScriptEnabled")
        private void setupUserInterface() {
            
            webView = (WebView) findViewById(R.id.webView_youku);
            buttonCast = (Button) findViewById(R.id.button_cast_youku);

            webView.getSettings().setJavaScriptEnabled(true);
            
            webView.setWebViewClient(new WebViewClient() { 
                public boolean shouldOverrideUrlLoading(WebView view, String url) { 
                    Log.i(LOG_TAG, "Processing webview url click..."); 
                    view.loadUrl(url); 
                    return true;
                }
                
                public void onPageFinished(WebView view, String url) { 
                    Log.i(LOG_TAG, "Finished loading URL: " +url); 
                }
            });
            
            webView.setWebChromeClient(new WebChromeClient() {
                
            });
            
            webView.loadUrl("http://www.youku.com");
            
            
            buttonCast.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = webView.getUrl();
                    if (url.length() < 1) {
                        Toast.makeText(context, "Invalid url", Toast.LENGTH_SHORT).show();
                    } else {
                        // cast media to chromecast
                        Intent intent = new Intent(context, CastActivity.class);
                        intent.putExtra("MEDIA_URL", url);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }

            });
        }
        
        
}
