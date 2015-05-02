package com.calikmustafa.structure;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.calikmustafa.activity.MainActivity;
import com.calikmustafa.activity.MapsActivity;
import com.calikmustafa.model.Soldier;
import com.calikmustafa.mpe.R;

import java.util.ArrayList;

/**
 * Created by Mustafa on 23-Nov-14.
 */
public class TeamListCustomArrayAdaptor extends BaseAdapter  implements View.OnClickListener{

    private ArrayList<Soldier> data;
    private Context context;
    public TeamListCustomArrayAdaptor(Context context, ArrayList<Soldier> data) {
        this.context = context;
        this.data = data;
    }
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View rowView = convertView;

        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.team_list_alertdialog, null);
        }
        ImageView rankImage = (ImageView) rowView.findViewById(R.id.rankImage);
        TextView soldierName = (TextView) rowView.findViewById(R.id.soldierName);

        // Get individual object from  ArrayList<ListData> and set ListView items
        Soldier temp_data = data.get(position);
        int imgID=R.drawable.privete;
        if(temp_data.getRank().equals("Private"))
            imgID=R.drawable.privete;
        else if(temp_data.getRank().equals("Specialist"))
            imgID=R.drawable.specialist;
        else if(temp_data.getRank().equals("Corporal"))
            imgID=R.drawable.corporal;
        else if(temp_data.getRank().equals("Sergeant"))
            imgID=R.drawable.sergeant;
        else if(temp_data.getRank().equals("Lieutenant"))
            imgID=R.drawable.lieutenant;
        else if(temp_data.getRank().equals("Captain"))
            imgID=R.drawable.specialist;
        else if(temp_data.getRank().equals("Colonel"))
            imgID=R.drawable.colonel;
        else
            imgID=R.drawable.general;

        rankImage.setImageResource(imgID);
        soldierName.setText(temp_data.getRank()+" "+temp_data.getName());

        rowView.setOnClickListener(new OnItemClickListener( position ));


        return rowView;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            MapsActivity sct = (MapsActivity)context;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/

            sct.onItemClick(mPosition);
        }
    }
}