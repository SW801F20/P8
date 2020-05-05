package dead.crumbs.data.DAO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.swagger.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location
import io.swagger.client.models.User
import kotlin.concurrent.thread
//This class should be the one that calls the api
class User_DAO (val client : DeadCrumbsApi) {
    fun getUser(userName: String): LiveData<User>{
        var user : User? = null
        val thread = thread (start = true){
            user = client.getUser(userName)
        }
        thread.join()
        if(user == null){
            //throw error here
            return MutableLiveData(user)
        }else{
            return MutableLiveData(user)
        }
    }

    fun getUsers(): LiveData<List<User>>{
        var all_users = arrayOf<User>()
        //operations over the internet should happen on a thread
        val thread = thread{
            all_users = client.getUsers()
        }
        //thread.start()
        //Wait for the action to complete
        thread.join()
        return MutableLiveData(all_users.toList())
    }
}