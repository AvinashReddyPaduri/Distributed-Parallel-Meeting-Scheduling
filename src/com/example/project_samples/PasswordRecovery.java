/***
This class is used to recover user password by emailing a random 4 digit code to the user (the user need to enter the code in the android application)
*/
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PasswordRecovery extends Activity{
	
	EditText ed;
	
	//email variables
	String to = "";
	String from;
	String host;
	Properties properties;
	Session session;
	
	String random;

	EditText userid;
	RadioGroup type;
	RadioButton role;
	String uid = "";
	String name;
	String roleText;
	
	public static final String URLCLIENT = "url to dpms server employee name retriever";
	public static final String URLMANAGER = "url to dpms server manager name retriever";
	public static final String SUCCESS ="success";
	public static final String MESSAGE ="message";
	
	//declaring the JSONparser class
	JSONParser jsonparser;
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.passwordrecovery);
		ed = (EditText) findViewById(R.id.pswdmail);
		jsonparser = new JSONParser();
	}
	
	public void send(View v){
		
		userid = (EditText) findViewById(R.id.orgid);
		type = (RadioGroup) findViewById(R.id.pswrecoverytype);
		int roleType = type.getCheckedRadioButtonId();//get the index of the selected radio button
		role = (RadioButton) findViewById(roleType);//get the reference to selected radio button
		uid = userid.getText().toString();
		roleText = (String) role.getText();//string of the selected radio button
		to = ed.getText().toString();
		
		if(uid.equals("") || to.equals(""))
			Toast.makeText(this, "no field(s) can be empty", Toast.LENGTH_LONG).show();
		else
		{
			if(roleText.equals("manager"))
			{
				new PasswordRecoveryManager().execute();
			}
			else
			{
				new PasswordRecoveryClient().execute();
			}
		}
	}
	
class PasswordRecoveryClient extends AsyncTask<String,String,String>{
		
	Boolean clientVerification = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Verifying details", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params) {

			int success;
			clientVerification = false;
			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("empid",uid));
				list.add(new BasicNameValuePair("email",to));
				
				Log.d("request!", "starting");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLCLIENT,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully registered!", json.toString());
					
					clientVerification = true;//this indicates success
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null; 
		}
	
	
		protected void onPostExecute(String message){
			
			if(clientVerification){
				name = message;
				sendMail();
			}
			else {
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
			}
		}
	}//PasswordRecoveryClient

class PasswordRecoveryManager extends AsyncTask<String,String,String>{
	
	Boolean managerVerification = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Verifying details", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params) {

			int success;
			managerVerification = false;
			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("mngrid",uid));
				list.add(new BasicNameValuePair("email",to));
				
				Log.d("request!", "starting");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLMANAGER,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully registered!", json.toString());
					
					managerVerification = true;//this indicates success
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null; 
		}
	
	
		protected void onPostExecute(String message){
			
			if(managerVerification){
				name = message;
				sendMail();
			}
			else {
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
			}
		}
	}//PasswordRecoveryManager
	
	public void sendMail(){
		
	    //Sender's email ID needs to be mentioned
	    from = "dpms email id";
	    host = "smtp.gmail.com";
			      
	    random = getRandom();//send a random verification code

		// Get system properties
		properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.auth", "true");

		// Get the default Session object.
		session = Session.getDefaultInstance(properties,new javax.mail.Authenticator(){
		            protected PasswordAuthentication getPasswordAuthentication(){
		            return new PasswordAuthentication(from,"password for the dpms email id");
		        }});
		new SendRandomPasswordMail().execute();
	}
	
	private String getRandom(){
		String random;
		int ran;

		ran =(int)(Math.random()*10000);
		random = String.valueOf(ran);

		if(random.length()>4)
			random = random.substring(0,3);
		else if(random.length()==1)
			random = random + 211;
		else if(random.length()==2)
			random = random + 53;
		else if(random.length()==3)
			random = random + 4;
		
		return random;//random number of size 4
	}
	
	 class SendRandomPasswordMail extends AsyncTask<String, String, String>
	  {

		Boolean sent = false;
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try{
		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		         // Set Subject: header field
		         message.setSubject("DPMS Password Recovery");

		         // Now set the actual message
		         String otpmessage = "Hi "+name+",\n\nUse this code to change your password: "+random;
		         message.setText(otpmessage+"\n\n\n\ndo not reply, this is an automated mail\n\nRegards,\nDPMS Team");

		         //Send message
		         Transport.send(message);
		         sent = true;
		      }catch (MessagingException mex){
		         mex.printStackTrace();
		      }
			return "mail has been sent";
		}
		protected void onPostExecute(String message){
			if(sent)
			{
				Intent i = new Intent("setnewpassword");
				i.putExtra("orgid", uid);
				i.putExtra("gencode", random);
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
				if(roleText.equals("manager"))
				{
					i.putExtra("type", "manager");
				}
				else
				{
					i.putExtra("type", "employee");
				}
				startActivity(i);
			}
			else
				Toast.makeText(getBaseContext(), "connection problem while sending mail", Toast.LENGTH_LONG).show();
		}
	  }
}