package com.karthi.electionhack.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.karthi.electionhack.utils.AppConstants;
import com.karthi.electionhack.utils.GPSTracker;
import com.karthi.electionhack.utils.ServiceHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class LauncherActivity extends Activity {

    //Variable declarations
    String deviceId;
    // GPSTracker class
    GPSTracker gps;

    ProgressDialog pDialog;
    double latitude;
    double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        getDeviceId();


        // create class object
        gps = new GPSTracker(LauncherActivity.this);

        // check if GPS enabled
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {

            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        //Make API call to neem service
        new MakeAPICall().execute("createMethod", deviceId, "Bangalore", String.valueOf(latitude), String.valueOf(longitude));


    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            //System.out.println("network "+"onstatus changed"+ s);
        }

        @Override
        public void onProviderEnabled(String s) {
            //System.out.println("network "+"on provider enabled"+ s);
        }

        @Override
        public void onProviderDisabled(String s) {
            //System.out.println("network "+"on provider disabled"+ s);
        }
    };


    public String getDeviceId() {
        String deviceId;
        try {
            deviceId = Settings.System.getString(getContentResolver(),
                    Settings.System.ANDROID_ID);
        } catch (Exception e) {
            return null;
        }
        return deviceId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launcher, menu);
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


    /**
     * Async task class to get json by making HTTP call
     */
    private class MakeAPICall extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LauncherActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(AppConstants.SERVER_BASE_URL + "&method=" + params[0] + "&name=" + params[1] +
                    "&address=" + params[2] + "&home_lat=" + latitude + "&home_long=" + longitude + "&polling_booth_lat=" + latitude
                    + "&polling_booth_long=" + longitude + "&voter_id=" + params[1] + "&format=json"
                    , ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
        }

    }


}
