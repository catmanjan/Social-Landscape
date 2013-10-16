package com.blitzm.sociallandscape.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blitzm.sociallandscape.fragments.FactPackListFragment;

/**
 * 
 * @author Jan
 * 
 */
public class FactPacks {

	/**
	 * In memory store of fact pack data
	 */
	public static List<FactPack> sFactPacks = new ArrayList<FactPack>();

	/**
	 * Static link between fact pack ID and model
	 */
	public static Map<String, FactPack> sFactPacksMap = new HashMap<String, FactPack>();

	/**
	 * Toggle after fact packs initial load
	 */
	public static boolean sLoaded = false;

	public static void addItem(FactPack item) {
		sFactPacks.add(item);
		sFactPacksMap.put(item.appleProductId, item);
	}

	public static void clear() {
		FactPacks.sFactPacks.clear();
		FactPacks.sFactPacksMap.clear();
	}

	public static void notifyDataSetChanged() {
		if (FactPackListFragment.arrayAdapter != null) {
			FactPackListFragment.arrayAdapter.notifyDataSetChanged();
		}
	}
}
