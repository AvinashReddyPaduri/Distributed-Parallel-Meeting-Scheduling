package com.example.project_samples;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Welcome extends Activity{

	TextView tv;
	String userid;
	String message = null;
	
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.welcome);
		tv = (TextView) findViewById(R.id.empselection);
		
		SharedPreferences login = getSharedPreferences("login", Context.MODE_PRIVATE);
		
		if(login.getBoolean("isLoggedin", false))//load home page on clicking the notification if the client is logged off
		{
			Intent i = new Intent(this,Home.class);
			startActivity(i);
		}
		Intent i = getIntent();
		if(i!=null)
		{
			message = i.getStringExtra("message");
			userid = i.getStringExtra("empid");
			if(message != null)
			{
				tv.setText(message);
				SharedPreferences meeting = getSharedPreferences("meeting", Context.MODE_PRIVATE);
				Editor editor = meeting.edit();
				editor.putString("meetingDetails", message);
				editor.putString("storedempid", userid);
				editor.commit();
			}
		}
		else
		{
			SharedPreferences meeting = getSharedPreferences("meeting", Context.MODE_PRIVATE);
			if(userid.equals(meeting.getString("storedempid", "not initialized")))
			{
				String meetingDetails = meeting.getString("meetingDetails", null);
				tv.setText(meetingDetails);
			}
			else
				tv.setText("You have no meetings now");
		}
	}
	
	public void logout(View v){
		
		SharedPreferences login = getSharedPreferences("login", Context.MODE_PRIVATE);
		Editor editor2 = login.edit();
		editor2.putBoolean("isLoggedin", false);
		editor2.putString("logintype", "employee");
		editor2.commit();
		
		Intent i = new Intent(this,Home.class);
		startActivity(i);
	}
	
	public void onBackPressed(){
		
		//loading mobile home page on back press
		  Intent startMain = new Intent(Intent.ACTION_MAIN);
		  startMain.addCategory(Intent.CATEGORY_HOME);
		  startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		  startActivity(startMain);
	}
}