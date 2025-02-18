package dev.shadoe.fork

import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.IBinder
import android.system.OsConstants
import android.util.Log

class SplitTunService : VpnService() {
    companion object {
        const val ACTION_START = "dev.shadoe.fork.ACTION_START"
        const val ACTION_STOP = "dev.shadoe.fork.ACTION_STOP"
        private const val LOG_TAG = "SplitTunService"
    }

    private lateinit var connectivityManager: ConnectivityManager
    private var cellularNetwork: Network? = null

    private val networkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(LOG_TAG, "$network")
            cellularNetwork = network
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            cellularNetwork = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "service created")
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()
        connectivityManager.registerNetworkCallback(
            networkRequest, networkCallback
        )
    }

    override fun onDestroy() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?, flags: Int, startId: Int
    ): Int {
        return when (intent?.action) {
            ACTION_START -> {
                Log.d(LOG_TAG, "starting from app")
                buildVPN()
                START_STICKY
            }

            ACTION_STOP -> {
                Log.d(LOG_TAG, "stopping")
                stopSelf()
                START_NOT_STICKY
            }

            SERVICE_INTERFACE -> {
                Log.d(LOG_TAG, "starting from system settings")
                buildVPN()
                START_STICKY
            }

            else -> {
                Log.d(LOG_TAG, "starting after OOM")
                buildVPN()
                START_STICKY
            }
        }
    }

    fun buildVPN() {
        val descriptor = Builder()
            .addAddress("192.168.2.2", 24)
            .addDnsServer("192.168.1.1")
            .allowFamily(OsConstants.AF_INET)
            .allowFamily(OsConstants.AF_INET6)
            .addAllowedApplication("com.twitter.android")
//            .setUnderlyingNetworks(arrayOf(cellularNetwork))
            .setSession("Fork").establish()
    }
}