package com.example.project_samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class UsersLocations extends Service{

	private LocationManager lm;
	private LocationListener locationListener;
	public Location location;
	String userid;
	
	public static final String URLSTORE = "url to dpms storing user locations php page";
	public static final String SUCCESS ="success";
	public static final String MESSAGE ="message";
	JSONParser jsonparser;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent i, int flags, int startId)
	{
		Toast.makeText(getBaseContext(), "DPMS location service started", Toast.LENGTH_LONG).show();
		jsonparser = new JSONParser();
		if( i == null)
		{
			SharedPreferences empid = getSharedPreferences("login", Context.MODE_PRIVATE);
			userid = empid.getString("userid", null);
		}
		else
		{
			userid = i.getStringExtra("userid");
			SharedPreferences empid = getSharedPreferences("userid", Context.MODE_PRIVATE);
			Editor editor = empid.edit();
			editor.putString("userid", userid);
			editor.commit();
		}
		
		if(userid != null)
		{
			lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			locationListener = new MyLocationListener();
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3600000,1000,locationListener);//3600000(milliseconds) indicates 1 hour and 1000(meters) indicate 1km
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Toast.makeText(getApplicationContext(), "DPMS location service is destroyed", Toast.LENGTH_SHORT).show();
	}
	
	private class MyLocationListener implements LocationListener{
		 
		 public void onLocationChanged(Location loc){
			 if (loc != null){
				 location = loc;
				 //Toast.makeText(getBaseContext(),"Location changed : Lat: " + loc.getLatitude() +" Lng: " + loc.getLongitude(),Toast.LENGTH_SHORT).show();
				 //send the locations of the user to the server based on the user id
				 //use asynchtask class and send the data to JSON parser class, which will send it to the app server
				 new StoreLocation().execute();
			 }
		 }
		 
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
	 }
	
class StoreLocation extends AsyncTask<String,String,String>{
		
	   Boolean locStored = false;
	   @Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "sending location", Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected String doInBackground(String... params) {

			int success;
			locStored = false;
			try{
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("empid",userid));
				list.add(new BasicNameValuePair("latitude",Double.toString(lat)));
				list.add(new BasicNameValuePair("longitude",Double.toString(lng)));
				
				Log.d("request!", "starting");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLSTORE,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				if(success == 1){
					Log.d("Successfully sent location!", json.toString());
					
					locStored = true;//this indicates success
					//sending the value to the onPostExecute method below
					return json.getString(MESSAGE);
				}
				else
					return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();}
		  
			return null; 
		}
	
	
		protected void onPostExecute(String message){
			
			if(locStored){
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(getBaseContext(), "Sending location failed"+message, Toast.LENGTH_LONG).show();
			}
		}
	}//StoreLocation
}