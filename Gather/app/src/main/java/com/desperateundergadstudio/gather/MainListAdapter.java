package com.desperateundergadstudio.gather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Ryan on 11/28/2014.
 */
public class MainListAdapter extends ArrayAdapter<JSONObject> {
    int resource;
//    String response;
//    Context context;

    public MainListAdapter(Context context, int resource, List<JSONObject> items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout eventView;
        JSONObject o1 = getItem(position);

        if(convertView == null) {
            eventView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            vi.inflate(resource,  eventView, true);
        } else {
            eventView = (LinearLayout)convertView;
        }

        TextView eventTitle = (TextView)eventView.findViewById(R.id.listItem_textView_title);
        TextView eventDescription = (TextView)eventView.findViewById(R.id.listItem_textView_description);
        try {
            eventTitle.setText(o1.getString("Title"));
            eventDescription.setText(o1.getString("Description"));
        } catch(JSONException e) {

        }

        return eventView;
    }
}
