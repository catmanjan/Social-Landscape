package com.blitzm.sociallandscape.providers;

import java.io.IOException;

import java.lang.ref.WeakReference;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.models.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is used for logging out the user
 * @author Siyuan Zhang
 *
 */
public class LogoutUser extends AsyncTask<String, Void, String>{

    /**
     * Weak reference to calling activity
     */
    private final WeakReference<Activity> mActivity;

    /**
     * Track error in alternate thread
     */

    public LogoutUser(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }
    
    @Override
    protected String doInBackground(String... args) {
        // TODO Auto-generated method stub 
        // TODO Auto-generated method stub
        String url = args[0];
        String JSESSIONID = args[1];
        
        String result = new String();
        
        url += "?ajax=true";
        try {          
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost logoutRequest = new HttpPost(url);
            // Apply HTTP headers
            logoutRequest.setHeader("Accept", "application/json");
            logoutRequest.setHeader("Content-type", "application/json");
            //include the JSESSIONID in the cookie
            logoutRequest.setHeader("Cookie", "JSESSIONID="+JSESSIONID);
            
            HttpResponse response;
            //send logout request to the server
            response = client.execute(logoutRequest);
            
//            result = client.execute(logoutRequest, new BasicResponseHandler());
            if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){ 
                //if there is no exception then clear the local cache
                result = EntityUtils.toString(response.getEntity());
                User.clear();
            }
        } catch (ClientProtocolException e) {
                Log.e("SocialLandscape", "ClientProtocolException", e);
        } catch (IOException e) {
                Log.e("SocialLandscape", "IOException", e);
        }
        

        return result;
    } 
    
    @Override
    protected void onPostExecute(String result) {
        
        
        final Activity activity = mActivity.get();
        
        if (activity != null) {
            
            // Hide progress dialog (if any)
            Utility.hideProgress(activity);

            if(User.isLogged){
                //messageBox("Logout Fail","Error");
                //if logout failed then force it quit
                User.clear();
                this.clearLocalRecord();
                mActivity.get().finish();
            }else{               
                this.clearLocalRecord();
                mActivity.get().finish();
            }
            
        }

    }
    
    /**
     * get message content and title then display them in a pop-up box.
     * @param msg
     * @param title
     */
    public void messageBox(String msg, String title) {
        Dialog alertDialog = new AlertDialog.Builder(mActivity.get())
                .setTitle(title).setMessage(msg).setNeutralButton("OK", null)
                .create();

        alertDialog.show();
    }
    
    /**
     * clear the local cache
     */
    private void clearLocalRecord(){
        SharedPreferences mSharedPreferences = mActivity.get().getSharedPreferences("SL_Account", 0);  

        SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
        mEditor.clear();
        mEditor.commit();
    }
}
