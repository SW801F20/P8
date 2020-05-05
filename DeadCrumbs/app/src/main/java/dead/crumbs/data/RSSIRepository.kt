package dead.crumbs.data

import dead.crumbs.data.DAO.RSSI_DAO
import io.swagger.client.models.Location

class RSSIRepository private constructor(private val rssiDao: RSSI_DAO) {

    fun bluetoothSync(my_username : String, target_mac: String,
                      distance : Double, dateTimeString: String) =
                    rssiDao.bluetoothSync(my_username, target_mac, distance, dateTimeString)


    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: RSSIRepository? = null

        fun getInstance(rssiDao: RSSI_DAO) =
            instance ?: synchronized(this) {
                instance ?: RSSIRepository(rssiDao).also { instance = it }
            }
    }
}