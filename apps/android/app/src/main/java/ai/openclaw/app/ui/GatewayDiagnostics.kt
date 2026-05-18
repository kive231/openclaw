package ai.openclaw.app.ui
import ai.openclaw.app.R

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import ai.openclaw.app.BuildConfig
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast

internal fun openClawAndroidVersionLabel(): String {
  val versionName = BuildConfig.VERSION_NAME.trim().ifEmpty { "dev" }
  return if (BuildConfig.DEBUG && !versionName.contains("dev", ignoreCase = true)) {
    "$versionName-dev"
  } else {
    versionName
  }
}

internal fun gatewayStatusForDisplay(statusText: String, offlineLabel: String = "offline"): String = statusText.trim().ifEmpty { offlineLabel }

internal fun gatewayStatusHasDiagnostics(statusText: String): Boolean {
  val lower = gatewayStatusForDisplay(statusText).lowercase()
  return lower != "offline" && !lower.contains("connecting")
}

internal fun gatewayStatusLooksLikePairing(statusText: String): Boolean {
  val lower = gatewayStatusForDisplay(statusText).lowercase()
  return lower.contains("pair") || lower.contains("approve")
}

internal fun buildGatewayDiagnosticsReport(
  screen: String,
  gatewayAddress: String,
  statusText: String,
  deviceNameLabel: String = "OpenClaw",
  offlineLabel: String = "offline",
): String {
  val device =
    listOfNotNull(Build.MANUFACTURER, Build.MODEL)
      .joinToString(" ")
      .trim()
      .ifEmpty { deviceNameLabel }
  val androidVersion =
    Build.VERSION.RELEASE
      ?.trim()
      .orEmpty()
      .ifEmpty { Build.VERSION.SDK_INT.toString() }
  val endpoint = gatewayAddress.trim().ifEmpty { "unknown" }
  val status = gatewayStatusForDisplay(statusText, offlineLabel = offlineLabel)
  return """
    Help diagnose this OpenClaw Android gateway connection failure.

    Please:
    - pick one route only: same machine, same LAN, Tailscale, or public URL
    - classify this as pairing/auth, TLS trust, wrong advertised route, wrong address/port, or gateway down
    - remember: Tailscale/public mobile routes require wss:// or Tailscale Serve; ws:// is loopback-only
    - quote the exact app status/error below
    - tell me whether `openclaw devices list` should show a pending pairing request
    - if more signal is needed, ask for `openclaw qr --json`, `openclaw devices list`, and `openclaw nodes status`
    - give the next exact command or tap

    Debug info:
    - screen: $screen
    - app version: ${openClawAndroidVersionLabel()}
    - device: $device
    - android: $androidVersion (SDK ${Build.VERSION.SDK_INT})
    - gateway address: $endpoint
    - status/error: $status
    """.trimIndent()
}

internal fun copyGatewayDiagnosticsReport(
  context: Context,
  screen: String,
  gatewayAddress: String,
  statusText: String,
) {
  val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
  val deviceName = listOfNotNull(Build.MANUFACTURER, Build.MODEL).joinToString(" ").trim().ifEmpty { "OpenClaw" }
  val report = buildGatewayDiagnosticsReport(
    screen = screen,
    gatewayAddress = gatewayAddress,
    statusText = statusText,
    deviceNameLabel = deviceName,
    offlineLabel = context.getString(R.string.offline_status),
  )
  clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.diag_title), report))
  Toast.makeText(context, context.getString(R.string.copied_diag), Toast.LENGTH_SHORT).show()
}
