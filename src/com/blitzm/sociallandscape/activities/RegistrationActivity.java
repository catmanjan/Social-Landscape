package com.blitzm.sociallandscape.activities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.blitzm.sociallandscape.providers.RegisterUser;

/**
 * Activity to instantiates the screen of registration 
 * @author Siyuan Zhang
 *
 */
public class RegistrationActivity extends FragmentActivity{
   
    ImageView email_info;
    ImageView password_info;
    EditText email_input;
    EditText password_input;
    EditText repassword_input;
    TextView email_err_msg;
    TextView password_err_msg;
    TextView repassword_err_msg;
    Button submit_btn;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Force sliding transition between activities
        overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        email_info = (ImageView) this.findViewById(R.id.registration_email_info);
        password_info = (ImageView) this.findViewById(R.id.registration_password_info);
        
        email_input = (EditText) this.findViewById(R.id.registration_email_enter);
        password_input = (EditText) this.findViewById(R.id.registration_password_enter);
        repassword_input = (EditText) this.findViewById(R.id.registration_repassword_enter);
        
        email_err_msg = (TextView) this.findViewById(R.id.registration_email_error_msg);
        password_err_msg = (TextView) this.findViewById(R.id.registration_password_error_msg);
        repassword_err_msg = (TextView) this.findViewById(R.id.registration_repassword_error_msg);
        
        submit_btn = (Button) this.findViewById(R.id.Registration_submit);
        
        /**
         * when pressed the button, display the information
         */
        email_info.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String title = "Email";
                String message = "You must use a valid email address, that you own. " +
                        "This is important to secure your subscription purchases.";
                messageBox(message,title);
            }
            
        });
        
        
        /**
         * when pressed the button, display the information
         */
        password_info.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String title = "Password";
                String message = "The password must be at least 8 characters and no more than 16." +
                		"You must remember your password to be able to recover your subscription purchases.";
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
                if(validateRegistration()){
                    register(email_input.getText().toString(),password_input.getText().toString());
                }
             
                
                //register("mynewaccount@bctces.com", "testtest");
            }
            
            
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Force sliding transition between activities
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
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
    
    /**
     * check whether the email address is valid
     * @param email
     * @return
     */
    public static boolean emailFormat(String email) 
    { 
        boolean tag = true; 
        final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"; 
        final Pattern pattern = Pattern.compile(pattern1); 
        final Matcher mat = pattern.matcher(email); 
        if (!mat.find()) { 
            tag = false; 
        } 
        return tag; 
    }
    
    /**
     * validate the inputs, show error messages when invalid
     */
    public boolean validateRegistration(){
        String emptyErrorMsg = "this field is required";
        String invalidEmailMsg = "not a valid email address";
        String shrtPswdMsg = "must be at least 8 characters";
        String matchFailMsg = "password does not match";
        
        String emailInput = email_input.getText().toString();
        String pswdInput = password_input.getText().toString();
        String repswdInput = repassword_input.getText().toString();
        
        boolean valid_email;
        if(emailInput.isEmpty()){
            email_err_msg.setText(emptyErrorMsg);
            valid_email = false;
        }else if(!emailFormat(emailInput)){
            email_err_msg.setText(invalidEmailMsg);
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
        
        boolean valid_repswd;
        if(repswdInput.isEmpty()){
            repassword_err_msg.setText(emptyErrorMsg);
            valid_repswd = false;
        }else if(!repswdInput.equals(pswdInput)){
            repassword_err_msg.setText(matchFailMsg);
            valid_repswd = false;
        }else{
            repassword_err_msg.setText("");
            valid_repswd = true;
        }
        
        return valid_email&&valid_pswd&&valid_repswd;
    }
    
    private void register(String username, String password){
        Log.d("SocialLandscape", "Registering Account");
        
        if (!User.isLogged) {
            // Show an intrusive dialog if fact packs have never been loaded
            Utility.showProgress(this, false, getString(R.string.registering_dialog_title),
                    getString(R.string.progress_dialog_message));
        } else {
            // Show a discreet loading bar if we've already loaded it before
            Utility.showProgress(this, true, getString(R.string.registering_dialog_title),
                    getString(R.string.progress_dialog_message));
        }
        Utility.execute(new RegisterUser(this),
                getString(R.string.register_url), username, password);
    }
}
