package com.tagbox.taglink;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Suhas on 4/13/2017.
 */

public class QTagDataAdapter extends ArrayAdapter<QTagData> {
    Context context;

    public QTagDataAdapter(Context context, ArrayList<QTagData> data) {
        super(context, 0, data);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        QTagData qData = getItem(position);

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.qtag_row_item, parent, false);
        }

        CompoundCtrlTvTv txtName = (CompoundCtrlTvTv)row.findViewById(R.id.tv_name);
        CompoundCtrlTvTv txtTemp = (CompoundCtrlTvTv)row.findViewById(R.id.tv_temp);
        CompoundCtrlTvTv txtHum = (CompoundCtrlTvTv)row.findViewById(R.id.tv_hum);

        TextView txtUploadSync = (TextView)row.findViewById(R.id.tv_node_last_sync);

        txtName.setLabel("Name : ");
        txtName.setValue(qData.getFriendlyName());

        txtTemp.setLabel("Temperature : ");
        if(qData.getTemperature() == null) {
            txtTemp.setValue("");
        } else {
            txtTemp.setValue(qData.getTemperature());
        }

        txtHum.setLabel("Humidity : ");
        if(qData.getHumidity() == null) {
            txtHum.setValue("");
        } else {
            txtHum.setValue(qData.getHumidity());
        }

        String uploadSyncText = "";
        if(qData.getStatus() == null || qData.getStatus() == "") {
            DatabaseHandler db = new DatabaseHandler((Activity) context);
            TagLogData tagLog = db.getTagLogData(qData.getTagAddress());
            db.close();
            if(tagLog == null) {
                uploadSyncText = "No data synced for this tag";
            } else {
                String dateTime = Utils.getDateTimeFromUnixTimestamp(tagLog.uploadTimestamp);
                uploadSyncText = "Tag last synced at " + dateTime;
            }
        } else {
            uploadSyncText = qData.getStatus();
        }

        txtUploadSync.setText(uploadSyncText);

        /*if(qData.getBreach() != null) {
            if (qData.getBreach() == 1) {
                row.setBackgroundColor(Color.RED);
            }
        }*/

        return row;
    }
}