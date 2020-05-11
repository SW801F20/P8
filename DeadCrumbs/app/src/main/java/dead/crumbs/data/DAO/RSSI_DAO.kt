package dead.crumbs.data.DAO

import dead.crumbs.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location
import kotlin.concurrent.thread

class RSSI_DAO (val client : DeadCrumbsApi) {

    //Updates the location of two users that are
    //in Bluetooth range of each other and returns
    //the updated locations.
    fun bluetoothSync(my_username : String, target_mac : String,
                      distance : Double, dateTimeString : String): Array<Location> {
        var res: Array<Location>? = null
        val thread = thread(start = true){
            res = client.postBluetoothSync(my_username, target_mac,
                distance, dateTimeString)
        }
        thread.join()
        if (res == null){
            throw Exception("Call to postBluetoothSync failed ")
        } else
        {
            return res!!;
        }
    }
}