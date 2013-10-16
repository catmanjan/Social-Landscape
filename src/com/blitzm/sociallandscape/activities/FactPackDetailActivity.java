package com.blitzm.sociallandscape.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.fragments.FactDetailFragment;
import com.blitzm.sociallandscape.fragments.FactListFragment;
import com.blitzm.sociallandscape.fragments.FactPackDetailFragment;
import com.blitzm.sociallandscape.models.FactPack;
import com.blitzm.sociallandscape.models.FactPacks;

public class FactPackDetailActivity extends FragmentActivity implements
		FactListFragment.Callbacks, FactPackDetailFragment.Callbacks {

	/**
	 * The content this fragment is presenting.
	 */
	private FactPack mItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factpack_detail);

		// Force sliding transition between activities
		overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);

		// Retrieve fact pack from store
		mItem = FactPacks.sFactPacksMap.get(getIntent().getStringExtra(
				FactPackDetailFragment.ARG_ITEM_ID));

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Set the title to be the FactPack name
		getActionBar().setTitle(mItem.title);

		// If this is the initial activity load
		if (savedInstanceState == null) {
			// Bundle fact pack details
			Bundle arguments = new Bundle();
			arguments.putString(FactPackDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(FactPackDetailFragment.ARG_ITEM_ID));

			// Create the detail fragment and add it to the activity
			// using a fragment transaction
			FactPackDetailFragment fragment = new FactPackDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.factpack_detail_container, fragment).commit();

			// Populate and show fact list from fact pack
			showFactList(mItem.appleProductId);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this, new Intent(this,
					SocialLandscapeActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		// Force sliding transition between activities
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	}

	@Override
	public void onFactSelected(String id) {
		Log.d("SocialLandscape", "Fact selected");

		showFactDetail(id);
	}

	@Override
	public void onMapSelected() {
		Log.d("SocialLandscape", "Map selected");

		showMap();
	}

	private void showFactList(String id) {
		// Bundle current fact pack ID into fragment
		Bundle arguments = new Bundle();
		arguments.putString(FactPackDetailFragment.ARG_ITEM_ID, id);

		// Create fragment and insert into view
		FactListFragment fragment = new FactListFragment();
		fragment.setArguments(arguments);
		getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(android.R.anim.slide_in_left,
						android.R.anim.slide_out_right,
						android.R.anim.slide_in_left,
						android.R.anim.slide_out_right)
				.replace(R.id.fact_detail_container, fragment).commit();
	}

	private void showFactDetail(String id) {
		Intent detailIntent = new Intent(this, FactDetailActivity.class);
		detailIntent.putExtra(FactDetailFragment.ARG_ITEM_ID, id);
		startActivity(detailIntent);
	}

	private void showMap() {
		Intent detailIntent = new Intent(this, MapActivity.class);
		startActivity(detailIntent);
	}
}
