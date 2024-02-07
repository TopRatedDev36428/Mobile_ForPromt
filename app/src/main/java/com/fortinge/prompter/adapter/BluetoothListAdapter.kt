package com.fortinge.prompter.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.fortinge.forprompt.databinding.BleSpinnerBinding

class BluetoothListAdapter(private val context: Context, private val deviceArray: ArrayList<BluetoothDevice>) : BaseAdapter() {

    override fun getItem(p0: Int): Any {
        return deviceList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return deviceList.size
    }

    var deviceList: ArrayList<BluetoothDevice>
    //Add this
    var inflater : LayoutInflater
    lateinit var binding: BleSpinnerBinding


    init {
        this.deviceList = deviceArray
        //val inflater  = LayoutInflater.from(context)
        inflater  = LayoutInflater.from(context)
    }
    @SuppressLint("ViewHolder")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        binding = BleSpinnerBinding.inflate(inflater)
        val view = binding.root
        binding.spinnerBle.setText(deviceList[p0].name)

        return view
    }

}