package com.blitzm.sociallandscape.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.blitzm.sociallandscape.R;

/**
 * Author: Siyuan Zhang Date: April 27, 2013 This is the splash screen. It
 * presents a image to users and start the main activity after 2 seconds It
 * allows the application do preparation works.
 */
public class SplashScreenActivity extends Activity {
	// switch
	private boolean mActive = true;
	// how long the screen last
	// TODO move this to config
	private int mSplashTime = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide title bar on splash screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_screen_splash);

		Thread splashTread = new Thread() {
			@Override
			public void run() {
				try {
					int waited = 0;
					while (mActive && (waited < mSplashTime)) {
						sleep(100);
						if (mActive) {
							waited += 100;
						}
					}
				} catch (InterruptedException e) {

				} finally {
					finish();
					Intent socialLandScapeIntent = new Intent(
							SplashScreenActivity.this,
							SocialLandscapeActivity.class);
					startActivity(socialLandScapeIntent);

				}
			}
		};
		splashTread.start();
	}

}
