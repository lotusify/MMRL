package com.dergoogler.mmrl.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.datastore.model.UserPreferences
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.platform.Platform.Companion.putPlatform
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.putModId
import com.dergoogler.mmrl.platform.model.ModuleConfig.Companion.asModuleConfig
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.topjohnwu.superuser.Shell

val Float.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }

val Long.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }


inline fun <T> withNewRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
    block: Shell.() -> T,
): T {
    return createRootShell(globalMnt, debug, commands).use(block)
}

fun createRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
): Shell {
    Shell.enableVerboseLogging = debug
    val builder = Shell.Builder.create()
    if (globalMnt) {
        builder.setFlags(Shell.FLAG_MOUNT_MASTER)
    }
    return builder.build(*commands)
}

internal val WebUIXPackageName = "com.dergoogler.mmrl.wx${if (BuildConfig.DEBUG) ".debug" else ""}"

fun UserPreferences.launchWebUI(context: Context, modId: ModId) {
    val moduleConfig = modId.asModuleConfig
    
    // Determine which WebUI engine to use
    val engineToUse = when (webuiEngine) {
        WebUIEngine.PREFER_MODULE -> {
            // Check module's preferred engine, fallback to WebUI X (built-in)
            when (moduleConfig.getWebuiEngine(context)) {
                "wx" -> WebUIEngine.WX
                "ksu" -> WebUIEngine.KSU
                else -> WebUIEngine.WX // Default to WebUI X (built-in)
            }
        }
        else -> webuiEngine
    }
    
    when (engineToUse) {
        WebUIEngine.WX -> {
            // Launch built-in WebUI X (WXActivity)
            val intent = Intent(context, com.dergoogler.mmrl.webui.activity.WXActivity::class.java).apply {
                putModId(modId)
                putPlatform(workingMode.toPlatform())
            }
            context.startActivity(intent)
        }
        WebUIEngine.KSU -> {
            // Launch KSU manager WebUI (external app like KernelSU, KernelSU-Next, SukiSU Ultra)
            val intent = Intent().apply {
                // Try to launch KSU manager's WebUI
                action = "android.intent.action.VIEW"
                setData(android.net.Uri.parse("ksu://webui/${modId.id}"))
                // Add fallback for different KSU managers
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "KSU Manager not found or doesn't support WebUI. Please install KernelSU, KernelSU-Next, or SukiSU Ultra.", Toast.LENGTH_LONG).show()
            }
        }
        WebUIEngine.PREFER_MODULE -> {
            // This should never happen as PREFER_MODULE is resolved above
            // Fallback to WebUI X
            val intent = Intent(context, com.dergoogler.mmrl.webui.activity.WXActivity::class.java).apply {
                putModId(modId)
                putPlatform(workingMode.toPlatform())
            }
            context.startActivity(intent)
        }
    }
}