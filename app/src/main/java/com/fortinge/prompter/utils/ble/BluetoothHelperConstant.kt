package com.fortinge.prompter.utils.ble

import java.util.*

class BluetoothHelperConstant {
    companion object {
        const val ACCESS_FINE_LOCATION = "Manifest.permission.ACCESS_FINE_LOCATION"
        const val ACCESS_COARSE_LOCATION = "Manifest.permission.ACCESS_COARSE_LOCATION"
        const val BLUETOOTH_SCAN = "Manifest.permission.BLUETOOTH_SCAN"
        const val BLUETOOTH_CONNECT = "Manifest.permission.BLUETOOTH_CONNECT"

        const val REQ_CODE = 1001

        val SERVICE_UUID = UUID.fromString("00003670-0000-1000-8000-00805F9B34FB")
        val BUTTON_UUID = UUID.fromString("00003671-0000-1000-8000-00805F9B34FB")
        val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }
}