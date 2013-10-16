package com.blitzm.sociallandscape.models;

import java.util.List;

import com.blitzm.sociallandscape.models.nodes.CategoryDataNode;
import com.blitzm.sociallandscape.models.nodes.CompositionDataNode;
import com.blitzm.sociallandscape.models.nodes.DistanceDataNode;
import com.blitzm.sociallandscape.models.nodes.LocationDataNode;
import com.blitzm.sociallandscape.models.nodes.TimeDataNode;

/**
 * 
 * @author Jan
 * 
 */
public class Fact {

	public String name;
	public String measure;
	public String dataType;
	public String imageId;

	public List<LocationDataNode> locationSeries;
	public List<CategoryDataNode> categorySeries;
	public List<CompositionDataNode> compositionSeries;
	public List<TimeDataNode> timeSeries;
	public DistanceDataNode distanceNode;

	@Override
	public String toString() {
		return name;
	}
}