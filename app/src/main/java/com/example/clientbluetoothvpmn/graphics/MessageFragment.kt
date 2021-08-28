package com.example.clientbluetoothvpmn.graphics

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clientbluetoothvpmn.MainActivity
import com.example.clientbluetoothvpmn.R
import com.example.clientbluetoothvpmn.connectivity.routing.Packet
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MessageFragment : Fragment() {
    // Oggetti grafici
    lateinit var settingsButton: FloatingActionButton
    lateinit var sendMessage: FloatingActionButton

    lateinit var lastMesFrom: TextView
    var lastMesFromValue=""
    lateinit var lastMesText: TextView
    var lastMesTextValue=""
    lateinit var log: TextView

    lateinit var ottetto1: EditText
    lateinit var ottetto2: EditText
    lateinit var ottetto3: EditText
    lateinit var ottetto4: EditText
    lateinit var typeMessage: EditText

    lateinit var main: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.fragment_message, container, false)

        settingsButton=view.findViewById(R.id.settings)
        sendMessage=view.findViewById(R.id.sendMessage)

        lastMesFrom=view.findViewById(R.id.lastMesFrom)
        lastMesFrom.text=lastMesFromValue
        lastMesText=view.findViewById(R.id.lastMesText)
        lastMesText.text=lastMesTextValue
        log=view.findViewById(R.id.log)

        ottetto1=view.findViewById(R.id.ottetto1)
        ottetto2=view.findViewById(R.id.ottetto2)
        ottetto3=view.findViewById(R.id.ottetto3)
        ottetto4=view.findViewById(R.id.ottetto4)
        typeMessage=view.findViewById(R.id.typeMessage)
        main=activity as MainActivity

        settingsButton.setOnClickListener {
            main.settingsFragment()
        }

        sendMessage.setOnClickListener {
            val destinazione=ottetto1.text.toString()+"."+ottetto2.text.toString()+"."+ottetto3.text.toString()+"."+ottetto4.text.toString()
            val p= Packet(main.client.ipAddress,destinazione,typeMessage.text.toString())
            main.networkMachine.forward(p,"#")
            main.hideKeyboard()
        }

        return view
    }

    fun setLastMessageFrom(s:String){
        lastMesFromValue=s
        if(this.isAdded)
            lastMesFrom.text=lastMesFromValue
    }

    fun setLastMessageText(s:String){
        lastMesTextValue=s
        if(this.isAdded)
            lastMesText.text=lastMesTextValue
    }

    fun setLog(s:String){
        if(this.isAdded)
            log.text=s
    }
}