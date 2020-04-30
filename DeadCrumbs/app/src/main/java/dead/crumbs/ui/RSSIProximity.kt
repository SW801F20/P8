package dead.crumbs.ui

import dead.crumbs.data.RSSIDist
import kotlin.math.exp

private const val NUMBER_MEASUREMENTS = 5; //Number of measurements stored for each mac address - used for average
private var deviceDistanceMap = mutableMapOf<String, MutableList<RSSIDist>>()


class RSSIProximity() {
    /*fun getNewAverageDist(rssiDist: RSSIDist): Double{
        addRSSIToMap(rssiDist, deviceDistanceMap)                           //Updates map with new rssi
        return averageDist(rssiDist.target_mac_address, deviceDistanceMap)
    }
    private fun addRSSIToMap(rssiDist: RSSIDist, map: MutableMap<String,MutableList<RSSIDist>>){
        if  (!map.containsKey(rssiDist.target_mac_address)){
            map[rssiDist.target_mac_address] = mutableListOf(rssiDist)
        }
        else if (map.containsKey(rssiDist.target_mac_address)
            && map[rssiDist.target_mac_address]!!.count() < NUMBER_MEASUREMENTS)
            map[rssiDist.target_mac_address]!!.add(rssiDist)
        else{
            map[rssiDist.target_mac_address]!!.removeAt(0);         //removes oldest element
            map[rssiDist.target_mac_address]!!.add(rssiDist)                  //adds new element
        }
    }

    private fun averageDist(mac_address: String, map: MutableMap<String,MutableList<RSSIDist>>): Double{
        var distance = 0.0;
        if (!map.containsKey(mac_address))
            throw java.lang.Exception("No rssi recoding for given mac address")
        for (rssiDist: RSSIDist in map[mac_address]!!){
            distance += distanceFromRSSI(rssiDist);
        }
        return distance/map[mac_address]!!.count()
    }*/

    //coefficients from exponential model fitting on recorded data set (with side by side rssi to distance data * 2)
    public fun distanceFromRSSI(rssi: Double): Double{
        return 0.006829659881992 * exp(-0.11713074201511 * rssi)
    }
}