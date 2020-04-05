package dead.crumbs.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dead.crumbs.R
import dead.crumbs.data.RSSI
import dead.crumbs.utilities.InjectorUtils
import io.swagger.client.models.Location
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.headline_textview
import kotlinx.android.synthetic.main.location_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeGetLocationsView()
    }


    private fun initializeGetLocationsView(){
        setContentView(R.layout.location_main)

        val factory = InjectorUtils.provideLocationViewModelFactory()
        val viewModel = ViewModelProviders.of(this, factory)
            .get(LocationViewModel::class.java)


        //below is just shitty code for debugging
        viewModel.getLocations().observe(this, Observer { Locations ->
            val stringBuilder = StringBuilder()
            Locations.forEach { location ->
                stringBuilder.append("$location\n\n")
            }
            all_localtions_textview.text = stringBuilder.toString()
        })

        post_btn.setOnClickListener{
            val newLocation = Location(1, 1, "Some rssi value")
            viewModel.addLocation(newLocation)
        }

        delete_btn.setOnClickListener{
            viewModel.deleteLocation(1)
        }

        get_btn.setOnClickListener{
            val loc = viewModel.getLocation(10)
            all_localtions_textview.text = loc.value.toString()
        }

        get_all_btn.setOnClickListener{
            viewModel.getLocations().observe(this, Observer { Locations ->
                val stringBuilder = StringBuilder()
                Locations.forEach { location ->
                    stringBuilder.append("$location\n\n")
                }
                all_localtions_textview.text = stringBuilder.toString()
            })
        }
    }

    private fun initializeRSSIView(){
        setContentView(R.layout.activity_main)
        initializeUi()
    }

    private fun initializeUi() {
        // Get the rssisViewModelFactory with all of it's dependencies constructed
        val factory = InjectorUtils.provideRSSIViewModelFactory()
        // Use ViewModelProviders class to create / get already created rssisViewModel
        // for this view (activity)
        val viewModel = ViewModelProviders.of(this, factory)
            .get(RSSIViewModel::class.java)

        // Observing LiveData from the RSSIViewModel which in turn observes
        // LiveData from the repository, which observes LiveData from the DAO ☺
        viewModel.getRSSIs().observe(this, Observer { RSSIs ->
            val stringBuilder = StringBuilder()
            RSSIs.forEach { rssi ->
                stringBuilder.append("$rssi\n\n")
            }
            headline_textview.text = stringBuilder.toString()
        })

        // When button is clicked, instantiate a rssi and add it to DB through the ViewModel
        button.setOnClickListener {
            val rssi_temp : RSSI = RSSI(1.0, 1.0, "hello")
            viewModel.addRSSI(rssi_temp)
            headline_textview.text = rssi_temp.toString()
            /*
            val rssi = RSSI(editText_rssi.text.toString(), editText_author.text.toString())
            viewModel.addrssi(rssi)
            editText_rssi.setText("")
            editText_author.setText("")
            */
        }
    }

}