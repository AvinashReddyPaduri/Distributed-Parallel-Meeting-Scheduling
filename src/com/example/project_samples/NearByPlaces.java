/***
This class is used to find the nearby places (hotels) to the central location
*/
package com.example.project_samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class NearByPlaces extends Activity{

	private static final String GOOGLE_API_KEY = "Google API Key";//google server api key which has google places api key for web server enabled
	double latitude = 0;
    double longitude = 0;
    private int PROXIMITY_RADIUS = 2000;//distance from central location
	String type;
	String address;
	ListView listLocations;
	ArrayList<String> listItems;
	ArrayAdapter<String> listAdapter;
	String mngrid;
	ArrayList<String> empIds;
	ArrayList<String> selectedEmpIds;
	
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.nearbyplaces);
		listLocations = (ListView) findViewById(R.id.nearbyplaces);
		listItems = new ArrayList<String>();
		
		Intent i = getIntent();
		latitude = i.getDoubleExtra("latitude",0.00);
		longitude = i.getDoubleExtra("longitude",0.00);
		mngrid = i.getStringExtra("mngrid");
		empIds = i.getExtras().getStringArrayList("selectedIds");
		selectedEmpIds = new ArrayList<String>();
		for (int j = 0; j < empIds.size(); j++){
			selectedEmpIds.add(empIds.get(j));
		}
		
		//Toast.makeText(this, "latitude: "+latitude+" longitude: "+longitude, Toast.LENGTH_LONG).show();
		
		type = "lodging";//fetches the locations nearby central location which are of type loadges(hotels)
		//send the data appended to the below url which is of google server url that handles nearby places search
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=" + type);
        googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
        googlePlacesUrl.append("&sensor=true");

        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
        Object[] toPass = new Object[1];
        toPass[0] = googlePlacesUrl.toString();
        googlePlacesReadTask.execute(toPass);
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
	
	class GooglePlacesReadTask extends AsyncTask<Object, Integer, String> {//get the nearby locations from google server
	    String googlePlacesData = null;

	    @Override
	    protected String doInBackground(Object... inputObj) {
	        try {
	            String googlePlacesUrl = (String) inputObj[0];
	            Http http = new Http();//for connecting to the server
	            googlePlacesData = http.read(googlePlacesUrl);//get the near by locations as string
	        } catch (Exception e) {
	            Log.d("Google Place Read Task", e.toString());
	        }
	        return googlePlacesData;
	    }

	    @Override
	    protected void onPostExecute(String result){
	    	Log.d("result: ", result);
	        //Toast.makeText(getBaseContext(), "in googleplacesreadtask, googleplacesdata:\n"+result, Toast.LENGTH_LONG).show();
	    	PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
	        Object[] toPass = new Object[1];
	        toPass[0] = result;
	        placesDisplayTask.execute(toPass);
	    }
	}
	
	class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {//show retrieved places to the manager

	    JSONObject googlePlacesJson;

	    @Override
	    protected List<HashMap<String, String>> doInBackground(Object... inputObj) {

	        List<HashMap<String, String>> googlePlacesList = null;
	        Places placeJsonParser = new Places();

	        try {
	            googlePlacesJson = new JSONObject((String) inputObj[0]);
	            googlePlacesList = placeJsonParser.parse(googlePlacesJson);
	        } catch (Exception e) {
	            Log.d("Exception", e.toString());
	        }
	        return googlePlacesList;
	    }

	    @Override
	    protected void onPostExecute(List<HashMap<String, String>> list){
	        
	    	//Toast.makeText(getBaseContext(), "displaying near by places", Toast.LENGTH_LONG).show();
	    	if(list.size() == 0)
	    		Toast.makeText(getBaseContext(), "no nearby places(hotels and banquets) found", Toast.LENGTH_LONG).show();
	    	else
	    	{
	          for (int i = 0; i < list.size(); i++){
	              HashMap<String, String> googlePlace = list.get(i);
	              String placeName = googlePlace.get("place_name");
	              String vicinity = googlePlace.get("vicinity");
	            
	              if(placeName.contains("Hostel"))//remove places which are hostels(google server is retrieving hostels too when we search for lodges)
	            	  continue;
	              listItems.add(placeName+"\n"+vicinity);
	          }
	    	}
	        if(list.size()!=0)
	        {
	        	listAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, listItems);
	        	listLocations.setAdapter(listAdapter);
	        	listLocations.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener(){

					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						String item = (String) listLocations.getItemAtPosition(position);
				        Toast.makeText(getBaseContext(),"You have selected : " + item,Toast.LENGTH_SHORT).show();
				        Intent i = new Intent(getBaseContext(),SendPushNotification.class);
				        i.putExtra("mngrid", mngrid);
				        i.putExtra("selectedPlace", item);
				        i.putExtra("selectedIds", selectedEmpIds);//send push notification to only these employees
				        startActivity(i);
					}
				});
	        }
	    }
	}
}