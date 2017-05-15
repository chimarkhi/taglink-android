package com.tagbox.taglink;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Suhas on 5/14/2017.
 */

public class QTagHistoryDataAdapter extends ArrayAdapter<QTagHistoryData> {

    Context context;

    public QTagHistoryDataAdapter(Context context, ArrayList<QTagHistoryData> data) {
        super(context, 0, data);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        QTagHistoryData qData = getItem(position);

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.qtag_history_row_item, parent, false);
        }

        CompoundCtrlTvTv txtName = (CompoundCtrlTvTv)row.findViewById(R.id.tv_name);

        TextView txtUploadSync = (TextView)row.findViewById(R.id.tv_node_last_sync);

        txtName.setLabel("Name : ");
        txtName.setValue(qData.getFriendlyName());

        Long uploadTimestamp = qData.getUploadTimestamp();

        if(uploadTimestamp == null) {
            txtUploadSync.setText("No data synced for this tag");
        } else {
            String dateTime = Utils.getDateTimeFromUnixTimestamp(uploadTimestamp);
            txtUploadSync.setText("Tag last synced at " + dateTime);
        }

        return row;
    }
}
