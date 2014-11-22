package com.calikmustafa.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.calikmustafa.common.Functions;
import com.calikmustafa.model.Mission;
import com.calikmustafa.model.Soldier;
import com.calikmustafa.model.Team;
import com.calikmustafa.mpe.R;
import com.calikmustafa.structure.JSONParser;
import com.calikmustafa.structure.MissionListCustomArrayAdapter;
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
import java.util.List;

public class MapsActivity extends FragmentActivity {
    JSONParser jParser = new JSONParser();
    private static String url_team = "http://www.calikmustafa.com/senior/getTeam.php";
    private static final String TAG_SUCCESS = "success";
    JSONArray teamJSON = null;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Mission mission;
    private Team team;
    private ArrayList<Soldier> teamList;

    private Button showDetails;
    private Button showTeamMembers;
    private Button functionalButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mission = (Mission) getIntent().getSerializableExtra("mission");
        setContentView(R.layout.activity_maps);

        new FetchTeamList().execute(mission.getTeamID()+"");

        showTeamMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog d = new Dialog(getApplicationContext());
                d.show();
            }
        });

        setUpMapIfNeeded();
        showDetails = (Button) findViewById(R.id.showMissionDetailsButton);
        showTeamMembers = (Button) findViewById(R.id.showTeamButton);
        functionalButton = (Button) findViewById(R.id.functionalButton);
        if(mission.getTeamLeaderID()== Functions.getUser().getId())
            functionalButton.setText("Activate");
        else
            functionalButton.setText("Ready");

    }

    //saçma sapan kod yazmışım mk
    @Deprecated
    private String[] getTeamListString() {
        String[] soldierNames = new String[team.getTeamList().size()];
        for (int i=0; i< team.getTeamList().size();i++) {
            soldierNames[i] = team.getTeamList().get(i).getName();
            if (team.getTeamList().get(i).getId() == team.getLeader().getId())
                soldierNames[i]+="(L)";
        }
        return soldierNames;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        MarkerOptions m = new MarkerOptions().position(new LatLng(mission.getLatitude(), mission.getLongitude())).title(mission.getName());
        m.icon(BitmapDescriptorFactory.fromResource(R.drawable.target));
        mMap.addMarker(m);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(mission.getLatitude(), mission.getLongitude())).zoom(12).build();
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);

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

                        Log.d("------->>>"+team.toString(),"");
                        Log.d("------->>>"+team.getTeamList().toString(),"");


                    } else
                        Log.d("no team!", "");
                } else {
                    Log.d("no team!", "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {

        }

    }

}
