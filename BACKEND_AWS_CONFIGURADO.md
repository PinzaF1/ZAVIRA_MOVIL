# ✅ BACKEND ACTUALIZADO A AWS

**Fecha:** 19 de noviembre de 2025  
**Cambio:** URL del backend actualizada de ngrok a servidor AWS  
**Estado:** ✅ **CONFIGURADO Y LISTO**

---

## 🔧 CAMBIO REALIZADO

### **Archivo Modificado:** `RetrofitClient.java`

**URL Anterior (ngrok):**
```java
private static final String BASE_URL = "https://churnable-nimbly-norbert.ngrok-free.dev/";
```

**URL Nueva (AWS):**
```java
private static final String BASE_URL = "http://98.92.245.3:3333/";
```

---

## ✅ VERIFICACIONES COMPLETADAS

### **1. Permisos de Internet** ✅
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### **2. Cleartext Traffic Habilitado** ✅
```xml
<!-- AndroidManifest.xml -->
<application
    android:usesCleartextTraffic="true">
```
Esto permite conexiones HTTP (no solo HTTPS).

### **3. Timeouts Configurados** ✅
```java
// RetrofitClient.java
.connectTimeout(20, TimeUnit.SECONDS)
.readTimeout(30, TimeUnit.SECONDS)
.writeTimeout(30, TimeUnit.SECONDS)
```

---

## 📊 CONFIGURACIÓN DEL SERVIDOR AWS

| Parámetro | Valor |
|-----------|-------|
| **IP Pública** | 98.92.245.3 |
| **Puerto** | 3333 |
| **Protocolo** | HTTP |
| **URL Completa** | http://98.92.245.3:3333/ |

---

## 🧪 PRUEBAS RECOMENDADAS

### **Test 1: Verificar Conectividad del Servidor**

Desde tu navegador o Postman:
```
GET http://98.92.245.3:3333/
```

**Respuesta esperada:**
- Status 200 OK
- O mensaje del servidor (ej: "API is running")

### **Test 2: Login desde la App**

1. Genera el APK con la nueva URL
2. Instala en dispositivo con Internet
3. Intenta hacer login con credenciales válidas
4. Verifica logs en Logcat:
   ```
   Filtro: "Retrofit" o "HTTP"
   ```

**Logs esperados:**
```
D/OkHttp: --> POST http://98.92.245.3:3333/api/auth/login
D/OkHttp: <-- 200 OK (XXXms)
```

### **Test 3: Cargar Quiz**

1. Después de login exitoso
2. Selecciona una isla y nivel
3. Verifica que carguen las preguntas
4. Revisa logs:
   ```
   D/OkHttp: --> POST http://98.92.245.3:3333/api/sesion/parada
   D/OkHttp: <-- 200 OK
   ```

---

## ⚠️ TROUBLESHOOTING

### **Problema 1: "Unable to resolve host"**

**Síntoma:**
```
java.net.UnknownHostException: Unable to resolve host "98.92.245.3"
```

**Causas posibles:**
1. Dispositivo sin Internet
2. Firewall bloqueando la IP
3. Servidor AWS caído

**Solución:**
```bash
# 1. Verificar que el servidor esté corriendo
# En tu servidor AWS:
netstat -tuln | grep 3333

# 2. Verificar puerto abierto en Security Group
# AWS Console → EC2 → Security Groups
# Inbound Rules debe tener:
# Type: Custom TCP
# Port Range: 3333
# Source: 0.0.0.0/0 (para pruebas) o tu IP específica
```

### **Problema 2: "Connection timeout"**

**Síntoma:**
```
java.net.SocketTimeoutException: timeout
```

**Causas posibles:**
1. Servidor AWS lento o sobrecargado
2. Red del dispositivo muy lenta
3. Timeouts muy cortos

**Solución:**
```java
// Si es necesario, aumentar timeouts en RetrofitClient.java:
.connectTimeout(30, TimeUnit.SECONDS)  // De 20 a 30
.readTimeout(45, TimeUnit.SECONDS)     // De 30 a 45
```

### **Problema 3: "Cleartext HTTP traffic not permitted"**

**Síntoma:**
```
java.net.UnknownServiceException: CLEARTEXT communication not permitted
```

**Causa:** Android bloqueando HTTP por seguridad

**Solución:**
Ya está configurado en AndroidManifest.xml:
```xml
android:usesCleartextTraffic="true"
```

Si persiste, crear archivo `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">98.92.245.3</domain>
    </domain-config>
</network-security-config>
```

Y en AndroidManifest.xml:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

### **Problema 4: "Connection refused"**

**Síntoma:**
```
java.net.ConnectException: Connection refused
```

**Causas:**
1. Servidor no está corriendo
2. Puerto 3333 cerrado
3. Firewall bloqueando

**Verificar en servidor AWS:**
```bash
# Ver si el servidor está corriendo
ps aux | grep node  # Si es Node.js
ps aux | grep python # Si es Python
ps aux | grep java  # Si es Java

# Ver si el puerto está escuchando
netstat -tuln | grep 3333

# Debería mostrar:
# tcp  0  0  0.0.0.0:3333  0.0.0.0:*  LISTEN
```

---

## 🔒 SEGURIDAD - RECOMENDACIONES

### **1. Migrar a HTTPS (IMPORTANTE)** 🔐

Actualmente estás usando HTTP (sin cifrado). Para producción, **deberías migrar a HTTPS**.

**Opciones:**

#### **Opción A: Usar un dominio con SSL (Recomendado)**
```
1. Comprar un dominio (ej: eduexce.com)
2. Apuntar dominio a tu IP AWS (98.92.245.3)
3. Instalar certificado SSL con Let's Encrypt (gratis):
   - Usar Nginx como reverse proxy
   - Certbot para certificado SSL
4. Actualizar app a: https://api.eduexce.com/
```

#### **Opción B: Usar AWS API Gateway**
```
1. Crear API Gateway en AWS
2. Apuntar a tu backend (98.92.245.3:3333)
3. API Gateway te da HTTPS gratis
4. Actualizar app a URL de API Gateway
```

#### **Opción C: CloudFlare Tunnel (Más fácil)**
```
1. Crear cuenta en CloudFlare (gratis)
2. Instalar cloudflared en tu servidor
3. Crear tunnel a localhost:3333
4. CloudFlare te da URL HTTPS gratis
5. Actualizar app a URL de CloudFlare
```

### **2. Restringir Acceso por IP (Opcional)**
```
AWS Security Group:
- En vez de 0.0.0.0/0 (todo el mundo)
- Poner IPs específicas que necesiten acceso
```

### **3. Implementar Rate Limiting**
```
En tu backend:
- Limitar requests por IP
- Prevenir ataques DDoS
- Ejemplo: 100 requests/minuto por IP
```

---

## 📋 CHECKLIST POST-CAMBIO

Antes de distribuir la app, verifica:

- [x] URL actualizada a http://98.92.245.3:3333/
- [x] Cleartext traffic habilitado
- [x] Permisos de Internet configurados
- [ ] Servidor AWS corriendo en puerto 3333
- [ ] Security Group permite tráfico en puerto 3333
- [ ] Login funciona desde la app
- [ ] Carga de quizzes funciona
- [ ] Envío de respuestas funciona
- [ ] Ranking se carga correctamente
- [ ] Notificaciones push funcionan

---

## 🚀 PASOS PARA GENERAR APK

Ahora que el backend está actualizado:

### **1. Sync Gradle**
```
Android Studio → File → Sync Project with Gradle Files
```

### **2. Clean & Rebuild**
```
Build → Clean Project
Build → Rebuild Project
```

### **3. Generar APK**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

### **4. Probar en Dispositivo**
```
Instala el APK en un dispositivo con Internet
Intenta hacer login
Verifica que todo funcione correctamente
```

---

## 📊 MONITOREO

### **Logs del Servidor (AWS)**
```bash
# Si usas PM2 (Node.js):
pm2 logs

# Si usas systemd:
journalctl -u tu-servicio -f

# Logs genéricos:
tail -f /var/log/tu-app.log
```

### **Logs de la App (Android)**
```
Android Studio → Logcat
Filtros útiles:
- "Retrofit"
- "OkHttp"
- "ERROR"
- "LoginActivity"
- "QuizActivity"
```

### **Métricas a Monitorear**
- Tiempo de respuesta de las APIs
- Tasa de errores (4xx, 5xx)
- Usuarios concurrentes
- Uso de CPU/RAM del servidor

---

## 🔄 CAMBIAR URL EN EL FUTURO

Si necesitas cambiar la URL nuevamente:

### **Ubicación del archivo:**
```
app/src/main/java/com/example/zavira_movil/remote/RetrofitClient.java
```

### **Línea a modificar:**
```java
private static final String BASE_URL = "http://TU_NUEVA_URL/";
```

### **Ejemplo para HTTPS:**
```java
private static final String BASE_URL = "https://api.eduexce.com/";
```

### **Después del cambio:**
```
1. Sync Gradle
2. Clean Project
3. Rebuild Project
4. Generar nuevo APK
```

---

## 📞 VERIFICACIÓN RÁPIDA

Para probar que el servidor está accesible:

### **Desde navegador:**
```
http://98.92.245.3:3333/
```

### **Desde terminal:**
```bash
# Windows PowerShell:
Test-NetConnection -ComputerName 98.92.245.3 -Port 3333

# Linux/Mac:
curl http://98.92.245.3:3333/

# Verificar respuesta:
curl -I http://98.92.245.3:3333/
```

### **Desde Postman:**
```
GET http://98.92.245.3:3333/api/health
GET http://98.92.245.3:3333/api/test
```

---

## ✅ RESUMEN

**Cambio Aplicado:**
- ✅ URL actualizada de ngrok a AWS
- ✅ Configuración HTTP verificada
- ✅ Permisos configurados

**Estado Actual:**
- ✅ App apunta a: `http://98.92.245.3:3333/`
- ✅ Listo para generar APK
- ✅ Listo para pruebas

**Próximos Pasos:**
1. Verifica que el servidor AWS esté corriendo
2. Sync Gradle en Android Studio
3. Genera APK
4. Prueba en dispositivo
5. ¡Listo para usar! 🎉

**Recomendación para Producción:**
⚠️ Migrar a HTTPS lo antes posible para proteger datos de usuarios

---

**Actualizado por:** GitHub Copilot  
**Fecha:** 19 de noviembre de 2025  
**Backend:** AWS (http://98.92.245.3:3333/)

