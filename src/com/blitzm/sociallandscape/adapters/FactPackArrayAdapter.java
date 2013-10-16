package com.blitzm.sociallandscape.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.fragments.FactPackListFragment;
import com.blitzm.sociallandscape.models.FactPack;
import com.blitzm.sociallandscape.providers.RetrieveIcon;

/**
 * 
 * @author Jan
 * 
 */
public class FactPackArrayAdapter extends ArrayAdapter<FactPack> {

	private final FactPackListFragment mFragment;
	private int listViewItemId;
	private List<FactPack> objects;

	public FactPackArrayAdapter(FactPackListFragment fragment, Context context,
			int listViewItemId, List<FactPack> objects) {
		super(context, 0, objects);
		mFragment = fragment;
		this.listViewItemId = listViewItemId;
		this.objects = objects;
	}

	public static class ViewHolder {
		public ImageView image1;
		public ImageView image2;
		public ImageView image3;
		public TextView text1;
		public TextView text2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;

		if (view == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(listViewItemId, null);

			holder = new ViewHolder();
			holder.image1 = (ImageView) view.findViewById(R.id.image1);
			holder.image2 = (ImageView) view.findViewById(R.id.image2);
			holder.image3 = (ImageView) view.findViewById(R.id.image3);
			holder.text1 = (TextView) view.findViewById(R.id.text1);
			holder.text2 = (TextView) view.findViewById(R.id.text2);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final FactPack item = objects.get(position);
		if (item != null) {
			if (item.paymentRequired) {
				holder.image1.setImageResource(R.drawable.padlock_icon);
			} else {
				holder.image1.setImageResource(R.drawable.information_light);
			}

			// Load icon asynchronously
			new RetrieveIcon(holder.image2, getContext()).execute(item.imageId);

			holder.image3.setImageResource(R.drawable.arrow);

			// Set fact pack title
			holder.text1.setText(item.title);

			// Set fact pack sub-title
			holder.text2.setText(item.shortDescription);

			// This seems to be the best way to have multiple button items
			// in a list view, but I don't like the circular dependency it
			// creates between fragment and adapter
			holder.image1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					// Link click listener to parent
					mFragment.setFactPackInfoSelected(item.appleProductId);
				}

			});
		}

		return view;
	}

}
