package com.example.grandlinev6

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Handler
import android.os.Looper

/**
 * Permite consultar las sesiones de audio/video activas cuando el usuario habilita
 * el acceso de notificaciones de GRAND LINE en los Ajustes del Fire TV.
 */
class GrandLineMediaNotificationListener : NotificationListenerService() {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onListenerConnected() {
        super.onListenerConnected()
        mainHandler.post { GrandLineMediaBridge.refresh(applicationContext) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        mainHandler.post { GrandLineMediaBridge.refresh(applicationContext) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Algunas apps reemplazan la notificación durante el cambio de canción.
        // Esperamos un momento para consultar la sesión que quedó activa.
        mainHandler.postDelayed({ GrandLineMediaBridge.refresh(applicationContext) }, 350L)
    }
}
