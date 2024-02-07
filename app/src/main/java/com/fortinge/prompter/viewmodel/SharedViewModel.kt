package com.fortinge.prompter.viewmodel
import android.bluetooth.BluetoothDevice
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    /*
    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text
     */

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> get() = _text

    fun setText(text: String) {
        _text.value = text
    }


    private val _fontSize = MutableLiveData<Float>()
    val fontSize: LiveData<Float> = _fontSize

    fun setFontSize(fontSize: Float) {
        _fontSize.value = fontSize
    }


    private val _isBold = MutableLiveData<Boolean>()
    val isBold: LiveData<Boolean> get() = _isBold

    fun setIsBold(isBold: Boolean) {
        _isBold.value = isBold
    }

    private val _alignment = MutableLiveData<Int>()
    val alignment: LiveData<Int> get() = _alignment

    fun setAlignment(alignment: Int) {
        _alignment.value = alignment
    }

    private val _speedRate = MutableLiveData<Double>()
    val speedRate: LiveData<Double> get() = _speedRate

    fun setSpeedRate(speedRate: Double) {
        _speedRate.value = speedRate
    }

    ////////////////////////////////////////////////////////////////////
    private val _countdownEnable = MutableLiveData<Boolean>()
    val countdownEnable: LiveData<Boolean> get() = _countdownEnable

    fun setCountdownEnable(countdownEnable: Boolean) {
        _countdownEnable.value = countdownEnable
    }

    private val _countdownValue = MutableLiveData<Int>()
    val countdownValue: LiveData<Int> get() = _countdownValue

    fun setCountdownValue(countdownValue: Int) {
        _countdownValue.value = countdownValue
    }

    ////////////////////////////////////////////////////////////////////
    private val _promptingLoop = MutableLiveData<Boolean>()
    val promptingLoop: LiveData<Boolean> get() = _promptingLoop

    fun setPromptingLoop(promptingLoop: Boolean) {
        _promptingLoop.value = promptingLoop
    }

    ////////////////////////////////////////////////////////////////////
    private val _showScroll = MutableLiveData<Boolean>()
    val showScroll: LiveData<Boolean> get() = _showScroll

    fun setshowScroll(showScroll: Boolean) {
        _showScroll.value = showScroll
    }
    ////////////////////////////////////////////////////////////////////
    private val _showCue = MutableLiveData<Boolean>()
    val showCue: LiveData<Boolean> get() = _showCue

    fun setshowCue(showCue: Boolean) {
        _showCue.value = showCue
    }
/////////////////////////////////////////////////////////////////////////

    private val _cueMarkerSeekBarPosition= MutableLiveData<Int>()
    val cueMarkerSeekBarPosition: LiveData<Int> get() = _cueMarkerSeekBarPosition

    fun setcueMarkerSeekBarPosition(cueMarkerSeekBarPosition: Int) {
        _cueMarkerSeekBarPosition.value = cueMarkerSeekBarPosition
    }

    ////////////////////////////////////////////////////////////////////
    private val _marginsEnable = MutableLiveData<Boolean>()
    val marginsEnable: LiveData<Boolean> get() = _marginsEnable

    fun setmarginsEnable(marginsEnable: Boolean) {
        _marginsEnable.value = marginsEnable
    }

    private val _marginsValue = MutableLiveData<Int>()
    val marginsValue: LiveData<Int> get() = _marginsValue

    fun setmarginsValue(marginsValue: Int) {
        _marginsValue.value = marginsValue
    }

    ////////////////////////////////////////////////////////////////////
    private val _RTL = MutableLiveData<Boolean>()
    val RTL: LiveData<Boolean> get() = _RTL

    fun setRTL(RTL: Boolean) {
        _RTL.value = RTL
    }

    ////////////////////////////////////////////////////////////////////
    private val _textColor = MutableLiveData<Int>()
    val textColor: LiveData<Int> get() = _textColor

    fun setTextColor(textColor: Int) {
        _textColor.value = textColor
    }
//////////////////////////////////////////////////////////////////


    private val _backgroundColor = MutableLiveData<Int>()
    val backgroundColor: LiveData<Int> get() = _backgroundColor

    fun setBackgroundColor(backgroundColor: Int) {
        _backgroundColor.value = backgroundColor
    }

    private val _prompPosition = MutableLiveData<Int>()
    val promptPosition: LiveData<Int> get() = _prompPosition

    fun setPromptPosition(promptPosition: Int) {
        _prompPosition.value = promptPosition
    }

    private val _promptEnable = MutableLiveData<Boolean>()
    val promptEnable: LiveData<Boolean> get() = _promptEnable

    fun setPromptEnable(promptEnable: Boolean) {
        _promptEnable.value = promptEnable
    }

    ///////////////////////BLUETOOTH/////////////////////////////////////////////

    private val _bleValue = MutableLiveData<Byte>()
    val bleValue: LiveData<Byte> get() = _bleValue

    fun setBleValue(bleValue: Byte) {
        _bleValue.postValue(bleValue)
    }

    private val _bleConnectionState = MutableLiveData<Boolean>()
    val bleConnectionState: LiveData<Boolean> get() = _bleConnectionState

    fun setBleConnectionState(bleConnectionState: Boolean) {
        _bleConnectionState.value = bleConnectionState
    }

    private val _bleConnectedDevice = MutableLiveData<ArrayList<BluetoothDevice>>()
    val bleConnectedDevice: LiveData<ArrayList<BluetoothDevice>> get() = _bleConnectedDevice

    fun setBleConnectedDevice(bleConnectedDevice: ArrayList<BluetoothDevice>) {
        _bleConnectedDevice.value = bleConnectedDevice
    }

    private val _bleStatusTxt = MutableLiveData<String>()
    val bleStatusTxt: LiveData<String> get() = _bleStatusTxt

    fun setBleStatusTxt(bleStatusTxt: String) {
        _bleStatusTxt.value = bleStatusTxt
    }

    private val _activeFragment = MutableLiveData<Fragment>()
    val activeFragment : LiveData<Fragment> get() = _activeFragment

    fun setActiveFragment(activeFragment: Fragment) {
        _activeFragment.value = activeFragment
    }

}