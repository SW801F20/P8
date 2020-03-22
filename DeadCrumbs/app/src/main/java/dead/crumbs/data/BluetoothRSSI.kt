package dead.crumbs.data

data class BluetoothRSSI(val bluetooth_rssi : Short, val target_device : String){

    override fun toString(): String {
        return "$target_device: bluetooth: $bluetooth_rssi"
    }
}