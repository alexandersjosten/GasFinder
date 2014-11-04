package sjosten.android.gasfinder;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import sjosten.android.gasfinder.parser.Parser;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class DownloadKMLActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		
		WebView webView = (WebView)findViewById(R.id.webview);
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(Constants.KML_SITE);
		
		webView.setDownloadListener(new DownloadListener() {
			
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				Toast.makeText(DownloadKMLActivity.this, "Downloading kml-file...", Toast.LENGTH_LONG).show();
				try {
					URL u = new URL(url);
					new DownloadFile().execute(u);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private class DownloadFile extends AsyncTask<URL, Integer, Void> {

		@Override
		protected Void doInBackground(URL... params) {
			URLConnection conn = null;
			try {
				conn = params[0].openConnection();
				conn.connect();
				
				InputStream input = new BufferedInputStream(params[0].openStream());
				OutputStream output = new FileOutputStream(Constants.KML_FOLDER + "tmp.kml");
				
				int count = 0;
				byte[] readBytes = new byte[1024];
				while((count = input.read(readBytes)) != -1) {
					output.write(readBytes, 0, count);
				}
				
				output.flush();
				output.close();
				input.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Toast.makeText(
				DownloadKMLActivity.this,
				"Download done, will insert into database.",
				Toast.LENGTH_SHORT
			).show();
			
			Parser.parseKMLFile(Constants.KML_FOLDER + "tmp.kml");
		}
		
	}
}
