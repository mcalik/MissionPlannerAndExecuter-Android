package com.calikmustafa.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.calikmustafa.model.Mission;
import com.calikmustafa.model.Soldier;
import com.calikmustafa.model.Team;
import com.calikmustafa.mpe.R;
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
import java.util.List;

public class MapsActivity extends FragmentActivity {
    JSONParser jParser = new JSONParser();
    JSONArray teamJSON = null;

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

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Mission mission;
    private Team team;
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

        new FetchTeamList().execute(mission.getTeamID()+"");

        setUpMapIfNeeded();
        showDetails = (Button) findViewById(R.id.showMissionDetailsButton);
        showTeamMembers = (Button) findViewById(R.id.showTeamButton);
        showTeamMembers.setEnabled(false);
        functionalButton = (Button) findViewById(R.id.functionalButton);

        if(mission.getTeamLeaderID()== Functions.getUser().getId())
            functionalButton.setText("Activate");
        else
            functionalButton.setText("Ready");


        showTeamMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teamDialog.show();
            }
        });

        showDetails.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d("mission name: ",mission.getName());
                detailDialog.setTitle("Mission Details");
                missionName.setText(mission.getName());
                targetLat.setText(mission.getLatitude()+"");
                targetLon.setText(mission.getLongitude()+"");
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
        setUpMapIfNeeded();
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


}
