package dead.crumbs.data

data class RSSIDist(val my_mac: String, val target_mac_address : String, val rssi_dist : Double, val timestamp: String){

    override fun toString(): String {
        return "$target_mac_address: bluetooth: $rssi_dist"
    }
}