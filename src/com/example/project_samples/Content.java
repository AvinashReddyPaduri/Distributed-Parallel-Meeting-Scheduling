package com.example.project_samples;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Content implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> registration_ids;
    private Map<String,String> data;

    public void addRegId(LinkedList<String> gcmregids){
        /*if(registration_ids == null)
            registration_ids = new LinkedList<String>();*/
        registration_ids = gcmregids;
    }

    public void createData(String title, String message){
        if(data == null)
            data = new HashMap<String,String>();

        data.put("title", title);
        data.put("message", message);
    }
}