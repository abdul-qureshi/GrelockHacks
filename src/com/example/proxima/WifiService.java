package com.example.proxima;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class WifiService extends Service implements LocationListener{
	LocationManager locationManager;
	Location location;
	WifiP2pManager mManager;
	Channel mChannel;
	private final Map<String, Integer> lastSent = new HashMap<String, Integer>();
	private WifiP2pDnsSdServiceRequest serviceRequest;
	private final Handler handler = new Handler();
	public static final String TAG = "Proxima";
	public static final String SERVICE_INSTANCE = "_proximaservice";
	public static final String TXTRECORD_PROP_AVAILABLE = "available";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO do something useful
        //smsHandler.sendEmptyMessageDelayed(DISPLAY_DATA, 1000);
    	System.out.println("QQ LOL");

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO for communication return IBinder implementation
        return null;
    }

    public class wifiBinder extends Binder {
    	public WifiService getService() {
    	        return WifiService.this;
    	}
    }
    @Override
    public void onCreate() {
        super.onCreate();
//        HandlerThread thread = new HandlerThread("Thread name", android.os.Process.THREAD_PRIORITY_BACKGROUND);
//        thread.start();
//        Looper looper = thread.getLooper();
//        OurHandler handler = new OurHandler(looper);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
	    // Define the criteria how to select the location provider -> use
	    // default
	    Criteria criteria = new Criteria();

	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
	    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        handler.postDelayed(runnable, 100);

    }
    public void updateLocation(){

    }
    private final Runnable runnable = new Runnable() {
    	   @Override
    	   public void run() {
    		   location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		    if (location != null) {
    		      System.out.println("location data:"+location.getLatitude()+","+location.getLongitude());
    		    } else {

    		    }
    	      System.out.println("FUCK");
    	      discoverService();

    	      /* and here comes the "trick" */
    	      handler.postDelayed(this, 5000);
    	   }
    	};
    @Override
    public void onDestroy(){
    	System.out.println("foook");
    }
    public class OurHandler extends Handler {
	  public OurHandler(Looper looper) {
	    super(looper);
	  }

	  @Override
	  public void handleMessage(Message msg) {
	    super.handleMessage(msg);


	  }
	}
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

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
				//appendStatus("Added service discovery request");
			}

			@Override
			public void onFailure(int arg0) {
				//appendStatus("Failed adding service discovery request");
			}
		});
		mManager.discoverServices(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				//appendStatus("Service discovery initiated");
			}

			@Override
			public void onFailure(int arg0) {
				//appendStatus("Service discovery failed");

			}
		});

	}

	private class PostTask extends AsyncTask<JSONObject, Integer, Integer> {
	     @Override
		protected void onProgressUpdate(Integer... progress) {
	         //setProgress(progress[0]);
	     }

	     @Override
		protected void onPostExecute(Integer result) {
	         //appendStatus("Status code: " + result);
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