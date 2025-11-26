# 🔥 DIAGNÓSTICO RÁPIDO - Problemas Comunes FCM

## 🎯 El backend envió la notificación, pero ¿llega a tu app?

Aquí están los **3 problemas más comunes** y cómo solucionarlos:

---

## ❌ PROBLEMA 1: Notificación NO Llega a la App

### 🔍 Síntoma:
En Logcat **NO aparece**:
```
D/FCMService: 🔔 NOTIFICACIÓN FCM RECIBIDA
```

### 🐛 Causas Comunes:

#### 1️⃣ App en Segundo Plano
**Solución**: Mantén la app en primer plano cuando pidas la notificación.

#### 2️⃣ Ahorro de Batería Activo
**Solución**: 
- Ve a Ajustes > Aplicaciones > EduExce
- Desactiva "Optimizar batería"

#### 3️⃣ Google Play Services Desactualizado
**Verificar**:
```bash
adb shell dumpsys package com.google.android.gms | findstr versionName
```
**Solución**: Actualiza Google Play Services desde la Play Store

#### 4️⃣ Sin Conexión a Internet
**Verificar**:
```bash
adb shell ping -c 3 8.8.8.8
```
**Solución**: Conecta a WiFi o datos móviles

#### 5️⃣ Token Desincronizado
**Solución**: Desinstala y reinstala la app completamente

---

## ❌ PROBLEMA 2: Llega pero NO se Guarda

### 🔍 Síntoma:
En Logcat **SÍ aparece**:
```
D/FCMService: 🔔 NOTIFICACIÓN FCM RECIBIDA
```

Pero **NO aparece**:
```
D/NotificationStorage: ✅ Guardado exitoso: true
```

### 🐛 Causa Más Probable:
Hay una **excepción silenciosa** en `saveNotificationToHistory()`.

### 🔧 Solución:
Busca en Logcat:
```
E/FCMService: ❌ Error al guardar notificación en historial
```

Si aparece un error, **copia el stack trace completo** y analízalo.

### 🔍 Verificación Adicional:
```bash
# Verificar permisos de SharedPreferences
adb shell run-as com.example.zavira_movil ls /data/data/com.example.zavira_movil/shared_prefs/
```

Deberías ver: `notifications_prefs.xml`

---

## ❌ PROBLEMA 3: Se Guarda pero NO se Muestra

### 🔍 Síntoma:
En Logcat **SÍ aparece**:
```
D/NotificationStorage: ✅ Guardado exitoso: true
D/NotificationStorage:   • Total notificaciones ahora: 1
```

Pero al abrir Notificaciones en la app:
```
D/NotificationsActivity:   • Total notificaciones: 0
D/NotificationsActivity:   ⚠️ No hay notificaciones
```

### 🐛 Causa Más Probable:
El contexto usado en `NotificationStorage` es diferente o hay un problema de sincronización.

### 🔧 Solución A: Forzar Recarga
1. Cierra completamente la app
2. Limpia los datos de la app:
   ```bash
   adb shell pm clear com.example.zavira_movil
   ```
3. Vuelve a iniciar sesión
4. Pide una notificación de prueba

### 🔧 Solución B: Verificar SharedPreferences
```bash
# Ver el contenido de las notificaciones guardadas
adb shell run-as com.example.zavira_movil cat /data/data/com.example.zavira_movil/shared_prefs/notifications_prefs.xml
```

Deberías ver un JSON con tus notificaciones. Si está vacío, hay un problema en el guardado.

### 🔧 Solución C: Verificar el Adaptador
En Logcat, busca:
```
D/NotificationsAdapter: 🎨 Binding notificación #0
D/NotificationsAdapter:   • Tipo: reto_recibido
D/NotificationsAdapter:   ✅ Mostrando chip de retador
```

Si **NO aparece**, el adaptador no se está creando correctamente.

---

## 🛠️ HERRAMIENTAS DE DEBUG

### 1. Ver Logs en Tiempo Real
```powershell
adb logcat -c ; adb logcat | Select-String "FCMService|NotificationStorage|NotificationsAdapter"
```

### 2. Verificar Notificaciones Guardadas
```bash
adb shell run-as com.example.zavira_movil cat /data/data/com.example.zavira_movil/shared_prefs/notifications_prefs.xml
```

### 3. Limpiar Todo y Empezar de Cero
```bash
adb shell pm clear com.example.zavira_movil
```

### 4. Forzar Cierre de la App
```bash
adb shell am force-stop com.example.zavira_movil
```

### 5. Verificar Permisos de Notificaciones
```bash
adb shell cmd notification allowed_listeners
```

---

## 🎯 CHECKLIST DE VERIFICACIÓN RÁPIDA

Marca cada uno:

- [ ] Google Play Services actualizado
- [ ] App en primer plano
- [ ] Ahorro de batería desactivado para la app
- [ ] Permisos de notificaciones concedidos
- [ ] Conexión a Internet activa
- [ ] Token FCM válido (verificado en logs de HomeActivity)
- [ ] Servicio FCM registrado en Manifest
- [ ] `google-services.json` presente en `app/`

---

## 📞 REPORTE AL BACKEND

Si ninguna solución funciona, reporta:

```markdown
He verificado todos los puntos comunes:
- ✅ Google Play Services actualizado
- ✅ App en primer plano
- ✅ Sin ahorro de batería
- ✅ Permisos concedidos
- ✅ Internet activo
- ✅ Token FCM válido: eg9CzMsHTa...

Logs observados:
[Pegar logs de Logcat]

Problema específico:
[Indicar si es Problema 1, 2 o 3]
```

---

## 🆘 ÚLTIMA OPCIÓN: Reinstalación Completa

Si NADA funciona:

1. **Desinstala** la app completamente del dispositivo
2. **Limpia** el proyecto en Android Studio:
   ```
   Build > Clean Project
   Build > Rebuild Project
   ```
3. **Elimina** la carpeta `.gradle` y `build` del proyecto
4. **Sincroniza** Gradle de nuevo
5. **Instala** la app desde cero
6. **Verifica** que el token se genere y registre correctamente

---

**Autor**: GitHub Copilot  
**Fecha**: 2025-11-26  
**Versión**: 5.0 - Guía de Problemas Comunes  

