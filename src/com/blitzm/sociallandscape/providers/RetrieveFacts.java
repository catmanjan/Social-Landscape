package com.blitzm.sociallandscape.providers;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.activities.SocialLandscapeActivity;
import com.blitzm.sociallandscape.models.Fact;
import com.blitzm.sociallandscape.models.Facts;
import com.blitzm.sociallandscape.models.User;
import com.blitzm.sociallandscape.models.nodes.CategoryDataNode;
import com.blitzm.sociallandscape.models.nodes.CompositionDataNode;
import com.blitzm.sociallandscape.models.nodes.DistanceDataNode;
import com.blitzm.sociallandscape.models.nodes.LocationDataNode;
import com.blitzm.sociallandscape.models.nodes.TimeDataNode;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * This class gets data stuff from the internet, stuffs it into a data model and
 * lets the rest of the app know when its got the goods
 * 
 * TODO make overlay dimensions dynamic (based on screen resolution?)
 * 
 * @author Jan
 * 
 */
public class RetrieveFacts extends AsyncTask<String, Void, String> {

	/**
	 * Do not add directly to Facts array adapter, not thread safe
	 */
	ArrayList<Fact> facts = new ArrayList<Fact>();

	/**
	 * Ensures that the original Google Map object can be garbage collected
	 */
	private final WeakReference<GoogleMap> mMapReference;

	/**
	 * NE, SW bounding box of the overlay (used for position and stretch)
	 */
	private LatLngBounds mOverlayBounds;

	/**
	 * TODO make this configurable?
	 */
	private double mOverlayScale = 1D / 3D;

	public RetrieveFacts(GoogleMap map) {
		mMapReference = new WeakReference<GoogleMap>(map);
	}

	@Override
	protected void onPostExecute(String result) {
		// Don't use Facts.clear() here, it wipes more than the fact list
		Facts.sFacts.clear();
		Facts.sFactsMap.clear();

		// Add temporary facts to array adapter
		for (Fact fact : facts) {
			Facts.addItem(fact);
		}

		final GoogleMap map = mMapReference.get();

		// Begin overlay sprite request
		if (map != null && mOverlayBounds != null) {
			// Calculate request pixel size based on bounding box
			Rect bounds = Utility.geoToPixel(map, mOverlayBounds);

			// Scale requested overlay dimensions
			int scaledWidth = (int) (bounds.width() * mOverlayScale);
			int scaledHeight = (int) (bounds.height() * mOverlayScale);

			// Pass dimensions to task as strings for consistency
			String width = Integer.toString(scaledWidth);
			String height = Integer.toString(scaledHeight);

			Utility.execute(new RetrieveOverlay(map), Facts.factPackId,
					Facts.bbox, Facts.latitude, Facts.longitude, width, height);

			// Move map camera to center of overlay with set padding
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(mOverlayBounds,
					48));

			// Store location so user can be sent there later
			User.sCameraPosition = map.getCameraPosition();
		}

		// Notify adapter of dirty data
		Facts.notifyDataSetChanged();

		// Hide progress dialog (if any)
		if (SocialLandscapeActivity.sProgressDialog != null) {
			SocialLandscapeActivity.sProgressDialog.dismiss();
		}
	}

	@Override
	protected String doInBackground(String... args) {
		// TODO format/error checking here
		String url = args[0];
		String factPackId = args[1];
		String latitude = args[2];
		String longitude = args[3];
		String result = new String();

		// Build service request URL from parameters
		url += "?factPackId=" + factPackId;
		url += "&x=" + longitude;
		url += "&y=" + latitude;
		
		// JM: TODO replace with proper data
		url += "&deviceId=XXXXYYYYZZZZ";
		url += "&receiptData=AAAABBBBCCCCDDDDEEEE";

		Log.d("SocialLandscape", "RetrieveFacts: " + url);

		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);

			// Apply HTTP headers (optional, but good practice)
			post.setHeader("Accept", "application/json");
			post.setHeader("Content-type", "application/json");

			// Actually retrieve data
			result = client.execute(post, new BasicResponseHandler());
		} catch (ClientProtocolException e) {
			Log.e("SocialLandscape", "ClientProtocolException", e);
		} catch (IOException e) {
			Log.e("SocialLandscape", "IOException", e);
		}

		try {
			// Break out of incorrect encapsulating array
			JSONArray factJsonArray = new JSONArray(result);
			JSONObject factJson = factJsonArray.getJSONObject(0);

			// Global fact information
			String suburbName = factJson.getString("suburbName");
			String stateName = factJson.getString("stateName");
			String councilName = factJson.getString("councilName");
			String bbox = factJson.getString("bbox");

			// Create geographic coordinate bounding box
			mOverlayBounds = Utility.parseBoundingBox(bbox);

			Facts.factPackId = factPackId;
			Facts.latitude = latitude;
			Facts.longitude = longitude;
			Facts.suburbName = suburbName;
			Facts.stateName = stateName;
			Facts.councilName = councilName;
			Facts.bbox = bbox;

			// Indicator (AKA fact) array
			JSONArray indicatorsJson = factJson.getJSONArray("indicators");
			for (int i = 0; i < indicatorsJson.length(); i++) {
				// Extract fact JSON hierarchy model
				JSONObject indicatorJson = indicatorsJson.getJSONObject(i);

				// Populate data model
				Fact fact = new Fact();
				fact.name = indicatorJson.getString("name");
				fact.measure = indicatorJson.getString("measure");
				fact.dataType = indicatorJson.getString("dataType");
				fact.imageId = indicatorJson.getString("imageId");

				// Location series
				if (!indicatorJson.isNull("locationSeries")) {
					JSONArray locationSeriesJson = indicatorJson
							.getJSONArray("locationSeries");
					fact.locationSeries = new ArrayList<LocationDataNode>();
					for (int q = 0; q < locationSeriesJson.length(); q++) {
						JSONObject nodeJson = locationSeriesJson
								.getJSONObject(q);
						LocationDataNode node = new LocationDataNode();
						node.value = nodeJson.getInt("value");
						node.level = nodeJson.getInt("level");
						fact.locationSeries.add(node);
					}
				}

				// Category series
				if (!indicatorJson.isNull("categorySeries")) {
					JSONArray categorySeriesJson = indicatorJson
							.getJSONArray("categorySeries");
					fact.categorySeries = new ArrayList<CategoryDataNode>();
					for (int q = 0; q < categorySeriesJson.length(); q++) {
						JSONObject nodeJson = categorySeriesJson
								.getJSONObject(q);
						CategoryDataNode node = new CategoryDataNode();
						node.label = nodeJson.getString("label");
						node.value = nodeJson.getString("value");
						node.order = nodeJson.getInt("order");
						fact.categorySeries.add(node);
					}
				}

				// Composition series
				if (!indicatorJson.isNull("compositionSeries")) {
					JSONArray compositionSeriesJson = indicatorJson
							.getJSONArray("compositionSeries");
					fact.compositionSeries = new ArrayList<CompositionDataNode>();
					for (int q = 0; q < compositionSeriesJson.length(); q++) {
						JSONObject nodeJson = compositionSeriesJson
								.getJSONObject(q);
						CompositionDataNode node = new CompositionDataNode();
						node.name = nodeJson.getString("name");
						node.value = nodeJson.getDouble("value");
						fact.compositionSeries.add(node);
					}
				}

				// Time series
				if (!indicatorJson.isNull("timeSeries")) {
					JSONArray timeSeriesJson = indicatorJson
							.getJSONArray("timeSeries");
					fact.timeSeries = new ArrayList<TimeDataNode>();
					for (int q = 0; q < timeSeriesJson.length(); q++) {
						JSONObject nodeJson = timeSeriesJson.getJSONObject(q);
						TimeDataNode node = new TimeDataNode();
						node.day = nodeJson.getInt("day");
						node.month = nodeJson.getInt("month");
						node.year = nodeJson.getInt("year");
						node.value = nodeJson.getString("value");
						fact.timeSeries.add(node);
					}
				}

				// Time series
				if (!indicatorJson.isNull("distanceNode")) {
					JSONObject distanceNodeJson = indicatorJson
							.getJSONObject("distanceNode");
					DistanceDataNode node = new DistanceDataNode();
					node.name = distanceNodeJson.getString("name");
					node.value = distanceNodeJson.getString("value");
					fact.distanceNode = node;
				}

				// Add fact to temporary fact list
				facts.add(fact);
			}
		} catch (Exception e) {
			Log.e("SocialLandscape", "RetrieveFacts parse error", e);
		}

		return result;
	}
}
