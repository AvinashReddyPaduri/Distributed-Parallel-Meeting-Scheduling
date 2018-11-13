package com.example.project_samples;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CentralLocation{
	
	public LatLng getCenterLocation(ArrayList<LatLng> locations){
		/***
		Uses K-medoid algorithm to find the central location
		*/
		
		double minDistance = Double.MAX_VALUE;//initially minimum distance is set to maximum of the double so that later when comparing, this will always be maximum initially
		int index=0;
		LatLng current;
		double distance;//stores the distance between 2 employees for each iteration in K-Medoids
		
		for(int i=0;i<locations.size();i++)//finding the point which has minimum distance to all other locations
		{
			distance = 0;
			current = locations.get(i);
			for(int j=0;j<locations.size();j++)//finding total distance from ith location (or) current location to all other positions(j)
			{
				if(i==j)//i==j returns true when the current location is same as the location we want to find the distance from it
					continue;//no need to find distance when both the locations are same
				distance += distanceBetweenLocations(current,locations.get(j));//distance between ith location (or) current location and jth location
			}
			if(distance<minDistance)//total distance from current location to all others is lesser than the previous minimum distance, then the current location should be preferred
			{
				minDistance = distance;
				index = i;//storing the position of location in index which has the minimum distance from it to all others
			}
		}
		
		LatLng central = locations.get(index);
		return central;
	}
	
	double distanceBetweenLocations(LatLng current, LatLng next)//calculating distance between 2 locations
	{
		double length;
		//calculating distance between 2 locations by using distance between 2 points formula, where x1, x2 indicate latitudes and y1, y2 indicate longitudes of the positions
		length = Math.sqrt((Math.pow((current.latitude-next.latitude),2))+(Math.pow((current.longitude-next.longitude),2)));
		return length;
	}
}