package com.example.project_samples;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

import com.example.project_samples.Content;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;

public class POST2GCM{

	static String apiKey;
	static Content content;
    public static void post(String apiKey1, Content content1){
    	
    	apiKey = apiKey1;
    	content = content1;
    	new AsyncTask<String, String, String>()
    	{

    		@Override
    		protected String doInBackground(String... params){
    			// TODO Auto-generated method stub
    			try{

    		        // 1. URL
    		        URL url = new URL("https://android.googleapis.com/gcm/send");

    		        // 2. Open connection
    		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    		        // 3. Specify POST method
    		        conn.setRequestMethod("POST");

    		        // 4. Set the headers
    		        conn.setRequestProperty("Content-Type", "application/json");
    		        conn.setRequestProperty("Authorization", "key="+apiKey);

    		        conn.setDoOutput(true);

    		            // 5. Add JSON data into POST request body

    		            //`5.1 Use Jackson object mapper to convert Contnet object into JSON
    		            ObjectMapper mapper = new ObjectMapper();
    		            mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);//enables the gcm server to access any fields of this object

    		            // 5.2 Get connection output stream
    		            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

    		            // 5.3 Copy Content "JSON" into
    		            mapper.writeValue(wr, content);

    		            // 5.4 Send the request
    		            wr.flush();

    		            // 5.5 close
    		            wr.close();

    		            // 6. Get the response
    		            int responseCode = conn.getResponseCode();
    		            Log.d("\nSending 'POST' request to URL : ", url.toString());
    		            Log.d("Response Code : ", Integer.toString(responseCode));

    		            BufferedReader in = new BufferedReader(
    		                    new InputStreamReader(conn.getInputStream()));
    		            String inputLine;
    		            StringBuffer response = new StringBuffer();

    		            while ((inputLine = in.readLine()) != null) {
    		                response.append(inputLine);
    		            }
    		            in.close();

    		            // 7. Print result
    		            Log.d("response: ",response.toString());

    		            } catch (Exception e){
    		            	Log.d("error: ","hello");
    		                e.printStackTrace();
    		            }
    			return null;
    		}
    		
    		public void onPostExecute(String message){
    			
    		}	
    	}.execute();
    }
}