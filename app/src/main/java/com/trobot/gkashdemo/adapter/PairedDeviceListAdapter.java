package com.trobot.gkashdemo.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.trobot.gkashdemo.R;

import java.util.ArrayList;

public class PairedDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;

    public PairedDeviceListAdapter(@NonNull Context context, int textViewResourceId, @NonNull ArrayList<BluetoothDevice> pairedDevices) {
        super(context, textViewResourceId, pairedDevices);
        this.mDevices = pairedDevices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mViewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        BluetoothDevice device = mDevices.get(position);

        if (device != null){
            TextView textViewDeviceName = convertView.findViewById(R.id.textViewPairedDeviceName);
            TextView textViewDeviceAddress = convertView.findViewById(R.id.textViewPairedDeviceAddress);

            if (textViewDeviceName != null){
                textViewDeviceName.setText(device.getName());
            }
            if (textViewDeviceAddress != null){
                textViewDeviceAddress.setText(device.getAddress());
            }
        }

        return convertView;
    }
}
