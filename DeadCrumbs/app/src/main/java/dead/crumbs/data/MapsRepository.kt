package dead.crumbs.data

import dead.crumbs.data.DAO.Maps_DAO
import dead.crumbs.data.DAO.RSSI_DAO

class MapsRepository private constructor(private val mapsDao: Maps_DAO) {

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: MapsRepository? = null

        fun getInstance(mapsDao: Maps_DAO) =
            instance ?: synchronized(this) {
                instance ?: MapsRepository(mapsDao).also { instance = it }
            }
    }
}