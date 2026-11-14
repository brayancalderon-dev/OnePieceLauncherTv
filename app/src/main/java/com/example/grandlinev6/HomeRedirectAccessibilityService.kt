package com.example.grandlinev6

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

/**
 * Fire OS no permite sustituir el launcher HOME mediante set-home-activity.
 * Este servicio observa cuándo el launcher de Amazon pasa a primer plano y
 * vuelve a abrir GRAND LINE encima. No intercepta ni lee contenido de pantalla.
 *
 * En Fire OS 8 puede verse un destello muy breve del inicio de Amazon antes
 * de que GRAND LINE vuelva al frente. El launcher original permanece intacto.
 */
class HomeRedirectAccessibilityService : AccessibilityService() {

    private var lastRedirectAt: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val foregroundPackage = event.packageName?.toString() ?: return
        if (foregroundPackage != AMAZON_HOME_PACKAGE) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastRedirectAt < REDIRECT_COOLDOWN_MS) return
        lastRedirectAt = now

        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            startActivity(intent)
        } catch (_: Exception) {
            // Si Fire OS bloquea este lanzamiento, no se fuerza ni se reinicia el servicio.
        }
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val AMAZON_HOME_PACKAGE = "com.amazon.tv.launcher"
        private const val REDIRECT_COOLDOWN_MS = 1_200L
    }
}
