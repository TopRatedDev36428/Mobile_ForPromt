package com.fortinge.prompter.utils.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import com.fortinge.prompter.utils.EventbusDataEvents
import org.greenrobot.eventbus.EventBus
import java.util.*


class BluetoothHelper(private val context: Context, private val listener: BluetoothHelperListener){

    private val mBluetoothAdapter by lazy {

        bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager?.getAdapter()

        bluetoothManager?.adapter?.let {
            return@lazy it
        } ?: run {
            throw RuntimeException(
                "Bluetooth is not supported on this hardware platform. " +
                        "Make sure you try it from the real device\n " +
                        "You could more information from here:\n" +
                        "https://developer.android.com/reference/android/bluetooth/BluetoothAdapter"
            )
        }
    }

    private var isRequiredPermission = false

    private var isEnabled = mBluetoothAdapter.isEnabled

    private var isDiscovering = mBluetoothAdapter.isDiscovering

    private var bluetoothGatt: BluetoothGatt? = null

    private var bluetoothManager: BluetoothManager? = null

    private var isConnected = false

    private var isRegisteredBluetoothStateChanged = false


    private val mBluetoothStateChangeReceiver by lazy {
        object : BluetoothStateChangeReceiver() {
            override fun onStartDiscovering() {
                isDiscovering = true
                listener.onStartDiscovery()
            }

            override fun onFinishDiscovering() {
                isDiscovering = false
                listener.onFinishDiscovery()
            }

            override fun onEnabledBluetooth() {
                isEnabled = true
                listener.onEnabledBluetooth()
            }

            override fun onDisabledBluetooth() {
                isEnabled = false
                listener.onDisabledBluetooth()
            }


        }
    }

    private val mBluetoothDeviceFounderReceiver by lazy {
        object : BluetoothDeviceFounderReceiver() {
            override fun getFoundDevices(device: BluetoothDevice) {
                listener.getBluetoothDeviceList(device)
            }
        }
    }

    private val mBluetoothDeviceConnectionStateChangeReceiver by lazy {
        object : BluetoothDeviceConnectionStateChangeReceiver() {

            override fun onConnectedDevice(device: BluetoothDevice) {
                listener.onConnectedDevice(device)
            }

            override fun onDisconnectedDevice() {
                listener.onDisconnectedDevice()
            }

        }
    }

    fun isBluetoothEnabled() = isEnabled

    fun isBluetoothScanning() = isDiscovering

    fun bleIsConnected() = isConnected

    fun isRegisteredBluetoothStateChanged() = isRegisteredBluetoothStateChanged

    fun enableBluetooth() {
        if (!isEnabled) mBluetoothAdapter.enable()
    }

    fun disableBluetooth() {
        if (isEnabled) mBluetoothAdapter.disable()
    }

    fun registerBluetoothStateChanged() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(mBluetoothStateChangeReceiver, intentFilter)
        isRegisteredBluetoothStateChanged = true
    }

    fun unregisterBluetoothStateChanged() {
        if (mBluetoothStateChangeReceiver.isOrderedBroadcast) {
            context.unregisterReceiver(mBluetoothStateChangeReceiver)
            isRegisteredBluetoothStateChanged = false
        }

    }

    fun startDiscovery() {
        if (isEnabled && !isDiscovering) {
            mBluetoothAdapter.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(mBluetoothDeviceFounderReceiver, discoverDevicesIntent)
            println("startdiscovery fonk geldi")

        }
    }

    fun stopDiscovery() {
        if (isEnabled && isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()

//            if (!mBluetoothAdapter.isDiscovering && mBluetoothDeviceFounderReceiver.isOrderedBroadcast) {
//                context.unregisterReceiver(mBluetoothDeviceFounderReceiver)
//            }
        }
    }




    fun checkBTPermissions() {
       if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S ) {
            var permissionCheck =
                context.checkSelfPermission(BluetoothHelperConstant.ACCESS_FINE_LOCATION)
            permissionCheck += context.checkSelfPermission(BluetoothHelperConstant.ACCESS_COARSE_LOCATION)

            if (permissionCheck != 0)
                (context as Activity).requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), BluetoothHelperConstant.REQ_CODE
                )

        }
        else  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           var permissionCheck =
               context.checkSelfPermission(BluetoothHelperConstant.BLUETOOTH_CONNECT)
           permissionCheck += context.checkSelfPermission(BluetoothHelperConstant.BLUETOOTH_SCAN)

           if (permissionCheck != 0)
               (context as Activity).requestPermissions(
                   arrayOf(
                       Manifest.permission.BLUETOOTH_CONNECT,
                       Manifest.permission.BLUETOOTH_SCAN
                   ), BluetoothHelperConstant.REQ_CODE
               )
       }
    }




    fun setPermissionRequired(isRequired: Boolean): BluetoothHelper {
        this.isRequiredPermission = isRequired
        return this
    }

    fun create(): BluetoothHelper {
        if (this.isRequiredPermission) checkBTPermissions()
        return BluetoothHelper(context, listener)
    }


    fun disconnectDevice() {
//        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        if (bluetoothManager != null) {
            println("bluetooth gatt: " + bluetoothManager)
            val connectedDevice = bluetoothManager?.getConnectedDevices(BluetoothProfile.GATT)
            println("connected device size ${connectedDevice?.size}")
            if (bluetoothManager!!.getConnectedDevices(BluetoothProfile.GATT).size > 0) {
                if (bluetoothGatt != null) {
                    bluetoothGatt!!.disconnect()
                    bluetoothGatt!!.close()
                }

            }
            if (mBluetoothDeviceConnectionStateChangeReceiver.isOrderedBroadcast)
                context.unregisterReceiver(mBluetoothDeviceConnectionStateChangeReceiver)
        }
    }

    fun connectDevice(
        serviceUUID: UUID,
        characteristicUUID: UUID,
        cccdUUID: UUID,
        willConnectDevice: BluetoothDevice,
        viewModel: ViewModel
    ) {
        stopDiscovery()

        val intentFilter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        context.registerReceiver(mBluetoothDeviceConnectionStateChangeReceiver, intentFilter)

            bluetoothGatt =
                willConnectDevice.connectGatt(context, false, object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt?,
                        status: Int,
                        newState: Int
                    ) {
                        super.onConnectionStateChange(gatt, status, newState)

                        println("connectionstate: $status , newstate: $newState")

                        if (newState == BluetoothProfile.STATE_CONNECTING) {
                            println("newstate: connectingggg")
                        }
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            println("newstate: connecteddd")
//                        setBleStatusText("Device connected.")
                            isConnected = true
                            gatt!!.requestMtu(512)
                        }

                        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            println("newstate: disconnected")
                            isConnected = false
                        }


                    }


                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        super.onServicesDiscovered(gatt, status)

                        println("servicediscovered: $gatt")
                        val services = gatt?.services
                        if (services != null) {
                            //println(gatt.readCharacteristic(services.get(0).characteristics.get(0)))
//                      gatt.readCharacteristic(services.get(0).getCharacteristic(UUID.fromString("00003670-0000-1000-8000-00805F9B34FB")))

                            for (service in services) {
                                println("services::: $service")
                                if (serviceUUID == service.uuid) {
                                    println("wanted service: $service")
                                    for (charac in service.characteristics) {
                                        println("characterss: ${charac}")
                                        if (charac.uuid == characteristicUUID) {
                                            gatt.readCharacteristic(charac)
                                            println("wanted gatt: $charac")
                                        }
                                    }
                                }

                            }

                        }


                    }

                    override fun onCharacteristicRead(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic?,
                        status: Int
                    ) {
                        super.onCharacteristicRead(gatt, characteristic, status)

                        println("characteristicread: ${characteristic?.value?.get(0)}")

                        gatt?.readRemoteRssi()

                        println(
                            " ıntvalue okundu::" + characteristic?.getIntValue(
                                BluetoothGattCharacteristic.FORMAT_SINT8,
                                0
                            )
                        )
//                  }
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            val bleCha =
                                gatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
                            if (gatt != null) {
                                gatt.setCharacteristicNotification(bleCha, true)
                                val descriptor = bleCha?.getDescriptor(cccdUUID)
                                if (descriptor != null) {
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                                }
                                gatt.writeDescriptor(descriptor)

                            }
                        }

                    }

                    override fun onDescriptorRead(
                        gatt: BluetoothGatt?,
                        descriptor: BluetoothGattDescriptor?,
                        status: Int
                    ) {
                        super.onDescriptorRead(gatt, descriptor, status)

                        println("descriptorread")
                    }

                    override fun onDescriptorWrite(
                        gatt: BluetoothGatt?,
                        descriptor: BluetoothGattDescriptor?,
                        status: Int
                    ) {
                        super.onDescriptorWrite(gatt, descriptor, status)
                        println("descrptor writeee: $descriptor ,,,, $status")

                        if (status == BluetoothGatt.STATE_CONNECTED) println("test edildi bağlantı var")
                    }

                    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                        super.onMtuChanged(gatt, mtu, status)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            gatt?.discoverServices()
                            println("mtu changed")
                        }

                    }


                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic?
                    ) {
                        super.onCharacteristicChanged(gatt, characteristic)

                        val value = characteristic?.value?.get(0)
                        println("valuecomiinggg: ${value}")
//                    setBleStatusText(value.toString())
                        if (value != null) {

                            EventBus.getDefault().postSticky(EventbusDataEvents.BleDataChanged(value))

                        }



                    }

                })

//        bluetoothGatt =
//            willConnectDevice.connectGatt(context, false, object : BluetoothGattCallback() {
//                override fun onConnectionStateChange(
//                    gatt: BluetoothGatt?,
//                    status: Int,
//                    newState: Int
//                ) {
//                    super.onConnectionStateChange(gatt, status, newState)
//
//                    println("connectionstate: $status , newstate: $newState")
//
//                    if (newState == BluetoothProfile.STATE_CONNECTING) {
//                        println("newstate: connectingggg")
//                    }
//                    if (newState == BluetoothProfile.STATE_CONNECTED) {
//                        println("newstate: connecteddd")
////                        setBleStatusText("Device connected.")
//                        gatt!!.requestMtu(512)
//                    }
//
//                    if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                        println("newstate: disconnected")
//                    }
//
//
//                }
//
//
//                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//                    super.onServicesDiscovered(gatt, status)
//
//                    println("servicediscovered: $gatt")
//                    val services = gatt?.services
//                    if (services != null) {
//                        //println(gatt.readCharacteristic(services.get(0).characteristics.get(0)))
////                      gatt.readCharacteristic(services.get(0).getCharacteristic(UUID.fromString("00003670-0000-1000-8000-00805F9B34FB")))
//
//                        for (service in services) {
//                            println("services::: $service")
//                            if (serviceUUID == service.uuid) {
//                                println("wanted service: $service")
//                                for (charac in service.characteristics) {
//                                    println("characterss: ${charac}")
//                                    if (charac.uuid == characteristicUUID) {
//                                        gatt.readCharacteristic(charac)
//
//                                    }
//                                }
//                            }
//
//                        }
//
//                    }
//
//
//                }
//
//                override fun onCharacteristicRead(
//                    gatt: BluetoothGatt?,
//                    characteristic: BluetoothGattCharacteristic?,
//                    status: Int
//                ) {
//                    super.onCharacteristicRead(gatt, characteristic, status)
//
//                    println("characteristicread: ${characteristic?.value?.get(0)}")
//
//                    gatt?.readRemoteRssi()
////                    binding.bluetoothStatusTxt.setText("connected")
//
//
////                  if (characteristic?.uuid == UUID.fromString("00003671-0000-1000-8000-00805F9B34FB")){
//                    println(
//                        " ıntvalue okundu::" + characteristic?.getIntValue(
//                            BluetoothGattCharacteristic.FORMAT_SINT8,
//                            0
//                        )
//                    )
////                  }
//                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        val bleCha =
//                            gatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
//
//                        gatt!!.setCharacteristicNotification(bleCha, true)
//                        if (gatt.setCharacteristicNotification(bleCha, true)) {
//                        }
//                        val descriptor = bleCha?.getDescriptor(cccdUUID)
//                        descriptor!!.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//                        gatt!!.writeDescriptor(descriptor)
//                    }
//
//                }
//
//                override fun onDescriptorRead(
//                    gatt: BluetoothGatt?,
//                    descriptor: BluetoothGattDescriptor?,
//                    status: Int
//                ) {
//                    super.onDescriptorRead(gatt, descriptor, status)
//
//                    println("descriptorread")
//                }
//
//                override fun onDescriptorWrite(
//                    gatt: BluetoothGatt?,
//                    descriptor: BluetoothGattDescriptor?,
//                    status: Int
//                ) {
//                    super.onDescriptorWrite(gatt, descriptor, status)
//                    println("descrptor writeee: $descriptor ,,,, $status")
//
//                    if (status == BluetoothGatt.STATE_CONNECTED) println("test edildi bağlantı var")
//                }
//
//                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
//                    super.onMtuChanged(gatt, mtu, status)
//                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        gatt?.discoverServices()
//                        println("mtu changed")
//                    }
//
//                }
//
//
//                override fun onCharacteristicChanged(
//                    gatt: BluetoothGatt?,
//                    characteristic: BluetoothGattCharacteristic?
//                ) {
//                    super.onCharacteristicChanged(gatt, characteristic)
//                    println("charchangedd: ${characteristic?.value?.get(0)}")
//                    val value = characteristic?.value?.get(0)
//
////                    setBleStatusText(value.toString())
//                    if (value != null) {
//
//                        EventBus.getDefault().postSticky(EventbusDataEvents.BleDataChanged(value))
//
//                    }
//
//
//
//                }
//
//            })

    }

}