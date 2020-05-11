package dead.crumbs.data

import androidx.lifecycle.LiveData
import dead.crumbs.data.DAO.Location_DAO
import dead.crumbs.data.DAO.User_DAO
import io.swagger.client.models.Location

class UserRepository private constructor(private val userDao: User_DAO){

    fun getUsers() = userDao.getUsers()
    fun getUser(userName: String) = userDao.getUser(userName)

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: UserRepository? = null

        fun getInstance(userDao: User_DAO) =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userDao).also { instance = it }
            }
    }
}