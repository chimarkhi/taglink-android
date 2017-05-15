package com.tagbox.taglink;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.tagbox.taglink.Constants.BT_SCAN_MSG;
import static com.tagbox.taglink.Constants.QTAG_ADDR;
import static com.tagbox.taglink.Constants.QTAG_ADV;
import static com.tagbox.taglink.Constants.QTAG_ADV_EXTRA;
import static com.tagbox.taglink.Constants.QTAG_ADV_LIST;
import static com.tagbox.taglink.Constants.QTAG_ADV_LIST_EXTRA;
import static com.tagbox.taglink.Constants.QTAG_ALERT;
import static com.tagbox.taglink.Constants.QTAG_LIST_SAVED;
import static com.tagbox.taglink.Constants.SERVICE_STOP_MSG;


/**
 * A simple {@link Fragment} subclass.
 */
public class QTagFragment extends Fragment {

    private ArrayList<QTagData> bleList;
    private ArrayList<QTagHistoryData> qTagHistoryDatas;
    private ListView lstTagDisplay;
    private QTagDataAdapter adapterListview;
    private QTagHistoryDataAdapter historyDataAdapter;

    boolean isServiceBound;
    private TagLinkService mBoundService;

    protected View mView;
    protected View mHeader;

    /*public QTagFragment() {
        // Required empty public constructor
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        View view = inflater.inflate(R.layout.fragment_qtag, container, false);
        View header = inflater.inflate(R.layout.list_header, null);

        this.mHeader = header;
        this.mView = view;

        updateListView(view);

        /*if(savedInstanceState == null) {
            bleList = new ArrayList<>();
        } else {
            bleList = savedInstanceState.getParcelableArrayList(QTAG_LIST_SAVED);
        }*/

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindTagLinkService();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerForLocalBroadcast();

        updateListView(this.mView);
    }

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private void updateListView(View view) {
        //TextView listHeaderTextView = (TextView)view.findViewById(R.id.tv_heading);
        Log.d("D/QTagFragment", "Updating Fragment View");

        bleList = new ArrayList<>();
        qTagHistoryDatas = new ArrayList<>();
        lstTagDisplay = (ListView) view.findViewById(R.id.listView_Tag);

        TextView headerView = (TextView)mHeader.findViewById(R.id.tv_heading);

        //ViewGroup header = (ViewGroup)inflater.inflate(R.layout.list_header, lstTagDisplay, false);

        lstTagDisplay.removeHeaderView(mHeader);

        boolean result = Utils.isServiceRunning(getActivity(), TagLinkService.class);
        if(result) {
            Log.d("D/QTagFragment", "Taglink Service is Running");

            if (!isServiceBound) {
                Log.d("D/QTagFragment", "Attempting to bind to Taglink Service");
                bindTagLinkService();
            }

            if(mBoundService != null) {
                bleList = mBoundService.getQTagDataList();
                Log.d("D/QTagFragment", "Binded to Taglink Service");
            }

            Log.d("D/QTagFragment", "Retrieving QTag List Size -> "
                    + Integer.toString(bleList.size()));

            headerView.setText("Tags identified during current Sync operation");
            lstTagDisplay.addHeaderView(mHeader, null, false);

            adapterListview = new QTagDataAdapter(getActivity(), bleList);
            lstTagDisplay.setAdapter(adapterListview);
            TextView tv = (TextView)view.findViewById(R.id.tv_empty_element);
            tv.setText("No Tags scanned");
            lstTagDisplay.setEmptyView(tv);
        } else {
            Log.d("D/QTagFragment", "Taglink Service is not running");

            headerView.setText("Tag History (Tags identified during previous Sync operations)");
            lstTagDisplay.addHeaderView(mHeader, null, false);

            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
            List<TagLogData> tagHistory = db.getAllTagLogData();
            db.close();
            for(TagLogData t : tagHistory) {
                QTagHistoryData qTagHistoryData = new QTagHistoryData();
                qTagHistoryData.setFriendlyName(t.friendlyName);
                qTagHistoryData.setUploadTimestamp(t.uploadTimestamp);
                qTagHistoryDatas.add(qTagHistoryData);
            }

            historyDataAdapter = new QTagHistoryDataAdapter(getActivity(), qTagHistoryDatas);
            lstTagDisplay.setAdapter(historyDataAdapter);
            TextView tv = (TextView)view.findViewById(R.id.tv_empty_element);
            tv.setText("No Tags scanned previously");
            lstTagDisplay.setEmptyView(tv);
        }

        //lstTagDisplay.addHeaderView(listHeaderTextView);
    }

    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putParcelableArrayList(QTAG_LIST_SAVED, bleList);
    }*/

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((TagLinkService.LocalBinder)service).getService();
            ArrayList<QTagData> bleDevices = mBoundService.getQTagDataList();
            updateListViewBleDevices(bleDevices);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    void unbindTagLinkService() {
        if (isServiceBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            isServiceBound = false;
        }
    }

    void bindTagLinkService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity(),
                TagLinkService.class), mConnection, 0);
        isServiceBound = true;
    }

    private void updateListViewBleDevices(List<QTagData> newList){
        bleList.clear();
        bleList.addAll(newList);
        if(adapterListview == null) {
            updateListView(getView());
        } else {
            adapterListview.notifyDataSetChanged();
        }
    }

    private void registerForLocalBroadcast(){
        // This registers mMessageReceiver to receive messages.
        IntentFilter filter = new IntentFilter(QTAG_ADV_LIST);
        filter.addAction(SERVICE_STOP_MSG);
        filter.addAction(QTAG_ALERT);
        // This registers mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                filter);
    }

    // handler for received Intents for the "my-integer" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent

            if(QTAG_ADV_LIST.equals(intent.getAction())){
                ArrayList<QTagData> bleDevices = intent.getParcelableArrayListExtra(QTAG_ADV_LIST_EXTRA);
                if(bleDevices != null){
                    updateListViewBleDevices(bleDevices);
                }
            } else if(QTAG_ALERT.equals(intent.getAction())) {
                String address = intent.getStringExtra(QTAG_ADDR);
                int breach = intent.getIntExtra(QTAG_ADV_EXTRA, -1);
                if(breach != -1) {
                    int index = getIndex(bleList, address);
                    if(breach == 1) {
                        lstTagDisplay.getChildAt(index + 1).setBackgroundColor(Color.RED);
                    } else {
                        lstTagDisplay.getChildAt(index + 1).setBackgroundColor(Color.GREEN);
                    }
                }
            } else if(SERVICE_STOP_MSG.equals(intent.getAction())){
                clearList();
            }
        }
    };

    public void clearList() {
        bleList.clear();
        adapterListview.notifyDataSetChanged();
    }

    public int getIndex(List<QTagData> c, String tagAddress) {
        for (int i = 0; i < c.size(); i++) {
            QTagData data = c.get(i);
            String devAddress = data.getTagAddress();
            if (devAddress.equals(tagAddress)) {
                return i;
            }
        }
        return -1;
    }
}
