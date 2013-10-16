package com.blitzm.sociallandscape.providers;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.activities.FactPackInfoActivity;
import com.blitzm.sociallandscape.activities.SocialLandscapeActivity;
import com.blitzm.sociallandscape.fragments.FactPackInfoFragment;
import com.blitzm.sociallandscape.models.Fact;
import com.blitzm.sociallandscape.models.Facts;
import com.blitzm.sociallandscape.models.User;
import com.blitzm.sociallandscape.models.nodes.CategoryDataNode;
import com.blitzm.sociallandscape.models.nodes.CompositionDataNode;
import com.blitzm.sociallandscape.models.nodes.DistanceDataNode;
import com.blitzm.sociallandscape.models.nodes.LocationDataNode;
import com.blitzm.sociallandscape.models.nodes.TimeDataNode;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * This class is similar to the RetrieveFacts class.
 * The only purpose for this class is to retrieve indicator information from the server
 * then list them when the info button is pressed
 * 
 * @author Siyuan Zhang
 * 
 */
public class SimpleRetrieveFacts extends AsyncTask<String, Void, String> {

    /**
     * Do not add directly to Facts array adapter, not thread safe
     */
    ArrayList<Fact> facts = new ArrayList<Fact>();

    /**
     * Weak reference to calling activity
     */
    private final WeakReference<Activity> mActivity;
    
    private String id;
    
    public SimpleRetrieveFacts(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }

    @Override
    protected void onPostExecute(String result) {
        final Activity activity = mActivity.get();
        // Don't use Facts.clear() here, it wipes more than the fact list
        Facts.sFacts.clear();

        // Add temporary facts to array adapter
        for (Fact fact : facts) {
            Facts.addItem(fact);
        }


        
        //notify that the indicators have been retrieved
        Facts.isIndicatorsLoaded = true;
        Facts.retrievedIndicatorID = id;
        
        Intent detailIntent = new Intent(activity, FactPackInfoActivity.class);
        detailIntent.putExtra(FactPackInfoFragment.ARG_ITEM_ID, id);
        activity.startActivity(detailIntent);
        
        
        // Hide progress dialog (if any)
        if(mActivity != null)
            Utility.hideProgress(activity);
    }

    @Override
    protected String doInBackground(String... args) {
        // TODO format/error checking here
        String url = args[0];
        String factPackId = args[1];
        String latitude = args[2];
        String longitude = args[3];
        id = args[4];
        String result = new String();
        

        // Build service request URL from parameters
        url += "?factPackId=" + factPackId;
        url += "&x=" + longitude;
        url += "&y=" + latitude;
        
        // JM: TODO replace with proper data
        url += "&deviceId=XXXXYYYYZZZZ";
        url += "&receiptData=AAAABBBBCCCCDDDDEEEE";

        Log.d("SocialLandscape", "RetrieveFacts: " + url);

        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
    
            // Apply HTTP headers (optional, but good practice)
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
    
            // Actually retrieve data
            result = client.execute(post, new BasicResponseHandler());
        } catch (ClientProtocolException e) {
            Log.e("SocialLandscape", "ClientProtocolException", e);
        } catch (IOException e) {
            Log.e("SocialLandscape", "IOException", e);
        }
    
        try {
            // Break out of incorrect encapsulating array
            JSONArray factJsonArray = new JSONArray(result);
            JSONObject factJson = factJsonArray.getJSONObject(0);
    
            // Indicator (AKA fact) array
            JSONArray indicatorsJson = factJson.getJSONArray("indicators");
            for (int i = 0; i < indicatorsJson.length(); i++) {
                // Extract fact JSON hierarchy model
                JSONObject indicatorJson = indicatorsJson.getJSONObject(i);
    
                    // Populate data model
                Fact fact = new Fact();
                fact.name = indicatorJson.getString("name");
                fact.measure = indicatorJson.getString("measure");
                fact.dataType = indicatorJson.getString("dataType");
                fact.imageId = indicatorJson.getString("imageId");                      
    
                // Add fact to temporary fact list
                facts.add(fact);
                
               
            }
        } catch (Exception e) {
            Log.e("SocialLandscape", "RetrieveFacts parse error", e);
        }
        return result;
    }
}
