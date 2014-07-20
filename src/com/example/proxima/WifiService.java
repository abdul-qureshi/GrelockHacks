package com.example.proxima;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public class WifiService extends Service implements LocationListener{
	LocationManager locationManager;
	Location location;
	private Handler handler = new Handler();
	
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
	    // Define the criteria how to select the location provider -> use
	    // default
	    Criteria criteria = new Criteria();

	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
	    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        handler.postDelayed(runnable, 100);
        
    }
    public void updateLocation(){
    	
    }
    private Runnable runnable = new Runnable() {
    	   @Override
    	   public void run() {
    		   location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		    if (location != null) {
    		      System.out.println("location data:"+location.getLatitude()+","+location.getLongitude());
    		    } else {
    		    	
    		    }
    	      System.out.println("FUCK");
    	      
    	      /* and here comes the "trick" */
    	      handler.postDelayed(this, 3000);
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
}