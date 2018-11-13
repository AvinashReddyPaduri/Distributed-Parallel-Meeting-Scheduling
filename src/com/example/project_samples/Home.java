package com.example.project_samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity{

	EditText userid;
	EditText password;
	TextView tv;
	RadioGroup type;
	RadioButton role;
	String uid = "";
	String pswd = "";
	
	/***
	Initialize the following constants to the corresponding server pages
	*/
	public static final String URLCLIENT = "url to user login php page in dpms website";
	public static final String URLMANAGER = "url to manager login php page in dpms website";
	public static final String URLDEREGISTERCLIENT = "url to user deregistration php page in dpms website";
	public static final String URLDEREGISTERMANAGER = "url to manager deregistration page in dpms website";
	public static final String SUCCESS ="success";
	public static final String MESSAGE ="message";
	
	//declaring the JSONparser class
	JSONParser jsonparser;

	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.home);
		jsonparser = new JSONParser();
		tv = (TextView) findViewById(R.id.title);
		tv.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);//to add underline to a text view
		
		SharedPreferences loggedin = getSharedPreferences("login", Context.MODE_PRIVATE);
		boolean isLoggedin = loggedin.getBoolean("isLoggedin", false);
		String logintype = loggedin.getString("logintype", null);
		if(isLoggedin)
		{
			if(logintype.equals("manager"))
			{
				Intent i = new Intent(this, UserDetails.class);
				i.putExtra("mngrid",loggedin.getString("mngrid", null));
				startActivity(i);
			}
			else if(logintype.equals("employee"))
			{
				Intent i = new Intent("welcome");
				startActivity(i);
			}
		}
	}
	
	public void onBackPressed(){
	  //loading mobile home page on back press
	  Intent startMain = new Intent(Intent.ACTION_MAIN);
	  startMain.addCategory(Intent.CATEGORY_HOME);
	  startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	  startActivity(startMain);
	}
	
	public void login(View v){
		
		  userid = (EditText) findViewById(R.id.ruserid);
		  password = (EditText) findViewById(R.id.password);
		  type = (RadioGroup) findViewById(R.id.type);
		  int roleType = type.getCheckedRadioButtonId();//get the index of the selected radio button
		  role = (RadioButton) findViewById(roleType);//get the reference to selected radio button
		  uid = userid.getText().toString();
		  pswd = password.getText().toString();
		  String roleText = (String) role.getText();//string of the selected radio button
		
		  if(uid.equals("") || pswd.equals(""))
		  {
			  String error = "";
			  if(uid.equals(""))
				  error = "Enter user id(your organization id)";
			  else if(pswd.equals(""))
				  error = "Enter password";
			  else
				  error = "User name and password should be entered";
			
			  Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
		  }
		  else
		  {
			  //authentication(get data from server)
			  if(roleText.equals("manager"))
			  {
				  new LoginManager().execute();
			  }
			  else
			  {
				  new LoginClient().execute();
			  }
		  }
	}
	
	public void resetPassword(View v){
		
		  Intent i = new Intent(this,PasswordRecovery.class);
		  startActivity(i);
	}
	
	public void register(View v){
		
		SharedPreferences registered = getSharedPreferences("registered", Context.MODE_PRIVATE);
		boolean isRegistered = registered.getBoolean("isRegistered", false);
		if(isRegistered)
		{
			Toast.makeText(this, "You have already registered\nDeregister to register again", Toast.LENGTH_LONG).show();
		}
		else
		{
			Intent i = new Intent("register");
			startActivity(i);
		}
	}
	
	public void deRegister(View v){
		userid = (EditText) findViewById(R.id.ruserid);
		password = (EditText) findViewById(R.id.password);
		type = (RadioGroup) findViewById(R.id.type);
		int roleType = type.getCheckedRadioButtonId();//get the index of the selected radio button
		role = (RadioButton) findViewById(roleType);//get the reference to selected radio button
		uid = userid.getText().toString();
		pswd = password.getText().toString();
		String roleText = (String) role.getText();//string of the selected radio button
		
		if(uid.equals("") || pswd.equals(""))
		{
			String error = "";
			if(uid.equals(""))
				error = "Enter user id(your organization id)";
			else if(pswd.equals(""))
				error = "Enter password";
			else
				error = "User name and password should be entered";
			
			Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
		}
		else
		{
			//authentication(get data from server)
			if(roleText.equals("manager"))
			{
				new DeRegisterForManager().execute();
			}
			else
			{
				new DeRegisterClient().execute();
			}
		}
	}
	
	class LoginManager extends AsyncTask<String,String,String>{
		
		Boolean login = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Logging in", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params){

			int success;
			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("mngrid",uid));
				list.add(new BasicNameValuePair("password",pswd));
				
				Log.d("request!", "starting");
				//creating the json object by making an HTTP request to webserver
				JSONObject json = jsonparser.sendHttpRequest(URLMANAGER,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully registered!", json.toString());
					
					login = true;
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null; 
		}
	
		protected void onPostExecute(String message){
			
			if(login){
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
				goNext("manager");//start corresponding activity of the manager
			}
			else {
				Toast.makeText(getBaseContext(), "Login Failed "+message, Toast.LENGTH_SHORT).show();
			}
		}
	}//LoginForManager
	
	class LoginClient extends AsyncTask<String,String,String>{
		
		Boolean login = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Logging in", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected String doInBackground(String... params) {

			int success;
			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("empid",uid));
				list.add(new BasicNameValuePair("password",pswd));
				
				Log.d("request!", "starting");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLCLIENT,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully registered!", json.toString());
					
					login = true;
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null; 
		}
	
	
		protected void onPostExecute(String message){
			
			if(login){
				Toast.makeText(getBaseContext(), "Successfully Logged in", Toast.LENGTH_SHORT).show();
				goNext("client");//start corresponding activity of the manager
			}
			else {
				Toast.makeText(getBaseContext(), "Login Failed "+message, Toast.LENGTH_SHORT).show();
			}
		}
	}//LoginForClient
	
class DeRegisterClient extends AsyncTask<String,String,String>{
		
	    Boolean deregister;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Deregistering", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params) {

			int success;
			deregister = false;
			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("empid",uid));
				list.add(new BasicNameValuePair("password",pswd));
				
				Log.d("request for deregister!", "starting");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLDEREGISTERCLIENT,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully deregistered!", json.toString());
					
					deregister = true;//this indicates success
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null;
		}
	
	
		protected void onPostExecute(String message){
			
			if(deregister){
				Toast.makeText(getBaseContext(), "Successfully deregistered "+message, Toast.LENGTH_SHORT).show();
				SharedPreferences registered = getSharedPreferences("registered", Context.MODE_PRIVATE);
				Editor editor = registered.edit();
				editor.putBoolean("isRegistered", false);
				editor.commit();
			}
			else {
				Toast.makeText(getBaseContext(), "Deregistration Failed "+message, Toast.LENGTH_SHORT).show();
			}
		}
	}//DeRegisterForClient

class DeRegisterForManager extends AsyncTask<String,String,String>{
	
    Boolean deregister;
	@Override
	protected void onPreExecute(){
		super.onPreExecute(); 
		Toast.makeText(getBaseContext(), "Deregistering", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected String doInBackground(String... params) {

		int success;
		deregister = false;
		try{
			//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new BasicNameValuePair("mngrid",uid));
			list.add(new BasicNameValuePair("password",pswd));
			
			Log.d("request for deregister!", "starting");
			//creating the json object by making an HTTP request to webserver 
			JSONObject json = jsonparser.sendHttpRequest(URLDEREGISTERMANAGER,"POST",list);
			
			
			//checking the status of the request whether it is success or failure
			success = json.getInt(SUCCESS);
			
			//if the query in php file is executed correctly then success value will be "1"
			if(success == 1){
				Log.d("Successfully deregistered!", json.toString());
				
				deregister = true;//this indicates success
				//sending the value to the onPostExecute method below
				return json.getString(MESSAGE);
			}
			else
				return json.getString(MESSAGE);
			
		}catch(Exception e){e.printStackTrace();}
	  
		return null;
	}


	protected void onPostExecute(String message){
		
		if(deregister){
			Toast.makeText(getBaseContext(), "Successfully deregistered "+message, Toast.LENGTH_SHORT).show();
			SharedPreferences registered = getSharedPreferences("registered", Context.MODE_PRIVATE);
			Editor editor = registered.edit();
			editor.putBoolean("isRegistered", false);
			editor.commit();
		}
		else {
			Toast.makeText(getBaseContext(), "Deregistration Failed "+message, Toast.LENGTH_SHORT).show();
		}
	}
}//DeRegisterForManager

    public void goNext(String role){
		
    	if(role.equals("manager"))
    	{
    		//this is used when the user closes  the application and re opens it, then it will not ask the user to login again, it takes him to the corresponding home page
    		SharedPreferences login = getSharedPreferences("login", Context.MODE_PRIVATE);
			Editor editor2 = login.edit();
			editor2.putBoolean("isLoggedin", true);
			editor2.putString("logintype", "manager");
			editor2.putString("mngrid",uid);
			editor2.commit();
			
    		Intent i = new Intent(this,UserDetails.class);
    		i.putExtra("mngrid",uid);
			startActivity(i);
    	}
    	else
    	{
    		//this is used when the user closes  the application and re opens it, then it will not ask the user to login again, it takes him to the corresponding home page
    		SharedPreferences login = getSharedPreferences("login", Context.MODE_PRIVATE);
			Editor editor2 = login.edit();
			editor2.putBoolean("isLoggedin", true);
			editor2.putString("logintype", "employee");
			editor2.commit();
			
    		Intent i = new Intent("welcome");
    		i.putExtra("empid", uid);
			startActivity(i);
    	}
	}
}