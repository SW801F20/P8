package dead.crumbs.data

import dead.crumbs.data.DAO.Maps_DAO
import io.swagger.client.models.Location

class MapsRepository private constructor(private val mapsDAO: Maps_DAO) {

    fun getLocation(userName: String) = mapsDAO.getLocation(userName);
    fun getUsers() = mapsDAO.getUsers()
    fun postLocation(location: Location) = mapsDAO.postLocation(location)
    fun getUser(userName: String) = mapsDAO.getUser(userName)

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: MapsRepository? = null

        fun getInstance(mapsDao: Maps_DAO) =
            instance ?: synchronized(this) {
                instance ?: MapsRepository(mapsDao).also { instance = it }
            }
    }
}