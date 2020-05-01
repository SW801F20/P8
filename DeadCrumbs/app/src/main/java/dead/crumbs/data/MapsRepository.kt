package dead.crumbs.data

import dead.crumbs.data.DAO.Maps_DAO
import dead.crumbs.data.DAO.RSSI_DAO
import io.swagger.client.models.Location

class MapsRepository private constructor(private val mapsDao: Maps_DAO) {

    fun getLocation(userName: String) = mapsDao.getLocation(userName);
    fun getUsers() = mapsDao.getUsers()
    fun postLocation(location: Location) = mapsDao.postLocation(location)
    fun getUser(userName: String) = mapsDao.getUser(userName)

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: MapsRepository? = null

        fun getInstance(mapsDao: Maps_DAO) =
            instance ?: synchronized(this) {
                instance ?: MapsRepository(mapsDao).also { instance = it }
            }
    }
}