package com.example.proxima;

import java.io.IOException;
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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class UserListActivity extends Activity {
	ListView listview;
	List<User> userData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);
		listview = (ListView) findViewById(R.id.userListView);
		new GetTask().execute();
	}
	
	private class GetTask extends AsyncTask<Void, Void, Void> {
	     @Override
		protected void onPostExecute(Void result) {
	     }

		@Override
		protected Void doInBackground(Void... params) {
			try {
				userData = getPassedUsers();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		private List<User> getPassedUsers() throws JSONException {
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
		            for (int i = 0; i < a.length(); i++) {
		            	userData.add(new User(((JSONObject) a.get(i)).getString("id"), ((JSONObject) a.get(i)).getString("display_picture")));
		            }
		            return null;
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
