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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HttpActivity extends ListActivity {
    private final String LOG_TAG = "HttpActivity";
    final HttpActivity activity = this;
    private String currentUrl;
    private TextView textView;
    private Button castButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private Toast backtoast;
    String base64login;
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayList<String> mediaUrls = new ArrayList<String>();
    private ArrayList<HttpDataModel> data = new ArrayList<HttpDataModel>();

    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        context = this.getApplicationContext();

        Intent intent = getIntent();
        String initialUrl = intent.getStringExtra("HTTP_SERVER_URL");
        if (initialUrl.length() < 1) {
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
            
            adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, names);
            listView = getListView();
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            setListAdapter(adapter);
            
            // retrieve file list
            new RetreiveURL().execute(initialUrl);

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Toast.makeText(context, "Cannot connect to " + initialUrl, Toast.LENGTH_LONG).show();
        }
    }

    private void setupUserInterface() {
        textView = (TextView) this.findViewById(R.id.current_url);
        castButton = (Button) this.findViewById(R.id.cast_selected);

        textView.setText(currentUrl);
        castButton.setVisibility(View.GONE);
        
        castButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaUrls.isEmpty()) {
                    castMedia();
                }
            }
        });
    }

    private void parseDoc(Document doc) {
        data.clear();
        try {
            Elements trs = doc.getElementsByTag("tr");
            Iterator<Element> it = trs.iterator();
            while (it.hasNext()) {
                HttpDataModel dm = new HttpDataModel();
                Element tr = it.next();
                Element th = tr.getElementsByTag("th").first();
                if (th != null) {
                    // skip header
                    continue;
                }
                Element td = tr.getElementsByClass("t").first();
                Element a = tr.getElementsByTag("a").first();
                dm.setName(a.attr("href"));
                
                if (td.text().contains("video") || td.text().contains("audio")) {
                    // this entry is a media
                    dm.setType(1); // media
                } else if (td.text().contains("Directory")) {
                    // this entry is a media
                    dm.setType(0); // dir
                } else {
                    dm.setType(-1);
                }

                data.add(dm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // update names
        names.clear();
        Iterator<HttpDataModel> it1 = data.iterator();
        while (it1.hasNext()) {
            HttpDataModel dm = it1.next();
            names.add(dm.getName());
        }

        for (int i=0; i < listView.getAdapter().getCount(); i++) {
            listView.setItemChecked(i, false);
        }
        // udpate the view
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(LOG_TAG, "onListItemClick");

        CheckedTextView check = (CheckedTextView)v;

        HttpDataModel dm = data.get(position);
        
        switch (dm.getType()) {
        case 0: // dir
            Log.d(LOG_TAG, "Directory");
            String next = dm.getName();
            if (!next.startsWith("http://")) {
                if (currentUrl.charAt(currentUrl.length() - 1) == '/') {
                    next = currentUrl + next;
                } else {
                    next = currentUrl + "/" + next;
                }
            }
            try {
                new RetreiveURL().execute(next);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            return;
        case 1: // media
            Log.d(LOG_TAG, "Media clicked" + dm.getName());
            if (check.isChecked()) {
                dm.setSelected(true);
            } else {
                dm.setSelected(false);
            }
            break;
        case -1: // invalid
        default:
            Log.d(LOG_TAG, "Invalid");
            check.setChecked(false);
            return;
        
        }
        
        mediaUrls.clear();
        // enable the castButton or not?
        Iterator<HttpDataModel> it1 = data.iterator();
        while (it1.hasNext()) {
            HttpDataModel dm1 = it1.next();
            if (dm1.isSelected()) {
                mediaUrls.add(dm1.getName());
            }
        }
       
        if (mediaUrls.isEmpty()) {
            castButton.setVisibility(View.GONE);
        } else {
            // enable the cast button
            castButton.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onBackPressed() {
        Log.i(LOG_TAG, "currentUrl=" + currentUrl);
        if (currentUrl.charAt(currentUrl.length() - 1) == '/') {
            currentUrl = currentUrl.substring(0, currentUrl.length() - 1);
        }
        int ind = currentUrl.lastIndexOf('/');
        if (ind > 6) {
            String next = currentUrl.substring(0, ind);
            Log.i(LOG_TAG, next);
            try {
                new RetreiveURL().execute(next);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        } else {
            if (backtoast != null && backtoast.getView().getWindowToken() != null) {
                finish();
            } else {
                backtoast = Toast.makeText(this, "Press back again to previous page",
                        Toast.LENGTH_SHORT);
                backtoast.show();
            }
        }
    }

    private class RetreiveURL extends AsyncTask<String, Void, Document> {

        private Exception exception;

        protected Document doInBackground(String... urls) {
            Log.d(LOG_TAG, "doInBackground, url=" + urls[0]);
            try {
                Response resp = Jsoup.connect(urls[0])
                        .header("Authorization", "Basic " + base64login).timeout(30 * 1000)
                        .method(Method.GET).execute();
                if (resp.contentType().contains("text/html")) {
                    Log.d(LOG_TAG, "New directory");
                    currentUrl = urls[0];
                    return resp.parse();
                } else {
                    Log.d(LOG_TAG, "UnsupportedContentType");
                    return null;
                }
            } catch (UnsupportedMimeTypeException me) {
                Log.d(LOG_TAG, "UnsupportedMimeTypeException");
                return null;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Other Exception");
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(Document doc) {
            Log.d(LOG_TAG, "onPostExecute");
            if (this.exception != null) {
                exception.printStackTrace();
            } else if (doc != null) {
                Log.d(LOG_TAG, "Enter New directory");

                parseDoc(doc);

                textView.setText(currentUrl);
                
            }
        }
    }

    private void castMedia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Cast media");
        builder.setMessage("Do you want to cast selected medias to your chromecast?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // cast media to chromecast
                Intent intent = new Intent(context, CastActivity.class);
                intent.putExtra("MEDIA_URL_LIST", mediaUrls);
                intent.putExtra("MEDIA_TITLE", "Multiple videos");
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
