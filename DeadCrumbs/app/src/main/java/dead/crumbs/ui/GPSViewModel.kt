package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient

class GPSViewModel () : ViewModel(){

    val PERMISSION_ID = 42;
    lateinit var mFusedLocationClient: FusedLocationProviderClient




}