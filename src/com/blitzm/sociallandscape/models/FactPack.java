package com.blitzm.sociallandscape.models;

import java.util.Date;

/**
 * 
 * @author Jan
 * 
 */
public class FactPack {
	
	public String appleProductId;
	public String imageId;
	public String largeIconId;
	public String shortDescription;
	public String longDescription;
	public String title;
	public boolean paymentRequired;
	public String versionRequired;
	public Date releasedDate;

	@Override
	public String toString() {
		return title;
	}
}