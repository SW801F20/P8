package dead.crumbs.data
import dead.crumbs.data.DAO.Location_DAO
import io.swagger.client.models.Location

class LocationRepository private constructor(private val locationDAO: Location_DAO) {

    fun addLocation(location: Location) {
        locationDAO.addLocations(location)
    }

    fun getLocations() = locationDAO.getLocations()

    fun getLocation(deviceId: Int) = locationDAO.getLocation(deviceId)

    fun deleteLocation(deviceId: Int) = locationDAO.deleteLocation(deviceId)

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: LocationRepository? = null

        fun getInstance(locationDAO: Location_DAO) =
            instance ?: synchronized(this) {
                instance ?: LocationRepository(locationDAO).also { instance = it }
            }
    }
}