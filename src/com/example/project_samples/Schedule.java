
package com.example.project_samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
public class Schedule extends Activity{

	TextView tv;
	LatLng latLng;
	ArrayList<Double> lats;
	ArrayList<Double> lngs;
	ArrayList<LatLng> locations;
	ArrayList<LatLng> selectedLocations;
	ArrayList<String> empIds;
	ArrayList<String> selectedEmpIds;
	String mngrid;
	String response = "";
	//Boolean latFinished = false;//true indicates latitudes are retrieved
	//Boolean lngFinished = false;//true indicates longitudes are retrieved
	Boolean isCentralLocationCalculated = false;
	int count = 0;//if cpmbineLatLng() is called twice and still there is no response, then it indicates there is an error
	
	//public static final String URLRETRIEVELAT = "http://dpmsproject.comlu.com/retrivelat.php";
	//public static final String URLRETRIEVELNG = "http://dpmsproject.comlu.com/retrievelong.php";
	public static final String SUCCESS ="success";
	public static final String MESSAGE ="message";
	
	//declaring the JSONparser class
	JSONParser jsonparser;
	String address;
	
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.schedule);
		Intent i = getIntent();
		mngrid = i.getStringExtra("mngrid");
		locations = i.getExtras().getParcelableArrayList("selectedLocations");
		empIds = i.getExtras().getStringArrayList("selectedIds");
		selectedLocations = new ArrayList<LatLng>();//locations is getting destoryed sometimes thus leading to NULLPOINTER Exception. In order to remove that exception, We have used another arraylist to store this data
		selectedEmpIds = new ArrayList<String>();
		for (int j = 0; j < locations.size(); j++){
			//Toast.makeText(this, locations.get(j).toString(), Toast.LENGTH_LONG).show();
			selectedLocations.add(locations.get(j));//reference for the retrievied locations from intent is no longer existing in the schedule method, so store them in another arraylist which can hold them as long as this activtiy not destroyed
			//selectedEmpIds.add(empIds.get(j));
		}
		selectedEmpIds = empIds;
		
		tv = (TextView) findViewById(R.id.centrallocation);
		//lats = new ArrayList<Double>();
		//lngs = new ArrayList<Double>();
		
		jsonparser = new JSONParser();
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
	
	/*public static void setLocations(ArrayList<LatLng> selectedLocations){
		
		for (int i = 0; i < selectedLocations.size(); i++){
			locations.add(selectedLocations.get(i));
		}
	}
	
	public static void setEmpIds(ArrayList<String> selectedIds){
		
		for (int i = 0; i<selectedIds.size(); i++){
			empIds.add(selectedIds.get(i));
		}
	}*/
	
	public void schedule(View v){
		
		tv.setText("calculating central location");
		lats = new ArrayList<Double>();
		lngs = new ArrayList<Double>();
		locations = new ArrayList<LatLng>();
		//new GetLatitudes().execute();
		//new GetLongitudes().execute();
		
		latLng = new CentralLocation().getCenterLocation(selectedLocations);//retrieves the central location
		
		address = "";
	    Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
	    try {//get the address of the central location
	    	List<Address> addresses = geoCoder.getFromLocation(latLng.latitude,latLng.longitude,1);
	    	if (addresses.size()>0)
	    	{
	    		for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
	    			address += addresses.get(0).getAddressLine(i) + "\n";
	    	}
	    	//Toast.makeText(getBaseContext(), address, Toast.LENGTH_SHORT).show();
	    }catch (Exception e){
	    	e.printStackTrace();
	    }
	    if(!address.equals(""))
	    {
	    	tv.setText("central location is: "+address);
	    	isCentralLocationCalculated = true;//if the central location is not calculated, then the manager shouldn't be able to find the nearby location. Because nearby location finds the places that are nearby a paticular location. If there is no location(central location), there there will be no point to call the NearByLocation activity
	    }
	    else
	    {
	    	tv.setText("something went wrong while calculating central location\nplease try again");
	    	isCentralLocationCalculated = false;
	    }
	}
	
	public void getSuggestions(View v){
		
		if(isCentralLocationCalculated)
		{
		  Toast.makeText(this, "getting near by locations", Toast.LENGTH_LONG).show();
		  Intent i = new Intent(this,NearByPlaces.class);
		  i.putExtra("latitude", latLng.latitude);
		  i.putExtra("longitude", latLng.longitude);
		  i.putExtra("mngrid", mngrid);
		  i.putExtra("selectedIds", selectedEmpIds);//send push notification to only these employees
		  startActivity(i);
		}
		else
			Toast.makeText(this, "central location needs to be calculated to get suggestions", Toast.LENGTH_LONG).show();
	}
	
	public void notifyEmployees(View v){
		
		Intent i = new Intent(this,SendPushNotification.class);
		i.putExtra("mngrid", mngrid);
		//as manager has not selected any place, we sending no value for it. In SendPushNotification activity, we will retrieve this vale from intent, in roder to prevent the NullPointerException, we are assigning this value to selectedplace
		i.putExtra("selectedPlace", "");
		i.putExtra("selectedIds", selectedEmpIds);//send push notification to only these employees
		startActivity(i);
	}
	
	/*class GetLatitudes extends AsyncTask<String,String,String>{
		
		String lat = "";
		String check = "";//check if the latitudes are retrieved correctly or not, this is temporaray in this project
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "getting locations", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params){

			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("mngrid",mngrid));
				
				Log.d("retrieving", "starting to retrieve locations");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLRETRIEVELAT,"POST",list);
				
				
				
					Log.d("Successfully retrieved user locations!", json.toString());
					
					response = json.toString();
					
					int count = json.getInt("count");
					if(count == 0)
						lat = "no locations(lat)";
					else
					{
					  for(int i=0; i<count;i++)
					  {
						  lat = json.getString("lat"+i);//assign new latitude to lat string
						  check += lat+"\n";
						  lats.add(Double.parseDouble(lat));//parse lat to double and store it in lats array list
					  }
					  latFinished = true;
					}
					//sending the value to the onPostExecute method below
					return check;
				//}
				//else
					//return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();
			response = e.getMessage();
			}
		  
			return response;
		}
	
	
		protected void onPostExecute(String message){
			Toast.makeText(getBaseContext(), "latitudes:\n"+check, Toast.LENGTH_LONG).show();
			combineLatLng();
		}
	}//GetLatitudes
	
class GetLongitudes extends AsyncTask<String,String,String>{
		
		String lng = "";
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "getting locations", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params){

			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("mngrid",mngrid));
				
				Log.d("retrieving", "starting to retrieve locations");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLRETRIEVELNG,"POST",list);
				
				
				//checking the status of the request whether it is success or failure
				//success = json.getInt(SUCCESS);
				
				//if the query in php file is executed correctly then success value will be "1"
				//if(success == 1){
					Log.d("Successfully retrieved user locations!", json.toString());
					
					response = json.toString();
					
					int count = json.getInt("count");
					if(count == 0)
						lng = "no locations(lng)";
					else
					{
					  for(int i=0; i<count;i++)
					  {
						  lng = json.getString("lng"+i);//store new longitude into lng string
						  lngs.add(Double.parseDouble(lng));//parse lng to double and store it in lngs array list
					  }
					  lngFinished = true;
					}
					//sending the value to the onPostExecute method below
					return lng;
				//}
				//else
					//return json.getString(MESSAGE);
				
			}catch(Exception e){e.printStackTrace();
			response = e.getMessage();
			}
		  
			return response;
		}
	
	
		protected void onPostExecute(String message){
			combineLatLng();
		}
	}//GetLongitudes

  public void combineLatLng(){
	  
	  count++;

	  String address = "";
	  
	  LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

      Location managerLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      Double lat= managerLocation.getLatitude();
      Double lng = managerLocation.getLongitude();
      latLng = new LatLng(lat, lng);
      locations.add(latLng);//considering manager's current location also for finding central location
	  
	  Toast.makeText(this, "call"+count,Toast.LENGTH_LONG).show();
	  if(latFinished == true && lngFinished == true)
	  {
		  if(lats.size() == lngs.size())
		  {
		    for(int i=0;i<lats.size();i++)
		    {
		      latLng = new LatLng(lats.get(i), lngs.get(i));
		      locations.add(latLng);
		    }
		    latLng = new CentralLocation().getCenterLocation(locations);//retrieves the central location
		    
		    Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		    try {
		    	List<Address> addresses = geoCoder.getFromLocation(latLng.latitude,latLng.longitude,1);
		    	if (addresses.size()>0)
		    	{
		    		for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
		    			address += addresses.get(0).getAddressLine(i) + "\n";
		    	}
		    	Toast.makeText(getBaseContext(), address, Toast.LENGTH_SHORT).show();
		    }catch (Exception e){
		    	e.printStackTrace();
		    }
		    tv.setText("central location is: "+address);
	     }
	  }
	  else
	  {
		  if(count == 2)
		  {
			  Toast.makeText(this, "Something went wrong,  try after sometime: latFinished: "+latFinished+" lngFinished: "+lngFinished,Toast.LENGTH_LONG).show();
			  count=0;
		  }
	  }
	  
	  if(count==2)
		  count=0;
  }*/
}