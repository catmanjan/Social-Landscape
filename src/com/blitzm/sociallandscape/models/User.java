package com.blitzm.sociallandscape.models;

import org.apache.http.client.CookieStore;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class User {
	/**
	 * Toggle after fact packs initial load
	 */
	public static boolean isLogged = false;
	public static boolean isRegistered = false;

	/**
	 * the sessionID, using for keeping login status
	 */
	public static String JSESSIONID = null;

	public static CookieStore cookie;

	// Location where user was last seen
	public static LatLng sLatLng;

	// Google maps camera position which the user last saw, accounts for zooming
	// for overlay
	public static CameraPosition sCameraPosition;

	/**
	 * Load bookmarks from application shared preferences
	 * 
	 * @param context
	 *            Application context, required to read from shared preferences
	 * @return An array of bookmark data models
	 */
	public static Bookmark[] getBookmarks(Context context) {
		// Shared preferences from activity context
		SharedPreferences store = context.getSharedPreferences(
				"SocialLandscape", Context.MODE_PRIVATE);

		// JSON parser
		Gson gson = new Gson();

		Log.d("SocialLandscape", store.getString("bookmarks", "[]"));

		Bookmark[] bookmarks = gson.fromJson(
				store.getString("bookmarks", "[]"), Bookmark[].class);

		return bookmarks;
	}

	/**
	 * Add a bookmark to the start of the user's bookmark list
	 * 
	 * @param context
	 *            Application context, required to write to shared preferences
	 * @param bookmark
	 *            The bookmark to be saved
	 */
	public static void addBoomark(Context context, Bookmark bookmark) {
		// null bookmarks will probably break the data
		if (bookmark == null)
			return;

		// Load existing bookmarks
		Bookmark[] existing = getBookmarks(context);

		// Copy across to new array with added bookmark at index 0
		Bookmark[] updated = new Bookmark[existing.length + 1];

		updated[0] = bookmark;

		for (int i = 0; i < existing.length; i++) {
			updated[i + 1] = existing[i];
		}

		// Shared preferences from activity context
		SharedPreferences store = context.getSharedPreferences(
				"SocialLandscape", Context.MODE_PRIVATE);

		// JSON parser
		Gson gson = new Gson();

		Log.d("SocialLandscape", gson.toJson(updated));

		store.edit().putString("bookmarks", gson.toJson(updated)).commit();
	}

	public static void clearJSESSION() {
		JSESSIONID = null;
	}

	public static String getJSESSIONID() {
		return JSESSIONID;
	}

	public static void setJSESSION(String id) {
		JSESSIONID = id;
	}

	public static void clearCookie() {
		cookie = null;
	}

	public static void clear() {
		cookie = null;
		JSESSIONID = null;
		isLogged = false;
		isRegistered = false;
	}
}
