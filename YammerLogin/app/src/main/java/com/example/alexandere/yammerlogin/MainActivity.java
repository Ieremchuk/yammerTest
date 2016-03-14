package com.example.alexandere.yammerlogin;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
{

	private final int YAMMER_OAUTH = 0;


	public String appScheme = "test.yammer.oauth.invisiblesolutions.com";
	WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mWebView = (WebView)findViewById(R.id.webView);

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.getSettings().setBuiltInZoomControls(true);

		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		mWebView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
			{
				super.onReceivedError(view, request, error);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				Log.e("url", url);
				Uri uri = Uri.parse(url);
				String currentScheme = uri.getScheme();
				if (url.toLowerCase().contains(appScheme.toLowerCase()))
				{
					Bundle bundle = new Bundle();
					bundle.putString("code", uri.getQueryParameter("code"));
					getLoaderManager().initLoader(YAMMER_OAUTH, bundle, callbacks);
				}

				return true;
			}
		});

		mWebView.loadUrl(String.format(YammerAPI.LOGIN_URL, YammerAPI.CLIENT_ID));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	private LoaderManager.LoaderCallbacks<JSONObject> callbacks = new LoaderManager.LoaderCallbacks<JSONObject>() {
		@Override
		public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
			return new ResultAsyncTaskLoader(MainActivity.this, args.getString("code"));
		}

		@Override
		public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {

			if(data.has("access_token"))
			{
				try {
					if(data.getJSONObject("access_token").has("token"))
					{
						YammerPreference.CachingToken(MainActivity.this, data.getJSONObject("access_token").getString("token"));
						MainActivity.this.setResult(RESULT_OK);
						MainActivity.this.finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			getLoaderManager().destroyLoader(loader.getId());
		}

		@Override
		public void onLoaderReset(Loader<JSONObject> loader) {

		}
	};

	private static class ResultAsyncTaskLoader extends AsyncTaskLoader<JSONObject>
	{

		private JSONObject result;
		private String mCode;
		public ResultAsyncTaskLoader(Context context, String code) {
			super(context);
			mCode = code;
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			if (result != null) {
				deliverResult(result);
			} else {
				forceLoad();
			}
		}

		@Override
		public JSONObject loadInBackground() {
			/**
			 * send request to server
			 */
			result =
					WebServices.SendHttpPost(
							String.format(YammerAPI.LOGIN_OAUTH, YammerAPI.CLIENT_ID, YammerAPI.CLIENT_SECRET, mCode), new JSONObject(), "");
			return result;
		}
	}

}
