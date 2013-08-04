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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class WebActivity extends Activity {
	private final static String LOG_TAG = "WebActivity";
	private Context context;
	private WebView webView;
	private Button buttonCast;
	private ArrayList<String> realLinks = new ArrayList<String>();
	private String realTitle = "";

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
				Log.i(LOG_TAG, "Finished loading URL: " + url);
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {

		});

		Intent intent = this.getIntent();
		String webUrl = intent.getStringExtra("WEB_URL");
		if (webUrl == null || webUrl.isEmpty()) {
			Log.e(LOG_TAG, "Invalid URL");
			finish();
			return;
		}

		webView.loadUrl(webUrl);

		buttonCast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = webView.getUrl();
				if (url.length() < 1) {
					Toast.makeText(context, "Invalid url", Toast.LENGTH_SHORT)
							.show();
				} else {
					realLinks.clear();
					realTitle = "";
					String requestParseUrl = "http://www.flvcd.com/parse.php?format=super&kw="
							+ url;
					try {
						new RetreiveURL().execute(requestParseUrl);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}

				}
			}

		});
	}

	private class RetreiveURL extends AsyncTask<String, Void, Document> {

		private Exception exception;

		protected Document doInBackground(String... urls) {
			try {
				Response resp = Jsoup
						.connect(urls[0])
						.timeout(10 * 1000)
						.cookie("pianhao",
								"%7B%22qing%22%3A%22super%22%2C%22qtudou%22%3A%22null%22%2C%22qyouku%22%3A%22null%22%2C%22q56%22%3A%22null%22%2C%22qcntv%22%3A%22null%22%2C%22qletv%22%3A%22null%22%2C%22qqiyi%22%3A%22null%22%2C%22qsohu%22%3A%22null%22%2C%22qqq%22%3A%22null%22%2C%22qku6%22%3A%22null%22%2C%22qyinyuetai%22%3A%22null%22%2C%22qtangdou%22%3A%22null%22%2C%22qxunlei%22%3A%22null%22%2C%22qfunshion%22%3A%22null%22%2C%22qsina%22%3A%22null%22%2C%22qpptv%22%3A%22null%22%2C%22xia%22%3A%22ask%22%2C%22pop%22%3A%22no%22%2C%22open%22%3A%22no%22%7D")
						.method(Method.GET).execute();

				return resp.parse();
			} catch (UnsupportedMimeTypeException me) {

				return null;
			} catch (Exception e) {
				this.exception = e;
				return null;
			}
		}

		protected void onPostExecute(Document doc) {
			if (this.exception != null) {
				exception.printStackTrace();
			} else if (doc != null) {
				parseFlvcdDoc(doc);

			}
		}
	}

	private void parseFlvcdDoc(Document doc) {
		try {
			Elements ets = doc.getElementsByTag("a");
			Iterator<Element> it = ets.iterator();
			while (it.hasNext()) {
				Element e = it.next();
				String href = e.attr("href");
				if (href.contains("mp4")) {
					Log.i(LOG_TAG, "href=" + href);
					realLinks.add(href);
				}
			}
			Elements ets1 = doc.getElementsByTag("input");
			Iterator<Element> it1 = ets1.iterator();
			while (it1.hasNext()) {
				Element e = it1.next();
				if (e.attr("name").equals("name")) {
					realTitle = e.attr("value");
					break;
				}
			}

			if (!realLinks.isEmpty()) {
				startCast();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Sorry!")
						.setMessage("No valid video found in this page.")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// do nothing
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startCast() {
		// cast media to chromecast
		Intent intent = new Intent(context, CastActivity.class);
		intent.putExtra("MEDIA_URL_LIST", realLinks);
		intent.putExtra("MEDIA_TITLE", realTitle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack())
			webView.goBack();
		else
			super.onBackPressed();
	}
}
