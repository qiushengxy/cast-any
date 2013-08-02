package com.qiusheng.cast;

import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HttpActivity extends Activity {
    private final String LOG_TAG = "HttpActivity";
    final Activity activity = this;
    private String currentUrl;
    private TextView textView;
    private ListView listView;
    
    private ArrayList<String> files = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private Toast backtoast;
    String base64login;
    private String mediaUrl;
    
    private Context context;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        context = this.getApplicationContext();
        
        Intent intent = getIntent();
        String initialUrl = intent.getStringExtra("HTTP_SERVER_URL");
        if (initialUrl.length()<1) {
            this.finish();
            return;
        }
        String username = intent.getStringExtra("HTTP_SERVER_USERNAME");
        String password = intent.getStringExtra("HTTP_SERVER_PASSWORD");
        
        currentUrl = initialUrl;

        setupUserInterface();

        String login = username + ":" + password;
        base64login = new String(Base64.encodeToString(login.getBytes(), Base64.DEFAULT));
        
        try {
            new RetreiveURL().execute(initialUrl);
            refreshList();
            
        } catch (Exception e) {
            e.printStackTrace();
            this.finish();
        }
    }

    private void setupUserInterface() {
        textView = (TextView) this.findViewById(R.id.current_url);
        listView = (ListView) this.findViewById(R.id.file_list_view);

        textView.setText(currentUrl);

    }
    
    private void parseDoc(Document doc) {
        files.clear();
        try {
            Elements ets = doc.getElementsByTag("a");
            Iterator<Element> it = ets.iterator();
            while (it.hasNext()) {
                Element e = it.next();
                //files.add(new String(e.attr("href").getBytes("utf-8"), "utf-8"));
                files.add(e.attr("href"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void refreshList() {
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, files);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                String next = files.get(position);
                if (!next.startsWith("http://")) {
                    if (currentUrl.charAt(currentUrl.length()-1) == '/') {
                        next = currentUrl + next;
                    } else {
                        next = currentUrl + "/" + next;
                    }
                }
                Log.i(LOG_TAG, next);

                try {
                    new RetreiveURL().execute(next);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                textView.setText(next);
                refreshList();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        Log.i(LOG_TAG, "currentUrl="+currentUrl);
        if (currentUrl.charAt(currentUrl.length()-1) == '/') {
            currentUrl = currentUrl.substring(0, currentUrl.length()-1);
        }
        int ind = currentUrl.lastIndexOf('/');
        if ( ind > 6) {
            String next = currentUrl.substring(0, ind);
            Log.i(LOG_TAG, next);
            try {
                new RetreiveURL().execute(next);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            textView.setText(next);
            refreshList();
        } else {
            if(backtoast!=null&&backtoast.getView().getWindowToken()!=null) {
                finish();
            } else {
                backtoast = Toast.makeText(this, "Press back again to previous page", Toast.LENGTH_SHORT);
                backtoast.show();
            }
        }
    }
    
    private class RetreiveURL extends AsyncTask<String, Void, Document> {

        private Exception exception;

        protected Document doInBackground(String... urls) {
            try {
                Response resp = Jsoup.connect(urls[0])
                        .header("Authorization", "Basic " + base64login)
                        .timeout(30*1000)
                        .method(Method.GET)
                        .execute();
                if (resp.contentType().contains("video") || resp.contentType().contains("audio")) {
                    mediaUrl = urls[0];
                    return null;
                } else {
                    mediaUrl = "";
                    currentUrl = urls[0];
                    return resp.parse();
                }
            } catch (UnsupportedMimeTypeException me) {
                mediaUrl = urls[0];
                return null;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(Document doc) {
            if (this.exception!=null) {
                exception.printStackTrace();
            } else if (doc!=null){
                parseDoc(doc);
                adapter.notifyDataSetChanged();
            } else if (!mediaUrl.isEmpty()) {
                // prompt user to cast the media to chromecast
                castMedia();
            }
        }
    }
    
    private void castMedia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Cast media");
        builder.setMessage("Do you want to cast this media to your chromecast?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // cast media to chromecast
                Intent intent = new Intent(context, CastActivity.class);
                intent.putExtra("MEDIA_URL", mediaUrl);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}


