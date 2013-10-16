package com.blitzm.sociallandscape.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.blitzm.sociallandscape.R;

public class SettingsActivity extends FragmentActivity {

	Button registration;
	Button login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// Force sliding transition between activities
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);

		/**
		 * if the calling activity is SocialLandscapeActivity, then change the
		 * anim direction.
		 */
		Intent intent = getIntent();

		if (intent != null) {
			String string = intent.getStringExtra("parent");
			if (string != null) {
				if (string.equals("sociallandscape"))
					overridePendingTransition(R.anim.left_slide_in,
							R.anim.left_slide_out);
			}
		}

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		registration = (Button) this
				.findViewById(R.id.settings_register_button);
		login = (Button) this.findViewById(R.id.settings_registered_button);

		registration.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SettingsActivity.this,
						RegistrationActivity.class);
				startActivity(intent);
			}
		});

		login.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SettingsActivity.this,
						LoginActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// Force sliding transition between activities
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	}

}
