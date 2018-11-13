/***
Registers user as an employee in a company or as a manager of a company
Only managers can schedule a meeting
*/
package com.example.project_samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Register extends Activity{

	EditText username;
	EditText password;
	EditText email;
	EditText phoneno;
	EditText orgid;
	EditText orgname;
	TextView tv;
	RadioGroup gender;
	RadioButton selectedgender;
	boolean manager = false;
	Spinner spinner;
	List<String> categories;
	ArrayAdapter<String> dataAdapter;
	
	String uname = "";
	String pswd = "";
	String mail = "";
	String pno = "";
	String userid = "";//orgid
	String organizationName = "";
	String clientSelectedOrg = "";
	String genderString = "";
	
	Double clientLat;
	Double clientLng;
	
	//email variables
	String to;
	String from;
	String host;
	Properties properties;
	Session session;
	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._]+@[A-Z0-9]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
	
	Boolean clientSignup = false;//if it success, then we need to run the userlocations in backgroud(service) to send the user's location frequently
	
	//php url's present in the WEBSERVER
		public static final String URLMANAGER = "url to dpms server manager registration php page";
		public static final String URLCLIENT = "url to dpms server user registration php page";
		public static final String URLSPINNER = "url to dpms company name retriever php page";
		public static final String SUCCESS ="success";
		public static final String MESSAGE ="message";
		
		//variable declaration for GCM
		GoogleCloudMessaging gcm = null;
	    String regid;
	    String PROJECT_NUMBER = "GCM project number";
		
		//declaring the JSONparser class
		JSONParser jsonparser;
		private boolean gps_enabled;
		private boolean network_enabled;
	
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.register);
		//get organization names from server****
		//new IntializeSpinner().execute();
		
		//check for availability of play services to get gcm id
		tv = (TextView) findViewById(R.id.orgdisplay);
		username = (EditText) findViewById(R.id.ruserid);
		password = (EditText) findViewById(R.id.rpassword);
		email = (EditText) findViewById(R.id.remailid);
		phoneno = (EditText) findViewById(R.id.rphoneno);
		orgid = (EditText) findViewById(R.id.rorgid);
		spinner = (Spinner) findViewById(R.id.rorgemp);//spinner to select the organization names from the list of given names
		spinner.setOnItemSelectedListener(new OrganizationChanged());
		
		//assign the organization names to the spinner
		categories = new ArrayList<String>();
	    new InitializeSpinner().execute();
	      
	    gender = (RadioGroup) findViewById(R.id.gender);
		orgname = (EditText) findViewById(R.id.rorgname);//orgname has to be entered only by manager, should be disabled to employee
		orgname.setVisibility(View.INVISIBLE);
		jsonparser = new JSONParser();
	}
	
	@Override
	protected void onResume()
	{
	       super.onResume();
	}
	
	public void onRoleChange(View v){//when user changes the role either from employee to manager or from manager to employee, this method is called
	
		if(manager)//before calling this method, if the role is manager, it has to be changed to employee
			manager = false;//false indicate that the user is not a manger, i.e., he is an employee
		else
			manager = true;
		
		if(manager)
		{
			//one organization should contain only 1 manager
			tv.setVisibility(View.INVISIBLE);
			spinner.setVisibility(View.INVISIBLE);
			orgname.setVisibility(View.VISIBLE);
		}
		else
		{
			//employee should select the orgname from the existing orgnames, he shouldn't enter the new orgname
			tv.setVisibility(View.VISIBLE);
			spinner.setVisibility(View.VISIBLE);
			orgname.setVisibility(View.INVISIBLE);
		}
	}
	
	public class OrganizationChanged implements OnItemSelectedListener
	{

		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			clientSelectedOrg = parent.getItemAtPosition(position).toString();
			
		}

		public void onNothingSelected(AdapterView<?> parent){
			// TODO Auto-generated method stub
			clientSelectedOrg = parent.getItemAtPosition(0).toString();
		}
	}
	
	
	public void register(View v){//validate the user details first
		
		//store data on server
		//get the data before storing on the server
		
		uname = username.getText().toString();
		pswd = password.getText().toString();
		mail = email.getText().toString();
		pno = phoneno.getText().toString();
		userid = orgid.getText().toString();
		
		int selected = gender.getCheckedRadioButtonId();
		selectedgender = (RadioButton) findViewById(selected);
		genderString = (String) selectedgender.getText();
		
		if(manager)
		 organizationName = orgname.getText().toString();
		else
		{
			organizationName = clientSelectedOrg;
		}
		
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(mail);
		//validations
		if(uname.equals("") || pswd.equals("") || mail.equals("") || pno.equals("") || userid.equals("") || organizationName.equals("") || genderString.equals(""))
		{
			Toast.makeText(this, "No fields can be empty", Toast.LENGTH_LONG).show();
		}
		
		else if(pno.length() != 10)
		{
			Toast.makeText(this, "Enter a valid 10 digit phone no", Toast.LENGTH_LONG).show();
		}
		
		else if(!(pno.startsWith("9") || pno.startsWith("8") || pno.startsWith("7")))
		{
			Toast.makeText(this, "Enter a valid phone no", Toast.LENGTH_LONG).show();
		}
		else if(!matcher.find())//validating an email address
		{
			Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_LONG).show();
		}
		else
		{
			//now we have the data ready
			//send the data to the JSON parser which intern sends the data to the application server and the application server stores the data on mySql
		
			if(manager){
				new RegisterManager().execute();
			}
			else//client
			{
				//get the gcmid before registering
				getRegId();
			}
		}
	}
	
	class RegisterManager extends AsyncTask<String,String,String>{
		
		Boolean signup = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Registering", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params) {

			int success;
			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("mngrname",uname));
				list.add(new BasicNameValuePair("password",pswd));
				list.add(new BasicNameValuePair("mngrid",userid));
				list.add(new BasicNameValuePair("email",mail));
				list.add(new BasicNameValuePair("phone",pno));
				list.add(new BasicNameValuePair("gender",genderString));
				list.add(new BasicNameValuePair("orgname",organizationName));
				
				Log.d("request!", "starting");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLMANAGER,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully registered!", json.toString());
					
					signup = true;
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null;
		}
	
		protected void onPostExecute(String message){
			
			if(signup){
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
				sendMail();

				goNext("manager");//start corresponding activity of the manager
			}
			else {
				Toast.makeText(getBaseContext(), "Registration Failed "+message, Toast.LENGTH_SHORT).show();
			}
		}
	}//RegisterForManager
	
	class RegisterClient extends AsyncTask<String,String,String>{
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Registering", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params) {

			int success;
			clientSignup = false;
			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("empname",uname));
				list.add(new BasicNameValuePair("password",pswd));
				list.add(new BasicNameValuePair("empid",userid));
				list.add(new BasicNameValuePair("email",mail));
				list.add(new BasicNameValuePair("phone",pno));
				list.add(new BasicNameValuePair("gender",genderString));
				list.add(new BasicNameValuePair("gcmid",regid));
				list.add(new BasicNameValuePair("orgname",organizationName));
				list.add(new BasicNameValuePair("latitude",Double.toString(clientLat)));
				list.add(new BasicNameValuePair("longitude",Double.toString(clientLng)));
				
				Log.d("request!", "starting");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLCLIENT,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully registered!", json.toString());
					
					clientSignup = true;//this indicates success
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null; 
		}
	
	
		protected void onPostExecute(String message){
			
			if(clientSignup){
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
				sendMail();
				
				goNext("client");//start the corresponding activity of the client(welcome activity)
			}
			else {
				Toast.makeText(getBaseContext(), "Registration Failed "+message, Toast.LENGTH_SHORT).show();
			}
		}
	}//RegisterForClient
	
	public void sendMail()
	{
		//Recipient's email ID needs to be mentioned.
	      to = mail;
	      // Sender's email ID needs to be mentioned
	      from = "dpms email id";
	      host = "smtp.gmail.com";

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
            return new PasswordAuthentication(from,"password");//1st parameter mail id, second parameter password
        }});
	      
	      new NotifiyRegistrationMail().execute();
	}
	
	  class NotifiyRegistrationMail extends AsyncTask<String, String, String>
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
		         message.setSubject("Successfully Registered");

		         // Now set the actual message
		         String mailMessage = "You have been successfully registered with DPMS. Thank you for registering with DPMS.\n\n\n\ndo not reply, this is an automated mail\n\nRegards,\nDPMS Team";
		         message.setText(mailMessage);

		         // Send message
		         Transport.send(message);
		         
		      }catch (MessagingException mex){
		         mex.printStackTrace();
		         return "failed to send mail";
		      }
			sent = true;
			return "mail has been sent";
		}
		protected void onPostExecute(String message){
			if(!sent)
			{
				Toast.makeText(getApplicationContext(), "something went wrong while sending a mail", Toast.LENGTH_LONG).show();
			}
		}
	  }
	
class InitializeSpinner extends AsyncTask<String,String,String>{
		
		String orgnames = "";
		String response = "";
		Boolean success = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "getting orgnames", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params){

			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				
				
				Log.d("retrieving", "starting to retrieve orgnames");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLSPINNER,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				//success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				//if(success == 1){
					Log.d("Successfully retrieved user locations!", json.toString());
					
					response = json.toString();
					
					int count = json.getInt("count");
					if(count == 0)
						response = "no orgnames";
					else
					{
						
					  for(int i=0; i<count;i++)
					  {
						  orgnames = json.getString("org"+i);//store new orgname into orgnames string
						  response = orgnames+"\n";
						  categories.add(orgnames);
					  }
					  success = true;
					}
					//sending the value to the onPostExecute method below
					return response;
				//}
				//else
					//return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();
			response = e.getMessage();
			}
		  
			return response;
		}
	
	
		protected void onPostExecute(String message){
			if(success)
			{
				initializeSpinner();
				
			}
			else
			{
				Toast.makeText(getBaseContext(), "something went wrong while retrieving organization names: "+response, Toast.LENGTH_LONG).show();
			}
		}
	}//InitializeSpinner

	public void initializeSpinner(){
		
		// Creating adapter for spinner
	    dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
	      
	    // Drop down layout style - list view with radio button
	    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	      
	    // attaching data adapter to spinner
	    spinner.setAdapter(dataAdapter);
	}
	
	public void goNext(String type){
		
		if(type.equals("manager"))
		{
			SharedPreferences login = getSharedPreferences("login", Context.MODE_PRIVATE);
			Editor editor2 = login.edit();
			editor2.putBoolean("isLoggedin", true);
			editor2.putString("logintype", "manager");
			editor2.putString("mngrid",userid);
			editor2.commit();
			
			Intent i = new Intent(this,UserDetails.class);
			i.putExtra("mngrid",userid);
			startActivity(i);
		}
		else
		{
			SharedPreferences registered = getSharedPreferences("registered", Context.MODE_PRIVATE);
			Editor editor = registered.edit();
			editor.putBoolean("isRegistered", true);
			editor.commit();
			
			SharedPreferences login = getSharedPreferences("login", Context.MODE_PRIVATE);
			Editor editor2 = login.edit();
			editor2.putBoolean("isLoggedin", true);
			editor2.putString("logintype", "employee");
			editor2.commit();
			
			Intent service = new Intent(this,UsersLocations.class);
			Toast.makeText(this, "userid: "+userid, Toast.LENGTH_LONG).show();
			service.putExtra("userid", userid);
			startService(service);//sending the user locations frequently

			//welcome message
			Intent i = new Intent("welcome");
			i.putExtra("empid", userid);
			startActivity(i);
		}
	}
	
	public void getRegId(){
		
		Toast.makeText(this, "getting registration id", Toast.LENGTH_LONG).show();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM",  msg);

                } catch (IOException ex) {
                    msg = "Error While getting gcm registration id:" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //Toast.makeText(getBaseContext(), regid, Toast.LENGTH_LONG).show();
                callRegisterForClient();
            }
        }.execute(null, null, null);
    }
	
	public void callRegisterForClient(){
		LocationManager mLocationManager;
		Location location = null;

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		  
	    clientLat = location.getLatitude();
	    clientLng = location.getLongitude();
	    //Toast.makeText(this, "latitude: "+Double.toString(clientLat)+"logitude: "+Double.toString(clientLng), Toast.LENGTH_LONG).show();
		new RegisterClient().execute();
	}
}//Register class