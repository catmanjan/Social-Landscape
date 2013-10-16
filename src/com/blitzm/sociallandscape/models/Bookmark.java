package com.blitzm.sociallandscape.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Bookmark data model, better to store geo information in primitive data types
 * rather than Android "LatLng" object, it may cause garbage collection problems
 * 
 * @author Jan
 * 
 */
public class Bookmark {

	private String name;
	private double latitude;
	private double longitude;

	public Bookmark(String name, double latitude, double longitude) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Bookmark(String name, LatLng latLng) {
		this(name, latLng.latitude, latLng.longitude);
	}

	public String getName() {
		return name;
	}

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof Bookmark))
			return false;

		// If we've gotten this far, the comparator is a Bookmark
		Bookmark bookmark = (Bookmark) other;

		// Two bookmarks are equal if their properties are the same
		return name.equals(bookmark.name) && latitude == bookmark.latitude
				&& longitude == bookmark.longitude;
	}

}
