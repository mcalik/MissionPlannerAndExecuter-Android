package com.calikmustafa.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.calikmustafa.common.Functions;
import com.calikmustafa.model.Mission;
import com.calikmustafa.model.Soldier;
import com.calikmustafa.mpe.R;
import com.calikmustafa.structure.JSONParser;
import com.calikmustafa.structure.MissionListCustomArrayAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//known issues
//ekranı çevirince yeniden login olma sorunu


public class MainActivity extends Activity {


    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USER = "soldier";
    private static final String TAG_MISSION = "mission";
    private static final String TAG_NAME = "name";
    private static final String TAG_ID = "id";
    private static final String TAG_RANK = "rankID";
    private static final String TAG_TEAM_ID = "teamID";
    private static final String TAG_LATITUDE = "latitude";
    private static final String TAG_LONGITUDE = "longitude";
    private static final String TAG_TIME = "time";
    private static final String TAG_DETAILS = "details";
    private static final String TAG_SERIAL = "serial";
    private static String url_login = "http://www.calikmustafa.com/senior/login.php";
    private static String get_mission_list = "http://www.calikmustafa.com/senior/getMissionList.php";
    JSONParser jParser = new JSONParser();
    JSONArray userJSON = null;
    ArrayList<Mission> missionList = new ArrayList<Mission>();
    ListView missionListView;
    TextView missionListViewHeader;
    private ProgressDialog pDialog;
    MissionListCustomArrayAdapter veriAdaptoru=null;
    Boolean leader=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        missionListView = (ListView) findViewById(R.id.missionList);


        String serial = "";

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        Log.d(serial,serial);
        if (serial.isEmpty())
            serial = "2";

        //Toast.makeText(getApplicationContext(), "Device serial is "+serial, Toast.LENGTH_LONG).show();

        if(Functions.hasInternet(getApplicationContext()))
            //if(Functions.getUser()==null
                new Login().execute(serial);
        else
            Toast.makeText(getApplicationContext(), "No Internet Access!", Toast.LENGTH_SHORT).show();

        missionListViewHeader = new TextView(getApplicationContext());
        missionListViewHeader.setEnabled(false);
        missionListView.addHeaderView(missionListViewHeader);

    }

    class Login extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Logging in. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("serial", args[0]));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_login, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("user: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    userJSON = json.getJSONArray(TAG_USER);
                    Functions.setUser(null);

                    if (userJSON.length() == 1) {
                        JSONObject c = userJSON.getJSONObject(0);
                        Functions.setUser( new Soldier(c.getInt(TAG_ID), c.getString(TAG_NAME), c.getString(TAG_RANK), c.getString(TAG_SERIAL)));
                    } else if (userJSON.length() < 1) {
                        Log.d("no user with serial!", "");
                    } else
                        Log.d("more than one user with this serial!", "");
                } else {
                    Log.d("no user with this serial!", "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                pDialog.dismiss();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if(Functions.getUser()!=null){
                missionListViewHeader.setText("Welcome " + Functions.getUser().getName());
                new UserMissionList().execute(Functions.getUser().getId() + "");
            }
        }

    }

    class UserMissionList extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Getting mission list. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("soldier_id", args[0]));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(get_mission_list, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("missions: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    Mission mission;
                    userJSON = json.getJSONArray(TAG_MISSION);

                    if (userJSON.length() >0) {
                        for(int i = 0 ; i<userJSON.length();i++){
                            JSONObject c = userJSON.getJSONObject(i);
                            mission = new Mission(c.getInt(TAG_ID),c.getString(TAG_NAME) ,c.getInt(TAG_TEAM_ID),c.getInt("leaderID"),c.getDouble(TAG_LATITUDE),c.getDouble(TAG_LONGITUDE),c.getString(TAG_TIME),c.getString(TAG_DETAILS));
                            if(Functions.getUser().getId() == c.getInt("leaderID"))
                                mission.setName(mission.getName()+"(L)");
                            missionList.add(mission);

        /*                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//format çevirilecek, web servisteki getMission sorgusuna Location parametresi eklenecek ve location tablosuna state kolonu eklenecek!!
                            try {
                                Date dd = dateFormat.parse(mission.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
*/
                            Log.d(mission.toString(),"");
                        }
                    } else
                        Log.d("no mission!", "");
                } else {
                    Log.d("no user with this serial!", "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                pDialog.dismiss();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            veriAdaptoru = new MissionListCustomArrayAdapter(MainActivity.this,missionList,MainActivity.this.getResources());
            missionListView.setAdapter(veriAdaptoru);

            pDialog.dismiss();
        }

    }

    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        Mission temp = missionList.get(mPosition);



                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("mission",temp);
                    startActivity(intent);


                Toast.makeText(getApplicationContext(), temp.getName() , Toast.LENGTH_SHORT).show();
    }

}