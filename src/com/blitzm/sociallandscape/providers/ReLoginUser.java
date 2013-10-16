package com.blitzm.sociallandscape.providers;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.activities.AccountActivity;
import com.blitzm.sociallandscape.models.User;

/**
 * This class is only using for re-logining the user.
 * Once the user have login, the username and password are stored in local.
 * The next time the user open the app, the app will automatically login 
 * @author Siyuan Zhang
 *
 */
public class ReLoginUser extends AsyncTask<String, Void, String>{

    /**
     * Weak reference to calling activity
     */
    private final WeakReference<Activity> mActivity;

    /**
     * Track error in alternate thread
     */
    private Throwable mThrowable;
    
    private String username;
    private String JSESSIONID;
    
    public ReLoginUser(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }
    @Override
    protected String doInBackground(String... args) {
        // TODO Auto-generated method stub
        String url = args[0];
        String username = args[1];
        this.username = username;
        String password = args[2];
        String JSESSIONID = args[3];
        this.JSESSIONID = JSESSIONID;

        
        //url paramters
        url += "?j_username=" + username;
        url += "&j_password=" + password;
        url += "&ajax=true";
        String result = new String();
        try {  
            
            //using http post to send login requests to the server
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            // Apply HTTP headers
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            //include the JSESSION in the cookie
            post.setHeader("Cookie", "JSESSIONID="+JSESSIONID);
            // Actually retrieve data
            result = client.execute(post, new BasicResponseHandler());
            
//            CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
//            User.clearCookie();
//            User.cookie=mCookieStore;
//
//            List<Cookie> cookies = mCookieStore.getCookies();                                    
//            if(cookies.isEmpty()){
//                Log.d("SocialLandscape", "Empty Cookie");
//            }else{
//                for (int i = 0; i < cookies.size(); i++) {
//                  //get the JSESSIONID
//                  if ("JSESSIONID".equals(cookies.get(i).getName())) {
//                      User.clearJSESSION();
//                      User.setJSESSION(cookies.get(i).getValue());
//                      
//                      break;
//                  }
//                }
//                 
//                //update the JSESSIONID in local
//            }            
            
        } catch (ClientProtocolException e) {
            Log.e("SocialLandscape", "ClientProtocolException", e);
            mThrowable = e;
        } catch (IOException e) {
            Log.e("SocialLandscape", "IOException", e);
            mThrowable = e;
        }
        
        try {
            //use JSON to parse the result, if login succeed then log the user as logged
            JSONObject LoginJSON = new JSONObject(result);
            
            String responseCode = LoginJSON.getString("success");
            if(responseCode.equals("true")){
                //if login succeed then keep the status in local memory
                User.isLogged = true;
                User.setJSESSION(this.JSESSIONID);
            }else{
                Log.d("SocialLandscape", "JSON parse error");
            }                       
        } catch (Exception e) {
            Log.e("SocialLandscape", "Registration parse error", e);
        }
        return result;
    }
    
    @Override
    protected void onPostExecute(String result) {

        final Activity activity = mActivity.get();

        if (activity != null) {
            // Hide progress dialog (if any)
            Utility.hideProgress(activity);
            if (mThrowable != null) {
                User.isLogged = false;
            } 
        }
    }
    
    /**
     * when login succeed then go to the Account screen
     */
    public void gotoAccountPage(){
        Intent intent = new Intent(mActivity.get(), AccountActivity.class);
        intent.putExtra("username", this.username);
        mActivity.get().startActivity(intent);
        mActivity.get().finish();
    }

}