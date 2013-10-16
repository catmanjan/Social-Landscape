package com.blitzm.sociallandscape.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.activities.FactPackDetailActivity;
import com.blitzm.sociallandscape.activities.SocialLandscapeActivity;
import com.blitzm.sociallandscape.models.Fact;
import com.blitzm.sociallandscape.models.Facts;
import com.blitzm.sociallandscape.models.Info;
import com.blitzm.sociallandscape.models.nodes.CategoryDataNode;
import com.blitzm.sociallandscape.models.nodes.CompositionDataNode;
import com.blitzm.sociallandscape.models.nodes.LocationDataNode;
import com.blitzm.sociallandscape.views.BarChartAxisView;
import com.blitzm.sociallandscape.views.BarChartBarView;
import com.blitzm.sociallandscape.views.BarChartLabelView;
import com.blitzm.sociallandscape.views.PieChartView;

/**
 * A fragment representing a single Fact Pack detail screen. This fragment is
 * either contained in a {@link SocialLandscapeActivity} in two-pane mode (on
 * tablets) or a {@link FactPackDetailActivity} on handsets.
 * 
 * @author Jan
 * @author Siyuan Zhang
 * 
 */
public class FactDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Fact mItem;

	// the width of the histogram
	public static int WIDTH;
	// the height of the histogram
	public static int HEIGHT;

	private BarChartBarView bar_view;
	private PieChartView pie_view;
	private LinearLayout graph_container;
	private FrameLayout chart_container;
	private TextView chart_title;

	// margin of the histogram
	private int margin = 30;

	// HashMap<String, String> hmap = new HashMap<String, String>();
	// list storing the inputs
	ArrayList<Info> list = new ArrayList<Info>();

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FactDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = Facts.sFactsMap.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chart, container,
				false);

		// rootView.setPadding(margin, margin, margin, margin);
		if (mItem != null) {
			// TODO Populate graph data

			DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
			HEIGHT = displaymetrics.heightPixels;
			WIDTH = displaymetrics.widthPixels;

			graph_container = (LinearLayout) rootView
					.findViewById(R.id.graph_container);
			chart_container = (FrameLayout) rootView
					.findViewById(R.id.chart_container);
			chart_title = (TextView) rootView.findViewById(R.id.chart_title);
			chart_title.setText(mItem.measure);

			graph_container.setPadding(margin, margin, margin, margin);
			if (mItem.locationSeries != null && mItem.categorySeries == null && mItem.compositionSeries == null) {
				displayBarChart("location");
			}
			
			if(mItem.locationSeries == null && mItem.categorySeries != null && mItem.compositionSeries == null) {
			    displayBarChart("category");
			}
			
			if (mItem.locationSeries == null && mItem.categorySeries == null && mItem.compositionSeries != null) {
				displayPieChart();
			}

		}
		return rootView;
	}

	/**
	 * 
	 * @return max value
	 */
	public float getMaxValue(String dataNodeType) {
		float max = 0;
		float cur;
		if(dataNodeType.equals("location")){
    		for (LocationDataNode node : mItem.locationSeries) {
    			if ((cur = node.value) > max) {
    				max = cur;
    			}
    		}
		}else{
		    for (CategoryDataNode node : mItem.categorySeries) {
                if ((cur = parseFloat(node.value)) > max) {
                    max = cur;
                }
            }
		}
		return max;
	}
	
	public float parseFloat(String str){
	    float result=0;
	    if(str=="null"){
	        str = "0";
	    }
	    try{
	        result = Float.parseFloat(str);
	    }catch(Exception e){
	        result = 0;
	    }
	    return result;
	}

	/**
	 * display the bar chart
	 */
	public void displayBarChart(String DataNodeType) {
		
		int barChartHeight = (int)(HEIGHT/3.0*2);
		// add x-axis and y-axis
		chart_container.addView(new BarChartAxisView(getActivity(), WIDTH,
		        barChartHeight, margin));
		float max = getMaxValue(DataNodeType);
		
		if(DataNodeType.equals("location")){
		//if using locationDataNode
		    
		// add bar
		
    		int size = mItem.locationSeries.size();
    		if (mItem.locationSeries != null) {
    			for (LocationDataNode node : mItem.locationSeries) {
    				int level = node.level;
    				bar_view = new BarChartBarView(getActivity(), node.value, max,
    						(float) ((WIDTH - 2 * margin) / size), barChartHeight,
    						mItem.dataType, DataNodeType);
    				if (node.level < size - 1) {
    					final String title = getMessageTitle(level);
    					final String message = getMessageContent(level);
    					bar_view.setClickable(true);
    					bar_view.setOnClickListener(new View.OnClickListener() {
    
    						@Override
    						public void onClick(View v) {
    							// TODO Auto-generated method stub
    							messageBox(message, title);
    						}
    					});
    				}
    				graph_container.addView(bar_view, new LayoutParams(
    						(int) ((float) WIDTH - 2 * margin) / size,
    						LayoutParams.MATCH_PARENT));
    			}
    		}
    		
    		putValue();
    		// add label
    		chart_container.addView(new BarChartLabelView(getActivity(), barChartHeight
    				+ margin, (WIDTH - 2 * margin) / size, margin, list,DataNodeType));
		}else{
		//if using categoryDataNode    
		    int size = mItem.categorySeries.size();
            if (mItem.categorySeries != null) {
                for (CategoryDataNode node : mItem.categorySeries) {
                    //int order = node.order;
                    
                    bar_view = new BarChartBarView(getActivity(), parseFloat(node.value) , max,
                            (float) ((WIDTH - 2 * margin) / size), barChartHeight,
                            mItem.dataType, DataNodeType);
//                    if (node.order < size - 1) {
//                        final String title = getMessageTitle(level);
//                        final String message = getMessageContent(level);
//                        bar_view.setClickable(true);
//                        bar_view.setOnClickListener(new View.OnClickListener() {
//    
//                            @Override
//                            public void onClick(View v) {
//                                // TODO Auto-generated method stub
//                                messageBox(message, title);
//                            }
//                        });
//                    }
                    
                    //parse the label
                    String range_from = "";
                    String range_to = "";
                    String unparsedLabel = node.label;
                    
                    if(unparsedLabel.contains("family")&&unparsedLabel.contains("child")){
                        String parsedLabel = unparsedLabel.replaceAll("_", " ");
                        parsedLabel = parsedLabel.replaceAll("Families", "");
                        
                        if(parsedLabel.contains("Couple")){
                            range_from = "Couple family";
                        }
                        
                        if(parsedLabel.contains("One parent")){
                            range_from = "One Parent family";
                        }
                        if(parsedLabel.contains("no children")&&!parsedLabel.contains("15")){
                            range_to = "no childern";
                        }
                        if(parsedLabel.contains("no children")&&parsedLabel.contains("15")){
                            range_to = "childern 15+";
                        }
                        if(parsedLabel.contains("children")&&parsedLabel.contains("15")&&!parsedLabel.contains("no")){
                            range_to = "childern under 15";
                        }
                        
                        list.add(new Info(range_from+" "+ range_to,"0"));
                    }else{
                        unparsedLabel = unparsedLabel.replaceAll("One", "1");
                        unparsedLabel = unparsedLabel.replaceAll("Two", "2");
                        unparsedLabel = unparsedLabel.replaceAll("Three", "3");
                        unparsedLabel = unparsedLabel.replaceAll("Four", "4");
                        unparsedLabel = unparsedLabel.replaceAll("Five", "5");
                        unparsedLabel = unparsedLabel.replaceAll("Six", "6");
                        unparsedLabel = unparsedLabel.replaceAll("Seven", "7");
                        unparsedLabel = unparsedLabel.replaceAll("Eight", "8");
                        unparsedLabel = unparsedLabel.replaceAll("Nine", "9");
                        unparsedLabel = unparsedLabel.replaceAll("Ten", "10");
                        
                        String[] label_array = unparsedLabel.split("_");
                        
                        for(int i=0; i<label_array.length;i++){
                            String label = label_array[i];
                            if(!(label.matches("\\d+")
                                    ||label.equals("Negative")
                                    ||label.equals("Nil")
                                    ||label.equals("One")
                                    ||label.equals("over")
                                    ||label.equals("more")
                                    )){
                                label_array[i]="";
                            }
                        }
                        
                        for(String label : label_array){
                            if(!label.equals("")){
                                if(range_from==""){
                                    range_from = label;
                                }else if (range_to == ""){
                                    range_to = label;
                                }
                            }
                        }
                        
                        String chart_label;
                        if(range_to.equals("")){
                            chart_label = range_from;
                        }
                        else if(range_to.equals("over")||range_to.equals("more")){
                            chart_label = range_from+"+";
                        }else{
                           chart_label = range_from+"-"+range_to;
                        }
                        
                        list.add(new Info(chart_label,"0"));
                    }
                    
                    graph_container.addView(bar_view, new LayoutParams(
                            (int) ((float) WIDTH - 2 * margin) / size,
                            LayoutParams.MATCH_PARENT));
                }
            }
            // add label
            chart_container.addView(new BarChartLabelView(getActivity(), barChartHeight
                    + margin, (WIDTH - 2 * margin) / size, margin, list, DataNodeType));
		    
		}
	}

	/**
	 * display the pie chart
	 */
	public void displayPieChart() {
	    int pieChartHeight = (int)(HEIGHT/2.0);
		ArrayList<Info> list = new ArrayList<Info>();
		if (mItem.compositionSeries != null) {
			for (CompositionDataNode node : mItem.compositionSeries) {
				list.add(new Info(node.name, "" + node.value));
			}
		}
		pie_view = new PieChartView(getActivity(), (WIDTH - 32) / 2, pieChartHeight / 2
				+ pieChartHeight / 10, (WIDTH - 4 * margin) / 2, list, 1);
		graph_container.addView(pie_view, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	/**
	 * get labels of locationDataNode
	 */
	public void putValue() {
		list.add(new Info("Selected Area", "0"));
		list.add(new Info(Facts.suburbName, "0"));
		list.add(new Info(Facts.councilName, "0"));
		list.add(new Info(Facts.stateName, "0"));
		list.add(new Info("Australia", "0"));
	}

	public String getMessageTitle(int level) {
		String string = "";
		switch (level) {
		case 0:
			string = "Selected Area";
			break;
		case 1:
			string = Facts.suburbName;
			break;
		case 2:
			string = Facts.councilName;
			break;
		case 3:
			string = Facts.stateName;
			break;
		case 4:
			string = "Austrlia";
			break;
		default:
			string = "Not found";
			break;
		}
		return string;

	}

	public String getMessageContent(int level) {
		String string = "";
		switch (level) {
		case 0:
			string = "This is the smallest geographical area for which data is available. "
					+ "It is outlined on the map.";
			break;
		case 1:
			string = "This is the suburb for the selected location.";
			break;
		case 2:
			string = "This is the local government area for the selected location. "
					+ "Its is usually the area of jurisdiction for a council.";
			break;
		case 3:
			string = "This is the State or Territory for the selected location.";
			break;
		default:
			string = "No description found";
			break;

		}
		return string;
	}

	public void messageBox(String msg, String title) {
		Dialog alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle(title).setMessage(msg).setNeutralButton("OK", null)
				.create();

		alertDialog.show();
	}

}
