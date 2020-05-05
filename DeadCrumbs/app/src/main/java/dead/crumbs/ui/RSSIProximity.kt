package dead.crumbs.ui

import kotlin.math.exp

class RSSIProximity {

    //coefficients from exponential model fitting on recorded data set (with side by side rssi to distance data * 2)
    fun distanceFromRSSI(rssi: Double): Double{
        return 0.006829659881992 * exp(-0.11713074201511 * rssi)
    }
}