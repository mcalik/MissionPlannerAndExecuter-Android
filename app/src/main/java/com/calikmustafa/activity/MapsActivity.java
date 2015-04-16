package com.calikmustafa.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.calikmustafa.common.Functions;
import com.calikmustafa.model.Location;
import com.calikmustafa.model.Mission;
import com.calikmustafa.model.Soldier;
import com.calikmustafa.model.Team;
import com.calikmustafa.mpe.R;
import com.calikmustafa.structure.GPSTracker;
import com.calikmustafa.structure.JSONParser;
import com.calikmustafa.structure.TeamListCustomArrayAdaptor;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity {
    JSONParser jParser = new JSONParser();
    JSONArray teamJSON = null;
    private String missionSituation = "";
    private int peopleReady=0;
    private GPSTracker gps;
    private LatLng myLocation;
    private Timer getLocations;
    private Handler handler;
    double lat=0,lon=0;

    private static String url_team = "http://www.calikmustafa.com/senior/getTeam.php";
    private static final String TAG_SUCCESS = "success";

    private Dialog teamDialog;
    private ListView teamListView ;
    private TeamListCustomArrayAdaptor teamListCustomArrayAdaptor;
    private View teamDialogView;

    private Dialog detailDialog;
    private View detailDialogView;
    private TextView missionName;
    private TextView targetLat;
    private TextView targetLon;
    private TextView missionTime;
    private TextView missionDetails;

    private GoogleMap mMap;
    private Mission mission;
    private Team team;
    private Location location;
    private HashMap<Integer,Location> locationList;
    private ArrayList<Soldier> teamList;

    private Button showDetails;
    private Button showTeamMembers;
    private Button functionalButton;


    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mission = (Mission) getIntent().getSerializableExtra("mission");
        setContentView(R.layout.activity_maps);

        gps = new GPSTracker(this);
        if(!gps.canGetLocation())
            gps.showSettingsAlert();

        locationList = new HashMap<Integer,Location>();
        handler = new Handler();
        //set teamDialog
        teamDialog = new Dialog(this);
        teamDialogView = getLayoutInflater().inflate(R.layout.team_custom_listview, null);
        teamListView= (ListView) teamDialogView.findViewById(R.id.listview);

        //set detailDialog
        detailDialog = new Dialog(this);
        detailDialogView = getLayoutInflater().inflate(R.layout.mission_details, null);
        missionName = (TextView) detailDialogView.findViewById(R.id.detailMissionName);
        targetLat = (TextView) detailDialogView.findViewById(R.id.detailTargetLat);
        targetLon = (TextView) detailDialogView.findViewById(R.id.detailTargetLon);
        missionTime = (TextView) detailDialogView.findViewById(R.id.detailMissionTime);
        missionDetails = (TextView) detailDialogView.findViewById(R.id.missionDetails);

        new FetchTeamList().execute(mission.getTeamID() + "");

        setUpMapIfNeeded();

        showDetails = (Button) findViewById(R.id.showMissionDetailsButton);
        showTeamMembers = (Button) findViewById(R.id.showTeamButton);
        showTeamMembers.setEnabled(false);
        functionalButton = (Button) findViewById(R.id.functionalButton);

        showTeamMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teamDialog.show();
            }
        });
        showDetails.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("mission name: ", mission.getName());
                detailDialog.setTitle("Mission Details");
                missionName.setText(mission.getName());
                targetLat.setText(mission.getLatitude() + "");
                targetLon.setText(mission.getLongitude() + "");
                missionTime.setText(mission.getTime());
                missionDetails.setText(mission.getDetails());
                detailDialog.setContentView(detailDialogView);
                detailDialog.show();
            }
        });


        getLocations = new Timer();
        getLocations.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d("location change Listener: ", myLocation+"");
                        double lat=0,lon=0;
                        if(myLocation!=null){
                            lat=myLocation.latitude;
                            lon=myLocation.longitude;
                        }
                        new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "");
                    }
                });
            }
        },2000, 4000);
    }
/*
            params.add(new BasicNameValuePair("mission_id", args[0]));
            params.add(new BasicNameValuePair("soldier_id", args[1]));
            params.add(new BasicNameValuePair("lat", args[2]));
            params.add(new BasicNameValuePair("lon", args[3]));
            params.add(new BasicNameValuePair("status", args[4]));
*/

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if(!gps.canGetLocation())
            gps.showSettingsAlert();
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
        getLocations.cancel();
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(android.location.Location location) {

            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d("location change Listener: ", location.getLatitude() +" "+ location.getLongitude());
            if(myLocation!=null){
                lat=myLocation.latitude;
                lon=myLocation.longitude;
            }
        }

    };
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        MarkerOptions m = new MarkerOptions().position(new LatLng(mission.getLatitude(), mission.getLongitude())).title(mission.getName());
        m.icon(BitmapDescriptorFactory.fromResource(R.drawable.target));
        mMap.addMarker(m);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(mission.getLatitude(), mission.getLongitude())).zoom(12).build();
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    class FetchTeamList extends AsyncTask<String, String, String> {


        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("team_id", args[0]));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_team, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("team: ", json.toString());

            populateTeam(json);
            return null;
        }

        protected void onPostExecute(String file_url) {
            Toast.makeText(MapsActivity.this,"Team list fetched!",Toast.LENGTH_SHORT).show();
            showTeamMembers.setEnabled(true);

            // Change MyActivity.this and myListOfItems to your own values
            teamListCustomArrayAdaptor = new TeamListCustomArrayAdaptor(MapsActivity.this, teamList);
            teamListView.setAdapter(teamListCustomArrayAdaptor);
            teamDialog.setTitle(team.getName());
            teamDialog.setContentView(teamDialogView);
        }
    }

    class MissionSituation extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mission_id", args[0]));
            params.add(new BasicNameValuePair("soldier_id", args[1]));
            params.add(new BasicNameValuePair("lat", args[2]));
            params.add(new BasicNameValuePair("lon", args[3]));
            params.add(new BasicNameValuePair("status", args[4]));


            JSONObject json = jParser.makeHttpRequest("http://www.calikmustafa.com/senior/missionLocation.php", "GET", params);

            Log.d("situation: ", json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);
                missionSituation = json.getString("situation");
                if(json.has("count"))
                    peopleReady = json.getInt("count");
                if(json.has("location"))
                    fillLocation(json.getJSONArray("location"));

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(String file_url) {
            Toast.makeText(MapsActivity.this,"situation got",Toast.LENGTH_SHORT).show();


            if(missionSituation.equals("NOTACTIVATED")){
                if(mission.getTeamLeaderID()==Functions.getUser().getId()){
                    functionalButton.setText("Activate");
                    functionalButton.setVisibility(View.VISIBLE);
                    functionalButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "ACTIVATE");
                        }
                    });
                }else
                    functionalButton.setVisibility(View.INVISIBLE);
            }else if (missionSituation.equals("STARTED")){
                if(mission.getTeamLeaderID()==Functions.getUser().getId()){
                    functionalButton.setText("Finish");
                    functionalButton.setVisibility(View.VISIBLE);
                    functionalButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "END");
                        }
                    });
                }else
                    functionalButton.setVisibility(View.INVISIBLE);
            }else if (missionSituation.equals("ACTIVATED")){
                functionalButton.setText("Ready");
                functionalButton.setVisibility(View.VISIBLE);
                functionalButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "READY");
                    }
                });
            }else if (missionSituation.equals("READY")){
                if(mission.getTeamLeaderID()==Functions.getUser().getId()){
                    functionalButton.setText("Start");
                    functionalButton.setVisibility(View.VISIBLE);
                    functionalButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "START");
                        }
                    });
                }else
                    functionalButton.setVisibility(View.INVISIBLE);
            }else if (missionSituation.equals("END")){
                getLocations.cancel();
                functionalButton.setText("End of Mission");
                functionalButton.setVisibility(View.VISIBLE);
            }

        }
    }

    private void populateTeam(JSONObject json) {
        try {
            // Checking for SUCCESS TAG
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1) {
                Soldier soldier;
                Soldier leader;
                teamJSON = json.getJSONArray("team");
                teamList = new ArrayList<Soldier>();

                if (teamJSON.length() >0) {
                    JSONObject teamObject = teamJSON.getJSONObject(0);
                    JSONArray teamListArray = teamObject.getJSONArray("soldierList");

                    //get soldier list
                    for(int i = 0 ; i< teamListArray.length();i++){
                        JSONObject soldierObject = teamListArray.getJSONObject(i);
                        soldier = new Soldier(soldierObject.getInt("id"),soldierObject.getString("soldierName"),soldierObject.getString("rankName"));
                        teamList.add(soldier);
                    }

                    //get leader
                    JSONArray leaderJson = teamObject.getJSONArray("leader");
                    leader = new Soldier(leaderJson.getJSONObject(0).getInt("id"),leaderJson.getJSONObject(0).getString("soldierName"),leaderJson.getJSONObject(0).getString("rankName"));

                    team = new Team(teamObject.getInt("id"),teamObject.getString("name"),leader,teamList);

                    Log.d("------->>>" + team.toString(), "");
                    Log.d("------->>>"+team.getTeamList().toString(),"");
                    for(Soldier s : teamList)
                        Log.d("------->>>"+s.getName(),"");


                } else
                    Log.d("no team!", "");
            } else {
                Log.d("no team!", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    void fillLocation(JSONArray locs){
        Location temp;
        JSONObject json;
        try {
            if(locs.length()>0){
                for(int i=0;i<locs.length();i++){
                    json = locs.getJSONObject(i);
                    //if(locationList.containsKey(json.getInt("soldierID"))){
                        temp=new Location(json.getString("status"),json.getInt("missionID"),json.getInt("soldierID"),json.getDouble("latitude"),json.getDouble("longitude"),json.getString("time"));
                        locationList.put(temp.getSoldierID(),temp);
                    //}

                }
            }
        } catch (JSONException e) {
        e.printStackTrace();
    }
    }

}
