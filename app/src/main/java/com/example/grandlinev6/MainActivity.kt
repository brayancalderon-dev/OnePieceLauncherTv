package com.example.grandlinev6

import android.content.ActivityNotFoundException
import androidx.compose.foundation.clickable
import android.provider.Settings
import android.content.ComponentName
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.grandlinev6.ui.theme.GrandLineV6Theme
import java.text.NumberFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val Ink = Color(0xFF2B1B10)
private val MarineGold = Color(0xFFE8B64E)
private val DarkNavy = Color(0xFF071A33)
private val Parchment = Color(0xFFE8D2A8)

private const val PREFS_NAME = "grand_line_v6"
private const val PREF_PRIMARY = "primary_packages"
private const val PREF_ENVIRONMENT = "environment_mode"
private const val PREF_SOUND = "sound_enabled"

private enum class EnvironmentMode(val label: String) {
    AUTO("AUTOMÁTICO"),
    DAY("DÍA"),
    SUNSET("ATARDECER"),
    NIGHT("NOCHE");

    companion object {
        fun fromStored(value: String?): EnvironmentMode =
            values().firstOrNull { it.name == value } ?: AUTO
    }
}

private enum class LauncherPanel {
    SETTINGS,
    ALL_APPS,
    ENTRIES
}

private data class InstalledLauncherApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable,
    val launchCategory: String,
    val systemCategory: Int
)

/**
 * Silueta irregular para recortar textura, sombreado y sombra del papel.
 * De esta manera no aparece un rectángulo translúcido detrás del cartel.
 */
private object TornPaperShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.035f, h * 0.012f)
            lineTo(w * 0.12f, h * 0.006f)
            lineTo(w * 0.19f, h * 0.018f)
            lineTo(w * 0.29f, h * 0.006f)
            lineTo(w * 0.39f, h * 0.014f)
            lineTo(w * 0.50f, h * 0.004f)
            lineTo(w * 0.61f, h * 0.016f)
            lineTo(w * 0.72f, h * 0.007f)
            lineTo(w * 0.83f, h * 0.018f)
            lineTo(w * 0.96f, h * 0.008f)
            lineTo(w * 0.988f, h * 0.10f)
            lineTo(w * 0.978f, h * 0.19f)
            lineTo(w * 0.994f, h * 0.29f)
            lineTo(w * 0.981f, h * 0.40f)
            lineTo(w * 0.996f, h * 0.51f)
            lineTo(w * 0.982f, h * 0.63f)
            lineTo(w * 0.994f, h * 0.75f)
            lineTo(w * 0.982f, h * 0.88f)
            lineTo(w * 0.994f, h * 0.985f)
            lineTo(w * 0.89f, h * 0.978f)
            lineTo(w * 0.78f, h * 0.994f)
            lineTo(w * 0.67f, h * 0.982f)
            lineTo(w * 0.55f, h * 0.996f)
            lineTo(w * 0.43f, h * 0.982f)
            lineTo(w * 0.31f, h * 0.995f)
            lineTo(w * 0.20f, h * 0.981f)
            lineTo(w * 0.08f, h * 0.994f)
            lineTo(w * 0.012f, h * 0.965f)
            lineTo(w * 0.022f, h * 0.86f)
            lineTo(w * 0.008f, h * 0.75f)
            lineTo(w * 0.021f, h * 0.63f)
            lineTo(w * 0.006f, h * 0.52f)
            lineTo(w * 0.019f, h * 0.40f)
            lineTo(w * 0.006f, h * 0.29f)
            lineTo(w * 0.019f, h * 0.18f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Consulta únicamente apps que ofrecen una actividad de launcher Android TV
 * o launcher Android. El bloque <queries> del Manifest habilita esta consulta
 * bajo las reglas de visibilidad de paquetes de Android 11+.
 */
private fun queryInstalledLauncherApps(context: Context): List<InstalledLauncherApp> {
    val pm = context.packageManager
    val ownPackage = context.packageName
    val found = linkedMapOf<String, InstalledLauncherApp>()

    val categories = listOf(
        Intent.CATEGORY_LEANBACK_LAUNCHER,
        Intent.CATEGORY_LAUNCHER
    )

    for (category in categories) {
        val query = Intent(Intent.ACTION_MAIN).addCategory(category)
        val resolved = try {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(query, 0)
        } catch (_: Exception) {
            emptyList()
        }

        for (resolveInfo in resolved) {
            val activity = resolveInfo.activityInfo ?: continue
            val packageName = activity.packageName ?: continue
            if (packageName == ownPackage || found.containsKey(packageName)) continue

            val label = try {
                resolveInfo.loadLabel(pm)?.toString()?.trim().orEmpty()
            } catch (_: Exception) {
                ""
            }.ifBlank { packageName.substringAfterLast('.') }

            val icon = try {
                resolveInfo.loadIcon(pm)
            } catch (_: Exception) {
                try {
                    pm.getApplicationIcon(packageName)
                } catch (_: Exception) {
                    continue
                }
            }

            val systemCategory = try {
                pm.getApplicationInfo(packageName, 0).category
            } catch (_: Exception) {
                ApplicationInfo.CATEGORY_UNDEFINED
            }
            found[packageName] = InstalledLauncherApp(
                packageName = packageName,
                activityName = activity.name,
                label = label,
                icon = icon,
                launchCategory = category,
                systemCategory = systemCategory
            )
        }
    }

    return found.values.sortedBy { it.label.lowercase(Locale.getDefault()) }
}

private enum class AppKind { ANIME, VIDEO, MUSIC, GAME, SOCIAL, PRODUCTIVITY, OTHER }

/** Categoría automática: primero identifica paquetes conocidos y después usa la categoría declarada en Android. */
private fun detectAppKind(app: InstalledLauncherApp): AppKind {
    val pkg = app.packageName.lowercase(Locale.ROOT)
    val label = app.label.lowercase(Locale.ROOT)

    if (listOf("crunchyroll", "funimation", "animeflv", "animepahe").any { pkg.contains(it) }) return AppKind.ANIME
    if (listOf("netflix", "primevideo", "amazonvideo", "disney", "youtube", "twitch", "plex", "hbomax", "max.", "paramount", "peacock", "tubi", "pluto", "stremio", "kodi", "vlc").any { pkg.contains(it) }) return AppKind.VIDEO
    if (listOf("spotify", "deezer", "tidal", "amazonmusic", "soundcloud", "pandora", "music.youtube", "applemusic").any { pkg.contains(it) }) return AppKind.MUSIC
    if (listOf("game", "games", "steam", "xbox", "playstation", "retroarch").any { pkg.contains(it) } || app.systemCategory == ApplicationInfo.CATEGORY_GAME) return AppKind.GAME

    return when (app.systemCategory) {
        ApplicationInfo.CATEGORY_VIDEO -> AppKind.VIDEO
        ApplicationInfo.CATEGORY_AUDIO -> AppKind.MUSIC
        ApplicationInfo.CATEGORY_SOCIAL -> AppKind.SOCIAL
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppKind.PRODUCTIVITY
        else -> when {
            listOf("tv", "video", "stream", "cine", "movie", "anime").any { label.contains(it) } -> AppKind.VIDEO
            listOf("music", "radio", "audio").any { label.contains(it) } -> AppKind.MUSIC
            else -> AppKind.OTHER
        }
    }
}

private fun taglineFor(app: InstalledLauncherApp): String = when (detectAppKind(app)) {
    AppKind.ANIME -> "¡Despierta tu Haki otaku!"
    AppKind.VIDEO -> "¡Un video más, capitán!"
    AppKind.MUSIC -> "¡La banda sonora del Sunny!"
    AppKind.GAME -> "¡Demuestra tu Haki, capitán!"
    AppKind.SOCIAL -> "¡Noticias de todos los mares!"
    AppKind.PRODUCTIVITY -> "¡Prepara el Sunny para zarpar!"
    AppKind.OTHER -> "¡Una nueva travesía te espera!"
}

private fun defaultPrimaryPackages(apps: List<InstalledLauncherApp>): List<String> {
    val result = mutableListOf<String>()
    val priorities = listOf("netflix", "youtube", "prime video", "disney", "crunchyroll")

    for (priority in priorities) {
        apps.firstOrNull {
            it.label.contains(priority, ignoreCase = true) && it.packageName !in result
        }?.let { result.add(it.packageName) }
    }

    for (app in apps) {
        if (result.size >= 5) break
        if (app.packageName !in result) result.add(app.packageName)
    }
    return result.take(5)
}

private fun rewardFor(context: Context, packageName: String): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val key = "reward_$packageName"
    val existing = prefs.getString(key, null)
    if (existing != null) return existing

    val amount = Random.nextLong(100, 9_999) * 1_000_000L
    val formatted = NumberFormat.getIntegerInstance(Locale.US).format(amount)
    val value = "฿ $formatted-"
    prefs.edit().putString(key, value).apply()
    return value
}

private const val AMAZON_LAUNCHER_PACKAGE = "com.amazon.tv.launcher"

/**
 * On Fire OS, generic ACTION_SETTINGS can resolve back to the Amazon launcher.
 * Probe specific settings sections and deliberately avoid that package so opening
 * Settings does not trigger the Home redirect service again.
 */
private fun openFirstFireSettingsPage(context: Context, actions: List<String>, label: String) {
    val pm = context.packageManager
    for (action in actions) {
        val candidates = try {
            pm.queryIntentActivities(Intent(action), 0)
        } catch (_: Exception) {
            emptyList()
        }
        val target = candidates.firstOrNull { resolve ->
            resolve.activityInfo?.packageName?.let { it != AMAZON_LAUNCHER_PACKAGE } == true
        }?.activityInfo ?: continue

        val intent = Intent(action)
            .setClassName(target.packageName, target.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            // Try the next Fire OS settings subsection.
        }
    }
    Toast.makeText(context, "No se encontró la pantalla de $label en este Fire TV", Toast.LENGTH_LONG).show()
}

private fun openWifiSettings(context: Context) {
    openFirstFireSettingsPage(
        context,
        listOf(
            Settings.ACTION_WIFI_SETTINGS,
            Settings.ACTION_WIRELESS_SETTINGS,
            Settings.ACTION_DEVICE_INFO_SETTINGS
        ),
        "Wi-Fi"
    )
}

private fun openFireDeviceSettings(context: Context) {
    openFirstFireSettingsPage(
        context,
        listOf(
            Settings.ACTION_DEVICE_INFO_SETTINGS,
            Settings.ACTION_DISPLAY_SETTINGS,
            Settings.ACTION_APPLICATION_SETTINGS,
            Settings.ACTION_WIFI_SETTINGS,
            Settings.ACTION_DATE_SETTINGS
        ),
        "Ajustes del dispositivo"
    )
}

private fun isHomeRedirectEnabled(context: Context): Boolean {
    val component = ComponentName(context, HomeRedirectAccessibilityService::class.java).flattenToString()
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ).orEmpty()
    return enabledServices.split(':').any { it.equals(component, ignoreCase = true) }
}

/** Returns false when WRITE_SECURE_SETTINGS has not yet been granted over ADB. */
private fun setHomeRedirectEnabled(context: Context, enabled: Boolean): Boolean {
    if (context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
        return false
    }

    return try {
        val resolver = context.contentResolver
        val component = ComponentName(context, HomeRedirectAccessibilityService::class.java).flattenToString()
        val current = Settings.Secure.getString(
            resolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty().split(':').filter { it.isNotBlank() }.toMutableSet()

        if (enabled) current.add(component) else current.removeAll { it.equals(component, ignoreCase = true) }

        val updated = current.joinToString(":")
        val saved = Settings.Secure.putString(
            resolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            updated
        )
        if (!saved) return false

        Settings.Secure.putInt(
            resolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            if (current.isNotEmpty()) 1 else 0
        )
        true
    } catch (_: SecurityException) {
        false
    } catch (_: Exception) {
        false
    }
}

private fun openAccessibilitySettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {
        Toast.makeText(context, "Abre Fire TV → Configuración → Accesibilidad", Toast.LENGTH_LONG).show()
    }
}

private fun openMediaNotificationSettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {
        Toast.makeText(context, "Abre Fire TV → Configuración → Notificaciones → Acceso a notificaciones", Toast.LENGTH_LONG).show()
    }
}

private fun launchInstalledApp(context: Context, app: InstalledLauncherApp) {
    try {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(app.launchCategory)
            .setClassName(app.packageName, app.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val fallback = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (fallback != null) {
            try {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo abrir ${app.label}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "La aplicación ya no está disponible", Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo abrir ${app.label}", Toast.LENGTH_SHORT).show()
    }
}

class MainActivity : ComponentActivity() {
    private var appRefreshVersion by mutableIntStateOf(0)

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GrandLineMediaBridge.loadSaved(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )

        setContent {
            GrandLineV6Theme {
                MaterialTheme {
                    GrandLineHome(
                        refreshVersion = appRefreshVersion,
                        onRefreshApps = { appRefreshVersion++ }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appRefreshVersion++
        GrandLineMediaBridge.loadSaved(this)
        GrandLineMediaBridge.refresh(this)
    }
}

@Composable
private fun GrandLineHome(refreshVersion: Int, onRefreshApps: () -> Unit) {
    val context = LocalContext.current
    val homeFocusRequester = remember { FocusRequester() }
    val topFocusRequesters = remember { List(2) { FocusRequester() } }
    val mediaFocusRequesters = remember { List(3) { FocusRequester() } }
    val mediaFocused = remember { mutableStateListOf(false, false, false) }
    val topFocused = remember { mutableStateListOf(false, false) }
    val bottomFocusRequesters = remember { List(3) { FocusRequester() } }
    val bottomFocused = remember { mutableStateListOf(false, false, false) }

    var installedApps by remember { mutableStateOf<List<InstalledLauncherApp>>(emptyList()) }
    var primaryPackages by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var focusOnTop by remember { mutableStateOf(false) }
    var focusedTopIndex by remember { mutableIntStateOf(1) }
    var focusOnMedia by remember { mutableStateOf(false) }
    var focusedMediaIndex by remember { mutableIntStateOf(1) }
    var focusOnBottom by remember { mutableStateOf(false) }
    var focusedBottomIndex by remember { mutableIntStateOf(1) }
    var homeRedirectEnabled by remember { mutableStateOf(isHomeRedirectEnabled(context)) }
    var activePanel by remember { mutableStateOf<LauncherPanel?>(null) }
    var pendingSlotIndex by remember { mutableStateOf<Int?>(null) }
    var environmentMode by remember { mutableStateOf(EnvironmentMode.AUTO) }
    var soundEnabled by remember { mutableStateOf(false) }
    var now by remember { mutableStateOf(ZonedDateTime.now(ZoneId.of("America/Guayaquil"))) }
    var loadingApps by remember { mutableStateOf(true) }

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        environmentMode = EnvironmentMode.fromStored(prefs.getString(PREF_ENVIRONMENT, "AUTO"))
        soundEnabled = prefs.getBoolean(PREF_SOUND, false)
        homeFocusRequester.requestFocus()
    }

    LaunchedEffect(refreshVersion) {
        loadingApps = true
        homeRedirectEnabled = isHomeRedirectEnabled(context)
        val found = withContext(Dispatchers.IO) { queryInstalledLauncherApps(context) }
        installedApps = found

        val stored = prefs.getString(PREF_PRIMARY, null)
        val storedPackages = stored
            ?.split("|")
            ?.filter { pkg -> pkg.isNotBlank() && found.any { it.packageName == pkg } }
            .orEmpty()

        val next = storedPackages.distinct().take(5).toMutableList()
        if (next.size < 5) {
            defaultPrimaryPackages(found).forEach { packageName ->
                if (next.size < 5 && packageName !in next) next.add(packageName)
            }
        }
        primaryPackages = next
        selectedIndex = selectedIndex.coerceIn(0, (next.size - 1).coerceAtLeast(0))
        prefs.edit().putString(PREF_PRIMARY, next.joinToString("|")).apply()
        loadingApps = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now(ZoneId.of("America/Guayaquil"))
            delay(30_000)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            GrandLineMediaBridge.refreshPosition()
            delay(1_000)
        }
    }

    val effectiveMode = when (environmentMode) {
        EnvironmentMode.AUTO -> when (now.hour) {
            in 6..16 -> EnvironmentMode.DAY
            in 17..19 -> EnvironmentMode.SUNSET
            else -> EnvironmentMode.NIGHT
        }
        else -> environmentMode
    }
    val nightAmount by animateFloatAsState(
        targetValue = if (effectiveMode == EnvironmentMode.NIGHT) 1f else 0f,
        animationSpec = tween(1_800, easing = FastOutSlowInEasing),
        label = "night-sky"
    )
    val dayAmount by animateFloatAsState(
        targetValue = if (effectiveMode == EnvironmentMode.DAY) 1f else 0f,
        animationSpec = tween(1_800, easing = FastOutSlowInEasing),
        label = "day-sky"
    )
    val sunsetAmount by animateFloatAsState(
        targetValue = if (effectiveMode == EnvironmentMode.SUNSET) 1f else 0f,
        animationSpec = tween(1_800, easing = FastOutSlowInEasing),
        label = "sunset-sky"
    )

    val primaryApps = primaryPackages.mapNotNull { packageName ->
        installedApps.firstOrNull { it.packageName == packageName }
    }
    val activeApp = primaryApps.getOrNull(selectedIndex)

    fun savePrimary(updated: List<String>) {
        primaryPackages = updated.distinct().take(5)
        selectedIndex = selectedIndex.coerceIn(0, (primaryPackages.size - 1).coerceAtLeast(0))
        prefs.edit().putString(PREF_PRIMARY, primaryPackages.joinToString("|")).apply()
    }

    fun playFocusSound() {
        if (!soundEnabled) return
        try {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 35)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 65)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                { tone.release() }, 120
            )
        } catch (_: Exception) {
            // El sonido es opcional; si el dispositivo no lo admite, se omite.
        }
    }

    fun restoreHomeFocus() {
        if (focusOnBottom) {
            bottomFocusRequesters[focusedBottomIndex].requestFocus()
        } else {
            homeFocusRequester.requestFocus()
        }
    }

    fun showTop(index: Int) {
        focusedTopIndex = index.coerceIn(0, 1)
        focusOnTop = true
        focusOnMedia = false
        focusOnBottom = false
        topFocusRequesters[focusedTopIndex].requestFocus()
        playFocusSound()
    }

    fun showMedia(index: Int) {
        focusedMediaIndex = index.coerceIn(0, 2)
        focusOnMedia = true
        focusOnTop = false
        focusOnBottom = false
        mediaFocusRequesters[focusedMediaIndex].requestFocus()
        playFocusSound()
    }

    fun showBottom(index: Int) {
        focusedBottomIndex = index.coerceIn(0, 2)
        focusOnBottom = true
        focusOnTop = false
        focusOnMedia = false
        bottomFocusRequesters[focusedBottomIndex].requestFocus()
        playFocusSound()
    }

    fun movePrimary(from: Int, delta: Int) {
        val to = from + delta
        if (to !in primaryPackages.indices) return
        val updated = primaryPackages.toMutableList()
        val item = updated.removeAt(from)
        updated.add(to, item)
        savePrimary(updated)
        selectedIndex = to
        playFocusSound()
    }

    fun assignAppToSlot(app: InstalledLauncherApp, slot: Int) {
        if (slot !in primaryPackages.indices) return
        if (app.packageName in primaryPackages && primaryPackages[slot] != app.packageName) {
            Toast.makeText(context, "Esa aplicación ya ocupa otro cartel", Toast.LENGTH_SHORT).show()
            return
        }
        val updated = primaryPackages.toMutableList()
        updated[slot] = app.packageName
        savePrimary(updated)
        selectedIndex = slot
        activePanel = LauncherPanel.SETTINGS
    }

    fun toggleHomeRedirect() {
        val desired = !homeRedirectEnabled
        if (setHomeRedirectEnabled(context, desired)) {
            homeRedirectEnabled = desired
            Toast.makeText(
                context,
                if (desired) "Redirección HOME activada" else "Redirección HOME desactivada",
                Toast.LENGTH_LONG
            ).show()
        } else {
            openAccessibilitySettings(context)
            Toast.makeText(
                context,
                "Activa GRAND LINE en Accesibilidad. Si esa pantalla no funciona, concede WRITE_SECURE_SETTINGS por ADB y vuelve aquí.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    BackHandler(enabled = activePanel != null) {
        activePanel = when (activePanel) {
            LauncherPanel.ALL_APPS -> if (pendingSlotIndex != null) LauncherPanel.SETTINGS else null
            else -> null
        }
        if (activePanel == null) {
            pendingSlotIndex = null
            restoreHomeFocus()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .focusRequester(homeFocusRequester)
            .onPreviewKeyEvent { event ->
                if (activePanel != null || event.type != KeyEventType.KeyDown) {
                    false
                } else {
                    when (event.key) {
                        Key.DirectionRight -> {
                            when {
                                focusOnTop -> showTop((focusedTopIndex + 1).coerceAtMost(1))
                                focusOnMedia -> showMedia((focusedMediaIndex + 1).coerceAtMost(2))
                                focusOnBottom -> showBottom((focusedBottomIndex + 1).coerceAtMost(2))
                                primaryApps.isNotEmpty() -> {
                                    selectedIndex = (selectedIndex + 1) % primaryApps.size
                                    playFocusSound()
                                }
                            }
                            true
                        }
                        Key.DirectionLeft -> {
                            when {
                                focusOnTop -> showTop((focusedTopIndex - 1).coerceAtLeast(0))
                                focusOnMedia -> showMedia((focusedMediaIndex - 1).coerceAtLeast(0))
                                focusOnBottom -> showBottom((focusedBottomIndex - 1).coerceAtLeast(0))
                                primaryApps.isNotEmpty() -> {
                                    selectedIndex = (selectedIndex - 1 + primaryApps.size) % primaryApps.size
                                    playFocusSound()
                                }
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            if (focusOnTop) {
                                showMedia(1)
                            } else if (focusOnMedia) {
                                focusOnMedia = false
                                homeFocusRequester.requestFocus()
                            } else if (!focusOnBottom) {
                                showBottom(1)
                            }
                            true
                        }
                        Key.DirectionUp -> {
                            if (focusOnBottom) {
                                focusOnBottom = false
                                homeFocusRequester.requestFocus()
                            } else if (focusOnMedia) {
                                showTop(0)
                            } else if (!focusOnTop) {
                                showTop(1)
                            }
                            true
                        }
                        Key.Enter, Key.DirectionCenter -> {
                            if (focusOnMedia) {
                                false // El botón de control enfocado procesa OK/Center.
                            } else if (!focusOnTop && !focusOnBottom && activeApp != null) {
                                launchInstalledApp(context, activeApp)
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                }
            }
            .focusable()
    ) {
        val cardWidth = maxWidth * 0.12f
        val cardHeight = maxHeight * 0.30f
        val carouselStep = maxWidth * 0.15f

        Image(
            painter = painterResource(id = R.drawable.grand_line_sea),
            contentDescription = "Mar y atardecer de Grand Line",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // El modo nocturno oscurece principalmente el cielo; conserva el mar visible.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.61f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF06162C).copy(alpha = 0.86f * nightAmount),
                        0.55f to Color(0xFF10294B).copy(alpha = 0.62f * nightAmount),
                        1f to Color.Transparent
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.61f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFFBDE3FF).copy(alpha = 0.22f * dayAmount),
                        0.65f to Color(0xFFDAF0FF).copy(alpha = 0.10f * dayAmount),
                        1f to Color.Transparent
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF4B1D28).copy(alpha = 0.30f * sunsetAmount),
                        0.55f to Color(0xFFFF9A3D).copy(alpha = 0.16f * sunsetAmount),
                        1f to Color.Transparent
                    )
                )
        )
        if (nightAmount > 0.02f) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.60f)
                    .align(Alignment.TopCenter)
            ) {
                val stars = listOf(
                    0.08f to 0.20f, 0.17f to 0.12f, 0.26f to 0.29f,
                    0.38f to 0.16f, 0.47f to 0.25f, 0.59f to 0.12f,
                    0.68f to 0.31f, 0.78f to 0.16f, 0.89f to 0.24f,
                    0.94f to 0.11f, 0.31f to 0.42f, 0.53f to 0.39f
                )
                stars.forEachIndexed { index, point ->
                    drawCircle(
                        color = Color.White.copy(alpha = (0.38f + (index % 3) * 0.15f) * nightAmount),
                        radius = if (index % 4 == 0) 2.dp.toPx() else 1.15.dp.toPx(),
                        center = Offset(size.width * point.first, size.height * point.second)
                    )
                }
                drawCircle(
                    color = Color(0xFFFFF4D8).copy(alpha = 0.12f * nightAmount),
                    radius = 25.dp.toPx(),
                    center = Offset(size.width * 0.82f, size.height * 0.48f)
                )
                drawCircle(
                    color = Color(0xFFFFF4D8).copy(alpha = 0.88f * nightAmount),
                    radius = 17.dp.toPx(),
                    center = Offset(size.width * 0.82f, size.height * 0.48f)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x44041630),
                        0.22f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1f to Color(0x99041630)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = maxWidth * 0.047f, vertical = maxHeight * 0.045f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = greetingFor(now.hour),
                    color = Color(0xFFFFD477),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            Color(0xE6001022), Offset(1f, 3f), 8f
                        )
                    )
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 57.sp,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                Color(0xE6001022), Offset(2f, 4f), 10f
                            )
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(48.dp)
                            .background(MarineGold)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            now.format(DateTimeFormatter.ofPattern("EEEE", Locale("es", "EC")))
                                .replaceFirstChar { it.titlecase(Locale("es", "EC")) },
                            color = Color(0xFFFFD477),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            now.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "EC"))),
                            color = Color(0xFFFFD477),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Log Pose abre los ajustes generales del Fire TV.
                Instrument(
                    label = "🧭",
                    contentDescription = "Ajustes del Fire TV",
                    focusRequester = topFocusRequesters[0],
                    onFocused = { topFocused[0] = it; if (it) { focusOnTop = true; focusOnMedia = false; focusOnBottom = false } },
                    onClick = { openFireDeviceSettings(context) }
                )
                // El instrumento del extremo derecho abre directamente Wi-Fi.
                Instrument(
                    label = "⌁",
                    contentDescription = "Configuración Wi-Fi",
                    focusRequester = topFocusRequesters[1],
                    onFocused = { topFocused[1] = it; if (it) { focusOnTop = true; focusOnMedia = false; focusOnBottom = false } },
                    onClick = { openWifiSettings(context) }
                )
            }
        }

        MediaPlayerWidget(
            width = maxWidth * 0.235f,
            height = maxHeight * 0.155f,
            focusRequesters = mediaFocusRequesters,
            onControlFocused = { index, focused ->
                mediaFocused[index] = focused
                if (focused) {
                    focusedMediaIndex = index
                    focusOnMedia = true
                    focusOnTop = false
                    focusOnBottom = false
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = maxWidth * 0.047f, top = maxHeight * 0.215f)
        )

        val activeApp = primaryApps.getOrNull(selectedIndex)
        if (activeApp != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = maxHeight * 0.235f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeApp.label,
                    color = Color(0xFFFFD36D),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            Color(0xF0001024), Offset(1f, 4f), 12f
                        )
                    )
                )
                Text(
                    text = taglineFor(activeApp),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            Color(0xF0001024), Offset(1f, 3f), 8f
                        )
                    )
                )
            }
        }

        // Coverflow: se dibujan cinco apps; posición, escala y sombra cambian con transición.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight * 1.30f)
                .align(Alignment.Center)
                .offset(y = maxHeight * 0.105f)
        ) {
            if (primaryApps.isEmpty()) {
                Text(
                    text = if (loadingApps) "Cargando aplicaciones..." else "No se encontraron aplicaciones de TV",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val visibleCount = minOf(5, primaryApps.size)
                val centerPosition = selectedIndex.coerceIn(0, visibleCount - 1)
                val relativePositions = when (visibleCount) {
                    5 -> -2..2
                    4 -> -1..2
                    3 -> -1..1
                    2 -> -1..0
                    else -> 0..0
                }
                for (relative in relativePositions) {
                    val appIndex = (centerPosition + relative + visibleCount) % visibleCount
                    val app = primaryApps[appIndex]
                    key(app.packageName) {
                        WantedPoster(
                            app = app,
                            reward = rewardFor(context, app.packageName),
                            width = cardWidth,
                            height = cardHeight,
                            relativePosition = relative,
                            horizontalStep = carouselStep,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = maxHeight * 0.045f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomLauncherAction(
                label = "⚓ ENTRADAS",
                focusRequester = bottomFocusRequesters[0],
                selected = focusOnBottom && focusedBottomIndex == 0,
                onFocused = { bottomFocused[0] = it }
            ) { activePanel = LauncherPanel.ENTRIES }

            BottomLauncherAction(
                label = "☰ AJUSTES",
                focusRequester = bottomFocusRequesters[1],
                selected = focusOnBottom && focusedBottomIndex == 1,
                onFocused = { bottomFocused[1] = it }
            ) { activePanel = LauncherPanel.SETTINGS }

            BottomLauncherAction(
                label = "▦ TODAS LAS APPS",
                focusRequester = bottomFocusRequesters[2],
                selected = focusOnBottom && focusedBottomIndex == 2,
                onFocused = { bottomFocused[2] = it }
            ) { activePanel = LauncherPanel.ALL_APPS }
        }

        Text(
            text = "← → NAVEGAR  ·  ↓ OPCIONES  ·  OK ABRIR",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 9.sp,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(Color.Black, Offset(1f, 2f), 5f)
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = maxWidth * 0.047f, bottom = 10.dp)
        )

        Text(
            text = "GRAND LINE ☠",
            color = Color.White,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(Color.Black, Offset(1f, 3f), 8f)
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = maxWidth * 0.045f, bottom = 8.dp)
        )
    }

    when (activePanel) {
        LauncherPanel.SETTINGS -> SettingsDialog(
            installedApps = installedApps,
            primaryPackages = primaryPackages,
            environmentMode = environmentMode,
            soundEnabled = soundEnabled,
            homeRedirectEnabled = homeRedirectEnabled,
            onToggleHomeRedirect = { toggleHomeRedirect() },
            onOpenMediaAccess = { openMediaNotificationSettings(context) },
            onClose = { activePanel = null; pendingSlotIndex = null; restoreHomeFocus() },
            onMove = { from, delta -> movePrimary(from, delta) },
            onChange = { slot -> pendingSlotIndex = slot; activePanel = LauncherPanel.ALL_APPS },
            onEnvironment = {
                environmentMode = it
                prefs.edit().putString(PREF_ENVIRONMENT, it.name).apply()
            },
            onSound = {
                soundEnabled = !soundEnabled
                prefs.edit().putBoolean(PREF_SOUND, soundEnabled).apply()
                playFocusSound()
            },
            onRefresh = onRefreshApps
        )

        LauncherPanel.ALL_APPS -> AllAppsDialog(
            apps = installedApps,
            primaryPackages = primaryPackages,
            selectingForSlot = pendingSlotIndex,
            onClose = {
                activePanel = if (pendingSlotIndex != null) LauncherPanel.SETTINGS else null
                if (activePanel == null) restoreHomeFocus()
            },
            onAppClick = { app ->
                val slot = pendingSlotIndex
                if (slot != null) {
                    assignAppToSlot(app, slot)
                    pendingSlotIndex = null
                    activePanel = LauncherPanel.SETTINGS
                } else {
                    activePanel = null
                    restoreHomeFocus()
                    launchInstalledApp(context, app)
                }
            }
        )

        LauncherPanel.ENTRIES -> EntriesDialog(
            onClose = { activePanel = null; restoreHomeFocus() }
        )

        null -> Unit
    }
}

private fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "¡Buenos días, nakama!"
    in 12..18 -> "¡Buenas tardes, nakama!"
    else -> "Noche de travesía, nakama"
}

@Composable
private fun Instrument(
    label: String,
    contentDescription: String,
    focusRequester: FocusRequester,
    onFocused: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(54.dp)
            .shadow(if (isFocused) 10.dp else 5.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF6F8796), Color(0xFF18344F), Color(0xFF081A30))
                )
            )
            .border(
                width = if (isFocused) 4.dp else 3.dp,
                color = if (isFocused) Color(0xFFFFD36D) else Color(0xFFC18A3D),
                shape = CircleShape
            )
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                onFocused(it.isFocused)
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color(0xFFFFE9B5), fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WantedPoster(
    app: InstalledLauncherApp,
    reward: String,
    width: Dp,
    height: Dp,
    relativePosition: Int,
    horizontalStep: Dp,
    modifier: Modifier = Modifier
) {
    val distance = kotlin.math.abs(relativePosition)
    val targetScale = when (distance) { 0 -> 1f; 1 -> 0.82f; else -> 0.66f }
    val targetRotation = when (relativePosition) {
        -2 -> 18f
        -1 -> 11f
        1 -> -11f
        2 -> -18f
        else -> 0f
    }
    val targetDarkness = when (distance) { 0 -> 0f; 1 -> 0.20f; else -> 0.42f }

    val scale by animateFloatAsState(targetScale, tween(460, easing = FastOutSlowInEasing), label = "scale-${app.packageName}")
    val rotation by animateFloatAsState(targetRotation, tween(460, easing = FastOutSlowInEasing), label = "rotation-${app.packageName}")
    val darkness by animateFloatAsState(targetDarkness, tween(520, easing = FastOutSlowInEasing), label = "dark-${app.packageName}")
    val xOffset by animateDpAsState(horizontalStep * relativePosition, tween(480, easing = FastOutSlowInEasing), label = "position-${app.packageName}")
    val iconBitmap = remember(app.packageName) {
        app.icon.toBitmap(width = 256, height = 180).asImageBitmap()
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .offset(x = xOffset)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationY = rotation
                cameraDistance = 20f * density
            }
            .shadow(
                elevation = when (distance) { 0 -> 17.dp; 1 -> 10.dp; else -> 6.dp },
                shape = TornPaperShape,
                clip = true
            )
            .clip(TornPaperShape)
            .zIndex((3 - distance).toFloat())
    ) {
        Image(
            painter = painterResource(R.drawable.wanted_paper),
            contentDescription = "Cartel WANTED de ${app.label}",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = width * 0.135f,
                    end = width * 0.135f,
                    top = height * 0.055f,
                    bottom = height * 0.075f
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "WANTED",
                color = Color(0xFF211309),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 0.7.sp,
                maxLines = 1
            )

            Spacer(Modifier.height(height * 0.045f))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(height * 0.31f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = "Icono de ${app.label}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(5.dp)
                )
            }

            Spacer(Modifier.height(height * 0.012f))

            Text(
                "EN AVENTURA O EN DESCANSO",
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 6.sp,
                lineHeight = 7.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                app.label,
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                reward,
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                maxLines = 1
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "GOBIERNO MUNDIAL",
                    color = Color(0xFF55371F),
                    fontFamily = FontFamily.Serif,
                    fontSize = 4.sp,
                    maxLines = 1
                )
                Text(
                    "MARINE",
                    color = Color(0xFF24150B),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = 6.sp
                )
            }
        }

        if (darkness > 0.001f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(TornPaperShape)
                    .background(Color(0xFF24180F).copy(alpha = darkness))
            )
        }
    }
}

@Composable
private fun BottomLauncherAction(
    label: String,
    focusRequester: FocusRequester,
    selected: Boolean,
    onFocused: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = if (selected) BorderStroke(3.dp, Color(0xFFFFD36D)) else BorderStroke(1.dp, Color(0xFF946A38)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF815326) else Color(0xFFE7D0A5),
            contentColor = if (selected) Color(0xFFFFF0D1) else Color(0xFF3C2919)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            focusedElevation = 12.dp,
            pressedElevation = 8.dp
        ),
        modifier = Modifier
            .height(42.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { onFocused(it.isFocused) }
            .shadow(if (selected) 10.dp else 2.dp, RoundedCornerShape(50))
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SettingsDialog(
    installedApps: List<InstalledLauncherApp>,
    primaryPackages: List<String>,
    environmentMode: EnvironmentMode,
    soundEnabled: Boolean,
    homeRedirectEnabled: Boolean,
    onToggleHomeRedirect: () -> Unit,
    onOpenMediaAccess: () -> Unit,
    onClose: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onChange: (Int) -> Unit,
    onEnvironment: (EnvironmentMode) -> Unit,
    onSound: () -> Unit,
    onRefresh: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f).fillMaxHeight(0.86f),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF0DDB8),
            border = BorderStroke(4.dp, Color(0xFF80562F)),
            shadowElevation = 18.dp
        ) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                PanelHeader("AJUSTES", "Personaliza tu barco", onClose)
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanelSectionTitle("MIS CINCO CARTELES · ORDEN MANUAL")
                    primaryPackages.forEachIndexed { index, packageName ->
                        val app = installedApps.firstOrNull { it.packageName == packageName }
                        if (app != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF5DF), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "${index + 1}. ${app.label}",
                                    color = Ink,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                SmallPanelButton("↑", enabled = index > 0) { onMove(index, -1) }
                                SmallPanelButton("↓", enabled = index < primaryPackages.lastIndex) { onMove(index, 1) }
                                SmallPanelButton("CAMBIAR") { onChange(index) }
                            }
                        }
                    }

                    PanelSectionTitle("AMBIENTE DEL MAR")
                    Text(
                        "Automático cambia con la hora de Ecuador: día 06:00–16:59, atardecer 17:00–19:59 y noche 20:00–05:59.",
                        color = Color(0xFF62482D),
                        fontSize = 12.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EnvironmentButton("AUTO", EnvironmentMode.AUTO, environmentMode, onEnvironment)
                        EnvironmentButton("DÍA", EnvironmentMode.DAY, environmentMode, onEnvironment)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EnvironmentButton("ATARDECER", EnvironmentMode.SUNSET, environmentMode, onEnvironment)
                        EnvironmentButton("NOCHE", EnvironmentMode.NIGHT, environmentMode, onEnvironment)
                    }

                    PanelSectionTitle("SONIDO DE NAVEGACIÓN")
                    SmallPanelButton(if (soundEnabled) "SONIDOS ACTIVADOS" else "SONIDOS DESACTIVADOS") {
                        onSound()
                    }
                    PanelSectionTitle("BOTÓN HOME · INICIO GRAND LINE")
                    Text(
                        "Fire OS conserva el inicio de Amazon como inicio del sistema. Esta opción activa una redirección por Accesibilidad: al abrirse el inicio de Amazon, GRAND LINE vuelve al frente. Puede verse un destello breve del inicio de Amazon.",
                        color = Color(0xFF62482D),
                        fontSize = 12.sp
                    )
                    SmallPanelButton(
                        if (homeRedirectEnabled) "DESACTIVAR REDIRECCIÓN HOME" else "CONFIGURAR REDIRECCIÓN HOME"
                    ) { onToggleHomeRedirect() }
                    Text(
                        if (homeRedirectEnabled) "Estado: servicio habilitado." else "Estado: servicio no habilitado. Si Fire OS no permite activarlo desde Accesibilidad, concede WRITE_SECURE_SETTINGS por ADB y vuelve a pulsar este botón.",
                        color = Color(0xFF62482D),
                        fontSize = 11.sp
                    )

                    PanelSectionTitle("REPRODUCTOR · SPOTIFY / YOUTUBE")
                    Text(
                        "Permite a GRAND LINE leer la sesión multimedia activa para mostrar portada, título y controles. La reproducción en segundo plano depende de las funciones que permita cada aplicación.",
                        color = Color(0xFF62482D),
                        fontSize = 12.sp
                    )
                    SmallPanelButton("CONECTAR / CONFIGURAR REPRODUCTOR") { onOpenMediaAccess() }

                    PanelSectionTitle("APLICACIONES")
                    Text(
                        "Se detectan automáticamente las aplicaciones que ofrecen una actividad de inicio Android TV o Android. Regresa a GRAND LINE después de instalar o desinstalar una app para actualizar el catálogo.",
                        color = Color(0xFF62482D),
                        fontSize = 12.sp
                    )
                    SmallPanelButton("ACTUALIZAR CATÁLOGO") { onRefresh() }
                }
                Spacer(Modifier.height(8.dp))
                SmallPanelButton("CERRAR") { onClose() }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AllAppsDialog(
    apps: List<InstalledLauncherApp>,
    primaryPackages: List<String>,
    selectingForSlot: Int?,
    onClose: () -> Unit,
    onAppClick: (InstalledLauncherApp) -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f).fillMaxHeight(0.86f),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF0DDB8),
            border = BorderStroke(4.dp, Color(0xFF80562F)),
            shadowElevation = 18.dp
        ) {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                PanelHeader(
                    "TODAS LAS APPS",
                    if (selectingForSlot == null) "${apps.size} aplicaciones · selecciona para abrir"
                    else "Elige la aplicación para el cartel ${selectingForSlot + 1}",
                    onClose
                )
                if (apps.isEmpty()) {
                    Text(
                        "No se encontraron aplicaciones con acceso directo. Revisa el bloque <queries> del AndroidManifest.xml.",
                        color = Ink
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        gridItems(apps, key = { it.packageName }) { app ->
                            val primaryIndex = primaryPackages.indexOf(app.packageName)
                            AppGridTile(
                                app = app,
                                primaryLabel = if (primaryIndex >= 0) "CARTEL ${primaryIndex + 1}" else "",
                                selecting = selectingForSlot != null,
                                onClick = { onAppClick(app) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SmallPanelButton("CERRAR") { onClose() }
            }
        }
    }
}

@Composable
private fun AppGridTile(
    app: InstalledLauncherApp,
    primaryLabel: String,
    selecting: Boolean,
    onClick: () -> Unit
) {
    val iconBitmap = remember(app.packageName) {
        app.icon.toBitmap(width = 128, height = 128).asImageBitmap()
    }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.12f)
            .shadow(if (isFocused) 8.dp else 2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color(0xFFFFE7B0) else Color(0xFFFFF5DF))
            .border(
                if (isFocused) 3.dp else 1.dp,
                if (isFocused) Color(0xFFB77B29) else Color(0xFFB99969),
                RoundedCornerShape(12.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
            .focusable()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            bitmap = iconBitmap,
            contentDescription = "Icono de ${app.label}",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(54.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            app.label,
            color = Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        val footer = when {
            selecting -> "ELEGIR CARTEL"
            primaryLabel.isNotBlank() -> primaryLabel
            else -> "ABRIR"
        }
        Text(footer, color = Color(0xFF79572F), fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun EntriesDialog(onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.65f),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF0DDB8),
            border = BorderStroke(4.dp, Color(0xFF80562F))
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "ENTRADAS",
                    color = Ink,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                )
                Text(
                    "Las entradas HDMI y fuentes físicas pertenecen al televisor, no al Fire TV Stick. Puedes cambiarlas con el control de tu TV.",
                    color = Ink
                )
                SmallPanelButton("ENTENDIDO") { onClose() }
            }
        }
    }
}

@Composable
private fun PanelHeader(title: String, subtitle: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp
            )
            Text(subtitle, color = Color(0xFF755735), fontSize = 12.sp)
        }
        SmallPanelButton("CERRAR ✕") { onClose() }
    }
}

@Composable
private fun PanelSectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF61401F),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 5.dp)
    )
}

@Composable
private fun SmallPanelButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF90673A)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE3C596),
            contentColor = Ink,
            disabledContainerColor = Color(0xFFD5C6A9),
            disabledContentColor = Color(0xFF8C806B)
        ),
        modifier = Modifier.height(38.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun EnvironmentButton(
    label: String,
    mode: EnvironmentMode,
    current: EnvironmentMode,
    onClick: (EnvironmentMode) -> Unit
) {
    val selected = mode == current
    Button(
        onClick = { onClick(mode) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            if (selected) 3.dp else 1.dp,
            if (selected) Color(0xFFAE792B) else Color(0xFF90673A)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF79502B) else Color(0xFFE3C596),
            contentColor = if (selected) Color(0xFFFFF0D1) else Ink
        )
    ) {
        Text(label, fontWeight = FontWeight.Black, fontSize = 10.sp)
    }
}

@Composable
private fun MediaPlayerWidget(
    width: Dp,
    height: Dp,
    focusRequesters: List<FocusRequester>,
    onControlFocused: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val media = GrandLineMediaBridge.state
    val context = LocalContext.current
    val progress = if (media.durationMs > 0L) {
        (media.positionMs.toFloat() / media.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Row(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xE91B2C36), Color(0xE90E1723))))
            .border(1.5.dp, Color(0xFFD6A34A), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Brush.radialGradient(listOf(Color(0xFFB88B4C), Color(0xFF4B392B))))
                .border(1.dp, Color(0xFFFFD477), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (media.artwork != null) {
                Image(
                    bitmap = media.artwork.asImageBitmap(),
                    contentDescription = "Portada del último contenido",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(7.dp))
                )
            } else {
                Text("♫", color = Color(0xFFFFE9B5), fontSize = 27.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "ÚLTIMA TRAVESÍA",
                color = Color(0xFFFFD477),
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Text(
                media.title,
                color = Color(0xFFFFF0D2),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1
            )
            val sourceLine = when {
                media.hasActiveSession && media.source.isNotBlank() -> "${media.source} · ${if (media.isPlaying) "en reproducción" else "en pausa"}"
                media.source.isNotBlank() -> "${media.source} · guardado"
                else -> media.subtitle
            }
            Text(sourceLine, color = Color(0xFFD1D7DB), fontSize = 7.sp, maxLines = 1)
            Box(Modifier.fillMaxWidth().height(2.dp).background(Color(0xFF59636A), RoundedCornerShape(2.dp))) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color(0xFFE9BD62), RoundedCornerShape(2.dp)))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MediaControlButton("|◀", focusRequesters[0], onControlFocused, 0) { GrandLineMediaBridge.previous() }
                MediaControlButton(if (media.isPlaying) "Ⅱ" else "▶", focusRequesters[1], onControlFocused, 1) { GrandLineMediaBridge.toggle(context) }
                MediaControlButton("▶|", focusRequesters[2], onControlFocused, 2) { GrandLineMediaBridge.next() }
            }
        }
    }
}

@Composable
private fun MediaControlButton(
    label: String,
    focusRequester: FocusRequester,
    onFocused: (Int, Boolean) -> Unit,
    index: Int,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(width = 25.dp, height = 19.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                focused = it.isFocused
                onFocused(index, it.isFocused)
            }
            .clip(RoundedCornerShape(3.dp))
            .background(if (focused) Color(0xFFFFD477) else Color(0xFFD8B879))
            .border(if (focused) 2.dp else 1.dp, Color(0xFF76532B), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color(0xFF21170D), fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
