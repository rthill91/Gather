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
import java.util.List;

/**
 * Created by Ryan on 12/3/2014.
 */
public class CommentListAdapter extends ArrayAdapter<JSONObject> {
    int resource;

    public CommentListAdapter(Context context, int resource, List<JSONObject> items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout commentView;
        JSONObject o1 = getItem(position);

        if(convertView == null) {
            commentView = new LinearLayout(getContext());
            String inflator = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflator);
            vi.inflate(resource, commentView, true);
        } else {
            commentView = (LinearLayout)convertView;
        }

        TextView commentText = (TextView)commentView.findViewById(R.id.listItem_textView_comment);
        try {
            commentText.setText(o1.getString("CommentText"));
        } catch(JSONException e) {

        }

        return commentView;
    }
}