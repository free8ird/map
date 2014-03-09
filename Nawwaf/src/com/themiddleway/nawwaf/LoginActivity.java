package com.themiddleway.nawwaf;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	public final static String Tutor = "tutor";
	public final static String Student  = "student";
	public final static String Admin = "admin";
	
	private static final String TAG_PASSWORD = "password";
    private static final String TAG_USERNAME = "username";
    ImageButton Back;
    Button Login;
    TextView activityTitle;
    String jsonStr;
    List<NameValuePair> nameValuePairs;
	String passWord;
    EditText passwordText;
    CheckBox rememberMe;
    String userName;
    EditText usernameText;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);
        usernameText = (EditText)findViewById(R.id.login_username);
        passwordText = (EditText)findViewById(R.id.login_password);
        Login = (Button)findViewById(R.id.login_login);
        rememberMe = (CheckBox)findViewById(R.id.login_remember_me);
        SharedPreferences sharedpreferences = getSharedPreferences("cred", 0);
        userName = sharedpreferences.getString("username", "");
        passWord = sharedpreferences.getString("password", "");
        usernameText.setText(userName);
        passwordText.setText(passWord);
        
        usernameText.setOnTouchListener(new android.view.View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent)
            {
                usernameText.setText("");
                return false;
            }
        });
        
        passwordText.setOnTouchListener(new android.view.View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent)
            {
                passwordText.setText("");
                return false;
            }
        });

        Login.setOnClickListener(new android.view.View.OnClickListener() {
        	
        	public void onClick(View view)
            {
                userName = usernameText.getText().toString();
                passWord = passwordText.getText().toString();
                ConnectivityManager connectivitymanager = (ConnectivityManager)getSystemService("connectivity");
                if (connectivitymanager.getActiveNetworkInfo() != null && connectivitymanager.getActiveNetworkInfo().isAvailable() && connectivitymanager.getActiveNetworkInfo().isConnected())
                {
                    if (rememberMe.isChecked())
                    {
                        android.content.SharedPreferences.Editor editor = getSharedPreferences("cred", 0).edit();
                        editor.putString("username", userName);
                        editor.putString("password", passWord);
                        editor.commit();
                    }
                    (new TryLogin()).execute(new Void[0]);
                   
                } else
                    showDialog();
                
            }

          });
    }
    public void showDialog()
    {
    	final Dialog d=new Dialog(LoginActivity.this);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.dialog);
		TextView dialogText=(TextView)d.findViewById(R.id.dialogText);
		TextView dialogTitle = (TextView) d.findViewById(R.id.dialogTitle);
		dialogTitle.setText("Login Failed");
		dialogText.setText("Sorry,login Failed to reach nawwaf server. "+
		"Please check your network connection or try again later");
		Button dialogOk=(Button)d.findViewById(R.id.dialogOk);
		dialogOk.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				d.dismiss();			
			}
		});
		d.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }
    
    
    private class TryLogin extends AsyncTask<Void, Void, Void>
    {

        private static final String TAG_ID = "id";
        private static final String TAG_USERTYPE = "usertype";
        private static final String TAG_VALID = "valid";
        private ProgressDialog pDialog;
		@Override
		protected Void doInBackground(Void... arg0) {
			ServiceHandler servicehandler = new ServiceHandler();
			nameValuePairs = new ArrayList<NameValuePair>(2);
		    nameValuePairs.add(new BasicNameValuePair("username", userName));
            nameValuePairs.add(new BasicNameValuePair("password", passWord));
            jsonStr = servicehandler.makeServiceCall("http://nawwafcentre.com/api/M123ER45T/user/",ServiceHandler.POST, nameValuePairs);
            Log.d("JSON_RESPONSE", jsonStr);
            return null;
		}
		 protected void onPreExecute()
	        {
	            super.onPreExecute();
	            pDialog = new ProgressDialog(LoginActivity.this);
	            pDialog.setMessage("Please wait...");
	            pDialog.setCancelable(true);
	            pDialog.show();
	        }

    	protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            
            if(jsonStr != null){
            	try {
            		
					JSONObject jsonObj = new JSONObject(jsonStr);
					String valid = jsonObj.getString(TAG_VALID);
					//jsonStr = valid + userType;
					if(valid.equals("false"))
					{
						//Handle Invalid UserCredentials
						Toast.makeText(LoginActivity.this, "Invalid User Credentials!!!", 1).show();
					}
					else{
						String userType = jsonObj.getString(TAG_USERTYPE);
						String tableId = jsonObj.getString(TAG_ID);
						String mode=null;
						if(userType.equals("2")){
							//Invoke Student Mode
							Toast.makeText(LoginActivity.this, "Student User Credentials!!!", 1).show();
							mode = Student;
						}
						else if(userType.equals("3")){
							Toast.makeText(LoginActivity.this, "tutor User Credentials!!!", 1).show();
							mode = Tutor;
							//Invoke Tutor Mode
						}
						else{
							mode = Admin;
							Toast.makeText(LoginActivity.this, "admin User Credentials!!!", 1).show();
							
							//Invoke Admmin Mode
							//Intent intent = new Intent(LoginActivity.this,AdminNavigation.class );
							//startActivity(intent);
						}
						
					}
						
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            if (pDialog.isShowing())
                pDialog.dismiss();
    	}
    }
    
}
