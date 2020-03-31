package dead.crumbs.ui

import dead.crumbs.data.BluetoothRSSI

private val NUMBER_MEASUREMENTS = 5; //Number of measurements stored for each mac address - used for average
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
            && map.get(rssi.target_mac_address)!!.count() < NUMBER_MEASUREMENTS)
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

    private fun distanceFromRSSI(rssi: BluetoothRSSI): Double{
        var result: Double;
        val oneMeterRSSI = -50;
        val envFactor = 3.3;
        result = Math.pow(
            10.toDouble(), (oneMeterRSSI.toDouble() - rssi.bluetooth_rssi)
                    / (10 * envFactor)
        );
        return result;
    }
}