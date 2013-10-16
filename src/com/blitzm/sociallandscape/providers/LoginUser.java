package com.blitzm.sociallandscape.providers;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.activities.AccountActivity;
import com.blitzm.sociallandscape.models.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

/**
 * this class extends asyncTask to do login task
 * @author Siyuan Zhang
 *
 */
public class LoginUser extends AsyncTask<String, Void, String>{

    /**
     * Weak reference to calling activity
     */
    private final WeakReference<Activity> mActivity;

    /**
     * Track error in alternate thread
     */
    private Throwable mThrowable;
    
    private String username;
    private String password;
    
    public LoginUser(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }
    @Override
    protected String doInBackground(String... args) {
        // TODO Auto-generated method stub
        String url = args[0];
        String username = args[1];
        this.username = username;
        String password = args[2];
        this.password = password;
        url += "?j_username=" + username;
        url += "&j_password=" + password;
        url += "&ajax=true";
        String result = new String();
        try {  
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            // Apply HTTP headers
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            // Actually retrieve data
            result = client.execute(post, new BasicResponseHandler());
            CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
            User.clearCookie();
            User.cookie=mCookieStore;
            List<Cookie> cookies = mCookieStore.getCookies();
            if(cookies.isEmpty()){
                Log.d("SocialLandscape", "Empty Cookie");
            }else{
                for (int i = 0; i < cookies.size(); i++) {
                  //get the JSESSIONID
                  if ("JSESSIONID".equals(cookies.get(i).getName())) {
                      User.clearJSESSION();
                      User.setJSESSION(cookies.get(i).getValue());
                      break;
                  }
                }
                
                
            }
            
//            HttpClient client = new DefaultHttpClient();
//            HttpPost loginRequest = new HttpPost(url);
//
//            // Apply HTTP headers
//            loginRequest.setHeader("Accept", "application/json");
//            loginRequest.setHeader("Content-type", "application/json");
//                      
//            // request parameters
//            List <NameValuePair> params=new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("j_username", username));
//            params.add(new BasicNameValuePair("j_password", password));
//            params.add(new BasicNameValuePair("ajax", "true"));
//            
//            HttpResponse response;
//            HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8"); 
//            loginRequest.setEntity(entity);
//            
//            //send request
//            response = client.execute(loginRequest);
//           
//            if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){     
//                //get response
//                result = EntityUtils.toString(response.getEntity());
//                CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
//                List<Cookie> cookies = mCookieStore.getCookies();
//                for (int i = 0; i < cookies.size(); i++) {
//                    //get the JSESSIONID
//                    if ("JSESSIONID".equals(cookies.get(i).getName())) {
//                        User.clearJSESSION();
//                        User.setJSESSION(cookies.get(i).getValue());
//                        break;
//                    }
//                }
//                User.isLogged = true;
//                gotoAccountPage();
//            }            
            
        } catch (ClientProtocolException e) {
            Log.e("SocialLandscape", "ClientProtocolException", e);
            mThrowable = e;
        } catch (IOException e) {
            Log.e("SocialLandscape", "IOException", e);
            mThrowable = e;
        }
        
        try {
            //use JSON to parse the result
            JSONObject LoginJSON = new JSONObject(result);
            
            String responseCode = LoginJSON.getString("success");
            if(responseCode.equals("true")){
                User.isLogged = true;
            }              
        } catch (Exception e) {
            Log.e("SocialLandscape", "Registration parse error", e);
        }
        return result;
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
    
    
    @Override
    protected void onPostExecute(String result) {
        //do after the doInBackground method
        final Activity activity = mActivity.get();

        if (activity != null) {
            // Hide progress dialog (if any)
            Utility.hideProgress(activity);
            if(User.isLogged){
                saveAccount();
                gotoAccountPage();
                
                if (mThrowable != null) {
                    User.isLogged = false;
                } else {
                    User.isLogged = true;
                }
                
            }else{
                messageBox("The email and password you entered is not a valid account." +
                        "Check you have entered them correctly", "Login Failed");
                
                Log.d("SocialLandscape", "JSON parse error");
                
            }         
            
        }
        
    }

    //save the user information in local
    private void saveAccount(){
        SharedPreferences mSharedPreferences = mActivity.get().getSharedPreferences("SL_Account", 0);  
//      SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  

        SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
        mEditor.putString("username", this.username); 
        mEditor.putString("password", this.password);
        mEditor.putString("JSESSIONID", User.JSESSIONID);
        mEditor.commit();  
    }   
    
    //switch to the account screen
    public void gotoAccountPage(){
        Intent intent = new Intent(mActivity.get(), AccountActivity.class);
        intent.putExtra("username", this.username);
        mActivity.get().startActivity(intent);
        mActivity.get().finish();
    }

}
