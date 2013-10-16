package com.blitzm.sociallandscape.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.activities.FactPackDetailActivity;
import com.blitzm.sociallandscape.activities.SocialLandscapeActivity;
import com.blitzm.sociallandscape.models.FactPack;
import com.blitzm.sociallandscape.models.FactPacks;
import com.blitzm.sociallandscape.models.Facts;
import com.blitzm.sociallandscape.models.User;
import com.blitzm.sociallandscape.providers.RetrieveFacts;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * A fragment representing a single Fact Pack detail screen. This fragment is
 * either contained in a {@link SocialLandscapeActivity} in two-pane mode (on
 * tablets) or a {@link FactPackDetailActivity} on handsets.
 * 
 * @author Jan
 * 
 */
public class FactPackDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;

	/**
	 * The content this fragment is presenting.
	 */
	private FactPack mItem;

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when the map has been selected.
		 */
		public void onMapSelected();
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onMapSelected() {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FactPackDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = FactPacks.sFactPacksMap.get(getArguments().getString(
					ARG_ITEM_ID));
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		setUpMapIfNeeded();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_factpack_detail,
				container, false);

		if (mItem != null) {
		}

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	private void setUpMapIfNeeded() {
		// Get the map fragment early for tracking purposes
		SupportMapFragment mapFragment = (SupportMapFragment) getActivity()
				.getSupportFragmentManager().findFragmentById(
						R.id.factpack_detail_map);

		// Keep map data on resize or rotate
		if (mapFragment != null) {
			mapFragment.setRetainInstance(true);

			// Try to obtain the map from the SupportMapFragment
			mMap = mapFragment.getMap();

			if (mMap != null) {
				// Send user to map activity when they tap on the map
				mMap.setOnMapClickListener(new OnMapClickListener() {

					@Override
					public void onMapClick(LatLng latLng) {
						mCallbacks.onMapSelected();
					}

				});

				// Set up google maps API specific stuff
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		// Create camera position based on GPS location
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(User.sLatLng).zoom(15).build();

		// If these values are mismatched between model, update fact data
		String factPackId = mItem.appleProductId;
		String longitude = Double.toString(User.sLatLng.longitude);
		String latitude = Double.toString(User.sLatLng.latitude);

		if (factPackId.equals(Facts.factPackId)
				&& longitude.equals(Facts.longitude)
				&& latitude.equals(Facts.latitude)) {
			Log.d("SocialLandscape", "Fact pack is already in memory");

			// User has loaded overlay before, move the map to that location
			if (User.sCameraPosition != null) {
				Log.d("SocialLandscape",
						"Overlay is already in memory, centering camera");

				cameraPosition = User.sCameraPosition;
			}
		} else {
			Log.d("SocialLandscape",
					"Fact pack or location mismatch, retrieving from web service");

			// Clear facts so the user never sees old data
			Facts.clear();

			// Notify fact list adapter of dirty data
			Facts.notifyDataSetChanged();

			// Add loading notification dialog
			// Utility.showProgress(getActivity(), false);
			SocialLandscapeActivity.sProgressDialog = ProgressDialog.show(
					getActivity(), "Updating Facts", "Loading...");
		}

		// JM: Always do a provider request, the overlay is probably disposed

		// Data to be passed into provider
		String url = getString(R.string.indicators_url);

		// Start provider request
		Utility.execute(new RetrieveFacts(mMap), url, factPackId, latitude,
				longitude);

		// Move map to user location (either GPS location of overlay location)
		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
}
