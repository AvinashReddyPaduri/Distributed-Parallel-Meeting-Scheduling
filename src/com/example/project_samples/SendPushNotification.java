package com.example.project_samples;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import android.widget.Toast;

import com.example.project_samples.Content;

public class SendPushNotification extends Activity
{
	EditText message;
	String mngrid;
	String selectedPlace;
	final static String URLGETREGID = "url to dpms retrieving gcm ids of employee";
	
	String msg = "";
	JSONParser jsonparser;
	
	LinkedList<String> gcmregids;
	ArrayList<String> empIds;
	ArrayList<String> selectedEmpIds;
	
	public void  onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.sendnotification);
		Intent i =getIntent();
		mngrid=i.getStringExtra("mngrid");
		selectedPlace = i.getStringExtra("selectedPlace");
		empIds = i.getExtras().getStringArrayList("selectedIds");//selected employee ids of the manager with id = mngrid
		selectedEmpIds = new ArrayList<String>();
		for (int j = 0; j < empIds.size(); j++){
			selectedEmpIds.add(empIds.get(j));
		}
		
		jsonparser= new JSONParser();
		message = (EditText) findViewById(R.id.message);
		message.setText("Scheduled at "+selectedPlace);
		
		gcmregids = new LinkedList<String>();
	}
	
	public void logout(View v){
		
		SharedPreferences login = getSharedPreferences("login", Context.MODE_PRIVATE);
		Editor editor2 = login.edit();
		editor2.putBoolean("isLoggedin", false);
		editor2.putString("logintype", "manager");
		editor2.putString("mngrid",mngrid);
		editor2.commit();
		
		Intent i = new Intent(this,Home.class);
		startActivity(i);
	}

    public void send(View v)
    {
    	new GetRegId().execute();
    }
    
    public void sendGCMPushNotifications(){
    	
    	Boolean sent = false;
    	try
    	{
    	  msg = message.getText().toString();
    	  if(msg.equals(""))
    	  {
    		  Toast.makeText(this, "Enter message to send", Toast.LENGTH_LONG).show();
    	  }
    	  else
    	  {
    		  Toast.makeText(this, "Sending request to GCM server", Toast.LENGTH_LONG).show();

    		  String apiKey = "Google server key";//google server key with google cloud messaging api enabled
    		  Content content = createContent();//setting the gcm registration key of the employees to whom the gcm push notification has to be sent and setting the message that has to be sent

    		  POST2GCM.post(apiKey, content);
    		  sent = true;
    	  }
    	}catch(Exception e)
    	{
    		Toast.makeText(getBaseContext(), "error "+e.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	
    	if(sent)
    		Toast.makeText(this, "Notification has been sent", Toast.LENGTH_LONG).show();
	}

    public Content createContent(){

        Content c = new Content();

        c.addRegId(gcmregids);
        c.createData("New Meeting", msg);

        return c;
    }
    
	
class GetRegId extends AsyncTask<String,String,String>{
		
		String gcmIds = "";
		String response = "";
		Boolean success = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "getting employees regreg ids for mngrid: "+mngrid, Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params){

			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("mngrid", mngrid));
				
				Log.d("retrieving", "starting to retrieve orgnames");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLGETREGID,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				//success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				//if(success == 1){
					Log.d("Successfully retrieved reg id!", json.toString());
					
					response = json.toString();
					
					int count = json.getInt("count");
					if(count == 0)
						response = "no clients registered";
					else
					{
					  response = "sending notification to:\n";
					  for(int i=0; i<count;i++)
					  {
						  gcmIds = json.getString("gcmid"+i);
						  String[] empdetails = gcmIds.split("\t");
						  if(selectedEmpIds.contains(empdetails[0]))
						  {
							  gcmregids.add(empdetails[1]);
							  response += empdetails[0]+"\n";
						  }
						  else
							  continue;
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
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
				sendGCMPushNotifications();
			}
			else
			{
				Toast.makeText(getBaseContext(), "something went wrong while retrieving reg id: "+response, Toast.LENGTH_LONG).show();
			}
		}
	}//GetRegId
}