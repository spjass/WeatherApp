package fi.tamk.home.weatherapp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	Intent intent;
	double lat;
	double lon;
	RetrieveWeather rw;
	ProgressBar bar;

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);       
        }

		private void updateUI(Intent intent) {
			// TODO Auto-generated method stub
			lat = intent.getDoubleExtra("latitude", 0);
			lon = intent.getDoubleExtra("longitude",0);
			rw = new RetrieveWeather();
			String url = urlBuilder(lat, lon);
			rw.execute(url);
			Log.d("MainActivity", Double.toString(lat) + " UPDATEUI");
		}
    };   
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getActionBar().hide();
		refreshWeather();
		
 	
	}
	

	
	@Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));
    }
    
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);         
    }
	
    public void refreshWeather() {
		rw = new RetrieveWeather();
		double[] latlon = getLastPosition();
		String url = urlBuilder(latlon[0], latlon[1]);
		rw.execute(url);
    }
    
	public void getWeather(View v) {
		
		intent = new Intent(this, LocationService.class);
		this.startService(intent);
		refreshWeather();
	}
	

	
	
	public String urlBuilder (double lat, double lon) {
		String url = "http://api.wunderground.com/api/4c1ad6c94dd209a4/conditions/pws/q/" 
				+ Double.toString(lat) + "," + Double.toString(lon) + ".json";
		return url;
	}
	
	private double[] getLastPosition() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
        List<String> providers = lm.getProviders(true);

        
        Location l = null;
        
        for (int i=providers.size()-1; i>=0; i--) {
                l = lm.getLastKnownLocation(providers.get(i));
                if (l != null) break;
        }
        
        double[] lonlat = new double[2];
        if (l != null) {
                lonlat[0] = l.getLatitude();
                lonlat[1] = l.getLongitude();
        }
        return lonlat;
	}
	
	public class RetrieveWeather extends AsyncTask<String, Void, HashMap<String, String>> {
		HashMap<String, String> observationList;
		
		protected HashMap<String, String> doInBackground(String... urls) {
			observationList = new HashMap<String, String>();
			try {
				
				URL url = new URL(urls[0]);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				
			    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			    String response = readStream(in);
			    urlConnection.disconnect();
			    observationList = parseJson(response);
			    return observationList;
			    
			} catch (Exception e) {
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String response = sw.toString();
				Log.d("RetrieveWeather", response);
				
			}
			return observationList;
		}
		
	    @Override
	    protected void onPreExecute(){

	    	bar = (ProgressBar) findViewById(R.id.progressBar);
	    	bar.setVisibility(View.VISIBLE);
	    	
	    }
		
	    @Override
		protected void onPostExecute(HashMap<String, String> observationList) {
	    	
	    	bar = (ProgressBar) findViewById(R.id.progressBar);
			TextView city = (TextView) findViewById(R.id.city);
			TextView temperature = (TextView) findViewById(R.id.temperature);
			
	    	String cityStr = observationList.get("city");
			String countryStr = observationList.get("country");
			String tempStr = observationList.get("temp_c");
			String weather = observationList.get("weather");
			String iconUrl = observationList.get("iconUrl");
			
			ImageLoader loader = new ImageLoader(); 
			loader.execute(iconUrl);
			
			city.setText(countryStr + "\n" + cityStr);
			temperature.setText(weather + ", " + tempStr + " C");
			
			
			super.onPostExecute(observationList);
		}


		
		protected String readStream(BufferedReader in) {
	        StringBuilder response = new StringBuilder();
	        String inputLine;
	        try {
	        
	        	while ((inputLine = in.readLine()) != null) {
	        		response.append(inputLine);
	        	}
	        
	        	in.close();	
	        	
	        } catch (Exception e) {
	        	StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String response2 = sw.toString();
				Log.d("RetrieveWeather", response2);
	        }
	        return response.toString();
		}
		
		protected HashMap<String, String> parseJson(String json) {
			
			
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					
					
					// Current observation
					JSONObject observation = jsonObj.getJSONObject("current_observation");
					String temperature = observation.getString("temp_c");
					String weather = observation.getString("weather");
					String descIconUrl= observation.getString("icon_url");
					Log.d("RetrieveWeather", temperature);
					
					// Current location
					JSONObject location = observation.getJSONObject("observation_location");
					String fullLocation = location.getString("full");
					
					JSONObject country = observation.getJSONObject("display_location");
					String countryLocation = country.getString("state_name");
					
					// Put data to hashmap
					observationList.put("temp_c", temperature);
					observationList.put("weather", weather);
					observationList.put("iconUrl", descIconUrl);
					observationList.put("city", fullLocation);
					observationList.put("country", countryLocation);
					
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					String response = sw.toString();
					Log.d("RetrieveWeather", response);
				}
			}
			
			return observationList;
		}

	}
	public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
		
		private Bitmap fetchImage( String... urlstr )
		{
		    try
		    {
		        URL url;
		        url = new URL( urlstr[0] );
	
		        HttpURLConnection c = ( HttpURLConnection ) url.openConnection();
		        c.setDoInput( true );
		        c.connect();
		        InputStream is = c.getInputStream();
		        Bitmap img;
		        img = BitmapFactory.decodeStream( is );
		        return img;
		    }
		    catch ( MalformedURLException e )
		    {
		        Log.d( "RemoteImageHandler", "invalid URL: " + urlstr );
		    }
		    catch ( IOException e )
		    {
		        Log.d( "RemoteImageHandler", "IO exception: " + e );
		    }
		    return null;
		}

		@Override
		protected Bitmap doInBackground(String... urlstr) {
			
			return fetchImage(urlstr[0]);
		}
	
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			ImageView icon = (ImageView) findViewById(R.id.imageView1);
			icon.setImageBitmap(bitmap);
			bar.setVisibility(View.GONE);
		}
	}
	

}
