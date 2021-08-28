package com.example.clientbluetoothvpmn.graphics

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clientbluetoothvpmn.MainActivity
import com.example.clientbluetoothvpmn.R
import com.example.clientbluetoothvpmn.adapter.DeviceAdapter

class SettingsFragment : Fragment() {
    // Lista di dispositivi precedentemente accoppiati
    val mBonded = mutableListOf<BluetoothDevice>()

    // Riferimenti agli oggetti grafici del fragment
    lateinit var sendButton:Button
    var sendButtonEnabled=false

    lateinit var switch:Switch
    lateinit var main: MainActivity

    lateinit var status:TextView
    var statusValue="Disconnesso"

    lateinit var recyclerViewBonded:RecyclerView
    lateinit var recAdapter:DeviceAdapter

    lateinit var ipAddr:TextView
    var ipAddrValue=""

    // indirizzo Mac del dispositivo a cui ci si connette come Client
    var mDevice:String?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inizializzazione oggetti grafici

        val view= inflater.inflate(R.layout.fragment_settings, container, false)
        sendButton=view.findViewById(R.id.send)
        switch=view.findViewById(R.id.switch2)
        status=view.findViewById(R.id.state)
        ipAddr=view.findViewById(R.id.ipAddress)
        main=activity as MainActivity

        status.text=statusValue
        ipAddr.text=ipAddrValue
        sendButton.isEnabled=sendButtonEnabled

        recyclerViewBonded=view.findViewById(R.id.recyclerBonded)
        // Recupero lo stato del fragment (il mac del dispositivo connesso se presente)
        recAdapter= DeviceAdapter(mBonded,this)
        recAdapter.connectDevice=mDevice
        recyclerViewBonded.adapter=recAdapter
        recyclerViewBonded.layoutManager= LinearLayoutManager(view.context)

        // Al click del sendButton cambio il fragment e salvo lo stato passando il mac del dispositivo connesso
        sendButton.setOnClickListener {
            main.messageFragment()
        }

        // Se il bluetooth `e attivo ne recupero i dispositivi accoppiati
        if(main.bluetoothAdapter?.isEnabled==true){
            setSwitch(true)
            val pairedDevices:Set<BluetoothDevice>?=main.bluetoothAdapter?.bondedDevices
            if(pairedDevices!=null)
                inflateRecyclerBonded(pairedDevices)
            else
                inflateRecyclerBonded(emptySet())
        }

        // Gestione del cambiamento di stato del bluetooth
        switch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                main.intentSenderLauncher.launch(enableBtIntent)
            }else{
                main.bluetoothAdapter?.disable()
                inflateRecyclerBonded(emptySet())
            }
        }

        return view
    }

    fun setSwitch(value: Boolean){
        switch.setChecked(value)
    }

    fun inflateRecyclerBonded(set :Set<BluetoothDevice>){
        mBonded.clear()
        mBonded.addAll(set)
        recAdapter.notifyDataSetChanged()
    }

    fun connectButtonPressed(device: BluetoothDevice){
        if(mDevice==null){
            main.connect(device)
            setStatus("Connessione in corso...")
            setIpAddress("")
            mDevice=device.address
        } else
            main.disconnect()
    }

    fun connectionSuccesfull(){
        setStatus("Connesso")
        recAdapter.connectDevice=mDevice
        val index=mBonded.indexOfFirst {
            it.address==mDevice
        }
        recAdapter.notifyItemChanged(index)
    }

    fun ipReceived(ip:String){
        setIpAddress(ip)
        setSendButtEnabled(true)
    }

    fun connectionFail(){
        setIpAddress("")
        setStatus("Disconnesso")
        setSendButtEnabled(false)
        mDevice=null
        recAdapter.connectDevice=mDevice
        recAdapter.notifyDataSetChanged()
    }

    fun clientDisconnected(){
        setIpAddress("")
        setStatus("Disconnesso")
        setSendButtEnabled(false)
        val index=mBonded.indexOfFirst {
            it.address==mDevice
        }
        mDevice=null
        recAdapter.connectDevice=mDevice
        recAdapter.notifyItemChanged(index)
    }

    private fun setStatus(s:String){
        statusValue=s
        status.text=statusValue
    }

    private fun setIpAddress(s:String){
        ipAddrValue=s
        ipAddr.text=ipAddrValue
    }

    private fun setSendButtEnabled(v:Boolean){
        sendButtonEnabled=v
        sendButton.isEnabled=sendButtonEnabled
    }

}