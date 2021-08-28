package com.example.clientbluetoothvpmn.connectivity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.example.clientbluetoothvpmn.StatusCode
import com.example.clientbluetoothvpmn.connectivity.routing.Packet
import com.example.clientbluetoothvpmn.connectivity.routing.Route
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ClientMesh(val device: BluetoothDevice,val handler : Handler,val machine: NetworkMachine):Thread() {
    val mUUID= UUID.fromString("fe964a9c-184c-11e6-b6ba-3e1d05defe78")
    private var keepAlive=true

    private val mmSocket: BluetoothSocket by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(mUUID)
    }

    private val inFlow: InputStream=mmSocket.inputStream
    private val outFlow: OutputStream=mmSocket.outputStream
    private var mmBufferIn: ByteArray =ByteArray(1024)
    private var mmBufferOut: ByteArray =ByteArray(1024)


    lateinit var ipAddress:String

    init{
        machine.client=this
    }

    override fun run(){
        Log.d("ClientMesh","clientMesh started")

        try {
            mmSocket.let { socket ->
                socket.connect()
            }
            handler.sendMessage(handler.obtainMessage(StatusCode.CONNECTION_SUCCESSFUL))
            retrieveIpAddress()
            Log.d("NetworkMachine",machine.routing.routeList.toString())
            while(keepAlive){
                try{
                    inFlow.read(mmBufferIn)
                    val p= Packet.createPacket(removeUnusedByte(mmBufferIn).decodeToString())
                    Log.d("ClientMesh","Pacchetto ricevuto: "+p.toString())
                    machine.forward(p,mmSocket.remoteDevice.address)
                    mmBufferIn=ByteArray(1024)
                }catch(e:IOException){
                    handler.sendMessage(handler.obtainMessage(StatusCode.CLIENT_DISCONNECTED))
                    machine.server?.disconnect()
                    machine.routing.clear()
                    keepAlive=false
                }
            }
        }catch(e:IOException){
            handler.sendMessage(handler.obtainMessage(StatusCode.CONNECTION_ERROR))
        }
    }

    private fun retrieveIpAddress(){
        try{
            val p=Packet("new","192.168.1.1",BluetoothAdapter.getDefaultAdapter().name)
            mmBufferOut=p.createBuffer()
            Log.d("ClientMesh","Pacchetto inviato: "+mmBufferOut.decodeToString())
            outFlow.write(mmBufferOut)
            mmBufferOut=ByteArray(1024)
        }catch(e:IOException){
            Log.d("ClientMesh","Impossible to send dhcp request")
        }

        var success=false
        while(!success){
            try{
                inFlow.read(mmBufferIn)
                val p= Packet.createPacket(removeUnusedByte(mmBufferIn).decodeToString())
                Log.d("ClientMesh","Pacchetto ricevuto: "+p.toString())
                if(p.sorgente.equals("192.168.1.1") && p.destinazione.equals("new")){
                    ipAddress=p.contenuto
                    handler.sendMessage(handler.obtainMessage(StatusCode.IP_RECEIVED,ipAddress))
                    success=true
                }
                machine.routing.addRoute(Route("#",ipAddress,"#","#"))
                mmBufferIn= ByteArray(1024)
            }catch(e:IOException){
                keepAlive=false
                Log.e("ClientMesh","Impossible reading buffer")
            }
        }
    }

    fun sendPacket(p: Packet){
        try {
            mmBufferOut=p.createBuffer()
            Log.d("ClientMesh","Pacchetto inviato: "+mmBufferOut.decodeToString())
            outFlow.write(mmBufferOut)

            //Pulisco il buffer
            mmBufferOut=ByteArray(1024)
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Error occurred when sending data")
        }
    }

    fun removeUnusedByte(buffer:ByteArray):ByteArray{
        return buffer.filter{
            it!=0.toByte()
        }.toByteArray()
    }

    fun disconnect(){
        try {
            mmSocket.close()
            Log.d("ClientMesh","Socket disconnected successfully")
        } catch (e: IOException) {
            Log.e("ClientMesh", "Could not close the connect socket", e)
        }
    }


}