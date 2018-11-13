package com.example.project_samples;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class UserDetails extends Activity{
	
	String mngrid;
	ArrayList<String> empid;
	ArrayList<String> empname;
	ArrayList<String> selectedIds;
	ArrayList<String> selectedNames;
	ArrayList<LatLng> location;
	ArrayList<LatLng> selectedLocations;
	String lat;
	String lng;
	String address;
	
	int count;
	int selected;
	
	public static final String URLUSERDETAILS = "url to dpms retrieving user details php page";
	JSONParser jsonparser;
	boolean network_enabled;
	
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.userdetails);
		Intent i = getIntent();
		mngrid = i.getStringExtra("mngrid");
		
		if(mngrid == null)
		{
			Toast.makeText(this, "something went wrong while retrieving your id", Toast.LENGTH_LONG).show();
			Toast.makeText(this, "Please logout and login again", Toast.LENGTH_LONG).show();
		}
		
		empid = new ArrayList<String>();
		empname = new ArrayList<String>();
		selectedIds = new ArrayList<String>();
		selectedNames = new ArrayList<String>();
		location = new ArrayList<LatLng>();
		selectedLocations = new ArrayList<LatLng>();
		jsonparser = new JSONParser();
		
		count = 0;
		selected = 0;
		new GetUserDetails().execute();
	}
	
	public void onBackPressed(){
		
		//loading mobile home page on back press
		  Intent startMain = new Intent(Intent.ACTION_MAIN);
		  startMain.addCategory(Intent.CATEGORY_HOME);
		  startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		  startActivity(startMain);
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
	
class GetUserDetails extends AsyncTask<String,String,String>{
		
		String response = "";
		Boolean retrieved = false;
		@Override
		protected void onPreExecute(){
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "getting user details", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params){

			try{
				//NameValuePair and BasicNameValuePair are the two classes which are used to send the messages to server i.e., act like a medium
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("mngrid",mngrid));
				
				Log.d("retrieving user details", "starting to retrieve user details");
				//creating the json object by making an HTTP request to webserver 
				JSONObject json = jsonparser.sendHttpRequest(URLUSERDETAILS,"POST",list);
				
				
				
					Log.d("Successfully retrieved user details!", json.toString());
					
					//response = json.toString();
					
					count = json.getInt("count");
					if(count == 0)
						response = "no users registered";
					else
					{
					  for(int i=0; i<count;i++)
					  {
						  response = json.getString("emp"+i);
						  String[] responseArray = response.split("\t");
						  empid.add(responseArray[0]);
						  empname.add(responseArray[1]);
						  lat = responseArray[2];
						  lng = responseArray[3];
						  location.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
					  }
					  retrieved = true;
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
			if(count == 0)
				Toast.makeText(getBaseContext(), "no users registered", Toast.LENGTH_LONG).show();
			showUserDetails();
		}
	}//GetUserDetails

	public void showUserDetails(){
		
		TableLayout table = (TableLayout) findViewById(R.id.table);//showing the details in the table layout
		LayoutParams trparams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);//table row will fill the entire width and height will wrapped to it's content
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);//other views like checkbox, text views for emp id, emp name and emp locations widths and heights will be wrapped to their contents
		for(int i=0; i<count; i++)
		{
			address = "";
			TableRow tr = new TableRow(this);
			tr.setLayoutParams(trparams);
			CheckBox cb = new CheckBox(this);//this checkbox will enable the manager to either select the corresponding employee or deselect the selected employee
			cb.setLayoutParams(params);//set the layout params to wrap it's content
			cb.setId(i);//set id so we can know which employee is selected or deselcted
			cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){//this listener will be called everytime a check box is either is checked or unchecked

			       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
			    	   
			    	   CheckBox checkBox = (CheckBox)buttonView;
						if(isChecked)//if the check box is checked, then the employee details will be added to selectedIds, selectedNames and selectedLocations
						{
							int index = checkBox.getId();//get the id of the checkbox selected
							selectedIds.add(empid.get(index));//get the empid from empid ArrayList at specified index
							selectedNames.add(empname.get(index));//get empname from empname ArrayList at specified index
							selectedLocations.add(location.get(index));//get location of the employee from location ArrayList at specified index
							selected++;//if the selected count == 0, the manager shouldn't be promoted to continue to schedule the meeting, i.e., atleast one employee should be selected to schedule a meeting
						}
						else
						{
							int index = checkBox.getId();
							//removing by searching for the selected object, because if we use id of the checkbox as index, then id of check box might be 2, say, which was first selected, then the index of that object in the arraylist will be 0, not 2.
							selectedIds.remove(selectedIds.indexOf(empid.get(index)));//empid.get(index) returns the unchecked objected, selectedId.indexOf(empid.get(index)) gives the index of the unchecked object 
							selectedNames.remove(selectedNames.indexOf(empname.get(index)));
							selectedLocations.remove(selectedLocations.indexOf(location.get(index)));
							selected--;
						}
			       }
			   }
			);
			
			TextView employeeid = new TextView(this);
			employeeid.setLayoutParams(params);
			employeeid.setText(empid.get(i));//emp id of the ith employee
			TextView employeename = new TextView(this);
			employeename.setLayoutParams(params);
			employeename.setText(empname.get(i));//emp name of the ith employee
			TextView employeelocation = new TextView(this);
			employeelocation.setLayoutParams(params);
			
			Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		    try {
		    	List<Address> addresses = geoCoder.getFromLocation(location.get(i).latitude,location.get(i).longitude,1);//get the address of the specified location(latitude and longitude)
		    	if (addresses.size()>0)
		    	{
		    		for (int j=0; j<addresses.get(0).getMaxAddressLineIndex();j++)
		    			address += addresses.get(0).getAddressLine(j) + "\n";
		    	}
		    }catch (Exception e){
		    	e.printStackTrace();
		    }
		    employeelocation.setText(address);//address location of the ith employee
		    
			tr.addView(cb);//1st column of a row will be the checkbox
			tr.addView(employeeid);//2nd cloumn will be the employee id
			tr.addView(employeename);//3rd parameter will be the emp name
			tr.addView(employeelocation);//4th parameter will be the employee's location
			
			table.addView(tr);//add the row to the table layout
		}
	}
	
	public void next(View v){
		
		if(count == 0)//if no employees registered
			Toast.makeText(this, "no users registered", Toast.LENGTH_LONG).show();
		else if(selected == 0)//if no employee is selected
			Toast.makeText(this, "select atleast 1 employee", Toast.LENGTH_LONG).show();
		else
		{
		  LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		  Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			Double lat= location.getLatitude();
	      	Double lng = location.getLongitude();
	      	LatLng latLng = new LatLng(lat, lng);
	      	selectedLocations.add(latLng);//considering manager's current location also for finding central location
	      	
	      	Intent i = new Intent(this,Schedule.class);
	      	i.putExtra("mngrid", mngrid);
	      	i.putExtra("selectedLocations", selectedLocations);
	      	i.putExtra("selectedIds", selectedIds);
	      	startActivity(i);
		}
	}
}