package com.example.clientbluetoothvpmn.connectivity

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.example.clientbluetoothvpmn.StatusCode
import com.example.clientbluetoothvpmn.connectivity.routing.Packet
import com.example.clientbluetoothvpmn.connectivity.routing.Route
import com.example.clientbluetoothvpmn.connectivity.routing.RoutingTable

class NetworkMachine(val handler: Handler) {
    val routing= RoutingTable()

    var server:ServerMesh?=null
        set(value){ field=value }

    var client:ClientMesh?=null
        set(value){ field=value }

    var newRoute=Route("","","","")
    var newMac=""

    fun forward(p: Packet, previousMac: String){
        var hop=routing.searchNextHop(p)

        when(hop){
            null->{
                if(p.destinazione.equals("new")){
                    newRoute.ipDest=p.contenuto
                    routing.addRoute(newRoute)
                    newRoute= Route("","","","")
                    Log.d("NetworkMachine",routing.routeList.toString())
                    server?.sendPacket(p,newMac)
                } else if(p.sorgente.equals("new")){
                    newRoute.nomeDest=p.contenuto.takeWhile {
                        it!='+'
                    }
                    newRoute.macDest=p.contenuto.takeLastWhile {
                        it!='+'
                    }
                    newRoute.macNextHop=previousMac
                    newMac=previousMac
                    client?.sendPacket(p)
                } else if(p.destinazione.equals("disconnected")) {
                    routing.removeRouteByMAC(p.contenuto)
                    Log.d("NetworkMachine",routing.routeList.toString())
                    client?.sendPacket(p)
                } else {
                    client?.sendPacket(p)
                }
            }
            "#"->{
                    if(p.contenuto.startsWith("01010101|"))
                        handler.sendMessage(handler.obtainMessage(StatusCode.LOG_MESSAGE,p))
                    else
                        handler.sendMessage(handler.obtainMessage(StatusCode.MESSAGE_RECEIVED,p))
            }
            else->server?.sendPacket(p,hop)
        }
    }


    fun deviceDisconnected(socket: BluetoothSocket){
        // Tolgo dalla lista delle connessioni del server il socket che disconnesso
        server?.removeConnection(socket)

        // Cancello le route associate a quel socket
        //routing.removeRouteByMAC(socket.remoteDevice.address)
    }
}