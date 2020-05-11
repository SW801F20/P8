package dead.crumbs.data

import dead.crumbs.data.DAO.Location_DAO
import dead.crumbs.data.DAO.RSSI_DAO
import dead.crumbs.data.DAO.User_DAO
import dead.crumbs.client.apis.DeadCrumbsApi

class Database private constructor() {


    val client = DeadCrumbsApi("http://130.225.57.95:8393/")
    // All the DAOs go here!
    var rssiDao = RSSI_DAO(client)
        private set

    var locationDao = Location_DAO(client)
        private set

    var userDao = User_DAO(client)
        private set

    companion object {
        // @Volatile - Writes to this property are immediately visible to other threads
        @Volatile private var instance: Database? = null

        // The only way to get hold of the FakeDatabase object
        fun getInstance() =
        // Already instantiated? - return the instance
            // Otherwise instantiate in a thread-safe manner
            instance ?: synchronized(this) {
                // If it's still not instantiated, finally create an object
                // also set the "instance" property to be the currently created one
                instance ?: Database().also { instance = it }
            }
    }
}