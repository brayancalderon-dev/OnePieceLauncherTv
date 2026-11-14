package com.example.grandlinev6

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileOutputStream

private const val MEDIA_PREFS = "grand_line_media"
private const val MEDIA_ART_FILE = "grand_line_last_media_art.jpg"

/**
 * Estado del reproductor global de GRAND LINE.
 * Conserva los datos del último contenido aunque la sesión deje de estar activa.
 */
data class GrandLineMediaState(
    val title: String = "Tu último contenido",
    val subtitle: String = "Spotify / YouTube · guardado",
    val source: String = "",
    val packageName: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val hasActiveSession: Boolean = false,
    val artwork: Bitmap? = null
)

object GrandLineMediaBridge {
    var state by mutableStateOf(GrandLineMediaState())
        private set

    private var activeController: MediaController? = null
    private var activeCallback: MediaController.Callback? = null

    fun loadSaved(context: Context) {
        val prefs = context.getSharedPreferences(MEDIA_PREFS, Context.MODE_PRIVATE)
        val artworkFile = File(context.filesDir, MEDIA_ART_FILE)
        val artwork = if (artworkFile.exists()) BitmapFactory.decodeFile(artworkFile.absolutePath) else null
        state = GrandLineMediaState(
            title = prefs.getString("title", null) ?: "Tu último contenido",
            subtitle = prefs.getString("subtitle", null) ?: "Spotify / YouTube · guardado",
            source = prefs.getString("source", null).orEmpty(),
            packageName = prefs.getString("package", null).orEmpty(),
            durationMs = prefs.getLong("duration", 0L),
            positionMs = prefs.getLong("position", 0L),
            isPlaying = false,
            hasActiveSession = false,
            artwork = artwork
        )
    }

    /**
     * Puede consultarse solo después de habilitar el acceso a notificaciones.
     * Android autoriza getActiveSessions a un NotificationListenerService activo.
     */
    fun refresh(context: Context) {
        try {
            val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val listener = ComponentName(context, GrandLineMediaNotificationListener::class.java)
            val sessions = manager.getActiveSessions(listener)
            val active = sessions.firstOrNull { controller ->
                controller.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: sessions.firstOrNull { controller ->
                controller.metadata != null && controller.playbackState?.state != PlaybackState.STATE_NONE
            }

            if (active != null) attach(context.applicationContext, active)
            else markNoActiveSession()
        } catch (_: SecurityException) {
            // El usuario todavía no ha habilitado el acceso al reproductor.
            markNoActiveSession()
        } catch (_: Exception) {
            markNoActiveSession()
        }
    }

    @Synchronized
    private fun attach(context: Context, controller: MediaController) {
        if (activeController?.sessionToken != controller.sessionToken) {
            activeCallback?.let { callback ->
                try { activeController?.unregisterCallback(callback) } catch (_: Exception) { }
            }
            activeController = controller
            val callback = object : MediaController.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    updateFromController(context, controller)
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    updateFromController(context, controller)
                }

                override fun onSessionDestroyed() {
                    synchronized(this@GrandLineMediaBridge) {
                        if (activeController?.sessionToken == controller.sessionToken) {
                            activeController = null
                            activeCallback = null
                            markNoActiveSession()
                        }
                    }
                }
            }
            activeCallback = callback
            try {
                controller.registerCallback(callback, Handler(Looper.getMainLooper()))
            } catch (_: Exception) {
                activeController = null
                activeCallback = null
                markNoActiveSession()
                return
            }
        }
        updateFromController(context, controller)
    }

    private fun updateFromController(context: Context, controller: MediaController) {
        val metadata = controller.metadata
        val playback = controller.playbackState
        val packageName = controller.packageName.orEmpty()
        val prefs = context.getSharedPreferences(MEDIA_PREFS, Context.MODE_PRIVATE)

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
            ?: state.title

        val subtitle = metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }
            ?: state.subtitle

        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L)
            ?: state.durationMs
        val position = currentPosition(playback, duration)
        val playing = playback?.state == PlaybackState.STATE_PLAYING

        val appSource = try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast('.').ifBlank { "Reproductor" }
        }

        val art = metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: state.artwork

        if (metadata != null && (title != "Tu último contenido" || packageName.isNotBlank())) {
            prefs.edit()
                .putString("title", title)
                .putString("subtitle", subtitle)
                .putString("source", appSource)
                .putString("package", packageName)
                .putLong("duration", duration)
                .putLong("position", position)
                .apply()
        }

        if (art != null) saveArtwork(context, art)

        state = GrandLineMediaState(
            title = title,
            subtitle = subtitle,
            source = appSource,
            packageName = packageName,
            durationMs = duration,
            positionMs = position,
            isPlaying = playing,
            hasActiveSession = true,
            artwork = art
        )
    }

    private fun currentPosition(playback: PlaybackState?, duration: Long): Long {
        if (playback == null) return 0L
        var position = playback.position.coerceAtLeast(0L)
        if (playback.state == PlaybackState.STATE_PLAYING) {
            val elapsed = (SystemClock.elapsedRealtime() - playback.lastPositionUpdateTime).coerceAtLeast(0L)
            position += (elapsed * playback.playbackSpeed).toLong().coerceAtLeast(0L)
        }
        return if (duration > 0L) position.coerceAtMost(duration) else position
    }

    private fun saveArtwork(context: Context, artwork: Bitmap) {
        try {
            val maxSide = 360
            val ratio = minOf(1f, maxSide.toFloat() / maxOf(artwork.width, artwork.height).coerceAtLeast(1))
            val scaled = if (ratio < 1f) {
                Bitmap.createScaledBitmap(
                    artwork,
                    (artwork.width * ratio).toInt().coerceAtLeast(1),
                    (artwork.height * ratio).toInt().coerceAtLeast(1),
                    true
                )
            } else artwork
            FileOutputStream(File(context.filesDir, MEDIA_ART_FILE)).use { output ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 86, output)
            }
        } catch (_: Exception) {
            // El control del audio sigue funcionando aunque no pueda guardarse la portada.
        }
    }

    @Synchronized
    private fun markNoActiveSession() {
        activeCallback?.let { callback ->
            try { activeController?.unregisterCallback(callback) } catch (_: Exception) { }
        }
        activeCallback = null
        activeController = null
        state = state.copy(isPlaying = false, hasActiveSession = false)
    }

    fun refreshPosition() {
        val controller = activeController ?: return
        val playback = controller.playbackState ?: return
        state = state.copy(
            positionMs = currentPosition(playback, state.durationMs),
            isPlaying = playback.state == PlaybackState.STATE_PLAYING,
            hasActiveSession = true
        )
    }

    fun previous() {
        try { activeController?.transportControls?.skipToPrevious() } catch (_: Exception) { }
    }

    fun next() {
        try { activeController?.transportControls?.skipToNext() } catch (_: Exception) { }
    }

    fun toggle(context: Context) {
        val controller = activeController
        if (controller != null) {
            try {
                if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
                return
            } catch (_: Exception) {
                // Si la sesión dejó de responder, se intenta abrir la última app.
            }
        }

        val packageName = state.packageName
        if (packageName.isBlank()) {
            Toast.makeText(context, "Todavía no hay contenido guardado", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null) {
                Toast.makeText(context, "No se encontró la aplicación del último contenido", Toast.LENGTH_SHORT).show()
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir el último reproductor", Toast.LENGTH_SHORT).show()
        }
    }
}
