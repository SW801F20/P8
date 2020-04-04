package dead.crumbs.ui

import android.content.Context
import dead.crumbs.data.BluetoothRSSI
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import kotlin.math.exp

private val NUMBER_MEASUREMENTS = 5; //Number of measurements stored for each mac address - used for average
private var deviceDistanceMap = mutableMapOf<String, MutableList<BluetoothRSSI>>()
private val CONFIG_FILE = "model-rssi-configurations.json"

class RSSIProximity(context: Context) {
    init {
        fun readFile(filepath: String): String {
            val file = File(context.filesDir, filepath)
            return file.readText()
        }
        fun parseJSON(str: String) : JSONObject{
            return JSONObject(str)
        }

        var fileStr = readFile(CONFIG_FILE);
        var jsonObject = parseJSON(fileStr)
        var configurations = jsonObject.getJSONArray("configurations")
        var i = 0
        i++

    }
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
            distance += bigboiE(rssi);
        }
        return distance/map[mac_address]!!.count()
    }
/*
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
*/
    private fun s2sL(rssi: BluetoothRSSI): Double{
        var result: Double;
        result =- 20.7513/ (1 - 163.4283 * exp(0.063 * rssi.bluetooth_rssi));
        return result;
    }

    private fun s2sE(rssi: BluetoothRSSI): Double{
        var result: Double;
        result =0.0019 * exp(-0.1341 * rssi.bluetooth_rssi)
        return result;
    }

    private fun bigboiE(rssi: BluetoothRSSI): Double{
        var result: Double;
        result =0.006829659881992 * exp(-0.11713074201511 * rssi.bluetooth_rssi)
        return result;
    }

    private fun a2aE(rssi: BluetoothRSSI): Double{
        var result: Double;
        result =0.000050224361684 * exp(-0.194209798645603 * rssi.bluetooth_rssi)
        return result;
    }
}