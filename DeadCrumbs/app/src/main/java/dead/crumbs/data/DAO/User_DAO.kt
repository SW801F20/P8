package dead.crumbs.data.DAO
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.swagger.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location
import io.swagger.client.models.User
import java.lang.Exception
import kotlin.concurrent.thread

class User_DAO (val client : DeadCrumbsApi) {

    //Returns a user from the database
    fun getUser(userName: String): LiveData<User>{
        var user : User? = null
        val thread = thread (start = true){
            user = client.getUser(userName)
        }
        thread.join()
        if(user == null){
            throw Exception("Call to getUser failed")
        }else{
            return MutableLiveData(user)
        }
    }

    //Returns all users in the database
    fun getUsers(): LiveData<List<User>>{
        var all_users = arrayOf<User>()

        val thread = thread{
            all_users = client.getUsers()
        }

        //Wait for the action to complete
        thread.join()
        return MutableLiveData(all_users.toList())
    }
}