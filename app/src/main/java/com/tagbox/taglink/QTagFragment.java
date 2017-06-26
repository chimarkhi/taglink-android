package com.tagbox.taglink;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Paint;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.tagbox.taglink.Constants.QTAG_ADV_LIST;
import static com.tagbox.taglink.Constants.QTAG_ADV_LIST_EXTRA;
import static com.tagbox.taglink.Constants.QTAG_ALERT;
import static com.tagbox.taglink.Constants.SERVICE_STOP_MSG;

/**
 * A simple {@link Fragment} subclass.
 */
public class QTagFragment extends Fragment {

    private ArrayList<QTagData> bleList;
    private ListView lstTagDisplay;
    private QTagDataAdapter adapterListview;

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

        createListView(view);

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

        //updateListView(this.mView);
        createListView(this.mView);
    }

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private void createListView(View view) {
        bleList = new ArrayList<>();

        ApplicationSettings applicationSettings = new ApplicationSettings(this.getActivity().getApplicationContext());
        String whitelist = applicationSettings.getAppSettingString(ApplicationSettings.TAG_WHITELIST);

        if(whitelist != null && whitelist != "") {
            try {
                JSONObject list = new JSONObject(whitelist);

                for(int i = 0; i < list.names().length(); i++) {
                    //JSONObject object = list.getJSONObject(i);
                    String macId = list.names().getString(i);
                    String clientId = list.getString(list.names().getString(i));

                    QTagData qTagData = new QTagData(macId);
                    qTagData.setFriendlyName(clientId);
                    bleList.add(qTagData);
                }
            } catch (Exception ex) {}
        }

        lstTagDisplay = (ListView) view.findViewById(R.id.listView_Tag);

        TextView headerView = (TextView)mHeader.findViewById(R.id.tv_heading);
        headerView.setPaintFlags(headerView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        lstTagDisplay.removeHeaderView(mHeader);

        String login_id = applicationSettings.getAppSettingString(ApplicationSettings.STRING_LOGIN_USERNAME);

        headerView.setText("Tags associated with Login ID - " + login_id);
        lstTagDisplay.addHeaderView(mHeader, null, false);

        TextView tv = (TextView)view.findViewById(R.id.tv_empty_element);
        tv.setText("No Tags to be scanned for Login ID - " + login_id);
        lstTagDisplay.setEmptyView(tv);

        updateListView(view);
    }

    private void updateListView(View view) {
        Log.d("D/QTagFragment", "Updating Fragment View");

        lstTagDisplay = (ListView) view.findViewById(R.id.listView_Tag);

        boolean result = Utils.isServiceRunning(getActivity(), TagLinkService.class);
        if(result) {
            Log.d("D/QTagFragment", "Taglink Service is Running");

            if (!isServiceBound) {
                Log.d("D/QTagFragment", "Attempting to bind to Taglink Service");
                bindTagLinkService();
            }

            ArrayList<QTagData> currList;

            if(mBoundService != null) {
                currList = mBoundService.getQTagDataList();
                updateBleList(currList);

                Log.d("D/QTagFragment", "Binded to Taglink Service");
            }

            Log.d("D/QTagFragment", "Retrieving QTag List Size -> "
                    + Integer.toString(bleList.size()));
        } else {
            Log.d("D/QTagFragment", "Taglink Service is not running");

            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
            List<TagLogData> tagHistory = db.getAllTagLogData();
            db.close();
            for(TagLogData t : tagHistory) {
                int index = getIndex(bleList, t.nodeId);
                if(index != -1) {
                    QTagData d = new QTagData(t.nodeId);
                    d.setFriendlyName(t.friendlyName);
                    String dateTime = Utils.getDateTimeFromUnixTimestamp(t.uploadTimestamp);
                    d.setStatus("Data synced till " + dateTime);
                }
            }
        }

        adapterListview = new QTagDataAdapter(getActivity(), bleList);
        lstTagDisplay.setAdapter(adapterListview);
    }

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

    private void updateListViewBleDevices(ArrayList<QTagData> newList){
        updateBleList(newList);
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
            }
        }
    };

    public void updateBleList(ArrayList<QTagData> currList) {
        for(QTagData data : currList) {
            int index = getIndex(bleList, data.getTagAddress());
            if(index != -1) {
                bleList.set(index, data);
            }
        }
    }

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