package com.example.proxima;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.proxima.WifiService.wifiBinder;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import android.os.IBinder;

import android.view.View.OnTouchListener;
import android.view.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {



	public static final String TAG = "Proxima";
	public static final String SERVICE_INSTANCE = "_proximaservice";
	public static final String TXTRECORD_PROP_AVAILABLE = "available";
	
	WifiP2pManager mManager;
	Channel mChannel;
	BroadcastReceiver mReceiver;
	IntentFilter mIntentFilter;
	PeerListListener mPeerListListener;
	private final List peers = new ArrayList();
	private final Map<String, Integer> lastSent = new HashMap<String, Integer>();
	private WifiP2pDnsSdServiceRequest serviceRequest;

	private TextView locationText;
	private TextView serviceTest;
	private Button button1;
	private LocationManager locationManager;
	private String provider;
	  
	WifiService mService;
	boolean mBound=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		Intent intent = new Intent(this, UserListActivity.class);
		startActivity(intent);

//		final Button discoverButton = (Button) findViewById(R.id.discover);
//		discoverButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(), "Start discovering peers", Toast.LENGTH_LONG).show();
////				mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
////					@Override
////					public void onSuccess() {
////						Toast.makeText(getApplicationContext(), "Found peers", Toast.LENGTH_LONG).show();
////					}
////
////					@Override
////					public void onFailure(int reasonCode) {
////						Toast.makeText(getApplicationContext(), "Failed to find peers", Toast.LENGTH_LONG).show();
////					}
////				})
//				discoverService();
//			}
//		});

		mPeerListListener = new PeerListListener() {
			@Override
			public void onPeersAvailable(WifiP2pDeviceList peerList) {
//				Toast.makeText(getApplicationContext(), "Peers available", Toast.LENGTH_LONG).show();
				// Out with the old, in with the new.
				peers.clear();
				peers.addAll(peerList.getDeviceList());

				if (peers.size() == 0) {
//					Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_LONG).show();
					return;
				}
			}
		};

		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListener);
		

//		locationText = (TextView) findViewById(R.id.location);
//		serviceTest = (TextView) findViewById(R.id.serviceTest);
//		button1 = (Button) findViewById(R.id.button1);
//		button1.setOnTouchListener(new OnTouchListener() {
//
//		    @Override
//		    public boolean onTouch(View v, MotionEvent event) {
//		    	updateLocation();
//		    	return false;
//		    }
//		   });

	    // Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the location provider -> use
	    // default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);
	    
	    // Initialize the location fields
//	    if (location != null) {
//	      System.out.println("Provider " + provider + " has been selected.");
//	      locationText.setText("let's get started");
//	      
//	      onLocationChanged(location);
//	    } else {
//	      locationText.setText("Location not available");
//	      button1.setText("test location");
//	    }

	}
	public void test(){
		Intent intent = new Intent(this,ChatActivity.class);
		startActivity(intent);
	}
	@Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
//        Intent intent = new Intent(this, WifiService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        System.out.println("starting service");
        Intent intent = new Intent(getApplicationContext(),WifiService.class);
        startService(intent);
    }
	
	private void discoverService() {

		/*
		 * Register listeners for DNS-SD services. These are callbacks invoked
		 * by the system when a service is actually discovered.
		 */

		mManager.setDnsSdResponseListeners(mChannel,
				new DnsSdServiceResponseListener() {

			@Override
			public void onDnsSdServiceAvailable(String instanceName,
					String registrationType, WifiP2pDevice srcDevice) {

				Log.d(TAG, "onBonjourServiceAvailable " +
							srcDevice + " " + instanceName + " " + registrationType);
				if (true) {//instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
					WiFiP2pService service = new WiFiP2pService();
					service.device = srcDevice;
					service.instanceName = instanceName;
					service.serviceRegistrationType = registrationType;

					JSONObject pass = new JSONObject();
					try {
						pass.put("sender", srcDevice.deviceAddress);
						pass.put("date", System.currentTimeMillis() / 1000L);
                        pass.put("latitude", 0);
                        pass.put("longitude", 0);
                        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        WifiInfo info = manager.getConnectionInfo();
                        String address = info.getMacAddress();
                        pass.put("passers", address);
                        Integer lastSentTime = lastSent.get(srcDevice.deviceAddress);
                        if (lastSentTime == null)
                          new PostTask().execute(pass);
		                else if ((lastSentTime - System.currentTimeMillis() / 1000L) > 10000)
		                  new PostTask().execute(pass);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}

			}
		}, new DnsSdTxtRecordListener() {

			/**
			 * A new TXT record is available. Pick up the advertised
			 * buddy name.
			 */
			 @Override
			 public void onDnsSdTxtRecordAvailable(
					 String fullDomainName, Map<String, String> record,
					 WifiP2pDevice device) {
				 Log.d(TAG,
						 device.deviceName + " is "
								 + record.get(TXTRECORD_PROP_AVAILABLE));
			 }
		});

		// After attaching listeners, create a service request and initiate
		// discovery.
		serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		mManager.addServiceRequest(mChannel, serviceRequest,
				new ActionListener() {

			@Override
			public void onSuccess() {
				appendStatus("Added service discovery request");
			}

			@Override
			public void onFailure(int arg0) {
				appendStatus("Failed adding service discovery request");
			}
		});
		mManager.discoverServices(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				appendStatus("Service discovery initiated");
			}

			@Override
			public void onFailure(int arg0) {
				appendStatus("Service discovery failed");

			}
		});

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/* register the broadcast receiver with the intent values to be matched */
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mIntentFilter);
	}

	/* unregister the broadcast receiver */
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	public void appendStatus(String status) {
		Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
    }

	private class PostTask extends AsyncTask<JSONObject, Integer, Integer> {
	     @Override
		protected void onProgressUpdate(Integer... progress) {
	         setProgress(progress[0]);
	     }

	     @Override
		protected void onPostExecute(Integer result) {
	         appendStatus("Status code: " + result);
	     }

		@Override
		protected Integer doInBackground(JSONObject... params) {
			HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://gentle-garden-5610.herokuapp.com/add_pass");
            httppost.setHeader("Content-type", "application/json");

            HttpResponse response = null;
			try {
				httppost.setEntity(new StringEntity(params[0].toString()));
				response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return response.getStatusLine().getStatusCode();
		}
	 }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
//		locationText.setText("Your altitude:"+location.getAltitude()+",longitude:"+location.getLongitude()+",latitude:"+location.getLatitude());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
		        Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
		        Toast.LENGTH_SHORT).show();
		
	}
	public void updateLocation(){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);

	    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//	    locationText.setText("Your altitude:"+location.getAltitude()+",longitude:"+location.getLongitude()+",latitude:"+location.getLatitude());
	    
//	    serviceTest.setText(mService.testService());
	    
		System.out.println("new location");
	}
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	wifiBinder binder = (wifiBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
