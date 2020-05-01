package dead.crumbs.data.DAO

import dead.crumbs.data.RSSIDist
import io.swagger.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location
import java.util.*
import kotlin.concurrent.thread

class RSSI_DAO (val client : DeadCrumbsApi) {

    fun bluetoothSync(rssi_Dist: RSSIDist): Array<Location> {
        var res: Array<Location>? = null
        val thread = thread(start = true){
            res = client.postBluetoothSync(rssi_Dist.my_username, rssi_Dist.target_mac_address,
                rssi_Dist.rssi_dist, rssi_Dist.timestamp)
        }
        thread.join()
        return res!!;
    }
}