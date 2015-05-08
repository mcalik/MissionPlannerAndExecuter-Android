package com.calikmustafa.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
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
import com.calikmustafa.structure.AudioPlayer;
import com.calikmustafa.structure.GPSTracker;
import com.calikmustafa.structure.JSONParser;
import com.calikmustafa.structure.TeamListCustomArrayAdaptor;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    JSONParser jParser = new JSONParser();
    JSONArray teamJSON = null;
    JSONArray messageJSON = null;
    AlertDialog.Builder messageDialog;
    private String missionSituation = "";
    private int peopleReady = 0;
    private GPSTracker gps;
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private LatLng myLocation;
    private Timer getLocations;
    double lat = 0, lon = 0;
    double lastLat = 0, lastLon = 0;
    int toSoldier = -1;
    AudioPlayer ap;
    int lastMessage = 0;
    int asyncTaskNumber = 0;
    final int MAXASYNCTASKNUMBER = 1;
    private InetAddress addr;
    private int numberOfNotReachable = 0;
    private boolean reachable = true;


    private static String url_team = Functions.SERVER + "/senior/getTeam.php";
    private static final String TAG_SUCCESS = "success";

    private Dialog teamDialog;
    private ListView teamListView;
    private TeamListCustomArrayAdaptor teamListCustomArrayAdaptor;
    private View teamDialogView;

    private Dialog detailDialog;
    private View detailDialogView;
    private TextView missionName;
    private TextView targetLat;
    private TextView targetLon;
    private TextView missionTime;
    private TextView missionDetails;
    private TextView readyLabel;
    private TextView messageTextView;

    private GoogleMap mMap;
    private Mission mission;
    private Team team;
    private HashMap<Integer, Location> locationList;
    private ArrayList<Soldier> teamList;
    private HashMap<Integer, Location> enemyList;
    private HashMap<Integer, String> messageList;

    private Button showDetails;
    private Button showTeamMembers;
    private Button functionalButton;
    private Button myLocationButton;
    private Button messageButton;


    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mission = (Mission) getIntent().getSerializableExtra("mission");
        setContentView(R.layout.activity_maps);

        gps = new GPSTracker(MapsActivity.this);
        if (!gps.canGetLocation())
            gps.showSettingsAlert();

        try {
            addr = InetAddress.getByName(Functions.domain);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        locationList = new HashMap<Integer, Location>();
        //set teamDialog
        teamDialog = new Dialog(this);
        teamDialogView = getLayoutInflater().inflate(R.layout.team_custom_listview, null);
        teamListView = (ListView) teamDialogView.findViewById(R.id.listview);
        //set detailDialog
        detailDialog = new Dialog(this);
        detailDialogView = getLayoutInflater().inflate(R.layout.mission_details, null);
        missionName = (TextView) detailDialogView.findViewById(R.id.detailMissionName);
        targetLat = (TextView) detailDialogView.findViewById(R.id.detailTargetLat);
        targetLon = (TextView) detailDialogView.findViewById(R.id.detailTargetLon);
        missionTime = (TextView) detailDialogView.findViewById(R.id.detailMissionTime);
        missionDetails = (TextView) detailDialogView.findViewById(R.id.missionDetails);
        readyLabel = (TextView) findViewById(R.id.readyLabel);
        messageTextView = (TextView) findViewById(R.id.messagesTextView);

        setUpMapIfNeeded();

        enemyList = new HashMap<Integer, Location>();
        messageList = new HashMap<Integer, String>();

        showDetails = (Button) findViewById(R.id.showMissionDetailsButton);
        showTeamMembers = (Button) findViewById(R.id.showTeamButton);
        showTeamMembers.setEnabled(false);
        functionalButton = (Button) findViewById(R.id.functionalButton);
        myLocationButton = (Button) findViewById(R.id.buttonMyLocation);
        messageButton = (Button) findViewById(R.id.buttonMessage);
        messageButton.setEnabled(false);

        new FetchMessageList().execute();


        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageDialog.show();
            }
        });

        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (myLocation != null && myLocation.latitude != 0) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(myLocation.latitude, myLocation.longitude)).zoom(20).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }*/
                if (locationClient != null && locationClient.isConnected()) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude())).zoom(20).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            locationClient = new LocationClient(this, this, this);
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationClient.connect();
        } else {
            Toast.makeText(this, "Google Play Service Error " + resp, Toast.LENGTH_LONG).show();
        }

        Log.i("onResume", "onResume");
        asyncTaskNumber = 0;
        getLocations = new Timer();

        getLocations.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                android.location.Location ll;
                if (locationClient != null && locationClient.isConnected()) {

                    ll = locationClient.getLastLocation();
                    lat = ll.getLatitude();
                    lon = ll.getLongitude();
                    //   Log.w("Can get location", lat + ", "+ lon);
                    //   Log.w("Accuracy", ll.getAccuracy()+"");
                    if (asyncTaskNumber < MAXASYNCTASKNUMBER)
                        if (lastLat == lat)
                            new MissionSituation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mission.getId() + "", Functions.getUser().getId() + "");
                        else
                            new MissionSituation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "");

                    lastLat = lat;
                    lastLon = lon;
                } else if (asyncTaskNumber < MAXASYNCTASKNUMBER)
                    new MissionSituation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mission.getId() + "", Functions.getUser().getId() + "");

                //new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "");

            }

        }, 0, 1000);

        if (!gps.canGetLocation())
            gps.showSettingsAlert();

    }

    @Override
    public void onPause() {
        getLocations.cancel();
        getLocations.purge();
        gps.stopUsingGPS();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i("onStop", "onStop");
        getLocations.cancel();
        getLocations.purge();
        gps.stopUsingGPS();
        locationClient.disconnect();
        super.onStop();
    }


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
        mMap.addMarker(m);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mission.getLatitude(), mission.getLongitude())).zoom(15).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MapsActivity.this);
                final LatLng ll = latLng;
                // set title
                alertDialogBuilder.setTitle("Enemy Detected!");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Warn team?")
                        .setCancelable(false)
                        .setIcon(R.drawable.target)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new setEnemyLocation().execute(mission.getId() + "", Functions.getUser().getId() + "", ll.latitude + "", ll.longitude + "");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Log.w("Disconnected", "Disconnected");
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
/*        if(location!=null){
            Log.w("Location Request",  location.getLatitude() + "," + location.getLongitude());
        }*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
            //Toast.makeText(MapsActivity.this, "Team list fetched!", Toast.LENGTH_SHORT).show();
            showTeamMembers.setEnabled(true);

            // Change MyActivity.this and myListOfItems to your own values
            teamListCustomArrayAdaptor = new TeamListCustomArrayAdaptor(MapsActivity.this, teamList);
            teamListView.setAdapter(teamListCustomArrayAdaptor);
            teamListView.setClickable(true);
            teamDialog.setTitle(team.getName());
            teamDialog.setContentView(teamDialogView);
            teamDialogView.setClickable(true);

            Log.i("team fetched ", "team fetched");

        }
    }

    class FetchMessageList extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            JSONObject json = jParser.makeHttpRequest(Functions.SERVER + "/senior/getMessageList.php", "GET", params);
            try {
                JSONArray messageJson = json.getJSONArray("message");
                if (messageJson.length() > 0) {
                    for (int i = 0; i < messageJson.length(); i++) {
                        messageList.put(messageJson.getJSONObject(i).getInt("id"), messageJson.getJSONObject(i).getString("message"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (messageList.size() > 10) {
                messageButton.setEnabled(true);

                messageDialog = new AlertDialog.Builder(
                        MapsActivity.this);
                messageDialog.setIcon(R.drawable.ic_launcher);
                messageDialog.setTitle("Send Message");
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        MapsActivity.this,
                        android.R.layout.select_dialog_singlechoice);
                for (String s : messageList.values())
                    arrayAdapter.add(s);
                messageDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                messageDialog.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new sendMessage().execute(mission.getId() + "", Functions.getUser().getId() + "", toSoldier + "", (which + 1) + "");
                                toSoldier = -1;
                                messageDialog.setTitle("Send Message");
                                if (teamDialog.isShowing())
                                    teamDialog.dismiss();
                            }
                        });

                new FetchTeamList().execute(mission.getTeamID() + "");
            } else
                this.execute();
        }
    }

    public void showMessages(JSONArray messageJson) {
        JSONObject temp;
        String asd = "";
        if (messageJson.length() > 0)
            for (int i = messageJson.length() - 1; i > -1; i--) {
                try {
                    temp = messageJson.getJSONObject(i);

                    if (lastMessage < temp.getInt("id") && temp.getInt("id") == messageJson.getJSONObject(0).getInt("id")) {
                        ap = new AudioPlayer(temp.getInt("messageID") + ".mp3", MapsActivity.this);
                        lastMessage = temp.getInt("id");
                    }


                    asd += "[" + temp.getString("time").substring(11) + "] " + temp.getString("name") + " : " + temp.getString("message") + "\n";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        messageTextView.setText(asd);
    }

    class MissionSituation extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            try {
                if (addr.isReachable(2000)) {
                    numberOfNotReachable = 0;
                    //Log.w("reachable",reachable+"");
                } else {
                    numberOfNotReachable++;
                    if (numberOfNotReachable > 5) {
                        reachable = false;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (reachable) {
                asyncTaskNumber++;
                //Log.i("AsyncTask started. Total in queue :",asyncTaskNumber+"");

                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("mission_id", args[0]));
                params.add(new BasicNameValuePair("soldier_id", args[1]));
                //Log.w("args.length",args.length+"");
                if (args.length > 2) {
                    params.add(new BasicNameValuePair("lat", args[2]));
                    params.add(new BasicNameValuePair("lon", args[3]));
                    params.add(new BasicNameValuePair("status", args[4]));
                }

                JSONObject json = jParser.makeHttpRequest(Functions.SERVER + "/senior/missionLocation.php", "GET", params);

                //Log.d("situation: ", json.toString());

                try {
                    int success = json.getInt(TAG_SUCCESS);
                    missionSituation = json.getString("situation");
                    if (json.has("count"))
                        peopleReady = json.getInt("count");
                    if (json.has("location")) {
                        fillLocation(json.getJSONArray("location"));
                    }
                    if (json.has("enemy")) {
                        fillEnemyLocation(json.getJSONArray("enemy"));
                    }
                    if (json.has("message")) {
                        messageJSON = json.getJSONArray("message");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (reachable) {
                if (missionSituation.equals("NOTACTIVATED")) {
                    readyLabel.setText("");
                    if (mission.getTeamLeaderID() == Functions.getUser().getId()) {
                        functionalButton.setEnabled(true);
                        functionalButton.setText("Activate");
                        functionalButton.setVisibility(View.VISIBLE);
                        functionalButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "ACTIVATE");
                            }
                        });
                    } else {
                        functionalButton.setEnabled(false);
                        functionalButton.setText("NOT ACTIVATED");
                    }
                } else if (missionSituation.equals("STARTED")) {
                    readyLabel.setText("");
                    if (mission.getTeamLeaderID() == Functions.getUser().getId()) {
                        functionalButton.setEnabled(true);
                        functionalButton.setText("Finish");
                        functionalButton.setVisibility(View.VISIBLE);
                        functionalButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "END");
                            }
                        });
                    } else {
                        functionalButton.setEnabled(false);
                        functionalButton.setText("ON GOING");
                    }
                } else if (missionSituation.equals("ACTIVATED")) {
                    readyLabel.setText(peopleReady + " soldier(s) ready!");
                    functionalButton.setEnabled(true);
                    functionalButton.setText("Ready");
                    functionalButton.setVisibility(View.VISIBLE);
                    functionalButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "READY");
                        }
                    });
                } else if (missionSituation.equals("READY")) {
                    readyLabel.setText(peopleReady + " soldier(s) ready!");
                    if (mission.getTeamLeaderID() == Functions.getUser().getId()) {
                        functionalButton.setText("Start");
                        functionalButton.setEnabled(true);
                        functionalButton.setVisibility(View.VISIBLE);
                        functionalButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new MissionSituation().execute(mission.getId() + "", Functions.getUser().getId() + "", lat + "", lon + "", "START");
                            }
                        });
                    } else {
                        functionalButton.setEnabled(false);
                        functionalButton.setText("NOT STARTED");
                    }
                } else if (missionSituation.equals("END")) {
                    readyLabel.setText("");
                    getLocations.cancel();
                    functionalButton.setText("Return Mission List");
                    functionalButton.setVisibility(View.VISIBLE);
                    functionalButton.setEnabled(true);
                    getLocations.purge();
                    getLocations.cancel();
                    //Toast.makeText(getApplicationContext(), "Mission Finished", Toast.LENGTH_SHORT).show();
                    functionalButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                }
                if (locationList.size() > 0)
                    showLocations();
                if (messageJSON != null && messageJSON.length() > 0)
                    showMessages(messageJSON);

                asyncTaskNumber--;
            }else{
                //Log.w("reachable",reachable+"");
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MapsActivity.this);
                alertDialogBuilder.setTitle("Host is NOT reachable!");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Please check your network connection!")
                        .setCancelable(false)
                        .setIcon(R.drawable.target)
                        .setNeutralButton("Ok!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getLocations.cancel();
                                getLocations.purge();
                                locationClient.disconnect();
                                MapsActivity.this.finish();
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.show();
            }
            //Log.i("AsyncTask finished. Total in queue :",asyncTaskNumber+"");
        }
    }

    class setEnemyLocation extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mission_id", args[0]));
            params.add(new BasicNameValuePair("soldier_id", args[1]));
            params.add(new BasicNameValuePair("lat", args[2]));
            params.add(new BasicNameValuePair("lon", args[3]));

            JSONObject json = jParser.makeHttpRequest(Functions.SERVER + "/senior/setEnemyLocation.php", "GET", params);

            return null;
        }
    }

    class sendMessage extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("missionID", args[0]));
            params.add(new BasicNameValuePair("soldierID", args[1]));
            params.add(new BasicNameValuePair("toSoldier", args[2]));
            params.add(new BasicNameValuePair("messageID", args[3]));

            jParser.makeHttpRequest(Functions.SERVER + "/senior/sendMessage.php", "GET", params);

            return null;
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

                if (teamJSON.length() > 0) {
                    JSONObject teamObject = teamJSON.getJSONObject(0);
                    JSONArray teamListArray = teamObject.getJSONArray("soldierList");

                    //get soldier list
                    for (int i = 0; i < teamListArray.length(); i++) {
                        JSONObject soldierObject = teamListArray.getJSONObject(i);
                        soldier = new Soldier(soldierObject.getInt("id"), soldierObject.getString("soldierName"), soldierObject.getString("rankName"));
                        teamList.add(soldier);
                    }

                    //get leader
                    JSONArray leaderJson = teamObject.getJSONArray("leader");
                    leader = new Soldier(leaderJson.getJSONObject(0).getInt("id"), leaderJson.getJSONObject(0).getString("soldierName"), leaderJson.getJSONObject(0).getString("rankName"));

                    team = new Team(teamObject.getInt("id"), teamObject.getString("name"), leader, teamList);

                    Log.d("------->>>" + team.toString(), "");
                    Log.d("------->>>" + team.getTeamList().toString(), "");
                    for (Soldier s : teamList)
                        Log.d("------->>>" + s.getName(), "");


                } else
                    Log.d("no team!", "");
            } else {
                Log.d("no team!", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    void fillLocation(JSONArray locs) {
        Location temp;
        JSONObject json;
        try {
            if (locs.length() > 0) {
                for (int i = 0; i < locs.length(); i++) {
                    json = locs.getJSONObject(i);
                    //if(locationList.containsKey(json.getInt("soldierID"))){
                    temp = new Location(json.getString("status"), json.getInt("missionID"), json.getInt("soldierID"), json.getDouble("latitude"), json.getDouble("longitude"), json.getString("time"));
                    if (!locationList.containsKey(temp.getSoldierID()))
                        locationList.put(temp.getSoldierID(), temp);
                    else
                        locationList.get(temp.getSoldierID()).set(temp);
                    //}

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void fillEnemyLocation(JSONArray locs) {
        Location temp;
        JSONObject json;
        try {
            if (locs.length() > 0) {
                for (int i = 0; i < locs.length(); i++) {
                    json = locs.getJSONObject(i);
                    temp = new Location("", json.getInt("missionID"), -1, json.getDouble("latitude"), json.getDouble("longitude"), json.getString("time"));
                    enemyList.put(json.getInt("id"), temp);
                    //Log.d("enemy List: ", temp.getLatitude() + "");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    HashMap<Integer, Marker> marker = new HashMap<Integer, Marker>();
    HashMap<Integer, Marker> enemyMarker = new HashMap<Integer, Marker>();

    void showLocations() {
        Location temp;
        //if(teamList!=null)
        //teamlist bo?sa patlar
        if (teamList != null)
            for (int i = 0; i < teamList.size(); i++) {
                if (locationList.containsKey(teamList.get(i).getId())) {
                    temp = locationList.get(teamList.get(i).getId());
                    if (!marker.containsKey(i)) {
                        marker.put(i, mMap.addMarker(new MarkerOptions().position(new LatLng(temp.getLatitude(), temp.getLongitude())).title(teamList.get(i).getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.user_soldier))));
                        if (teamList.get(i).getId() == Functions.getUser().getId()) {
                            marker.get(i).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.soldier_own));
                        } else if (mission.getTeamLeaderID() == teamList.get(i).getId())
                            marker.get(i).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.soldier_teamleader));

                    } else {
                        marker.get(i).setPosition(new LatLng(temp.getLatitude(), temp.getLongitude()));

                    }
                }
            }
        for (int i : enemyList.keySet()) {
            temp = enemyList.get(i);
            //Log.d("Enemy : ", temp.toString());
            if (!enemyMarker.containsKey(i)) {
                enemyMarker.put(i, mMap.addMarker(new MarkerOptions().position(new LatLng(temp.getLatitude(), temp.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.enemy))));

            }
        }
    }

    public void onItemClick(int mPosition) {
        if (messageDialog != null) {
            messageDialog.setTitle("Message to " + teamList.get(mPosition).getName());
            toSoldier = teamList.get(mPosition).getId();
            messageDialog.show();
        }
    }
}
