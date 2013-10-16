package com.blitzm.sociallandscape.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blitzm.sociallandscape.R;
import com.blitzm.sociallandscape.Utility;
import com.blitzm.sociallandscape.models.User;
import com.blitzm.sociallandscape.providers.LoginUser;

/**
 * This class provides the login user interface 
 * @author Siyuan Zhang
 *
 */
public class LoginActivity extends FragmentActivity{
    
    ImageView login_info;
    EditText email_input;
    EditText password_input;
    TextView email_err_msg;
    TextView password_err_msg;
    Button submit_btn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Force sliding transition between activities
        overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        email_input = (EditText) this.findViewById(R.id.login_email_enter);
        password_input = (EditText) this.findViewById(R.id.login_password_enter);
        
        email_err_msg = (TextView) this.findViewById(R.id.login_email_error_msg);
        password_err_msg = (TextView) this.findViewById(R.id.login_password_error_msg);
        
        submit_btn = (Button) this.findViewById(R.id.login_submit);
        
        login_info = (ImageView) this.findViewById(R.id.login_info);        
        login_info.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
                String title = "Login";
                String message = "You must register an account before logging in here.";
                messageBox(message,title);
            }
            
        });
        
        /**
         * submit the registration request
         */
        submit_btn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(validateLogin()){
                    login(email_input.getText().toString(),password_input.getText().toString());
                }
                
                //login("mynewaccount@blitzm.com", "testtest");
            }
            
            
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Force sliding transition between activities
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }
    
    public void messageBox(String msg, String title) {
        Dialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title).setMessage(msg).setNeutralButton("OK", null)
                .create();

        alertDialog.show();
    }
    
    /**
     * validate the inputs, show error messages when invalid
     */
    public boolean validateLogin(){
        String emptyErrorMsg = "this field is required";
        String shrtPswdMsg = "must be at least 8 characters";
        
        String emailInput = email_input.getText().toString();
        String pswdInput = password_input.getText().toString();
        
        boolean valid_email;
        if(emailInput.isEmpty()){
            email_err_msg.setText(emptyErrorMsg);
            valid_email = false;
        }else{
            email_err_msg.setText("");
            valid_email = true;
        }
        
        boolean valid_pswd;
        if(pswdInput.isEmpty()){
            password_err_msg.setText(emptyErrorMsg);
            valid_pswd = false;
        }else if(pswdInput.length()<8){
            password_err_msg.setText(shrtPswdMsg);
            valid_pswd = false;
        }else{
            password_err_msg.setText("");
            valid_pswd = true;
        }
        
        
        return valid_email&&valid_pswd;
    }
    
    private void login(String username, String password){
        Log.d("SocialLandscape", "Login Account");
        
        if (!User.isLogged) {
            // Show an intrusive dialog if fact packs have never been loaded
            Utility.showProgress(this, false, getString(R.string.login_dialog_title),
                    getString(R.string.progress_dialog_message));
        } else {
            // Show a discreet loading bar if we've already loaded it before
            Utility.showProgress(this, true, getString(R.string.login_dialog_title),
                    getString(R.string.progress_dialog_message));
        }
        
        Utility.execute(new LoginUser(this),
                getString(R.string.login_url), username, password);
    }
}
