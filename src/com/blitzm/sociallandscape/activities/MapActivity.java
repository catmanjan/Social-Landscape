package com.blitzm.sociallandscape.activities;

import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.models.Bookmark;
import com.blitzm.sociallandscape.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This activity allows the user to manually update their position (rather than
 * relying on the GPS location).
 * 
 * Instantiates a map marker based on the user's current location and allows
 * drag and dropping of the marker which reflects on the user model.
 * 
 * @author Jan
 * 
 */
public class MapActivity extends FragmentActivity implements
		OnMarkerDragListener {

	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;

	/**
	 * Map marker which represents the user's current location or the location
	 * they have selected.
	 */
	private Marker mMarker;

	/**
	 * Any invalid latitude or longitude value, used to identify "Add Bookmark"
	 * button, hacky, but works.
	 */
	private int mAddBookmark = -9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		// Force sliding transition between activities
		overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Store input manager so we can mess around with virtual keyboard later
		final InputMethodManager inputManager = (InputMethodManager) getBaseContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		// ------------ Bookmark UI ------------
		final Spinner bookmarksSpinner = (Spinner) findViewById(R.id.bookmarks_spinner);

		final ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();

		// Initial bookmark population
		updateUserBookmarks(bookmarks, bookmarksSpinner);

		bookmarksSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// Get bookmark by index from original list
						Bookmark bookmark = bookmarks.get(position);

						// Extract geo-coordinate
						LatLng latLng = bookmark.getLatLng();

						// User has clicked on "Add Bookmark" button
						if (bookmark.getLatitude() == mAddBookmark
								&& bookmark.getLongitude() == mAddBookmark) {
							// Add new bookmark
							addUserBookmark(bookmarks, bookmarksSpinner);
						} else {
							// Real bookmark selected, animate map to location
							User.sLatLng = latLng;

							// Moves marker and camera to current location
							setUpMap();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}

				});

		// ------------ Address UI ------------
		final EditText locationEditText = (EditText) findViewById(R.id.set_location_edittext);

		locationEditText
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							// Get entered text
							String locationName = v.getText().toString();

							// Clean and standardise the input text
							locationName = locationName.trim();
							locationName = locationName
									.toLowerCase(Locale.ENGLISH);

							// Append the word "Australia" to keep requests
							// relevant
							if (!locationName.contains("australia")) {
								locationName += " australia";
							}

							// Update user location using geocoder
							User.sLatLng = Utility.addressToGeo(
									getBaseContext(), locationName);

							// Update marker position
							mMarker.setPosition(User.sLatLng);

							// Update camera position
							moveCamera();

							// Close virtual keyboard
							inputManager.hideSoftInputFromWindow(
									locationEditText.getWindowToken(), 0);

							return true;
						}

						return false;
					}

				});

		// Set location to current user location when button clicked
		findViewById(R.id.set_location_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						updateUserPosition();

						mMarker.setPosition(User.sLatLng);

						moveCamera();
					}

				});

		// ------------ Done UI ------------
		// Done button, take user back to fact pack detail view
		findViewById(R.id.map_done).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				onBackPressed();
			}

		});
	}

	private void addUserBookmark(final ArrayList<Bookmark> bookmarks,
			final Spinner bookmarksSpinner) {
		// Set an EditText view to get user input
		final EditText bookmarkName = new EditText(MapActivity.this);

		final DialogInterface.OnClickListener bookmarkClick = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = bookmarkName.getEditableText().toString();

				if (name.trim().length() > 0) {
					User.addBoomark(MapActivity.this.getApplicationContext(),
							new Bookmark(name, User.sLatLng.latitude,
									User.sLatLng.longitude));

					updateUserBookmarks(bookmarks, bookmarksSpinner);
				}
			}
		};

		// Dialog with input for bookmark name
		new AlertDialog.Builder(this).setTitle("Add Bookmark")
				.setMessage("Enter a name for your bookmark:")
				.setView(bookmarkName).setPositiveButton("Ok", bookmarkClick)
				.setNegativeButton("Cancel", null).show();
	}

	private void updateUserBookmarks(final ArrayList<Bookmark> bookmarks,
			final Spinner bookmarksSpinner) {
		bookmarks.clear();

		// Merge user bookmarks into array adapter
		for (Bookmark bookmark : User.getBookmarks(getApplicationContext())) {
			bookmarks.add(bookmark);
		}

		// Default "add bookmark" button, identify it by its invalid LatLng
		bookmarks.add(new Bookmark("Add Bookmark", mAddBookmark, mAddBookmark));

		// Load bookmark items into array adapter with custom layout
		ArrayAdapter<Bookmark> adapter = new ArrayAdapter<Bookmark>(this,
				android.R.layout.simple_spinner_dropdown_item, bookmarks);

		// Set list items of spinner
		bookmarksSpinner.setAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();

		setUpMapIfNeeded();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		// Force sliding transition between activities
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// By finishing the activity we can make sure that the resources
			// used by the graphs are disposed and garbage collected
			finish();

			// Force sliding transition between activities
			overridePendingTransition(R.anim.right_slide_in,
					R.anim.right_slide_out);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		// Log.d("SocialLandscape", "Marker dragged");
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		Log.d("SocialLandscape", "Marker drag end");

		// Update user location based on new marker position
		User.sLatLng = marker.getPosition();

		Log.d("SocialLandscape", "User location is now " + User.sLatLng);
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		Log.d("SocialLandscape", "Marker drag start");
	}

	private void updateUserPosition() {
		Log.d("SocialLandscape", "Retrieving user location...");

		Location location = null;

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// Request GPS current location
			location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} else if (locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			// Request network location
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if (location == null) {
			// Create location with no provider (poor design by Google)
			location = new Location("");

			// Set to default location, Sydney
			location.setLatitude(-33.873651);
			location.setLongitude(151.2068896);
		}

		double lat = location.getLatitude();
		double lng = location.getLongitude();

		Log.d("SocialLandscape", "User location now: " + lat + ", " + lng);

		// Transform location to LatLng
		User.sLatLng = new LatLng(lat, lng);
	}

	private void setUpMapIfNeeded() {
		// Get the map fragment early for tracking purposes
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.activity_map);

		// Keep map data on resize or rotate
		if (mapFragment != null) {
			mapFragment.setRetainInstance(true);

			// Try to obtain the map from the SupportMapFragment.
			mMap = mapFragment.getMap();

			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		addMarker();
		moveCamera();
	}

	private void addMarker() {
		// Set this activity as the receiver for drag updates.
		mMap.setOnMarkerDragListener(this);

		// Prevent floating duplicates
		if (mMarker != null) {
			mMarker.remove();
		}

		// Add a map marker which can be dragged.
		mMarker = mMap.addMarker(new MarkerOptions().position(User.sLatLng)
				.draggable(true));
	}

	private void moveCamera() {
		// Create camera position based on location.
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(User.sLatLng).zoom(15).build();

		// Move map to current location.
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}
}
