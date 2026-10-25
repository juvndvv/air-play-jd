package io.github.jqssun.airplay.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.net.Inet4Address
import java.net.NetworkInterface

private val lanTransports = intArrayOf(NetworkCapabilities.TRANSPORT_WIFI, NetworkCapabilities.TRANSPORT_ETHERNET)

private fun ConnectivityManager.hasLan(except: Network? = null): Boolean = allNetworks.any { n ->
    n != except && getNetworkCapabilities(n)?.let { caps -> lanTransports.any(caps::hasTransport) } == true
}

@Composable
fun rememberLanAvailable(): Boolean {
    val context = LocalContext.current
    val cm = remember(context) { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    var available by remember { mutableStateOf(cm.hasLan()) }
    DisposableEffect(cm) {
        val request = NetworkRequest.Builder().apply { lanTransports.forEach(::addTransportType) }.build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { available = true }
            override fun onLost(network: Network) { available = cm.hasLan(except = network) }
        }
        cm.registerNetworkCallback(request, callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }
    return available
}

fun localIpv4(): String? = runCatching {
    NetworkInterface.getNetworkInterfaces().toList()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.toList() }
        .firstOrNull { it is Inet4Address && it.isSiteLocalAddress }
        ?.hostAddress
}.getOrNull()
