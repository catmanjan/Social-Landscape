package com.blitzm.sociallandscape.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.fragments.FactListFragment;
import com.blitzm.sociallandscape.fragments.FactPackDetailFragment;
import com.blitzm.sociallandscape.fragments.FactPackInfoFragment;
import com.blitzm.sociallandscape.fragments.FactPackListFragment;
import com.blitzm.sociallandscape.models.FactPacks;
import com.blitzm.sociallandscape.models.Facts;
import com.blitzm.sociallandscape.models.User;
import com.blitzm.sociallandscape.providers.ReLoginUser;
import com.blitzm.sociallandscape.providers.RetrieveFactPacks;
import com.blitzm.sociallandscape.providers.SimpleRetrieveFacts;
import com.google.android.gms.maps.model.LatLng;

public class SocialLandscapeActivity extends FragmentActivity implements
		FactPackListFragment.Callbacks, FactListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTabletMode;

    
    private String username;
    
	/**
	 * Loading dialog, may be used throughout activity
	 */
	public static ProgressDialog sProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_social_landscape);
		// Force sliding transition between activities
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);

		if (findViewById(R.id.factpack_detail_container) != null) {
			mTabletMode = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((FactPackListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.factpack_list))
					.setActivateOnItemClick(true);
		}

		addEmailButton();

		addFailureButton();

		loadFactPacks();
		
		loginAccount();
	}

	@Override
	public void onResume() {
		super.onResume();

		// This gets called when the activity is created and restored
		updateUserPosition();
	}

	private void loadFactPacks() {
		Log.d("SocialLandscape", "Loading fact packs...");

		if (!FactPacks.sLoaded) {
			// Show an intrusive dialog if fact packs have never been loaded
			Utility.showProgress(this, false, getString(R.string.progress_dialog_title),
                    getString(R.string.progress_dialog_message));
		} else {
			// Show a discreet loading bar if we've already loaded it before
			Utility.showProgress(this, true, getString(R.string.progress_dialog_title),
                    getString(R.string.progress_dialog_message));
		}

		// Load fact packs from service URL
		Utility.execute(new RetrieveFactPacks(this),
				getString(R.string.fact_pack_url));
	}
	
	private void loginAccount(){
	    Log.d("SocialLandscape", "Login Account");

	    SharedPreferences mSharedPreferences = this.getSharedPreferences("SL_Account", 0);  
	    username = mSharedPreferences.getString("username", null);
	    String password = mSharedPreferences.getString("password", null);
	    String sessionID = mSharedPreferences.getString("JSESSIONID", null);
	     
	    if(username!=null&&password!=null&&sessionID!=null){
	        Utility.execute(new ReLoginUser(this),
	                getString(R.string.login_url), username, password, sessionID);
	    }
        
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

	private void addFailureButton() {
		// Not really adding the button, but adding click listener
		findViewById(R.id.failure_button).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						loadFactPacks();
					}
				});
	}

	private void addEmailButton() {
		// Make the data request email button click listener
		((ImageView) findViewById(R.id.data_request))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);

						String email = getString(R.string.email_address);
						String subject = getString(R.string.email_subject);
						String body = getString(R.string.email_body);

						intent.setData(Uri.parse(String.format(
								"mailto:%s?subject=%s&body=%s", email, subject,
								body)));

						startActivity(intent);
					}
				});
	}

	/**
	 * Callback method from {@link FactPackListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onFactPackSelected(String id) {
		// If payment required, redirect to fact pack information
		if (FactPacks.sFactPacksMap.get(id).paymentRequired) {
			onFactPackInfoSelected(id);
		} else {
			if (mTabletMode) {
				Bundle arguments = new Bundle();
				arguments.putString(FactPackDetailFragment.ARG_ITEM_ID, id);
				FactPackDetailFragment fragment = new FactPackDetailFragment();
				fragment.setArguments(arguments);
				getSupportFragmentManager()
						.beginTransaction()
						.setCustomAnimations(android.R.anim.slide_in_left,
								android.R.anim.slide_out_right,
								android.R.anim.slide_in_left,
								android.R.anim.slide_out_right)
						.replace(R.id.factpack_list, fragment).commit();
			} else {
				Intent detailIntent = new Intent(this,
						FactPackDetailActivity.class);
				detailIntent.putExtra(FactPackDetailFragment.ARG_ITEM_ID, id);
				startActivity(detailIntent);
				overridePendingTransition(R.anim.left_slide_in,
						R.anim.left_slide_out);
			}
		}
	}

	   @Override
	    public void onFactPackInfoSelected(String id) {
	        
	        if ((!Facts.isIndicatorsLoaded || !Facts.retrievedIndicatorID.equals(id)) && !FactPacks.sFactPacksMap.get(id).paymentRequired) {
	            //if the information of indicators haven't been retrieved then retrieve them	            
	                Utility.showProgress(this, false, getString(R.string.factpackInfo_dialog_title),
	                        getString(R.string.progress_dialog_message));
	           
	            Utility.execute(new SimpleRetrieveFacts(this), 
	                    getString(R.string.indicators_url), 
	                    FactPacks.sFactPacksMap.get(id).appleProductId, 
	                    Double.toString(User.sLatLng.latitude),
	                    Double.toString(User.sLatLng.longitude), 
	                    id);
	        } else{
	            //if retrieved before then display them
	            if(FactPacks.sFactPacksMap.get(id).paymentRequired){
	                Facts.sFacts.clear();
	            }
	            Intent detailIntent = new Intent(this, FactPackInfoActivity.class);
	            detailIntent.putExtra(FactPackInfoFragment.ARG_ITEM_ID, id);
	            this.startActivity(detailIntent);
	        }
	        
	        
	    }
	/**
	 * Callback method from {@link FactListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onFactSelected(String id) {
		// TODO for tablets
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.social_landscape_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
		    
			Intent intent;
			Bundle bundle = new Bundle();
			if(User.isLogged){
			    intent = new Intent(this, AccountActivity.class);
			    intent.putExtra("username", username);
			}else{
			    intent = new Intent(this, SettingsActivity.class);
			}
			// notify SettingsActivity to change anim direction
			
			bundle.putString("parent", "sociallandscape");
			intent.putExtras(bundle);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
