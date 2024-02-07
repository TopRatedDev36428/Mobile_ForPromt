package com.fortinge.prompter.ui

import PrefRepository
import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fortinge.forprompt.R
import com.fortinge.forprompt.databinding.FragmentSettingsBinding
import com.fortinge.prompter.adapter.BluetoothListAdapter
import com.fortinge.prompter.utils.EventbusDataEvents
import com.fortinge.prompter.utils.ble.BluetoothHelper
import com.fortinge.prompter.utils.ble.BluetoothHelperConstant
import com.fortinge.prompter.utils.ble.BluetoothHelperListener
import com.fortinge.prompter.utils.isColorDark
import com.fortinge.prompter.viewmodel.SharedViewModel
import com.google.android.material.button.MaterialButton
import org.greenrobot.eventbus.Subscribe
import java.util.*


class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener, BluetoothHelperListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    val tcButtonsArray = ArrayList<MaterialButton>()
    val bcButtonsArray = ArrayList<Button>()
    val colorsArray = ArrayList<Int>()
    var tcIndex = 0
    var bcIndex = 1
    var gpsStatus: Boolean = false
    var isTablet = false
    var a = 0

    var createdBluetoothHelper = false

    private lateinit var sharedPref: PrefRepository


    lateinit var bluetoothhelper: BluetoothHelper
    var deviceArray = arrayListOf<BluetoothDevice>()
    lateinit var willConnectDevice: BluetoothDevice
    var adapter: BluetoothListAdapter? = null

    private lateinit var sharedViewModel: SharedViewModel
    private var permissionsToRequest: ArrayList<String>? = null
    private val permissionsRejected = ArrayList<String>()
    private val permissions = ArrayList<String>()

    // integer for permissions results request
    private val ALL_PERMISSIONS_RESULT = 1101
    private lateinit var alertDialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedPref = PrefRepository(requireContext())


        binding.speedRateSpinner.onItemSelectedListener = this

        setVariablesForStart()


        binding.bleRotatingImage.setOnClickListener {
            println("1.....")
            if (this::bluetoothhelper.isInitialized) {
                if (bluetoothhelper.isBluetoothScanning()) {
                    if (deviceArray.size > 0) {
                        deviceArray.clear()
                        adapter?.notifyDataSetChanged()
                        if (bluetoothhelper.isBluetoothScanning()) {
                            bluetoothhelper.stopDiscovery()
                        }
                    }
                    if (binding.bleConnectionSwitch.isChecked) {
                        bluetoothhelper.disconnectDevice()
                    }
                }

                bleHelper()
            }

        }



        binding.bleScanSwitch.setOnClickListener {
            Log.e("Log","switch condition "+binding.bleScanSwitch.isChecked);
            if (binding.bleScanSwitch.isChecked) {
                if (checkPermission(requireContext())) {
                    Log.e("Log","if condition"+checkPermission(requireContext()))
                    println("2.2......")
                    islocationEnabled()
                    if (!gpsStatus) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.openlocationservice),
                            Toast.LENGTH_LONG
                        ).show()
                        binding.bleScanSwitch.isChecked = false
                        return@setOnClickListener
                    }

                    bleHelper()
                } else {
                    Log.e("Log","else condition")
//                    requestPermission()
                    showDialogWithOkButton()
                    binding.bleScanSwitch.isChecked = false
                    return@setOnClickListener
                }


            } else {
                if (this::bluetoothhelper.isInitialized) {
                    if (deviceArray.size > 0) {
                        deviceArray.clear()
                        adapter?.notifyDataSetChanged()
                        println(deviceArray)

                    }

                    if (bluetoothhelper.isBluetoothScanning()) {
                        bluetoothhelper.stopDiscovery()
                    }
                    if (binding.bleConnectionSwitch.isChecked) {
                        if (bluetoothhelper.bleIsConnected()) {
                            bluetoothhelper.disconnectDevice()
                        }
                        binding.bleConnectionSwitch.isChecked = false
                        binding.bleRotatingImage.isEnabled = false
                    }
                }


            }

        }

        binding.bleConnectionSwitch.setOnClickListener {
            if (!binding.bleConnectionSwitch.isChecked) {
//                println("bluetooth gatt: " + bluetoothGatt)
//                println("connected device size ${bluetoothGatt?.connectedDevices?.size}")

                bluetoothhelper.disconnectDevice()
                sharedViewModel.setBleConnectionState(false)

            } else {
                val device = binding.bleDevicesSpinner.selectedItem as? BluetoothDevice
                if (device != null) {
                    willConnectDevice = device
                    bluetoothhelper.connectDevice(
                        BluetoothHelperConstant.SERVICE_UUID,
                        BluetoothHelperConstant.BUTTON_UUID,
                        BluetoothHelperConstant.CCCD_UUID,
                        willConnectDevice,
                        sharedViewModel
                    )
                }


//                sharedViewModel.setBleConnectedDevice(willConnectDevice)

            }
        }


        val spinnerList = R.array.speed_rate_spinner
        val speedRateSpinnerArrayAdapter =
            ArrayAdapter.createFromResource(requireContext(), spinnerList, R.layout.spinner_item)
        speedRateSpinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.speedRateSpinner.adapter = speedRateSpinnerArrayAdapter


        tcButtonsArray.add(binding.tc1Btn)
        tcButtonsArray.add(binding.tc2Btn)
        tcButtonsArray.add(binding.tc3Btn)
        tcButtonsArray.add(binding.tc4Btn)
        tcButtonsArray.add(binding.tc5Btn)
        tcButtonsArray.add(binding.tc6Btn)
        tcButtonsArray.add(binding.tc7Btn)
        tcButtonsArray.add(binding.tc8Btn)
        tcButtonsArray.add(binding.tc9Btn)
        tcButtonsArray.add(binding.tc10Btn)
        tcButtonsArray.add(binding.tc11Btn)
        tcButtonsArray.add(binding.tc12Btn)
        tcButtonsArray.add(binding.tc13Btn)
        tcButtonsArray.add(binding.tc14Btn)
        tcButtonsArray.add(binding.tc15Btn)
        tcButtonsArray.add(binding.tc16Btn)
        tcButtonsArray.add(binding.tc17Btn)
        tcButtonsArray.add(binding.tc18Btn)


        bcButtonsArray.add(binding.bc1Btn)
        bcButtonsArray.add(binding.bc2Btn)
        bcButtonsArray.add(binding.bc3Btn)
        bcButtonsArray.add(binding.bc4Btn)
        bcButtonsArray.add(binding.bc5Btn)
        bcButtonsArray.add(binding.bc6Btn)
        bcButtonsArray.add(binding.bc7Btn)
        bcButtonsArray.add(binding.bc8Btn)
        bcButtonsArray.add(binding.bc9Btn)
        bcButtonsArray.add(binding.bc10Btn)
        bcButtonsArray.add(binding.bc11Btn)
        bcButtonsArray.add(binding.bc12Btn)
        bcButtonsArray.add(binding.bc13Btn)
        bcButtonsArray.add(binding.bc14Btn)
        bcButtonsArray.add(binding.bc15Btn)
        bcButtonsArray.add(binding.bc16Btn)
        bcButtonsArray.add(binding.bc17Btn)
        bcButtonsArray.add(binding.bc18Btn)


        colorsArray.add(R.color.white)
        colorsArray.add(R.color.black)
        colorsArray.add(R.color.settings_color_3)
        colorsArray.add(R.color.settings_color_4)
        colorsArray.add(R.color.settings_color_5)
        colorsArray.add(R.color.settings_color_6)
        colorsArray.add(R.color.settings_color_7)
        colorsArray.add(R.color.settings_color_8)
        colorsArray.add(R.color.settings_color_9)
        colorsArray.add(R.color.settings_color_10)
        colorsArray.add(R.color.settings_color_11)
        colorsArray.add(R.color.settings_color_12)
        colorsArray.add(R.color.settings_color_13)
        colorsArray.add(R.color.settings_color_14)
        colorsArray.add(R.color.settings_color_15)
        colorsArray.add(R.color.settings_color_16)
        colorsArray.add(R.color.settings_color_17)
        colorsArray.add(R.color.settings_color_18)


        ////////////////COUNTDOWN
        binding.constraintLayout.isGone = true
        binding.countdownSwitch.setOnClickListener {
            if (binding.countdownSwitch.isChecked) {
                binding.constraintLayout.isGone = false
                var countdownValue = binding.countdownNumberPicker.text.toString().toIntOrNull()
                if (countdownValue != null) {
                    if (countdownValue == 0) {
                        countdownValue = 1
                        binding.countdownNumberPicker.setText(countdownValue.toString())

                    }
                    sharedViewModel.setCountdownValue(countdownValue)
                    sharedViewModel.setCountdownEnable(true)

                    sharedPref.setCountdownValue(countdownValue)
                    sharedPref.setCountdownEnable(true)

                } else {

                    binding.countdownNumberPicker.error = getString(R.string.entervalue)
                    binding.countdownSwitch.isChecked = false
                }

            } else {
                binding.constraintLayout.isGone = true
                sharedViewModel.setCountdownEnable(false)
                sharedPref.setCountdownEnable(false)
            }

            println("countdown value: " + sharedViewModel.countdownValue.value)
        }

        binding.countdownMinusBtn.setOnClickListener {
            var number = binding.countdownNumberPicker.text.toString().toIntOrNull()
            if (number != null) {
                number--
                if (number < 0) number = 0
                binding.countdownNumberPicker.setText(number.toString())
                println(number)

            } else {
                number = 1
            }

            sharedViewModel.setCountdownValue(number)
            sharedPref.setCountdownValue(number)

        }

        binding.countdownPlusBtn.setOnClickListener {
            var number = binding.countdownNumberPicker.text.toString().toIntOrNull()
            if (number != null) {
                number = number?.plus(1)
                binding.countdownNumberPicker.setText(number.toString())
                println(number)

            } else {
                number = 1
            }

            sharedViewModel.setCountdownValue(number!!)
            sharedPref.setCountdownValue(number!!)
        }

        ////////////////LOOP
        binding.loopSwitch.setOnClickListener {
            if (binding.loopSwitch.isChecked) {
                sharedViewModel.setPromptingLoop(true)
                sharedPref.setPromptLoop(true)
            } else {
                sharedViewModel.setPromptingLoop(false)
                sharedPref.setPromptLoop(false)
            }
        }

//////////////showSpeedRate
        binding.speedRateSpinner.isGone = true
        binding.speedRateSpinner2.isGone = true
        binding.showSpeedRate.setOnClickListener {
            if (binding.showSpeedRate.isChecked) {
                binding.speedRateSpinner.isGone = false
                binding.speedRateSpinner2.isGone = false
            } else {
                binding.speedRateSpinner.isGone = true
                binding.speedRateSpinner2.isGone = true
            }

        }


        ////////////////SHOW SCROLL
        binding.showScrollSwitch.setOnClickListener {
            if (binding.showScrollSwitch.isChecked) {
                sharedViewModel.setshowScroll(true)
                sharedPref.setShowScroll(true)
            } else {
                sharedViewModel.setshowScroll(false)
                sharedPref.setShowScroll(false)
            }
        }
///// cue


        binding.markerSwitch.setOnClickListener {
            println("burdasın")
            if (binding.markerSwitch.isChecked) {
                println("asagisi")
                binding.editCueMarkerPosition.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                        if (p1 == 0) {
                            sharedViewModel.setcueMarkerSeekBarPosition(0)
                        } else if (p1 == 1) {
                            sharedViewModel.setcueMarkerSeekBarPosition(1)
                        } else if (p1 == 2) {
                            sharedViewModel.setcueMarkerSeekBarPosition(2)
                        } else if (p1 == 3) {
                            sharedViewModel.setcueMarkerSeekBarPosition(3)
                        } else if (p1 == 4) {
                            sharedViewModel.setcueMarkerSeekBarPosition(4)
                        } else if (p1 == 5) {
                            sharedViewModel.setcueMarkerSeekBarPosition(5)
                        } else if (p1 == 6) {
                            sharedViewModel.setcueMarkerSeekBarPosition(6)
                        } else if (p1 == 7) {
                            sharedViewModel.setcueMarkerSeekBarPosition(7)
                        } else if (p1 == 8) {
                            sharedViewModel.setcueMarkerSeekBarPosition(8)
                        } else if (p1 == 9) {
                            sharedViewModel.setcueMarkerSeekBarPosition(9)
                        } else if (p1 == 10) {
                            sharedViewModel.setcueMarkerSeekBarPosition(10)
                        }

                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                sharedViewModel.setshowCue(true)
                binding.editCueMarkerPosition.isGone = false

                //sharedPref.setShowCue(false)
            } else {
                sharedViewModel.setshowCue(false)
                binding.editCueMarkerPosition.isGone = true
            }
        }


        ////////////////MARGINS
        binding.constraintLayout2.isGone = true
        binding.marginsSwitch.setOnClickListener {
            if (binding.marginsSwitch.isChecked) {
                binding.constraintLayout2.isGone = false
                var number = binding.marginsNumberPicker.text.toString().toIntOrNull()
                if (number != null) {
                    if (number!! > 50 || number!! < 0) {
                        number = 50
                    }
                    if (number == 0) {
                        number = 1
                    }
                    binding.marginsNumberPicker.setText(number!!.toString())

                    sharedViewModel.setmarginsValue(
                        binding.marginsNumberPicker.text.toString().toInt()
                    )
                    sharedViewModel.setmarginsEnable(true)
                    sharedPref.setMarginsEnable(true)
                } else {
                    binding.marginsNumberPicker.error = getString(R.string.entervalue)
                    binding.marginsSwitch.isChecked = false
                }
            } else {
                binding.constraintLayout2.isGone = true
                sharedViewModel.setmarginsEnable(false)
                sharedPref.setMarginsEnable(false)
            }

        }

        binding.marginsPlusBtn.setOnClickListener {
            var number = binding.marginsNumberPicker.text.toString().toIntOrNull()
            if (number != null) {
                number = number?.plus(1)
                if (number!! > 50) number = 50
                binding.marginsNumberPicker.setText(number.toString())
            } else {
                number = 1
            }

            sharedViewModel.setmarginsValue(number!!)
            sharedPref.setMarginsValue(number!!)

        }

        binding.marginsMinusBtn.setOnClickListener {
            var number = binding.marginsNumberPicker.text.toString().toIntOrNull()
            if (number != null) {
                number = number?.minus(1)
                if (number!! < 0) number = 0
                binding.marginsNumberPicker.setText(number.toString())
            } else {
                number = 1
            }

            sharedViewModel.setmarginsValue(number!!)

        }

        ////////////////RTL
        binding.rtlSwitch.setOnClickListener {
            if (binding.rtlSwitch.isChecked) {
                sharedViewModel.setRTL(true)
                sharedPref.setRTL(true)
            } else {
                sharedViewModel.setRTL(false)
                sharedPref.setRTL(false)
            }


//            val fragment = PromptFragment()
//            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.nav_host_fragment, fragment)?.commit()
        }


        ///////////////Text Color
        for (color in tcButtonsArray) {
            color.setOnClickListener {
                tcIndex = tcButtonsArray.indexOf(it)
                val selectedColor = ContextCompat.getColor(requireContext(), colorsArray[tcIndex])
                binding.colorSampleText.setTextColor(selectedColor)
                sharedViewModel.setTextColor(selectedColor)
                sharedPref.setTextColor(selectedColor)


                if (bcIndex == tcIndex) {
                    if (ContextCompat.getColor(requireContext(), colorsArray[bcIndex])
                            .isColorDark()
                    ) {
                        binding.tc1Btn.callOnClick()
                    } else {
                        binding.tc2Btn.callOnClick()
                    }
                }
            }
        }


        ///////////////Background Color
        bcButtonsArray.forEach {
            it.setOnClickListener {
                bcIndex = bcButtonsArray.indexOf(it)
                val selectedColor = ContextCompat.getColor(requireContext(), colorsArray[bcIndex])
                binding.colorSampleText.setBackgroundColor(selectedColor)
                sharedViewModel.setBackgroundColor(selectedColor)
                sharedPref.setBackgroundColor(selectedColor)


                if (bcIndex == tcIndex) {
                    if (ContextCompat.getColor(requireContext(), colorsArray[bcIndex])
                            .isColorDark()
                    ) {
                        binding.bc1Btn.callOnClick()
                    } else {
                        binding.bc2Btn.callOnClick()
                    }
                }

            }
        }

        binding.systemInfoLblBtn.setOnClickListener {
            val manufacturer = Build.MANUFACTURER.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            val controller = if (isTablet) "Fortinge BT1" else "Fortinge Mia Controller"

            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(getString(R.string.systeminfo))
            alert.setMessage(
                (getString(R.string.devicemodel) + ": " + manufacturer + " / " + Build.MODEL + "\n" + getString(
                    R.string.androidversion
                ) + ": " + Build.VERSION.RELEASE +
                        "\n" + getString(R.string.supportedremotecontroller) + ": " + controller)

            )
            alert.setPositiveButton(
                getString(R.string.close),
                { dialogInterface: DialogInterface, i: Int ->

                })
            alert.show()


        }

        return view
    }

    private fun showDialogWithOkButton() {
//       requestPermission()
        val mDialog = Dialog(requireContext(), R.style.CustomDialogTheme)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setCanceledOnTouchOutside(false)
        mDialog.setCancelable(false)
        mDialog.setContentView(R.layout.dialog_ok_button)
        val btnYes = mDialog.findViewById<View>(R.id.btnOk) as TextView
        val btnCancel = mDialog.findViewById<View>(R.id.btnCancel) as TextView
        btnYes.setOnClickListener {
            mDialog.dismiss()
            requestPermission()
        }
        btnCancel.setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.show()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsToRequest = permissionsToRequest(permissions)
        if (permissionsToRequest!!.size > 0) {
            requestPermissions(
                permissionsToRequest!!.toTypedArray<String>(),
                ALL_PERMISSIONS_RESULT
            )
        }
    }

    fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    permissionsToRequest!!.toTypedArray<String>(),
                    ALL_PERMISSIONS_RESULT
                )
            }

        } else {
            Toast.makeText(requireContext(), "Permission already granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            STORAGE_PERMISSION_REQUEST -> if (grantResults.size > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED
//            ) {
//                // permission was granted :)
//                downloadFile()
//            } else {
//                // permission was not granted
//                if (activity == null) {
//                    return
//                }
//                if (ActivityCompat.shouldShowRequestPermissionRationale(
//                        requireActivity(),
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    )
//                ) {
//                    showStoragePermissionRationale()
//                } else {
//                    val snackbar = Snackbar.make(
//                        view!!,
//                        resources.getString(R.string.message_no_storage_permission_snackbar),
//                        Snackbar.LENGTH_LONG
//                    )
//                    snackbar.setAction(
//                        resources.getString(R.string.settings),
//                        View.OnClickListener {
//                            if (activity == null) {
//                                return@OnClickListener
//                            }
//                            val intent = Intent()
//                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                            val uri = Uri.fromParts("package", activity!!.packageName, null)
//                            intent.data = uri
//                            this@OrderDetailFragment.startActivity(intent)
//                        })
//                    snackbar.show()
//                }
//            }
//        }
//    }

    private fun permissionsToRequest(wantedPermissions: java.util.ArrayList<String>): java.util.ArrayList<String> {
        val result = java.util.ArrayList<String>()
        for (perm in wantedPermissions) {
            if (hasPermission(perm)) {
                result.add(perm)
            }
        }
        return result
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            )  == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            )  == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun setVariablesForStart() {
//        binding.bleStatusTxt.setText("Bluetooth is ready.")
        binding.bleConnectionSwitch.isEnabled = false
        binding.bleRotatingImage.isEnabled = false
//Tablet mi telefon  u sorguasu yapıyor
        isTablet = isTablet(requireContext())
        println("bu cihaz bir tablet mi:======= $isTablet")

//        val kosul1 = true
//        val kosul2 = false
//        val kosul3 = true
//
//        if ((kosul1&&kosul3) && (kosul2 && kosul3) || (kosul1 && kosul3)) {
//            print("koşullar sağlandı gibi")
//        } else {
//            println("olmadı")
//        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSharedPref()

        sharedViewModel.speedRate.observe(viewLifecycleOwner, Observer {
            when (it) {
                0.25 -> binding.speedRateSpinner.setSelection(0)
                0.5 -> binding.speedRateSpinner.setSelection(1)
                1.0 -> binding.speedRateSpinner.setSelection(2)
                2.0 -> binding.speedRateSpinner.setSelection(3)
                4.0 -> binding.speedRateSpinner.setSelection(4)
            }
        })

        sharedViewModel.countdownEnable.observe(viewLifecycleOwner, Observer {
            binding.countdownSwitch.isChecked = it
        })

        sharedViewModel.countdownValue.observe(viewLifecycleOwner, Observer {
            binding.countdownNumberPicker.setText(it.toString())
        })

        sharedViewModel.promptingLoop.observe(viewLifecycleOwner, Observer {
            binding.loopSwitch.isChecked = it
        })

        sharedViewModel.showScroll.observe(viewLifecycleOwner, Observer {
            binding.showScrollSwitch.isChecked = it
        })

        sharedViewModel.showCue.observe(viewLifecycleOwner, Observer {
            binding.markerSwitch.isChecked = it
        })





        sharedViewModel.cueMarkerSeekBarPosition.observe(viewLifecycleOwner, Observer {
            //binding.markerSwitch.isChecked = it
        })


//cueMarkerPosition


        sharedViewModel.marginsEnable.observe(viewLifecycleOwner, Observer {
            binding.marginsSwitch.isChecked = it
        })

        sharedViewModel.marginsValue.observe(viewLifecycleOwner, Observer {
            binding.marginsNumberPicker.setText(it.toString())
        })

        sharedViewModel.RTL.observe(viewLifecycleOwner, Observer {
            binding.rtlSwitch.isChecked = it
        })


        sharedViewModel.backgroundColor.observe(viewLifecycleOwner, Observer {
            it.let {
                binding.colorSampleText.setBackgroundColor(it)
            }
        })

        sharedViewModel.textColor.observe(viewLifecycleOwner, Observer {
            it.let {
                binding.colorSampleText.setTextColor(it)
            }
        })



        sharedViewModel.bleConnectionState.observe(viewLifecycleOwner, {
//            if (it) {
//                binding.bleConnectionSwitch.isChecked = true
//                binding.bleScanSwitch.isChecked = true
//                binding.bleConnectionSwitch.isEnabled = true
//                bleConnected = true
//
//                if(!this::bluetoothhelper.isInitialized){
//                    bluetoothhelper =
//                        BluetoothHelper(requireContext(), object : BluetoothHelperListener {
//                            override fun onStartDiscovery() {
//                                setBleStatusText("Devices scanning..")
//                            }
//
//                            override fun onFinishDiscovery() {
//                                if (deviceArray.isEmpty()) {
//                                    setBleStatusText("Device not found.")
//                                } else if (deviceArray.size == 1) {
//                                    setBleStatusText("Device listed.")
//                                } else {
//                                    setBleStatusText("Devices listed.")
//                                }
//
//                            }
//
//                            override fun onEnabledBluetooth() {
//                                setBleStatusText("Bluetooth is active.")
//                                bleScan()
//                            }
//
//                            override fun onDisabledBluetooth() {
//                                setBleStatusText("Bluetooth is deactive.")
//                            }
//
//                            override fun getBluetoothDeviceList(device: BluetoothDevice) { //kontrol
////                        val adapter = BluetoothListAdapter(requireContext(), deviceArray)
////                        binding.bleDevicesSpinner.adapter = adapter
//                                if (device.name != null && device.name.length > 0) {
//                                    println("cihaz null değil ismi de ${device.name}")
//
//                                    if (((!isTablet && device.name.startsWith("Fortinge", true))) ||
//                                            (isTablet && device.name.startsWith("Fortinge BT1", true))) {
////
//                                        deviceArray.add(device)
//                                        println(deviceArray.size)
//
////                                    sharedViewModel.setBleConnectedDevice(deviceArray)
//                                        adapter =
//                                            BluetoothListAdapter(requireContext(), deviceArray)
//                                        binding.bleDevicesSpinner.adapter = adapter
//                                        adapter!!.notifyDataSetChanged()
//                                        binding.bleConnectionSwitch.isEnabled = true
//                                    }
//                                }
//                            }
//
//                            override fun onConnectedDevice(device: BluetoothDevice) {
//                                sharedViewModel.setBleConnectionState(true)
//
//                            }
//
//                            override fun onDisconnectedDevice() {
//                                sharedViewModel.setBleConnectionState(false)
//                            }
//
//                        })
//                            .setPermissionRequired(false)
//                            .create()
//
//
//                    if (!bluetoothhelper.isRegisteredBluetoothStateChanged())
//                        bluetoothhelper.registerBluetoothStateChanged()
//
//                }
//
//            } else {
//            binding.bleConnectionSwitch.isChecked = false
//            binding.bleScanSwitch.isChecked = false
//            bleConnected = false
//            }
        })

        sharedViewModel.bleConnectedDevice.observe(viewLifecycleOwner, {
//            val adapter = BluetoothListAdapter(requireContext(), it)
//            binding.bleDevicesSpinner.adapter = adapter

            // binding.bleStatusTxt.setText(it[0].name + " connected.")
        })

        sharedViewModel.bleStatusTxt.observe(viewLifecycleOwner, {
            binding.bleStatusTxt.text = it
        })

    }

    private fun getSharedPref() {
        when (sharedPref.getSpeedRate()) {
            0.25 -> binding.speedRateSpinner.setSelection(0)
            0.5 -> binding.speedRateSpinner.setSelection(1)
            1.0 -> binding.speedRateSpinner.setSelection(2)
            2.0 -> binding.speedRateSpinner.setSelection(3)
            4.0 -> binding.speedRateSpinner.setSelection(4)
        }

        binding.countdownSwitch.isChecked = sharedPref.getCountdownEnable()
        binding.countdownNumberPicker.setText(sharedPref.getCountdownValue().toString())

        binding.loopSwitch.isChecked = sharedPref.getPromptLoop()
        binding.showScrollSwitch.isChecked = sharedPref.getShowScroll()

        // binding.markerSwitch.isChecked =     sharedPref.getShowCue()
        binding.marginsSwitch.isChecked = sharedPref.getMarginsEnable()


        binding.marginsNumberPicker.setText(sharedPref.getMarginsValue().toString())

        binding.rtlSwitch.isChecked = sharedPref.getRTL()
//        binding.colorSampleText.setTextColor(sharedPref.getTextColor())
        binding.colorSampleText.setBackgroundColor(sharedPref.getBackgroundColor())


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if (this::bluetoothhelper.isInitialized) {
            bluetoothhelper.unregisterBluetoothStateChanged()
        }

    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val value = Math.pow(2.toDouble(), (position - 2).toDouble())
        sharedViewModel.setSpeedRate(value)
        sharedPref.setSpeedRate(value)

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(
            requireContext(),
            "Speed Rate: ${sharedViewModel.speedRate.value}",
            Toast.LENGTH_LONG
        ).show()
        println("Speed Rate: ${sharedViewModel.speedRate.value}")
    }

    fun bleScan() {
        println("blescan fonk geldi")
        if (!bluetoothhelper.isBluetoothScanning()) {
            if (deviceArray.size > 0) {
                deviceArray.clear()
                adapter?.notifyDataSetChanged()
                println("isbluetoothscanning fonk geldi")
            }
            bluetoothhelper.startDiscovery()
            println("scan tarama fonk geldi")
        } else {
            println("isscanning true geliyor")
        }

    }

    fun bleHelper() {
        if (!createdBluetoothHelper) {
            createdBluetoothHelper = true
            bluetoothhelper =
                BluetoothHelper(requireContext(), this)
                    .setPermissionRequired(true)
                    .create()

            if (bluetoothhelper.isBluetoothEnabled()) {
                if (!bluetoothhelper.isRegisteredBluetoothStateChanged())
                    bluetoothhelper.registerBluetoothStateChanged()

            } else {
                bluetoothhelper.enableBluetooth()
            }

        } else {
            bluetoothhelper.checkBTPermissions()
        }
        bleScan()
    }

    fun bleRemoteControllerPurchasing() {
        val alert = AlertDialog.Builder(requireContext())
        alert.setTitle("Fortinge")
        alert.setMessage("Do you have Fortinge remote controller?")
        alert.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
            bleScan()
        })
        alert.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Fortinge")
            alert.setMessage("Would you like to buy  Fortinge remote controller?")
            alert.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
                val url = "https://www.fortinge.com"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            })
            alert.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->

            })
            alert.show()
        })
        alert.show()
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == BluetoothHelperConstant.REQ_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            bluetoothhelper.startDiscovery()
//        } else {
//            binding.bleScanSwitch.isChecked = false
//        }
//    }

    fun setBleStatusText(text: String) {
        activity?.let {
            binding.bleStatusTxt.text = text
        }

        sharedViewModel.setBleStatusTxt(text)
    }

    fun isTablet(context: Context): Boolean {
        val xlarge =
            context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
        val large =
            context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }

    private fun islocationEnabled() {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        println("gpsstatus: $gpsStatus")
    }


    @Subscribe
    internal fun bleDataReceivedEvent(bleData: EventbusDataEvents.BleDataChanged) {
//        val settingsFragment = SettingsFragment()
//        if (activity != null) {
//            if (requireActivity()!!.supportFragmentManager?.fragments.get(0) == settingsFragment){
//                println("settings eventbus??")
//                activity?.onBackPressed()
//            }
//        }

    }

    override fun onPause() {
        super.onPause()
        println("settings pause")
    }

    override fun onResume() {
        super.onResume()
        println("settings resume")
    }

    override fun onStartDiscovery() {
        setBleStatusText("Devices scanning..")
        binding.bleRotatingImage.isEnabled = true

        val a = AnimationUtils.loadAnimation(requireContext(), R.anim.progress_anim)
        a.duration = 2500
        binding.bleRotatingImage.startAnimation(a)
        binding.bleRotatingImage.isEnabled = false

    }

    override fun onFinishDiscovery() {
        if (deviceArray.isEmpty()) {
            setBleStatusText("Device not found.")
        } else if (deviceArray.size == 1) {
            setBleStatusText("Device listed.")
        } else {
            setBleStatusText("Devices listed.")
        }

        binding.bleRotatingImage.clearAnimation()
        binding.bleRotatingImage.isEnabled = true

    }

    override fun onEnabledBluetooth() {
        setBleStatusText("Bluetooth is active.")
//                        bleScan()
    }

    override fun onDisabledBluetooth() {
        setBleStatusText("Bluetooth is deactive.")
    }

    override fun getBluetoothDeviceList(device: BluetoothDevice) {
        if (device.name != null && device.name.length > 0) {
            println("cihaz null değil ismi de ${device.name}")
            if (((!isTablet && device.name.startsWith("Fortinge", true))) ||
                (isTablet && device.name.startsWith("Fortinge BT1", true))
            ) {

                if (deviceArray.contains(device)) return
                deviceArray.add(device)
                println(deviceArray.size)
//                                    sharedViewModel.setBleConnectedDevice(deviceArray)
                adapter =
                    BluetoothListAdapter(requireContext(), deviceArray)
                binding.bleDevicesSpinner.adapter = adapter
                adapter!!.notifyDataSetChanged()
                if (!binding.bleConnectionSwitch.isEnabled) {
                    binding.bleConnectionSwitch.isEnabled = true
                }
            }
        }
    }

    override fun onConnectedDevice(device: BluetoothDevice) {
        sharedViewModel.setBleConnectionState(true)
        setBleStatusText(device.name + " connected.")
        binding.bleRotatingImage.isEnabled = false

    }

    override fun onDisconnectedDevice() {
        sharedViewModel.setBleConnectionState(false)
        setBleStatusText("Disconnected.")

    }
}