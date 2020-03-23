package dead.crumbs.data

data class BluetoothRSSI(val bluetooth_rssi : Short, val target_mac_address : String){

    override fun toString(): String {
        return "$target_mac_address: bluetooth: $bluetooth_rssi"
    }
}