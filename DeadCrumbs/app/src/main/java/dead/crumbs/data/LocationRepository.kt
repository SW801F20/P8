package dead.crumbs.data

import dead.crumbs.data.DAO.Location_DAO
import io.swagger.client.models.Location

class LocationRepository private constructor(private val locationDAO: Location_DAO){

    fun getLocation(userName: String) = locationDAO.getLocation(userName);
    fun postLocation(location: Location) = locationDAO.postLocation(location)
    fun updateLocation(userName: String, orientation: Double, dist: Double, timeStamp: String) =
        locationDAO.updateLocation(userName, orientation, dist, timeStamp)
    
    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: LocationRepository? = null

        fun getInstance(locationDAO: Location_DAO) =
            instance ?: synchronized(this) {
                instance ?: LocationRepository(locationDAO).also { instance = it }
            }
    }
}