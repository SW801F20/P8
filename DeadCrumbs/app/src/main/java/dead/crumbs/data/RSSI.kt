package dead.crumbs.data

data class RSSI(val bluetooth_rssi : Double, val wifi_rssi : Double, val target_device : String){

    override fun toString(): String {
        return "$target_device: bluetooth: $bluetooth_rssi, wi-fi: $wifi_rssi"
    }
}