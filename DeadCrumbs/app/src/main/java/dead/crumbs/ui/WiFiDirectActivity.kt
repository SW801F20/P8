package dead.crumbs.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import dead.crumbs.R
import dead.crumbs.utilities.InjectorUtils


class WiFiDirectActivity(): AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wifi_direct_activity)
    }

    fun onPressDiscover(view: View){
        //Start discovery of wifi-direct devices

    }


}