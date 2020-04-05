package dead.crumbs.data

import dead.crumbs.data.DAO.Location_DAO
import dead.crumbs.data.DAO.RSSI_DAO

class FakeDatabase private constructor() {

    // All the DAOs go here!
    var rssiDao = RSSI_DAO()
        private set
    var locationDao = Location_DAO()
        private set

    companion object {
        // @Volatile - Writes to this property are immediately visible to other threads
        @Volatile private var instance: FakeDatabase? = null

        // The only way to get hold of the FakeDatabase object
        fun getInstance() =
        // Already instantiated? - return the instance
            // Otherwise instantiate in a thread-safe manner
            instance ?: synchronized(this) {
                // If it's still not instantiated, finally create an object
                // also set the "instance" property to be the currently created one
                instance ?: FakeDatabase().also { instance = it }
            }
    }
}