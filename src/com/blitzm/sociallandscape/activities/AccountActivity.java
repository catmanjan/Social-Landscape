package com.blitzm.sociallandscape.activities;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.models.User;
import com.blitzm.sociallandscape.providers.LogoutUser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * this class is the user interface of Account screen
 * it is only displayed when the user login 
 * @author Siyuan Zhang
 *
 */
public class AccountActivity extends FragmentActivity{
    ImageView sync_info;
    ImageView logout_info;
    
    Button btn_sync;
    Button btn_logout;
    
    TextView website_link;
    TextView v_username;
    TextView v_subscription;
    
    
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

     // Force sliding transition between activities
        overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        sync_info = (ImageView) this.findViewById(R.id.account_sync_info);
        logout_info = (ImageView) this.findViewById(R.id.account_logout_info);
        
        btn_sync = (Button) this.findViewById(R.id.account_sync);
        btn_logout = (Button) this.findViewById(R.id.account_logout);
        
        website_link = (TextView) this.findViewById(R.id.account_website);
        v_username = (TextView) this.findViewById(R.id.account_name);
        v_subscription = (TextView) this.findViewById(R.id.account_subscription_count);
                
        sync_info.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String title = "Sync";
                String message = "Syncing secures your subscriptions and also downloads any previously secured subscriptions. " +
                		"This allows you to restore subscriptions to a different device. Automatic syncing will be attempted for new purchase.";
                messageBox(message,title);
            }
            
        });
        
        logout_info.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String title = "Logout";
                String message = "Logging out removes your account including your subscriptions from this device. " +
                		"This means that this device will no longer count towards your device limit.";
                messageBox(message,title);
            }
            
        });
        
        website_link.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setData(Uri.parse("www.blitzm.com"));
                intent.setAction(Intent.ACTION_VIEW);
                AccountActivity.this.startActivity(intent);
            }
            
        });
        
        btn_sync.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
               
            }
            
        });
        
        btn_logout.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LogoutMessage("Logging out will remove any synced subscriptions from this device. " +
                        "You will have to log back in to recover them." +
                        "Are you sure you with to continue?","Logout");
            }
            
        });
        
        this.username = this.getIntent().getExtras().getString("username");
        if(username!=null || username!=""){
            v_username.setText(username);
        }
        
    }
    
    /**
     * get message content and title then display them in a pop-up box.
     * @param msg
     * @param title
     */
    public void messageBox(String msg, String title) {
        Dialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title).setMessage(msg).setNeutralButton("OK", null)
                .create();

        alertDialog.show();
    }
    
    public void LogoutMessage(String msg, String title) {
        Dialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title).setMessage(msg)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("ok", new OnClickListener(){

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        logout();
                    }
                    
                })
                .create();

        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Force sliding transition between activities
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }
    
    private void logout(){
        Log.d("SocialLandscape", "Logout Account");
        
        if (User.isLogged) {
            // Show an intrusive dialog if fact packs have never been loaded
            Utility.showProgress(this, false, getString(R.string.logout_dialog_title),
                    getString(R.string.progress_dialog_message));
        } else {
            // Show a discreet loading bar if we've already loaded it before
            Utility.showProgress(this, true, getString(R.string.logout_dialog_title),
                    getString(R.string.progress_dialog_message));
        }
        
        Utility.execute(new LogoutUser(this),
                getString(R.string.logout_url), User.getJSESSIONID());
        
        //User.clear();
//        SharedPreferences mSharedPreferences = this.getSharedPreferences("SL_Account", 0);  
//
//        SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
//        mEditor.clear();
//        mEditor.commit();
        
//        this.finish();
    }
    
}
