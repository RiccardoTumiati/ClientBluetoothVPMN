package com.example.clientbluetoothvpmn.adapter

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clientbluetoothvpmn.R
import com.example.clientbluetoothvpmn.graphics.SettingsFragment

class DeviceAdapter(val deviceList:MutableList<BluetoothDevice>, val setFrag: SettingsFragment): RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
        var connectDevice:String?=null
            set(value){ field=value }

        // Describes an item view and its place within the RecyclerView
        inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
            private val deviceMAC: TextView = itemView.findViewById(R.id.deviceMAC)
            private val connectButton: Button =itemView.findViewById(R.id.connectButton)
            private lateinit var bluetoothDevice:BluetoothDevice

            fun bind(device: BluetoothDevice) {
                deviceName.text = device.name
                deviceMAC.text=device.address
                bluetoothDevice=device

                if(deviceMAC.text==connectDevice){
                    connectButton.setBackgroundColor(Color.RED)
                    connectButton.text="Connesso"
                } else {
                    connectButton.setBackgroundColor(Color.parseColor("#6200EE"))
                    connectButton.text="Connetti"
                }
            }

            init{
                connectButton.setOnClickListener {
                    setFrag.connectButtonPressed(bluetoothDevice)
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
            return DeviceViewHolder(view)
        }

        override fun getItemCount(): Int {
            return deviceList.size
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            holder.bind(deviceList.get(position))
        }


}