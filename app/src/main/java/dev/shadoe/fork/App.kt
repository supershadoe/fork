package dev.shadoe.fork

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class VpnPermissionContract : ActivityResultContract<Intent, Boolean>() {
    override fun createIntent(context: Context, input: Intent): Intent {
        return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val canStartVpn = rememberSaveable { mutableStateOf(false) }
    val permissionRequestIntent = remember { mutableStateOf<Intent?>(null) }
    val vpnPermissionRequester = rememberLauncherForActivityResult(contract = VpnPermissionContract()) { canStartVpn.value = true }
    LaunchedEffect(Unit) {
        permissionRequestIntent.value = VpnService.prepare(context)
        if (permissionRequestIntent.value == null) {
            canStartVpn.value = true
        }
    }
    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier.padding(it).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Hello, world")
                ElevatedButton(onClick = {
                    if (canStartVpn.value) {
                        val intent = Intent(context, SplitTunService::class.java).apply {
                            action = SplitTunService.ACTION_START
                        }
                        context.startService(intent)
                    } else {
                        vpnPermissionRequester.launch(permissionRequestIntent.value!!)
                    }
                }) {
                    Text("Start VPN")
                }
            }
        }
    }
}