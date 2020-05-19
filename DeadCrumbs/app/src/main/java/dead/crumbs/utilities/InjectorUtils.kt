package dead.crumbs.utilities

import android.location.Location
import dead.crumbs.data.Database
import dead.crumbs.data.LocationRepository
import dead.crumbs.data.RSSIRepository
import dead.crumbs.data.*
import dead.crumbs.ui.SingletonMapsViewModelFactory
import dead.crumbs.ui.RSSIViewModelFactory

object InjectorUtils {

    // This will be called from RSSIsActivity
    fun provideRSSIViewModelFactory(): RSSIViewModelFactory {
        // ViewModelFactory needs a repository, which in turn needs a DAO from a database
        // The whole dependency tree is constructed right here, in one place
        val rssiRepository = RSSIRepository.getInstance(Database.getInstance().rssiDao)
        val userRepository = UserRepository.getInstance(Database.getInstance().userDao)
        return RSSIViewModelFactory(rssiRepository, userRepository)
    }

    var singletonMapsViewModelFactory: SingletonMapsViewModelFactory? = null

    // This provides the same instance of MapsViewModelFactory each time.
    fun singletonProvideMapsViewModelFactory(): SingletonMapsViewModelFactory {
        if (singletonMapsViewModelFactory == null)
        {
            // ViewModelFactory needs a repository, which in turn needs a DAO from a database
            // The whole dependency tree is constructed right here, in one place
            val locationRepository = LocationRepository.getInstance(Database.getInstance().locationDao)
            val userRepository = UserRepository.getInstance(Database.getInstance().userDao)
            singletonMapsViewModelFactory = SingletonMapsViewModelFactory(locationRepository, userRepository)
        }
        return singletonMapsViewModelFactory as SingletonMapsViewModelFactory
    }
}