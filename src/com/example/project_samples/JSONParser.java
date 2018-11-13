package com.example.project_samples;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser{

	static InputStream inputstream = null;
	static JSONObject  jsonobject  = null;
	static String json = "";
	
	public JSONObject sendHttpRequest(String url,String method,List<NameValuePair> list){
		
		try{
			
			if(method.equals("POST")){
				//creating the Httpclient and http post
				DefaultHttpClient  httpclient  = new DefaultHttpClient();
				HttpPost           httppost = new HttpPost(url);//url will set by the method which creates an instance to of this class
				//attaching the list or the edittext data to the post method
				httppost.setEntity(new UrlEncodedFormEntity(list));
				//getting the response by executing post method using the httpclient
				HttpResponse httpResponse = httpclient.execute(httppost);
				//converting the hardcoded response into input stream by using the below two steps
				HttpEntity httpEntity = httpResponse.getEntity();
                inputstream = httpEntity.getContent();
			}
			else if(method.equals("GET")){
                // same as above creating the http client but in GET method we will send directly the data in header itself
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String credentials = URLEncodedUtils.format(list, "utf-8");
                url += "?" + credentials;
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                inputstream = httpEntity.getContent();
            }    
		}catch(Exception e){e.printStackTrace();}
		
		//till now we changed the http response into input stream and now we will change the input stream into buffered stream and then
		//change to a JSONObject i.e., a string
		try{
			
			BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
			//take a string builder or string buffer to store the response of the http request or else you can take a simple string and append to string by += operator in while loop below
			StringBuffer sb = new StringBuffer();
			String c = "";
			while((c = br.readLine())!=null){
				sb.append(c+"\n");
			}
			inputstream.close();
			json = sb.toString();
			
		}catch(Exception e){e.printStackTrace(); Log.e("Error", " something wrong with converting result " + e.toString());}
		
		try{
			jsonobject = new JSONObject(json);
			//jsonobject =  new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}")));
		}catch(Exception e){e.printStackTrace();Log.e("json Parsering", "" + e.toString()+" "+json );}
		
		return jsonobject;
	}
}