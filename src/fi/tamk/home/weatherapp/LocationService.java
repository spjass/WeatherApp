package fi.tamk.home.weatherapp;

import java.util.List;

import fi.tamk.home.weatherapp.MainActivity.RetrieveWeather;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
	LocationManager locationManager;
	LocationListener locationListener;
    public static final String BROADCAST_ACTION = "fi.tamk.home.weatherapp.displayevent";
    Intent intent;

	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("LocationService", "onCreate()");
		locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location loc) {
				// TODO Auto-generated method stub
				double lat = loc.getLatitude();
				double lon = loc.getLongitude();
				Log.d("LocationService", lat + " " + lon);

				Intent intent = new Intent(BROADCAST_ACTION);
				intent.putExtra("latitude", lat);
				intent.putExtra("longitude", lon);
				sendBroadcast(intent);
				
				locationManager.removeUpdates(this);
				locationManager = null;
				LocationService.this.stopSelf();
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				Log.d("LocationService", "disabled");
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				Log.d("LocationService", "enabled");
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				Log.d("LocationService", "status changed");
			}
			
		};
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("LocationService", "onStartCommandasd()");
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*15, 0, locationListener);

		
		return super.onStartCommand(intent, flags, startId);
		
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("LocationService", "onDestroy()");
	}
	
	

}
