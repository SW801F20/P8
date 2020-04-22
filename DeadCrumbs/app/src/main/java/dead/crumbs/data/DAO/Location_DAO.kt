package dead.crumbs.data.DAO

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.net.URL

//This class should be the one that calls the api
class Location_DAO{
    private val client = OkHttpClient()

    fun getLocation(){
        val result = URL("130.225.57.95:8393/swagger/index.html/Users").readText()
        Log.v("response", "response: " + result.toString())
    }

    fun postLocation(){

        run("130.225.57.95:8393/swagger/index.html/Location")//130.225.57.95:8393/swagger/index.html/Location
    }

    fun run(url: String){
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body?.string()) // used to be body()
        })
    }

}