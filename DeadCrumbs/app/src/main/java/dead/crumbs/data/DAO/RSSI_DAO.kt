package dead.crumbs.data.DAO

import dead.crumbs.data.RSSIDist
import io.swagger.client.apis.DeadCrumbsApi
import java.util.*
import kotlin.concurrent.thread

class RSSI_DAO (val client : DeadCrumbsApi) {

    init {

        val currYear = Calendar.getInstance().get(Calendar.YEAR).toString().padStart(4,'0')
        val currMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString().padStart(2,'0')
        val currDate = Calendar.getInstance().get(Calendar.DATE).toString().padStart(2,'0')
        val currHour = Calendar.getInstance().get(Calendar.HOUR).toString().padStart(2,'0')
        val currMinute = Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2,'0')
        val currSecond = Calendar.getInstance().get(Calendar.SECOND).toString().padStart(2,'0')
        val dateTimeString = currYear + "-" + currMonth + "-" + currDate+ "T" + currHour + ":" + currMinute + ":" + currSecond


        update_locations(RSSIDist("Jacob2", "A8:87:B3:ED:DF:7E", 1.0, dateTimeString ))
    }
    fun update_locations(rssi_Dist: RSSIDist) {
        val thread = thread(start = true){
            client.postBluetoothSync(rssi_Dist.my_username, rssi_Dist.target_mac_address,
                rssi_Dist.rssi_dist, rssi_Dist.timestamp)
        }
        thread.join()
    }
}