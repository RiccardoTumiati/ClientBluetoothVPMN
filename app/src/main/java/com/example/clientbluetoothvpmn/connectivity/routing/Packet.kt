package com.example.clientbluetoothvpmn.connectivity.routing

import android.util.Log

data class Packet(var sorgente:String,var destinazione:String,var contenuto:String) {
    companion object{
        fun createPacket(buffer:String): Packet {
            val s=buffer.subSequence(0,buffer.indexOf("-")).toString()
            val d=buffer.subSequence(buffer.indexOf("-")+1,buffer.indexOf("?")).toString()
            val c=buffer.subSequence(buffer.indexOf("?")+1,buffer.lastIndex+1).toString()

            return Packet(s,d,c)
        }
    }

    fun createBuffer():ByteArray{
        val buffer=sorgente+"-"+destinazione+"?"+contenuto
        return buffer.encodeToByteArray()
    }
}