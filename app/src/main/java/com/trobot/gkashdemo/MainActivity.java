package com.trobot.gkashdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.trobot.gkashdemo.adapter.DeviceListAdapter;
import com.trobot.gkashdemo.adapter.PairedDeviceListAdapter;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 1; // A constant need to use in startActivityForResult()
    public final static String PASSING_MAC_ADDRESS = "passing_mac_address";
    private BluetoothAdapter bluetoothAdapter;

    private Button bluetoothButton;
    private Button discoverButton;
    private Button searchDeviceButton;
    private ListView newDeviceListView;
    private ListView pairedDeviceListView;

    private Set<BluetoothDevice> pairedDevices;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> pairedDevicesArray = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    public PairedDeviceListAdapter mPairedDeviceListAdapter;

    private String deviceName;
    private String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothButton = findViewById(R.id.bluetoothButton);
        discoverButton = findViewById(R.id.discoverButton);
        searchDeviceButton = findViewById(R.id.searchDeviceButton);
        newDeviceListView = findViewById(R.id.newDeviceListView);
        pairedDeviceListView = findViewById(R.id.pairedDeviceListView);

        mBTDevices = new ArrayList<>();
        pairedDevicesArray = new ArrayList<>();

        // BluetoothAdapter is required for any Bluetooth activity
        // To get the BluetoothAdapter, call static method getDefaultAdapter().
        // This returns a BluetoothAdapter that represents the devices's own Bluetooth Adapter.
        // There is always one Bluetooth adapter for the entire system (your phone in this case).
        // You application can interact with the device's adapter through the object declared.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Query paired devices by using getBondedDevices() method and store paired devices
        // into a SET called pairedDevices.
        pairedDevices = bluetoothAdapter.getBondedDevices();

        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBT();
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverPairedBtDevice();
            }
        });

        searchDeviceButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                discoverNewDevice();
            }
        });

        // Run this broadcastReceiver here is because have to constantly check if any bond state changed
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver2, filter);

        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                deviceName = pairedDevicesArray.get(position).getName();
                deviceAddress = pairedDevicesArray.get(position).getAddress();

                Toast.makeText(MainActivity.this, "onItemClick: "+ deviceName + " with MAC:" + deviceAddress, Toast.LENGTH_SHORT).show();

                // Navigate to new Screen by passing deviceAddress for connection purpose later.
                Intent newScreenIntent = new Intent(MainActivity.this, HomeActivity.class);
                newScreenIntent.putExtra(PASSING_MAC_ADDRESS, deviceAddress);
                startActivity(newScreenIntent);
            }
        });

        newDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                deviceName = mBTDevices.get(position).getName();
                deviceAddress = mBTDevices.get(position).getAddress();

                Toast.makeText(MainActivity.this, "onItemClick: "+ deviceName + " with MAC:" + deviceAddress, Toast.LENGTH_SHORT).show();

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                    Toast.makeText(MainActivity.this, "Trying to pair with "+deviceName, Toast.LENGTH_SHORT).show();
                    mBTDevices.get(position).createBond();
                }
            }
        });


    }

    public void enableDisableBT(){
        // If bluetoothAdapter returns null, means no Bluetooth in your device.
        if (bluetoothAdapter == null){
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }
        // You need to ensure that Bluetooth is enabled or not. Call isEnabled() to check whether
        // Bluetooth is currently enabled. If this method returns false, then Bluetooth is diabled.
        if (!bluetoothAdapter.isEnabled()){
            // To request that Bluetooth be enabled, call startActivityForResult(), passing in an
            // ACTION_REQUEST_ENABLE intent action. This call, issues a request to enable Bluetooth
            // through the system settings (Without stopping your application).
            // A dialog appears requesting user permission to enable Bluetooth. If the user responds
            // 'YES', the system begins to enable Bluetooth, and focus returns to your application
            // once the process completes.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a locally
            // defined integer that must be greater than 0. The system passes this constant back to
            // you in your onActivityResult as the requestCode parameter.
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
        }
    }

    public void discoverPairedBtDevice(){
        // getBondedDevices is literally means get paired devices.
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (!pairedDevicesArray.contains(device))
                    pairedDevicesArray.add(device);
            }
            // Display it as list on the UI.
            mPairedDeviceListAdapter = new PairedDeviceListAdapter(getApplicationContext(), R.layout.paired_device_adapter_view, pairedDevicesArray);
            pairedDeviceListView.setAdapter(mPairedDeviceListAdapter);
        } else {
            Toast.makeText(this, "No paired device found", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void discoverNewDevice(){
        // To start discovering new devices, simply call startDiscovery().
        // The discovery process usually involves an inquiry scan of about 12 seconds,
        // followed by a page scan of each device found to retrieve its Bluetooth name.
        //
        // In order to receive information about each device discovered, your application must
        // register a BroadcastReceiver for the ACTION_FOUND intent.
        // The system will broadcasts this intent for each device. [Continue at BroadcastReceiver...]
        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();

            // If your API is > Lollipop, you have to check BT permission
            checkBTPermissions();

            Toast.makeText(this, "Restarting Discovery...", Toast.LENGTH_SHORT).show();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        }

        if (!bluetoothAdapter.isDiscovering()){
            // If your API is > Lollipop, you have to check BT permission
            checkBTPermissions();

            Toast.makeText(this, "Starting Discovery...", Toast.LENGTH_SHORT).show();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkBTPermissions(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }else{
                Log.d("MainActivity Tag", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If enabling Bluetooth succeeds, your activity receives the RESULT_OK result code
        // in the onActivityResult() callback. If Bluetooth was not enabled due to an error (or responded 'No')
        // then the result code is RESULT_CANCELED
        if (requestCode == 1 && resultCode == RESULT_OK){
            Toast.makeText(this, "Bluetooth enabling succeeds", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 1 && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Bluetooth was not enabled due to an error", Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        // The intent contains the extra fields EXTRA_DEVICE and EXTRA_CLASS,
        // which in turn contain a BluetoothDevice and a BluetoothClass, respectively.
        // Basically this onReceive will keep looping when there is new device. (Something like tat)
        // Because of the EXTRA_DEVICE.
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("MainActivity", "onReceive: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mBTDevices.contains(device)){
                    mBTDevices.add(device);
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    newDeviceListView.setAdapter(mDeviceListAdapter);
                }
            } else {
                Toast.makeText(context, "No device found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(context, "BroadcastReceiver: BOND_BONDED", Toast.LENGTH_SHORT).show();
                    discoverPairedBtDevice();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Toast.makeText(context, "BroadcastReceiver: BOND_BONDING", Toast.LENGTH_SHORT).show();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Toast.makeText(context, "BroadcastReceiver: BOND_NONE", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBroadcastReceiver2);
    }


}
