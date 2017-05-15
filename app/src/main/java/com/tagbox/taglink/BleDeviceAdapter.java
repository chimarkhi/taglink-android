package com.tagbox.taglink;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;

/**
 * Created by Suhas on 10/31/2016.
 */

public class BleDeviceAdapter extends ArrayAdapter<BleDevice> {
    /*********** Declare Used Variables *********/
    Context context;

    public BleDeviceAdapter(Context context, ArrayList<BleDevice> data) {
        super(context, 0, data);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        BleDevice bleData = getItem(position);

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.lv_ble_row_item, parent, false);
        }

        CompoundCtrlTvTv txtAddress = (CompoundCtrlTvTv)row.findViewById(R.id.tv_address);
        CompoundCtrlTvTv txtName = (CompoundCtrlTvTv)row.findViewById(R.id.tv_name);
        TextView txtUploadSync = (TextView)row.findViewById(R.id.tv_node_last_sync);

        txtAddress.setLabel("Addr : ");
        //txtRssi.setLabel("Rssi : ");
        //txtBondState.setLabel("State : ");
        txtAddress.setValue(bleData.getTagAddress());
        txtName.setLabel("Name : ");
        txtName.setValue(bleData.getFriendlyName());
        //txtRssi.setValue(Integer.toString(bleData.getRssi()));
        DatabaseHandler db = new DatabaseHandler((Activity)context);
        TagLogData tagLog = db.getTagLogData(bleData.getTagAddress());
        db.close();
        if(tagLog == null) {
            txtUploadSync.setText("No data uploaded for this tag");
        } else {
            String dateTime = Utils.getDateTimeFromUnixTimestamp(tagLog.uploadTimestamp);
            txtUploadSync.setText("Tag last synced at " + dateTime);
        }
        /*switch (bleData.getBondState()){
            case BOND_BONDED:
                txtBondState.setValue("Bonded");
                break;
            case BOND_BONDING:
                txtBondState.setValue("Bonding");
                break;
            case BOND_NONE:
                txtBondState.setValue("Not Bonded");
                break;
            default:
                txtBondState.setValue("N/A");
                break;
        }*/

        return row;
    }
}
