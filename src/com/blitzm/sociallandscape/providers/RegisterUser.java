package com.blitzm.sociallandscape.providers;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.models.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;

/**
 * this class extneds asyncTack to do registration task
 * @author Siyuan Zhang
 *
 */
public class RegisterUser extends AsyncTask<String, Void, String>{
    /**
     * Weak reference to calling activity
     */
    private final WeakReference<Activity> mActivity;
    
    
    
    private String username;
    private String password;
    
    
    public RegisterUser(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }
    @Override
    protected String doInBackground(String... args) {
        // TODO Auto-generated method stub
        String url = args[0];
        this.username = args[1];
        this.password = args[2];
        
        String result = new String();
        // Build service request URL from parameters
        url += "?username=" + username;
        url += "&password=" + password;
        url += "&ajax=true";

        Log.d("SocialLandscape", "RegisterUser: " + url);

       
        try {               
//            HttpClient client = new DefaultHttpClient();
//            HttpPost registerRequest = new HttpPost(url);
//
//            // Apply HTTP headers
//            registerRequest.setHeader("Accept", "application/json");
//            registerRequest.setHeader("Content-type", "application/json");
//                      
//            // request parameters
//            List <NameValuePair> params=new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("username", this.username));
//            params.add(new BasicNameValuePair("password", this.password));
//            params.add(new BasicNameValuePair("ajax", "true"));
//            
//            HttpResponse response;
//            HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8"); 
//            registerRequest.setEntity(entity);
//            
//            //send request
//            response = client.execute(registerRequest);
//           
//            if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){     
//                //get response
//                result = EntityUtils.toString(response.getEntity());
//            }
//            

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            // Apply HTTP headers
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
            //use JSON to parse the result
            JSONObject RegistrationJson = new JSONObject(result);
            
            int responseCode = RegistrationJson.getInt("resultCode");
            if(responseCode!=0){
                if(responseCode == 30001){
                    //if the registration succeed
                    User.isRegistered = true;
                    Log.d("SocialLandscape", "registeration succeed");
                }else if(responseCode == 30004){
                    //if the email has already been registered, returning the message
                    messageBox("The email given is already in use. " +
                            "The user can not use that email to create a new registration."
                            ,"Already Registered");
                    Log.d("SocialLandscape", "email in use");
                }else if(responseCode == 30005){
                    //if the parameters are invalid, returning the message
                    messageBox("The parameters given with the request were invalid."
                            ,"Invalid Parameters");
                    Log.d("SocialLandscape", "parameters invalid");
                }else{
                    messageBox("Unknown error occurs"
                            ,"Unknown error");
                    Log.d("SocialLandscape", "unknown error..usually because email in use");
                }
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
        //if registration succeed, then save the user info and login
final Activity activity = mActivity.get();
        
        if (activity != null) {
            
            // Hide progress dialog (if any)
            
            
            if(User.isRegistered){
           
                this.loginAccount();
            }else{
                Utility.hideProgress(activity);
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
    
    

    //login 
    private void loginAccount(){
        Activity activity = mActivity.get();
        Utility.execute(new LoginUser(activity),
                activity.getString(R.string.login_url), username, password);
    }
}
