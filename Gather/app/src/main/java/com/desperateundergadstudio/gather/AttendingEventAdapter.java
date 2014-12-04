package com.desperateundergadstudio.gather;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Ryan on 12/3/2014.
 */
public class AttendingEventAdapter extends ArrayAdapter<JSONObject> {
    int resource;
    private AttendingEventAdapterCallback callback;

    public AttendingEventAdapter(Context context, int resource, List<JSONObject> items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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

        TextView eventTitle = (TextView)eventView.findViewById(R.id.attending_LItextView_title);
        Button deleteButton = (Button)eventView.findViewById(R.id.attending_LIbutton_delete);

        try {
            eventTitle.setText(o1.getString("Title"));
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.deletePressed(position);
                }
            });
        } catch(JSONException e) {

        }

        return eventView;
    }

    public void setCallback(AttendingEventAdapterCallback callback) {
        this.callback = callback;
    }

    public interface AttendingEventAdapterCallback {
        public void deletePressed(int position);
    }
}
