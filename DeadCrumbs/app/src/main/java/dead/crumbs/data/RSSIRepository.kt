package dead.crumbs.data

import dead.crumbs.data.DAO.RSSI_DAO
import io.swagger.client.models.Location

class RSSIRepository private constructor(private val rssiDao: RSSI_DAO) {

    // This may seem redundant.
    // Imagine a code which also updates and checks the backend.
    fun bluetoothSync(rssiDist: RSSIDist) = rssiDao.bluetoothSync(rssiDist)


    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: RSSIRepository? = null

        fun getInstance(rssiDao: RSSI_DAO) =
            instance ?: synchronized(this) {
                instance ?: RSSIRepository(rssiDao).also { instance = it }
            }
    }
}