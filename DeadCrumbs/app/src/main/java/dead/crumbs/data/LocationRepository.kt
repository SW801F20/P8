package dead.crumbs.data

import dead.crumbs.data.DAO.Location_DAO

class LocationRepository private constructor(private val locationDao: Location_DAO){

    fun getLocation() {
        locationDao.getLocation();
    }

    fun postLocation(){

    }

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: LocationRepository? = null

        fun getInstance(locationDao: Location_DAO) =
            instance ?: synchronized(this) {
                instance ?: LocationRepository(locationDao).also { instance = it }
            }
    }
}