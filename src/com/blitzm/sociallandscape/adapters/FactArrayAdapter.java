package com.blitzm.sociallandscape.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.models.Fact;
import com.blitzm.sociallandscape.models.nodes.LocationDataNode;
import com.blitzm.sociallandscape.providers.RetrieveIcon;

/**
 * 
 * @author Jan
 * 
 */
public class FactArrayAdapter extends ArrayAdapter<Fact> {

	private int listViewItemId;
	private List<Fact> objects;

	public FactArrayAdapter(Context context, int listViewItemId,
			List<Fact> objects) {
		super(context, 0, objects);
		this.listViewItemId = listViewItemId;
		this.objects = objects;
	}

	public static class ViewHolder {
		public ImageView image1;
		public TextView text1;
		public TextView text2;
		public TextView text3;
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
			holder.text1 = (TextView) view.findViewById(R.id.text1);
			holder.text2 = (TextView) view.findViewById(R.id.text2);
			holder.text3 = (TextView) view.findViewById(R.id.text3);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final Fact item = objects.get(position);
		if (item != null) {
			// Load icon asynchronously
			Utility.execute(new RetrieveIcon(holder.image1, getContext()),
					item.imageId);
			// Set fact title
			holder.text1.setText(item.name);
			// Set fact value (shown to the right)
			holder.text2.setText(item.measure);
			// If fact is populated with location data...
			if (item.locationSeries != null) {
				// Local data node, value corresponds to overlay area
				LocationDataNode localDataNode = null;
				// Iterate through series to find local data node
				for (LocationDataNode node : item.locationSeries) {
					// Level: 0 is the "closest" node approximation available
					if (node.level == 0) {
						localDataNode = node;
						break;
					}
				}
				// If the local data node wasn't found, just skip it
				if (localDataNode != null) {
					// Value field of the local data node
					int value = localDataNode.value;
					//if value is negative (NULL input), make it zero
					if(value<0)value=0;
					// Create text value
					String text = "";
					// If data type is dollars, prepend dollar symbol
					if (item.dataType.equals("dollars")) {
						text = "$";
					}
					// Append real value to string
					text += String.valueOf(value);
					// If data type is percentage, append percent symbol
					if (item.dataType.equals("percentage")) {
						text += "%";
					}
					// Set fact item TextView value
					holder.text3.setText(text);
				}
			}
		}

		return view;
	}

}
