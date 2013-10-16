package com.blitzm.sociallandscape.providers;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.blitzm.sociallandscape.Utility;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

public class RetrieveOverlay extends AsyncTask<String, Void, String> {

	/**
	 * Ensures that the original Google Map object can be garbage collected
	 */
	private final WeakReference<GoogleMap> mMapReference;

	/**
	 * NE, SW bounding box of the overlay (used for position and stretch)
	 */
	private LatLngBounds mOverlayBounds;

	/**
	 * TODO
	 */
	private Drawable mOverlay;

	public RetrieveOverlay(GoogleMap map) {
		mMapReference = new WeakReference<GoogleMap>(map);
	}

	@Override
	protected void onPostExecute(String result) {
		final GoogleMap map = mMapReference.get();

		// Add overlay to Google map fragment
		if (map != null) {
			// Google maps expects overlay as a bitmap
			Bitmap overlayBitmap = ((BitmapDrawable) mOverlay).getBitmap();

			// Clear existing overlay from map
			map.clear();

			// Add overlay to Google map within bounds
			map.addGroundOverlay(new GroundOverlayOptions().image(
					BitmapDescriptorFactory.fromBitmap(overlayBitmap))
					.positionFromBounds(mOverlayBounds));
		}
	}

	@Override
	protected String doInBackground(String... args) {
		// TODO parse and validate
		// TODO move this to config
		String url = "https://apps.blitzm.com/statisticsserver-test/demographics/getHighlightedAreaMap";
		String factPackId = args[0];
		String bbox = args[1];
		String latitude = args[2];
		String longitude = args[3];
		String width = args[4];
		String height = args[5];

		// Generate URL based on previously gathered parameters
		String overlayUrl = String.format(url
				+ "?factPackId=%s&bbox=%s&width=%s&height=%s&sy=%s&sx=%s",
				factPackId, bbox, width, height, latitude, longitude);

		// Create geographic coordinate bounding box
		mOverlayBounds = Utility.parseBoundingBox(bbox);

		// Load content from URL into Facts
		mOverlay = Utility.drawableFromUrl(overlayUrl);

		// Return the URL for debug
		return overlayUrl;
	}

}
