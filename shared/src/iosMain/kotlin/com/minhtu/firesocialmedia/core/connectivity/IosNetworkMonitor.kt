package com.minhtu.firesocialmedia.core.connectivity

import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.*

class IosNetworkMonitor : NetworkMonitor {
    override val isOnline: Flow<Boolean> = callbackFlow {
        val monitor = NWPathMonitor()
        val queue = dispatch_queue_create("com.minhtu.firesocialmedia.network", null)
        monitor.setUpdateHandler { path: nw_path_t? ->
            val hasInternet = path != null &&
                    (nw_path_get_status(path) == nw_path_status_satisfied)
            trySend(hasInternet)
        }
        monitor.start(queue)

        awaitClose {
            monitor.cancel()
        }
    }.distinctUntilChanged()
}



