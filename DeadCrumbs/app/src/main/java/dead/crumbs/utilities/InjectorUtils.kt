package dead.crumbs.utilities

import dead.crumbs.data.Database
import dead.crumbs.data.LocationRepository
import dead.crumbs.data.MapsRepository
import dead.crumbs.data.RSSIRepository
import dead.crumbs.ui.LocationViewModelFactory
import dead.crumbs.ui.SingletonMapsViewModelFactory
import dead.crumbs.ui.RSSIViewModelFactory

object InjectorUtils {

    // This will be called from RSSIsActivity
    fun provideRSSIViewModelFactory(): RSSIViewModelFactory {
        // ViewModelFactory needs a repository, which in turn needs a DAO from a database
        // The whole dependency tree is constructed right here, in one place
        val rssiRepository = RSSIRepository.getInstance(Database.getInstance().rssiDao)
        return RSSIViewModelFactory(rssiRepository)
    }

    var singletonMapsViewModelFactory: SingletonMapsViewModelFactory? = null

    // This provides the same instance of MapsViewModelFactory each time.
    fun singletonProvideMapsViewModelFactory(): SingletonMapsViewModelFactory {
        if (singletonMapsViewModelFactory == null)
        {
            // ViewModelFactory needs a repository, which in turn needs a DAO from a database
            // The whole dependency tree is constructed right here, in one place
            val mapsRepository = MapsRepository.getInstance(Database.getInstance().mapsDao)
            singletonMapsViewModelFactory = SingletonMapsViewModelFactory(mapsRepository)
        }
        return singletonMapsViewModelFactory as SingletonMapsViewModelFactory
    }
/*
    fun provideLocation(): LocationViewModelFactory{
        val locationRepository = LocationRepository.getInstance(Database.getInstance().locationDao)
        return LocationViewModelFactory(locationRepository)
    }*/
}