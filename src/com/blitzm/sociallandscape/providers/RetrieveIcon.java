package com.blitzm.sociallandscape.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.blitzm.sociallandscape.Utility;

public class RetrieveIcon extends AsyncTask<String, Void, String> {

	private final WeakReference<ImageView> mImageViewReference;

	private Bitmap mIcon;

	private Context mContext;

	public RetrieveIcon(ImageView imageView, Context context) {
		mImageViewReference = new WeakReference<ImageView>(imageView);
		mContext = context;
	}

	@Override
	protected void onPostExecute(String result) {
		if (mImageViewReference != null && mIcon != null) {
			final ImageView imageView = mImageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(mIcon);
			}
		}
	}

	@Override
	protected String doInBackground(String... args) {
		// TODO validate url
		// TODO log errors
		String id = args[0];
		String root = mContext.getFilesDir().getPath();
		String pathName = String.format("%s/%s.png", root, id);
		String url = String.format(
				"https://apps.blitzm.com/statisticsserver-test/iconImages/%s.png",
				id);

		File file = new File(pathName);

		if (file.exists()) {
			mIcon = BitmapFactory.decodeFile(pathName);
		} else {
			mIcon = Utility.bitmapFromUrl(url);

			try {
				file.createNewFile();
				FileOutputStream out = new FileOutputStream(pathName);
				mIcon.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (FileNotFoundException e) {
				Log.e("SocialLandscape", pathName, e);
			} catch (IOException e) {
				Log.e("SocialLandscape", pathName, e);
			}
		}

		return url;
	}

}
