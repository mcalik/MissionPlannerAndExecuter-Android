package com.calikmustafa.structure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.calikmustafa.model.Soldier;
import com.calikmustafa.mpe.R;

import java.util.ArrayList;

/**
 * Created by Mustafa on 23-Nov-14.
 */
public class TeamListCustomArrayAdaptor extends BaseAdapter {
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
     //   rankImage.setImageResource(temp_data.rankImage());
        soldierName.setText(temp_data.getName());

        return rowView;
    }
}