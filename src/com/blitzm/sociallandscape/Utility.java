package com.blitzm.sociallandscape;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.blitzm.sociallandscape.activities.SocialLandscapeActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Functions which have been deemed useful to many different activities and
 * fragments, yet their scope does not warrant segregation... yet?
 * 
 * @author Jan
 * 
 */
public class Utility {

	/**
	 * Shows an obtrusive progress dialog for the specified activity, to remove
	 * the dialog call Utility.hideProgress
	 * 
	 * @param activity
	 *            The current activity, required to reference Android specific
	 *            API calls
	 * @param discreet
	 *            Whether the dialog should be discreet, a discreet dialog
	 *            should have the ID "loading" and occur within the active view
	 */
	public static void showProgress(Activity activity, boolean discreet, String title, String message) {
		if (discreet) {
			// Show discreet dialog by toggling visibility of "loading" element
			View loadingDialog = activity.findViewById(R.id.loading);

			// If view with an ID of loading was not found, ignore this command
			if (loadingDialog != null) {
				loadingDialog.setVisibility(View.VISIBLE);
			}
		} else {
			// Show obtrusive dialog with fields populated by string resources
			SocialLandscapeActivity.sProgressDialog = ProgressDialog.show(
					activity, title, message);
		}
	}

	/**
	 * Hide any obtrusive progress dialog associated with the specified activity
	 * 
	 * @param activity
	 *            The current activity, required to reference Android specific
	 *            API calls
	 */
	public static void hideProgress(Activity activity) {
		// Whether or not the dialog is obtrusive is unimportant, same amount of
		// calls to just hide both
		View loadingDialog = activity.findViewById(R.id.loading);

		// Hide unobtrusive progress dialog
		if (loadingDialog != null) {
			loadingDialog.setVisibility(View.INVISIBLE);
		}

		// Hide obtrusive progress dialog
		if (SocialLandscapeActivity.sProgressDialog != null) {
			SocialLandscapeActivity.sProgressDialog.dismiss();
		}
	}

	public static Drawable drawableFromUrl(String path) {
		try {
			InputStream stream = (InputStream) new URL(path).getContent();
			return Drawable.createFromStream(stream, null);
		} catch (IOException e) {
			return null;
		}
	}

	public static Bitmap bitmapFromUrl(String path) {
		Bitmap bitmap = null;

		try {
			bitmap = BitmapFactory.decodeStream(new URL(path).openConnection()
					.getInputStream());
		} catch (MalformedURLException e) {
			Log.e("SocialLandscape", path, e);
		} catch (IOException e) {
			Log.e("SocialLandscape", path, e);
		}

		return bitmap;
	}

	/**
	 * This is a wrapper function which provides backwards compatibility for
	 * various AsyncTask workers. In new versions of Android, AsyncTask can be
	 * run in different pools, concurrency is only guaranteed for tasks in the
	 * same pool.
	 * 
	 * By forcing all the worker tasks to be in the THREAD_POOL_EXECUTOR pool we
	 * can ensure that tasks aren't allocated background pools, defeating the
	 * purpose of asynchronous operations.
	 * 
	 * @param task
	 *            AsyncTask to be executed
	 * @param params
	 *            Parameters which are to be passed to the AsyncTask
	 */
	public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task,
			P... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Force newer versions to run tasks concurrently in the same pool
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			// Older versions only use one pool so it is not allocated
			task.execute(params);
		}
	}

	/**
	 * This is just an abstraction for Utility.execute(task, parameters) where
	 * there are no parameters to be passed in. This function only exists in the
	 * interest of clean code.
	 * 
	 * @param task
	 *            AsyncTask to be executed
	 */
	public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task) {
		execute(task, (P[]) null);
	}

	/**
	 * TODO say what format bbox is supposed to be in "lat, lng, lat, lng"?
	 * 
	 * @param data
	 * @return
	 */
	public static LatLngBounds parseBoundingBox(String bbox) {
		String[] bboxData = bbox.split(",");

		// Create north east point from bounding box data
		LatLng topRight = new LatLng(Double.parseDouble(bboxData[1]),
				Double.parseDouble(bboxData[0]));

		// Create south west point from bounding box data
		LatLng bottomLeft = new LatLng(Double.parseDouble(bboxData[3]),
				Double.parseDouble(bboxData[2]));

		// Create geographic coordinate bounding box
		return new LatLngBounds(topRight, bottomLeft);
	}

	/**
	 * Convert geographic (latitude, longitude) bounding box, to a pixel
	 * rectangle based on map overlay parameters. Note: this function does take
	 * into account zoom levels and orientation.
	 * 
	 * @param map
	 * @param overlayBounds
	 * @return
	 */
	public static Rect geoToPixel(GoogleMap map, LatLngBounds overlayBounds) {
		Point northeast = map.getProjection().toScreenLocation(
				overlayBounds.northeast);

		Point southwest = map.getProjection().toScreenLocation(
				overlayBounds.southwest);

		Rect container = new Rect();
		container.top = northeast.y;
		container.bottom = southwest.y;
		container.left = southwest.x;
		container.right = northeast.x;

		// TODO do error logging here
		// TODO make sure left < right, top < bottom etc

		return container;
	}

	/**
	 * Use Android's geocoder to map an address to a geographic coordinate
	 * 
	 * @param context
	 * @param locationName
	 * @return
	 */
	public static LatLng addressToGeo(Context context, String locationName) {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());

		Address address = null;
		
		Log.d("SocialLandscape", "Geocoding " + locationName);

		try {
			address = geocoder.getFromLocationName(locationName, 1).get(0);
		} catch (Exception e) {
			Log.e("SocialLandscape", "Geocoder error, what should happen here?");
		}

		if (address != null) {
			double latitude = address.getLatitude();
			double longitude = address.getLongitude();

			Log.d("SocialLandscape", locationName + " = " + latitude + ", "
					+ longitude);

			return new LatLng(latitude, longitude);
		} else {
			Log.d("SocialLandscape", locationName + " = not found");

			return null;
		}
	}
}
