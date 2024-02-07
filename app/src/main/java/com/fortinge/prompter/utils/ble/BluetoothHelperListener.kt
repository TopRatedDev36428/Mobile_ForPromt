package com.fortinge.prompter.utils.ble


import android.bluetooth.BluetoothDevice

interface BluetoothHelperListener {

    fun onStartDiscovery()

    fun onFinishDiscovery()

    fun onEnabledBluetooth()

    fun onDisabledBluetooth()

    fun getBluetoothDeviceList(device: BluetoothDevice)

    fun onConnectedDevice(device: BluetoothDevice)

    fun onDisconnectedDevice()
}