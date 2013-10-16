package com.blitzm.sociallandscape.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.fragments.FactDetailFragment;
import com.blitzm.sociallandscape.models.Fact;
import com.blitzm.sociallandscape.models.Facts;

/**
 * This activity instantiates the fragments related to fact information
 * retrieval and display.
 * 
 * @author Jan
 * 
 */
public class FactDetailActivity extends FragmentActivity {

	/**
	 * The content this fragment is presenting.
	 */
	private Fact mItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factpack_detail);

		// Force sliding transition between activities
		overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);

		// Retrieve model from store based on bundled arguments
		mItem = Facts.sFactsMap.get(getIntent().getStringExtra(
				FactDetailFragment.ARG_ITEM_ID));

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Set the title to be the Fact name
		getActionBar().setTitle(mItem.name);

		// If this is the initial activity load
		if (savedInstanceState == null) {
			// Bundle fact details
			Bundle arguments = new Bundle();
			arguments.putString(FactDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(FactDetailFragment.ARG_ITEM_ID));

			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			FactDetailFragment fragment = new FactDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.factpack_detail_container, fragment).commit();
		}
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
	public void onBackPressed() {
		super.onBackPressed();

		// Force sliding transition between activities
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	}
}
