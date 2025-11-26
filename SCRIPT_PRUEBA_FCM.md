# 🔬 SCRIPT DE PRUEBA - Diagnóstico Completo FCM

## ✅ CONFIRMACIÓN PREVIA

Ya verificamos que:
- ✅ El servicio FCM está registrado en AndroidManifest.xml
- ✅ El backend envía notificaciones correctamente
- ✅ Tu token FCM está registrado: `eg9CzMsHTa...`

---

## 🧪 PRUEBA EN VIVO - PASO A PASO

### 📱 Preparación (5 minutos)

1. **Abre Android Studio**
2. **Ve a Logcat** (Alt+6 o View > Tool Windows > Logcat)
3. **Limpia los logs**: Click en el icono de escoba 🧹
4. **Configura el filtro**:
   ```
   package:com.example.zavira_movil level:debug tag:FCMService|NotificationStorage|HomeActivity
   ```

### 🎬 Ejecución de la Prueba

#### FASE 1: Verificar Token (2 minutos)

1. **Cierra completamente la app** (Force Stop desde Android Studio o ajustes del dispositivo)
2. **Abre la app**
3. **Busca en Logcat**:

```
D/HomeActivity: ========================================
D/HomeActivity: 🔍 VERIFICACIÓN DE TOKEN FCM
D/HomeActivity: ========================================
```

**¿Qué deberías ver?**
```
✅ TOKEN FCM ENCONTRADO:
  📱 Token: eg9CzMsHTa...
```

**Si ves esto**: ✅ Continúa a FASE 2  
**Si NO lo ves**: ❌ Hay un problema con Firebase - Ver Solución A

---

#### FASE 2: Solicitar Notificación de Prueba (1 minuto)

**Pide al backend que envíe una notificación usando su endpoint de debug:**

```bash
curl -X POST https://churnable-nimbly-norbert.ngrok-free.dev/debug/send-notification \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "user",
    "targetUserId": 325,
    "title": "¡Nuevo Reto!",
    "body": "Te han retado en Matemáticas",
    "data": {
      "tipo": "reto_recibido",
      "retador_nombre": "Juan Sebastian Mejia Lopez",
      "area": "Matemáticas",
      "reto_id": "999",
      "retador_id": "319"
    }
  }'
```

O simplemente diles: **"Envíen una notificación de prueba a mi usuario (ID: 325) usando el endpoint de debug"**

---

#### FASE 3: Monitorear Logcat (Crítico - 30 segundos)

**INMEDIATAMENTE después de que el backend envíe la notificación**, busca en Logcat:

##### ✅ ESCENARIO EXITOSO:

```
D/FCMService: ========================================
D/FCMService: 🔔 NOTIFICACIÓN FCM RECIBIDA
D/FCMService: ========================================
D/FCMService: De: 123456789
D/FCMService: ID Mensaje: 0:1234567890123456%abcd1234
D/FCMService: ----------------------------------------
D/FCMService: 📦 DATOS COMPLETOS DEL MENSAJE:
D/FCMService:   • tipo = reto_recibido
D/FCMService:   • retador_nombre = Juan Sebastian Mejia Lopez
D/FCMService:   • area = Matemáticas
D/FCMService:   • reto_id = 999
D/FCMService:   • retador_id = 319
D/FCMService: ----------------------------------------
D/FCMService: 🎮 CAMPOS DE RETO DETECTADOS:
D/FCMService:   • tipo: reto_recibido
D/FCMService:   • retador_nombre: Juan Sebastian Mejia Lopez
D/FCMService:   • area: Matemáticas
D/FCMService:   • reto_id: 999
D/FCMService:   • retador_id: 319
```

**Si ves esto**: ✅ La notificación SÍ llegó - Continúa a FASE 4  
**Si NO lo ves**: ❌ La notificación NO llegó - Ver Solución B

---

#### FASE 4: Verificar Guardado (30 segundos)

Si la notificación llegó, busca estos logs INMEDIATAMENTE después:

```
D/FCMService: 💾 saveNotificationToHistory() - Iniciando...
D/FCMService:   • title: ¡Nuevo Reto!
D/FCMService:   • message: Te han retado en Matemáticas
D/FCMService:   📊 Datos extraídos:
D/FCMService:     • tipo: reto_recibido
D/FCMService:     • area: Matemáticas
D/FCMService:     • puntaje: null
D/FCMService:     • retador_id: 319
D/FCMService:     • retador_nombre: Juan Sebastian Mejia Lopez
D/FCMService:     • retador_foto: null
D/FCMService:     • reto_id: 999
D/FCMService:   🎮 Detectado como RETO, usando constructor extendido
D/FCMService:   ✅ NotificationItem de RETO creado:
D/FCMService:     • tipo: reto_recibido
D/FCMService:     • retadorNombre: Juan Sebastian Mejia Lopez
D/FCMService:     • area: Matemáticas
D/FCMService:     • retoId: 999
D/NotificationStorage: 📥 saveNotification() llamado
D/NotificationStorage:   • Tipo: reto_recibido
D/NotificationStorage:   • Título: ¡Nuevo Reto!
D/NotificationStorage:   • Retador: Juan Sebastian Mejia Lopez
D/NotificationStorage:   • Guardado exitoso: true
D/NotificationStorage:   • Total notificaciones ahora: 1
```

**Si ves esto**: ✅ Se guardó correctamente - Continúa a FASE 5  
**Si NO lo ves**: ❌ Problema en el guardado - Ver Solución C

---

#### FASE 5: Verificar Visualización (1 minuto)

1. **En la app**, ve a **Notificaciones** (icono de campana en el header)
2. **Busca en Logcat**:

```
D/NotificationsActivity: 📋 loadNotifications() - Cargando notificaciones...
D/NotificationsActivity:   • Total notificaciones: 1
D/NotificationsActivity:   • Notif #0: tipo=reto_recibido, retador=Juan Sebastian Mejia Lopez, área=Matemáticas
D/NotificationsActivity:   ✅ Mostrando notificaciones en RecyclerView
D/NotificationsAdapter: 🎨 Binding notificación #0
D/NotificationsAdapter:   • Tipo: reto_recibido
D/NotificationsAdapter:   • Título: ¡Nuevo Reto!
D/NotificationsAdapter:   • Mensaje: Te han retado en Matemáticas
D/NotificationsAdapter:   • Retador: Juan Sebastian Mejia Lopez
D/NotificationsAdapter:   • Área: Matemáticas
D/NotificationsAdapter:   ✅ Mostrando chip de retador
```

**Si ves esto**: ✅ **¡TODO FUNCIONA!** - La notificación debe aparecer en la app  
**Si NO lo ves**: ❌ Problema en la visualización - Ver Solución D

---

## 🔧 SOLUCIONES

### Solución A: Token FCM NO Encontrado

**Problema**: Firebase no está generando el token o no se está guardando.

**Pasos**:
1. Verifica que `google-services.json` está en `app/`
2. Sincroniza Gradle: File > Sync Project with Gradle Files
3. Desinstala la app completamente
4. Vuelve a instalar desde Android Studio
5. Verifica los permisos de notificaciones en el dispositivo

**Comando para verificar**:
```bash
adb shell dumpsys package com.example.zavira_movil | findstr /C:"INTERNET" /C:"POST_NOTIFICATIONS"
```

---

### Solución B: Notificación NO Llega a la App

**Problema**: Firebase NO está entregando la notificación a la app.

**Causas posibles**:
1. **App en segundo plano**: Trae la app al frente antes de pedir la notificación
2. **Ahorro de batería**: Desactiva la optimización de batería para la app
3. **Google Play Services**: Verifica que está actualizado
4. **Conexión a Internet**: Verifica que el dispositivo tiene Internet

**Comandos de verificación**:
```bash
# Verificar Google Play Services
adb shell dumpsys package com.google.android.gms | findstr /C:"versionName"

# Verificar conexión
adb shell ping -c 3 8.8.8.8
```

---

### Solución C: Se Recibe pero NO se Guarda

**Problema**: `saveNotificationToHistory()` falla o no se ejecuta.

**Pasos de debug**:
1. Busca en Logcat si hay algún log de error:
   ```
   E/FCMService: ❌ Error al guardar notificación en historial
   ```

2. Verifica que SharedPreferences tiene permisos:
   ```bash
   adb shell run-as com.example.zavira_movil ls /data/data/com.example.zavira_movil/shared_prefs/
   ```

3. Si no hay logs de error, el método no se está llamando. Verifica el código de `onMessageReceived()`.

---

### Solución D: Se Guarda pero NO se Muestra

**Problema**: `NotificationsActivity` no está leyendo las notificaciones o el adaptador no las muestra.

**Pasos de debug**:
1. Verifica que `loadNotifications()` se está llamando:
   ```
   D/NotificationsActivity: 📋 loadNotifications() - Cargando notificaciones...
   ```

2. Verifica el total de notificaciones:
   ```
   D/NotificationsActivity:   • Total notificaciones: X
   ```

3. Si es > 0 pero no se muestran, el problema está en:
   - El layout `item_notification.xml`
   - El adaptador `NotificationsAdapter`
   - El contenedor de retador está oculto por defecto

---

## 📊 TABLA DE RESULTADOS

Anota aquí los resultados de cada fase:

| Fase | Resultado | Logs Observados | Estado |
|------|-----------|-----------------|--------|
| 1. Token FCM | ☐ Sí / ☐ No | | |
| 2. Notificación Enviada | ☐ Sí (backend) | | |
| 3. Notificación Recibida | ☐ Sí / ☐ No | | |
| 4. Notificación Guardada | ☐ Sí / ☐ No | | |
| 5. Notificación Mostrada | ☐ Sí / ☐ No | | |

---

## 📞 REPORTE AL BACKEND

Después de completar las pruebas, envía este reporte al backend:

```markdown
## 🧪 RESULTADOS DE PRUEBA FCM

**Usuario**: 325  
**Token FCM**: eg9CzMsHTa...  
**Fecha**: 2025-11-26  

### Resultados:

**FASE 1 - Token FCM**: [✅ OK / ❌ FALLO]
[Copiar logs relevantes]

**FASE 2 - Notificación Enviada**: [✅ Confirmado por backend]

**FASE 3 - Notificación Recibida**: [✅ OK / ❌ FALLO]
[Copiar logs de FCMService o indicar que no aparecieron]

**FASE 4 - Notificación Guardada**: [✅ OK / ❌ FALLO]
[Copiar logs de NotificationStorage]

**FASE 5 - Notificación Mostrada**: [✅ OK / ❌ FALLO]
[Captura de pantalla de la app]

### Conclusión:

[Indicar en qué fase falló y cuál es el problema identificado]
```

---

**Autor**: GitHub Copilot  
**Fecha**: 2025-11-26  
**Versión**: 4.0 - Script de Prueba Completo  

