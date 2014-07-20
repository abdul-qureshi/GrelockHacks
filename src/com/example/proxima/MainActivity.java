package com.example.proxima;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String TAG = "Proxima";
	public static final String SERVICE_INSTANCE = "_proximaservice";
	public static final String TXTRECORD_PROP_AVAILABLE = "available";

	WifiP2pManager mManager;
	Channel mChannel;
	BroadcastReceiver mReceiver;
	IntentFilter mIntentFilter;
	PeerListListener mPeerListListener;
	private final List peers = new ArrayList();
	private WifiP2pDnsSdServiceRequest serviceRequest;

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

		final Button discoverButton = (Button) findViewById(R.id.discover);
		discoverButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Start discovering peers", Toast.LENGTH_LONG).show();
//				mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//					@Override
//					public void onSuccess() {
//						Toast.makeText(getApplicationContext(), "Found peers", Toast.LENGTH_LONG).show();
//					}
//
//					@Override
//					public void onFailure(int reasonCode) {
//						Toast.makeText(getApplicationContext(), "Failed to find peers", Toast.LENGTH_LONG).show();
//					}
//				})
				discoverService();
			}
		});

		mPeerListListener = new PeerListListener() {
			@Override
			public void onPeersAvailable(WifiP2pDeviceList peerList) {
				Toast.makeText(getApplicationContext(), "Peers available", Toast.LENGTH_LONG).show();
				// Out with the old, in with the new.
				peers.clear();
				peers.addAll(peerList.getDeviceList());

				if (peers.size() == 0) {
					Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_LONG).show();
					return;
				}
			}
		};

		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListener);

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
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date date = new Date();
						pass.put("date", dateFormat.format(date));
                        pass.put("latitude", 0);
                        pass.put("longitude", 0);
                        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        WifiInfo info = manager.getConnectionInfo();
                        String address = info.getMacAddress();
                        pass.put("passers", address);
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
}
