package dead.crumbs.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dead.crumbs.R
import dead.crumbs.data.RSSI
import dead.crumbs.utilities.InjectorUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            textView.text = stringBuilder.toString()
        })

        // When button is clicked, instantiate a rssi and add it to DB through the ViewModel
        button.setOnClickListener {
            val rssi_temp : RSSI = RSSI(1.0, 1.0, "hello")
            viewModel.addRSSI(rssi_temp)
            textView.text = rssi_temp.toString()
            /*
            val rssi = RSSI(editText_rssi.text.toString(), editText_author.text.toString())
            viewModel.addrssi(rssi)
            editText_rssi.setText("")
            editText_author.setText("")
            */
        }
    }

}