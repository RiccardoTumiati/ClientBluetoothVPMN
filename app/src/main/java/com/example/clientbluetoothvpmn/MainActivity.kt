package com.example.clientbluetoothvpmn

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.example.clientbluetoothvpmn.connectivity.ClientMesh
import com.example.clientbluetoothvpmn.connectivity.NetworkMachine
import com.example.clientbluetoothvpmn.connectivity.ServerMesh
import com.example.clientbluetoothvpmn.connectivity.routing.Packet
import com.example.clientbluetoothvpmn.graphics.DenyFragment
import com.example.clientbluetoothvpmn.graphics.MessageFragment
import com.example.clientbluetoothvpmn.graphics.SettingsFragment

class MainActivity : AppCompatActivity() {
    var setFrag: SettingsFragment = SettingsFragment()
    var mesFrag: MessageFragment = MessageFragment()
    val denyFrag: DenyFragment = DenyFragment()
    val fragmentManager=supportFragmentManager

    val bluetoothAdapter:BluetoothAdapter?=BluetoothAdapter.getDefaultAdapter()

    val handler: Handler = UIHandler()
    lateinit var server: ServerMesh
    lateinit var client: ClientMesh
    val networkMachine= NetworkMachine(handler)

    lateinit var intentSenderLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted)
                denyFragment()
        }

        intentSenderLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode== RESULT_CANCELED){
                bluetoothAdapter?.disable()
                setFrag.setSwitch(false)
            }else{
                val pairedDevices:Set<BluetoothDevice>?=bluetoothAdapter?.bondedDevices
                if(pairedDevices!=null)
                    setFrag.inflateRecyclerBonded(pairedDevices)
                else
                    setFrag.inflateRecyclerBonded(emptySet())
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        fragmentManager.commit {
            add(R.id.container,setFrag)
            setReorderingAllowed(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }


    fun messageFragment(){
        fragmentManager.commit {
            replace(R.id.container,mesFrag)
            setReorderingAllowed(true)
        }
    }

    fun settingsFragment(){
        fragmentManager.commit {
            replace(R.id.container,setFrag)
            setReorderingAllowed(true)
        }
    }

    fun denyFragment(){
        fragmentManager.commit {
            replace(R.id.container,denyFrag)
            setReorderingAllowed(true)
        }
    }


    fun connect(device:BluetoothDevice){
        instantiateClient(device)
        instantiateServer()
    }

    fun disconnect(){
        client.disconnect()
    }

    private fun instantiateClient(device:BluetoothDevice){
        if(bluetoothAdapter!=null){
            client= ClientMesh(device,handler,networkMachine)
            client.start()
        }
    }

    private fun instantiateServer(){
        if(bluetoothAdapter!=null){
            server= ServerMesh(bluetoothAdapter,handler,networkMachine)
            server.start()
        }
    }

    fun hideKeyboard(){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if( inputMethodManager.isAcceptingText())
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()?.getWindowToken(), 0)
    }

    inner class UIHandler: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                StatusCode.CONNECTION_ERROR->setFrag.connectionFail()
                StatusCode.CLIENT_DISCONNECTED->{
                    setFrag.clientDisconnected()
                    settingsFragment()
                }
                StatusCode.CONNECTION_SUCCESSFUL->setFrag.connectionSuccesfull()
                StatusCode.IP_RECEIVED->setFrag.ipReceived(msg.obj as String)
                StatusCode.MESSAGE_RECEIVED->{
                    mesFrag.setLastMessageFrom((msg.obj as Packet).sorgente)
                    mesFrag.setLastMessageText((msg.obj as Packet).contenuto)
                }
                StatusCode.LOG_MESSAGE->{
                    val content=(msg.obj as Packet).contenuto
                    mesFrag.setLog(content.subSequence(9,content.lastIndex+1).toString())
                }
            }
            super.handleMessage(msg)
        }
    }



}