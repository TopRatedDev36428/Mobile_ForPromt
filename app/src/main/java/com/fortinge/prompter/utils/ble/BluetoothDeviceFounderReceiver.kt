package com.fortinge.prompter.utils.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.aware.Characteristics

abstract class BluetoothDeviceFounderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val action = intent.action

            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null)
                    getFoundDevices(device)
            }
        }

    }

    abstract fun getFoundDevices(device: BluetoothDevice)
}