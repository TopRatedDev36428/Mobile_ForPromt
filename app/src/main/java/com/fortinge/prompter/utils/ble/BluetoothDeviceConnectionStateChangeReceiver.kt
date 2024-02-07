package com.fortinge.prompter.utils.ble

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fortinge.prompter.utils.EventbusDataEvents

abstract class BluetoothDeviceConnectionStateChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent?.action

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null)
                onConnectedDevice(device)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> onDisconnectedDevice()

        }

    }


    abstract fun onConnectedDevice(device: BluetoothDevice)

    abstract fun onDisconnectedDevice()
}