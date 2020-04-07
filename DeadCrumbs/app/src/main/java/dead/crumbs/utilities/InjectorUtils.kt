package dead.crumbs.utilities

import dead.crumbs.data.FakeDatabase
import dead.crumbs.data.MapsRepository
import dead.crumbs.data.RSSIRepository
import dead.crumbs.ui.MapsViewModelFactory
import dead.crumbs.ui.RSSIViewModelFactory

object InjectorUtils {

    // This will be called from RSSIsActivity
    fun provideRSSIViewModelFactory(): RSSIViewModelFactory {
        // ViewModelFactory needs a repository, which in turn needs a DAO from a database
        // The whole dependency tree is constructed right here, in one place
        val rssiRepository = RSSIRepository.getInstance(FakeDatabase.getInstance().rssiDao)
        return RSSIViewModelFactory(rssiRepository)
    }

    // This will be called from ?
    fun provideMapsViewModelFactory(): MapsViewModelFactory {
        // ViewModelFactory needs a repository, which in turn needs a DAO from a database
        // The whole dependency tree is constructed right here, in one place
        val mapsRepository = MapsRepository.getInstance(FakeDatabase.getInstance().mapsDao)
        return MapsViewModelFactory(mapsRepository)
    }
}