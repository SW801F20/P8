package dead.crumbs.ui

import dead.crumbs.data.BluetoothRSSI
import kotlin.math.exp

private const val NUMBER_MEASUREMENTS = 5; //Number of measurements stored for each mac address - used for average
private var deviceDistanceMap = mutableMapOf<String, MutableList<BluetoothRSSI>>()


class RSSIProximity() {
    fun getNewAverageDist(rssi: BluetoothRSSI): Double{
        addRSSIToMap(rssi, deviceDistanceMap)                           //Updates map with new rssi
        return averageDist(rssi.target_mac_address, deviceDistanceMap)
    }
    private fun addRSSIToMap(rssi: BluetoothRSSI, map: MutableMap<String,MutableList<BluetoothRSSI>>){
        if  (!map.containsKey(rssi.target_mac_address)){
            map[rssi.target_mac_address] = mutableListOf(rssi)
        }
        else if (map.containsKey(rssi.target_mac_address)
            && map[rssi.target_mac_address]!!.count() < NUMBER_MEASUREMENTS)
            map[rssi.target_mac_address]!!.add(rssi)
        else{
            map[rssi.target_mac_address]!!.removeAt(0);         //removes oldest element
            map[rssi.target_mac_address]!!.add(rssi)                  //adds new element
        }
    }

    private fun averageDist(mac_address: String, map: MutableMap<String,MutableList<BluetoothRSSI>>): Double{
        var distance = 0.0;
        if (!map.containsKey(mac_address))
            throw java.lang.Exception("No rssi recoding for given mac address")
        for (rssi: BluetoothRSSI in map[mac_address]!!){
            distance += distanceFromRSSI(rssi);
        }
        return distance/map[mac_address]!!.count()
    }

    //coefficients from exponential model fitting on recorded data set (with side by side rssi to distance data * 2)
    private fun distanceFromRSSI(rssi: BluetoothRSSI): Double{
        return 0.006829659881992 * exp(-0.11713074201511 * rssi.bluetooth_rssi)
    }
}