package com.blitzm.sociallandscape.activities;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Intent;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.fragments.FactPackInfoFragment;
import com.blitzm.sociallandscape.models.Fact;
import com.blitzm.sociallandscape.models.FactPack;
import com.blitzm.sociallandscape.models.FactPacks;
import com.blitzm.sociallandscape.models.Facts;
import com.blitzm.sociallandscape.models.User;
import com.blitzm.sociallandscape.providers.RetrieveFacts;
import com.blitzm.sociallandscape.providers.RetrieveIcon;
import com.blitzm.sociallandscape.providers.SimpleRetrieveFacts;

import com.blitzm.sociallandscape.billingUtility.IabHelper;
import com.blitzm.sociallandscape.billingUtility.IabResult;
import com.blitzm.sociallandscape.billingUtility.Inventory;
import com.blitzm.sociallandscape.billingUtility.Purchase;

/**
 * Activity which instantiates the information view for each fact pack. This
 * includes rendering relevant icons and payment detail information.
 * 
 * Eventually, this activity will have to be linked to payment services.
 * 
 * Work has begun on payment services - jacob -10/10/13
 * Finished initial implementation of subscription service.
 * Still requires the:
 * --base64EncodedPublicKey
 * --Verify developer payload
 * Jacob - 14/10/13
 * 
 * @author Jan
 * 
 */
public class FactPackInfoActivity extends FragmentActivity {

	/**
	 * The content this fragment is presenting.
	 */
	private FactPack mItem;

	//set up to allow the page to be worked on while subscriptions are still being implemented
	boolean subTesting = false;

	// Debug tag, for logging
	static final String TAG = "SocialLandscape";

	// Does the user have an active subscription to the Gold plan?
	boolean mSubscribedToGold = false;

	// Does the user have an active subscription to the Silver plan?
	boolean mSubscribedToSilver = false;

	//Bronze is not yet implemented into the current version of the application
	// Does the user have an active subscription to the Bronze plan?
	//boolean mSubscribedToBronze = false;

	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = 10001;

	// SKU for gold subscription
	static final String SKU_GOLD_SUBSCRIPTION = "gold_subscription";

	// SKU for silver subscription
	static final String SKU_SILVER_SUBSCRIPTION = "silver_subscription";

	//Bronze is not yet implemented into the current version of the application
	// SKU for bronze subscription
	//static final String SKU_BRONZE_SUBSCRIPTION = "bronze_subscription";

	// The helper object
	IabHelper mHelper;

	private LinearLayout indicators;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factpack_info);

		// Force sliding transition between activities
		overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);

		mItem = FactPacks.sFactPacksMap.get(getIntent().getStringExtra(
				FactPackInfoFragment.ARG_ITEM_ID));

		indicators = (LinearLayout) this.findViewById(R.id.factpack_indicators);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Set the title to be the FactPack name
		getActionBar().setTitle(mItem.title);

		// If this is the initial activity load
		if (savedInstanceState == null) {
			// Load icon asynchronously
			ImageView icon = (ImageView) findViewById(R.id.factpack_icon);
			new RetrieveIcon(icon, getApplicationContext())
			.execute(mItem.largeIconId);
			// Set title
			((TextView) findViewById(R.id.factpack_title)).setText(mItem.title);
			// Set description text
			((TextView) findViewById(R.id.factpack_info))
			.setText(mItem.longDescription);

			//list all indicators


			List<Fact> list = Facts.sFacts;
			if(!list.isEmpty()){
				((TextView) findViewById(R.id.factpack_indicators_title))
				.setText("The fact pack contains the following facts: ");
				for(Fact fact:list){
					TextView view = new TextView(this);
					view.setText(fact.name);
					view.setPadding(5, 5, 5, 5);
					indicators.addView(view);

				}
			}

			// Show/hide subscription buttons
			if (mItem.paymentRequired) {
				findViewById(R.id.factpack_paid).setVisibility(View.GONE);
				findViewById(R.id.factpack_subscribe).setVisibility(
						View.VISIBLE);
			} else {
				findViewById(R.id.factpack_paid).setVisibility(View.VISIBLE);
				findViewById(R.id.factpack_subscribe).setVisibility(View.GONE);
			}
		}


		String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE"; //TODO

		/*Some sanity checks to see if the developer (that's you!) really followed the
	    // instructions to run this sample (don't put these checks on your app!)
	    if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
	    throw new RuntimeException("Please put your app's public key in MainActivity.java. See README.");
	        }
	        if (getPackageName().startsWith("com.example")) {
	            throw new RuntimeException("Please change the sample's package name! See README.");
	        }
		 */   
		// Create the helper, passing it our context and the public key to verify signatures with
		Log.d(TAG, "Creating IAB helper.");
		mHelper = new IabHelper(this, base64EncodedPublicKey);

		// enable debug logging (for a production application, you should set this to false).
		mHelper.enableDebugLogging(true);

		// Start setup. This is asynchronous and the specified listener
		// will be called once setup completes.
		Log.d(TAG, "Starting setup.");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				Log.d(TAG, "Setup finished.");

				if (!result.isSuccess()) {
					// there was a problem.
					Log.d(TAG, "Problem setting up In-app Billing: " + result);
					return;
				}

				// IAB is fully set up. Now, get an inventory of stuff we own.
				Log.d(TAG, "Setup successful. Querying inventory.");
				mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});

	}

	// Listener that's called when we finish querying the items and subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Log.d(TAG, "Query inventory finished.");
			if (result.isFailure()) {
				//complain("Failed to query inventory: " + result);
				return;
			}

			Log.d(TAG, "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Do we have the gold subscription plan?
			Purchase goldSubscriptionPurchase = inventory.getPurchase(SKU_GOLD_SUBSCRIPTION);
			mSubscribedToGold = (goldSubscriptionPurchase != null && 
					verifyDeveloperPayload(goldSubscriptionPurchase));
			Log.d(TAG, "User " + (mSubscribedToGold ? "HAS" : "DOES NOT HAVE") 
					+ " gold subscription.");
			if (mSubscribedToGold) {
				//TODO
			}


			// Do we have the silver subscription plan?
			Purchase silverSubscriptionPurchase = inventory.getPurchase(SKU_SILVER_SUBSCRIPTION);
			mSubscribedToSilver = (silverSubscriptionPurchase != null && 
					verifyDeveloperPayload(silverSubscriptionPurchase));
			Log.d(TAG, "User " + (mSubscribedToSilver ? "HAS" : "DOES NOT HAVE") 
					+ " silver subscription.");
			if (mSubscribedToSilver) {
				//TODO
			}

			/*
			// Do we have the bronze subscription plan?
			Purchase bronzeSubscriptionPurchase = inventory.getPurchase(SKU_BRONZE_SUBSCRIPTION);
			mSubscribedToBronze = (bronzeSubscriptionPurchase != null && 
					verifyDeveloperPayload(bronzeSubscriptionPurchase));
			Log.d(TAG, "User " + (mSubscribedToBronze ? "HAS" : "DOES NOT HAVE") 
					+ " bronze subscription.");
			if (mSubscribedToBronze) {
				//TODO
			}
			 */
			Log.d(TAG, "Initial inventory query finished; enabling main UI.");
		}
	};


	/*
	public void onBronzeButtonClicked(View arg0) {

	}
	 */
	public void onSilverButtonClicked(View arg0) {
		if (!mHelper.subscriptionsSupported()) {
			complain("Subscriptions not supported on your device yet. Sorry!");
			return;
		}

		if (mSubscribedToGold) {
			complain("No need! You're subscribed to Gold");
			return;
		}

		/* TODO: for security, generate your payload here for verification. See the comments on 
		 *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use 
		 *        an empty string, but on a production app you should carefully generate this. */
		String payload = ""; 

		Log.d(TAG, "Launching purchase flow for infinite gas subscription.");
		mHelper.launchPurchaseFlow(this,
				SKU_SILVER_SUBSCRIPTION, IabHelper.ITEM_TYPE_SUBS, 
				RC_REQUEST, mPurchaseFinishedListener, payload); 

	}

	public void onGoldButtonClicked(View arg0) {
		if (!mHelper.subscriptionsSupported()) {
			complain("Subscriptions not supported on your device yet. Sorry!");
			return;
		}

		/* TODO: for security, generate your payload here for verification. See the comments on 
		 *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use 
		 *        an empty string, but on a production app you should carefully generate this. */
		String payload = ""; 

		Log.d(TAG, "Launching purchase flow for infinite gas subscription.");
		mHelper.launchPurchaseFlow(this,
				SKU_SILVER_SUBSCRIPTION, IabHelper.ITEM_TYPE_SUBS, 
				RC_REQUEST, mPurchaseFinishedListener, payload); 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		}
		else {
			Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}


	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();

		/*
		 * TODO: verify that the developer payload of the purchase is correct. It will be
		 * the same one that you sent when initiating the purchase.
		 * 
		 * WARNING: Locally generating a random string when starting a purchase and 
		 * verifying it here might seem like a good approach, but this will fail in the 
		 * case where the user purchases an item on one device and then uses your app on 
		 * a different device, because on the other device you will not have access to the
		 * random string you originally generated.
		 *
		 * So a good developer payload has these characteristics:
		 * 
		 * 1. If two different users purchase an item, the payload is different between them,
		 *    so that one user's purchase can't be replayed to another user.
		 * 
		 * 2. The payload must be such that you can verify it even when the app wasn't the
		 *    one who initiated the purchase flow (so that items purchased by the user on 
		 *    one device work on other devices owned by the user).
		 * 
		 * Using your own server to store and verify developer payloads across app
		 * installations is recommended.
		 */

		return true;
	}

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
			if (result.isFailure()) {
				complain("Error purchasing: " + result);
				return;
			}
			if (!verifyDeveloperPayload(purchase)) {
				complain("Error purchasing. Authenticity verification failed.");
				return;
			}

			Log.d(TAG, "Purchase successful.");

			if (purchase.getSku().equals(SKU_GOLD_SUBSCRIPTION)) {
				// bought the gold subscription
				Log.d(TAG, "Gold subscription purchased.");
				alert("Thank you for subscribing to Gold!");
				mSubscribedToGold = true;

			}else if (purchase.getSku().equals(SKU_SILVER_SUBSCRIPTION)) {
				// bought the silver subscription
				Log.d(TAG, "Silver subscription purchased.");
				alert("Thank you for subscribing Silver!");
				mSubscribedToSilver = true;

			}
		}
	};

	// We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// Force sliding transition between activities
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);

		//clear the indicator list
		indicators.removeAllViews();

	}

	@Override
	public void onStop(){
		super.onStop();

		indicators.removeAllViews();
	}

	void complain(String message) {
		Log.e(TAG, "**** SocialLandscape Error: " + message);
		alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		Log.d(TAG, "Showing alert dialog: " + message);
		bld.create().show();
	}

}

