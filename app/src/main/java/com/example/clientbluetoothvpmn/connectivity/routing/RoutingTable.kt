package com.example.clientbluetoothvpmn.connectivity.routing

class RoutingTable {
    val routeList= mutableListOf<Route>()

    fun addRoute(route: Route){
        routeList.add(route)
    }

    fun nameRoute(mac: String):String{
        return routeList.find{
            it.macDest==mac
        }!!.nomeDest
    }

    fun ipRoute(mac : String):String{
        return routeList.find{
            it.macDest==mac
        }!!.ipDest
    }

    fun subTree(mac : String):List<Route>{
        return routeList.filter{
            it.macNextHop==mac
        }
    }

    fun removeRouteByMAC(mac:String){
        routeList.removeAll{
            it.macDest==mac
        }
        routeList.removeAll{
            it.macNextHop==mac
        }
    }

    fun searchNextHop(p:Packet):String?{
        return routeList.find {
            it.ipDest==p.destinazione
        }?.macNextHop
    }

    fun clear(){
        routeList.clear()
    }
}