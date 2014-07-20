package com.example.proxima;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class UserListActivity extends Activity {
	ListView listview;
	ArrayList<User> userData;
	ArrayAdapter<String> itemsAdapter;
	ArrayList<String> userNames;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);
		listview = (ListView) findViewById(R.id.userListView);
		userData = new ArrayList<User>();
		userNames = new ArrayList<String>();
   	    itemsAdapter = 
			    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userNames);
   	    listview.setAdapter(itemsAdapter);
   	    
	   	listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
				startActivity(intent);
				
			}
	     });    
		new GetTask().execute();
	}
	
	private class GetTask extends AsyncTask<Void, Void, ArrayList<User>> {
	     @Override
		protected void onPostExecute(ArrayList<User> result) {
	    	 userData = result;
	    	 if (userData != null) {
		    	 for (User user : userData) {
		    		 userNames.add(user.name);
		    	 }
		    	 itemsAdapter.notifyDataSetChanged();
	    	 }
	    }

		@Override
		protected ArrayList<User> doInBackground(Void... params) {
			try {
				return getPassedUsers();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		private ArrayList<User> getPassedUsers() throws JSONException {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://gentle-garden-5610.herokuapp.com/passed_users/" + getMacAddress());
			
			HttpResponse response = null;
			try {
				response = client.execute(get);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();

		        if (entity != null) {
		            JSONObject object = null;
					try {
						object = new JSONObject(EntityUtils.toString(entity));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					JSONArray a = object.getJSONArray("PassedUsers");
					ArrayList<User> userData = new ArrayList<User>();
		            for (int i = 0; i < a.length(); i++) {
		            	userData.add(new User(((JSONObject) a.get(i)).getString("name"), ((JSONObject) a.get(i)).getString("display_picture")));
		            }
		            return userData;
		        }
			} else {
				System.out.println("Failed to get passed users");
			}
			return null;
		}
		
		private String getMacAddress() {
			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = manager.getConnectionInfo();
			return info.getMacAddress();
		}
	 }
	
	class User {
		String name;
		String displayUrl;
		
		public User(String name, String displayUrl) {
			this.name = name;
			this.displayUrl = displayUrl;
		}
	}
}
