package com.blitzm.sociallandscape.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blitzm.sociallandscape.fragments.FactListFragment;

/**
 * 
 * @author Jan
 * 
 */
public class Facts {

	// Fact data invalidated when these change
	public static String factPackId;
	public static String longitude;
	public static String latitude;

	// Global fact values retrieved from web service
	public static String suburbName;
	public static String stateName;
	public static String councilName;
	public static String bbox;

	public static boolean isIndicatorsLoaded;
	public static String retrievedIndicatorID;
	
	/**
	 * In memory store of fact data
	 */
	public static List<Fact> sFacts = new ArrayList<Fact>();

	/**
	 * Static link between fact ID and model
	 */
	public static Map<String, Fact> sFactsMap = new HashMap<String, Fact>();

	public static void addItem(Fact item) {
		sFacts.add(item);
		sFactsMap.put(item.name, item);
	}

	public static void clear() {
		sFacts.clear();
		sFactsMap.clear();

		factPackId = null;
		longitude = null;
		latitude = null;
		suburbName = null;
		stateName = null;
		councilName = null;
		bbox = null;
		isIndicatorsLoaded = false;
		retrievedIndicatorID = null;
	}

	public static void notifyDataSetChanged() {
		if (FactListFragment.arrayAdapter != null) {
			FactListFragment.arrayAdapter.notifyDataSetChanged();
		}
	}
}
