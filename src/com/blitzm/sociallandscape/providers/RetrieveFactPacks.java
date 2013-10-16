package com.blitzm.sociallandscape.providers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.models.FactPack;
import com.blitzm.sociallandscape.models.FactPacks;

/**
 * 
 * @author Jan
 * 
 */
public class RetrieveFactPacks extends AsyncTask<String, Void, String> {

	/**
	 * Do not add directly to FactPacks array adapter, not thread safe
	 */
	ArrayList<FactPack> factPacks = new ArrayList<FactPack>();

	/**
	 * Purely for automated testing purposes
	 */
	public static CountDownLatch sSignal = new CountDownLatch(1);

	/**
	 * Track error in alternate thread
	 */
	private Throwable mThrowable;

	/**
	 * Weak reference to calling activity
	 */
	private final WeakReference<Activity> mActivity;

	public RetrieveFactPacks(Activity activity) {
		// Hide failure message as soon as request starts
		activity.findViewById(R.id.failure).setVisibility(View.INVISIBLE);

		mActivity = new WeakReference<Activity>(activity);
	}

	@Override
	protected void onPostExecute(String result) {
		// Clear existing fact packs
		FactPacks.clear();

		// Store cached fact packs into adapter
		for (FactPack factPack : factPacks) {
			FactPacks.addItem(factPack);
		}

		// Notify adapter of dirty, dirty data
		FactPacks.notifyDataSetChanged();

		final Activity activity = mActivity.get();

		if (activity != null) {
			// Hide progress dialog (if any)
			Utility.hideProgress(activity);

			// If there was an error, show refresh option
			if (mThrowable != null) {
				FactPacks.sLoaded = false;

				activity.findViewById(R.id.failure).setVisibility(View.VISIBLE);
			} else {
				// Prevent fact packs from loading again
				FactPacks.sLoaded = true;
			}
		}

		// Release latch (for unit testing purposes only)
		sSignal.countDown();
	}

	@Override
	protected String doInBackground(String... args) {
		// Parse data
		String url = args[0];
		String result = new String();

		Log.d("SocialLandscape", "RetrieveFactPacks: " + url);

		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			// Apply HTTP headers
			post.setHeader("Accept", "application/json");
			post.setHeader("Content-type", "application/json");
			// Actually retrieve data
			result = client.execute(post, new BasicResponseHandler());

			JSONArray factPacksJson = new JSONArray(result);
			for (int i = 0; i < factPacksJson.length(); i++) {
				// Extract fact pack JSON hierarchy model
				JSONObject factPackJson = factPacksJson.getJSONObject(i);
				// Populate data model
				FactPack factPack = new FactPack();
				factPack.appleProductId = factPackJson
						.getString("appleProductId");
				factPack.imageId = factPackJson.getString("imageId");
				factPack.largeIconId = factPackJson.getString("largeIconId");
				factPack.shortDescription = factPackJson
						.getString("shortDescription");
				factPack.longDescription = factPackJson
						.getString("longDescription");
				factPack.title = factPackJson.getString("title");
				factPack.paymentRequired = factPackJson
						.getBoolean("paymentRequired");
				factPack.versionRequired = factPackJson
						.getString("versionRequired");
				factPack.releasedDate = new Date(
						factPackJson.getInt("releaseDate"));
				// Add fact pack to list
				factPacks.add(factPack);
			}
		} catch (Exception e) {
			mThrowable = e;
		}

		return result;
	}

}
