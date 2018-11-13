package com.example.project_samples;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SetNewPassword extends Activity{
	
	EditText codeEd, pswdEd, repswdEd;
	
	String orgid;
	String type;
	String gencode;
	String code = "";
	String newpswd = "";
	String renewpswd = "";
	
	public static final String URLCLIENT = "url to dpms employee password change php page";
	public static final String URLMANAGER = "url to dpms manager password change php page";
	public static final String SUCCESS ="success";
	public static final String MESSAGE ="message";
	
	//declaring the JSONparser class
	JSONParser jsonparser;
	
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.newpassword);
		codeEd = (EditText) findViewById(R.id.code);
		pswdEd = (EditText) findViewById(R.id.newpswd);
		repswdEd = (EditText) findViewById(R.id.renewpswd);
		
		Intent i = getIntent();
		orgid = i.getStringExtra("orgid");
		type = i.getStringExtra("type");
		gencode = i.getStringExtra("gencode");
		
		jsonparser = new JSONParser();
	}
	
	public void changePassword(View v){
		code = codeEd.getText().toString();
		newpswd = pswdEd.getText().toString();
		renewpswd = repswdEd.getText().toString();
		
		if(code.equals("") || newpswd.equals("") || renewpswd.equals(""))
			Toast.makeText(this, "no field(s) can be empty", Toast.LENGTH_LONG).show();
		else if(!newpswd.equals(renewpswd))
			Toast.makeText(this, "Entered password and re entered password are not same", Toast.LENGTH_LONG).show();
		else if(!gencode.equals(code))
			Toast.makeText(this, "code entered doesn't match with the generated code", Toast.LENGTH_LONG).show();
		else
		{
			if(type.equals("manager"))
				new PasswordChangeManager().execute();
			else
				new PasswordChangeClient().execute();
		}
	}
	
	class PasswordChangeClient extends AsyncTask<String,String,String>{
		
		Boolean clientpswdchanged = false;
			@Override
			protected void onPreExecute(){
				super.onPreExecute(); 
				Toast.makeText(getBaseContext(), "changing password", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			protected String doInBackground(String... params){

				int success;
				clientpswdchanged = false;
				try{
					//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
					List<NameValuePair> list = new ArrayList<NameValuePair>();
					list.add(new BasicNameValuePair("empid",orgid));
					list.add(new BasicNameValuePair("password",newpswd));
					
					Log.d("request!", "starting");
					//creating the json object by making an HTTP request to webserver 
					JSONObject json = jsonparser.sendHttpRequest(URLCLIENT,"POST",list);
					
					
					//checking the status of the request whether it is success or failure
					success = json.getInt(SUCCESS);
					
					//if the query in php file is executed correctly then success value will be "1"
					if(success == 1){
						Log.d("Successfully registered!", json.toString());
						
						clientpswdchanged = true;//this indicates success
						//sending the value to the onPostExecute method below
						return json.getString(MESSAGE);
					}
					else
						return json.getString(MESSAGE);
					
				}catch(Exception e){e.printStackTrace();}
			  
				return null; 
			}
		
		
			protected void onPostExecute(String message){
				
				if(clientpswdchanged){
					Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
					Intent i = new Intent(getBaseContext(),Home.class);
					startActivity(i);
				}
				else {
					Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
				}
			}
		}//PasswordChangeClient
	
	class PasswordChangeManager extends AsyncTask<String,String,String>{
		
		Boolean managerpswdchanged = false;
			@Override
			protected void onPreExecute(){
				super.onPreExecute(); 
				Toast.makeText(getBaseContext(), "changing password", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			protected String doInBackground(String... params) {

				int success;
				managerpswdchanged = false;
				try{
					//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
					List<NameValuePair> list = new ArrayList<NameValuePair>();
					list.add(new BasicNameValuePair("mngrid",orgid));
					list.add(new BasicNameValuePair("password",newpswd));
					
					Log.d("request!", "starting");
					//creating the json object by making an HTTP request to webserver 
					JSONObject json = jsonparser.sendHttpRequest(URLMANAGER,"POST",list);
					
					
					//checking the status of the request whether it is success or failure
					success = json.getInt(SUCCESS);
					
					//if the query in php file is executed correctly then success value will be "1"
					if(success == 1){
						Log.d("Successfully registered!", json.toString());
						
						managerpswdchanged = true;//this indicates success
						//sending the value to the onPostExecute method below
						return json.getString(MESSAGE);
					}
					else
						return json.getString(MESSAGE);
					
				}catch(Exception e){e.printStackTrace();}
			  
				return null; 
			}
		
		
			protected void onPostExecute(String message){
				
				if(managerpswdchanged){
					Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
					Intent i = new Intent(getBaseContext(),Home.class);
					startActivity(i);
				}
				else {
					Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
				}
			}
		}//PasswordChangeManager
}