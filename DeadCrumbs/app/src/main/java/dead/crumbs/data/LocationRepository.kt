package dead.crumbs.data

import androidx.lifecycle.LiveData
import dead.crumbs.data.DAO.Location_DAO
import io.swagger.client.models.Location
import io.swagger.client.models.User

class LocationRepository private constructor(private val locationDao: Location_DAO){

    fun getLocation(userName: String) = locationDao.getLocation(userName);
    fun getUsers() = locationDao.getUsers()
    fun postLocation(location: Location) = locationDao.postLocation(location)
    fun getUser(userName: String) = locationDao.getUser(userName)

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: LocationRepository? = null

        fun getInstance(locationDao: Location_DAO) =
            instance ?: synchronized(this) {
                instance ?: LocationRepository(locationDao).also { instance = it }
            }
    }
}