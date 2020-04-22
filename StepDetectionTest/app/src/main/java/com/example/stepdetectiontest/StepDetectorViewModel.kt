package com.example.stepdetectiontest

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepDetectorViewModel : ViewModel() {
    public var stepCounter : Int = -1
    public var stepDetectorCount : Int = 0
    public var stepCounterInitial : Int = 0
    public var scarletDistSum : Double = 0.0
    public var simpleDistSum : Double = 0.0
    public var weinbergDistSum : Double = 0.0
    
    public var ourStepCounter : Int = 0

    override fun onCleared() {
        super.onCleared()
        Toast.makeText(null, "ViewModel onCleared", Toast.LENGTH_SHORT).show()
    }

    // From https://developer.android.com/topic/libraries/architecture/viewmodel.html

//    private val users: MutableLiveData<List<User>> by lazy {
//        MutableLiveData().also {
//            loadUsers()
//        }
//    }
//
//
//    fun getUsers(): LiveData<List<User>> {
//        return users
//    }
//
//    private fun loadUsers() {
//        // Do an asynchronous operation to fetch users.
//    }


}