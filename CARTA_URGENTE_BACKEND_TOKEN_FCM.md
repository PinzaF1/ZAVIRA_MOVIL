# 📧 CARTA URGENTE AL BACKEND - Verificación de Token FCM

**Fecha**: 2025-11-26  
**Hora**: 02:35 AM  
**De**: Equipo Móvil Android  
**Para**: Equipo Backend  
**Asunto**: 🚨 URGENTE - Token FCM no recibe notificaciones automáticas de retos  

---

## 🔍 SITUACIÓN ACTUAL

Hemos confirmado que nuestro token FCM está activo y funcionando correctamente:

### ✅ Token FCM Confirmado:
```
eg9CzMsHTaWwyWxOeJYXLa:APA91bGxy8NPieT36rp5mLSdpngfpAE294E2WmMAZC9L2LryPes9bVeMbLI8fSFE3PziyOf55I5KDemSo82fIgoAGxp8pbQdN6ErXWw74HtnWIll7agvyu4
```

**Usuario**: 325  
**Device ID**: 205176cedc31cf98  
**Platform**: Android  

---

## 🚨 PROBLEMA CONFIRMADO

### Lo que FUNCIONA:
- ✅ Los retos se crean correctamente en el backend
- ✅ Los retos aparecen en el apartado "Retos Recibidos" de la app
- ✅ El token FCM se genera correctamente en la app
- ✅ Firebase está configurado correctamente en la app

### Lo que NO FUNCIONA:
- ❌ **Las notificaciones FCM NO llegan a la app cuando se crea un reto**
- ❌ El método `onMessageReceived()` NUNCA se ejecuta
- ❌ Las notificaciones NO aparecen en el historial de notificaciones
- ❌ Los usuarios NO ven el chip morado del retador

### Evidencia:
- **Reto ID 335** creado en Sociales por usuario 319
- **Fecha**: 2025-11-26T07:25:56.571+00:00
- **Estado**: Aparece en "Retos Recibidos" pero NO en "Notificaciones"
- **Logs de la app**: CERO logs de FCM recibido

---

## 🔍 VERIFICACIONES NECESARIAS

### 1️⃣ ¿Este token está registrado en su base de datos?

Por favor ejecuten esta consulta:

```sql
SELECT * FROM fcm_tokens 
WHERE token = 'eg9CzMsHTaWwyWxOeJYXLa:APA91bGxy8NPieT36rp5mLSdpngfpAE294E2WmMAZC9L2LryPes9bVeMbLI8fSFE3PziyOf55I5KDemSo82fIgoAGxp8pbQdN6ErXWw74HtnWIll7agvyu4';
```

O por usuario:

```sql
SELECT * FROM fcm_tokens WHERE id_usuario = 325;
```

**¿Resultado esperado**: ¿El token aparece? ¿Está activo (`is_active = true`)?

---

### 2️⃣ ¿El envío de FCM es automático al crear retos?

Por favor confirmen:

**Pregunta**: ¿Cuando se ejecuta el endpoint `POST /movil/retos` (crear reto), automáticamente se envía una notificación FCM al oponente?

**Esperado**: SÍ, debería enviarse automáticamente.

**Verificación**: Busquen en su código de `RetosService.crearReto()` o similar, algo como:

```javascript
// Después de guardar el reto en la base de datos
await enviarNotificacionFCM(tokenOponente, payload);
```

---

### 3️⃣ ¿Qué payload están enviando?

Según su confirmación anterior, dijeron que cambiaron el payload a solo `data`. Por favor confirmen que están usando:

```javascript
const message = {
  data: {  // ← Solo data, sin notification
    tipo: "reto_recibido",
    retador_nombre: "...",
    area: "...",
    reto_id: "...",
    retador_id: "...",
    title: "¡Nuevo Reto!",
    body: "Te han retado en ..."
  },
  token: tokenFCM
};
```

**¿Están usando este formato?**

---

## 🧪 PRUEBA MANUAL SOLICITADA

Por favor envíen una notificación de prueba MANUALMENTE a nuestro token usando su endpoint de debug:

```bash
POST /debug/send-notification
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZF91c3VhcmlvIjoxLCJyb2wiOiJhZG1pbmlzdHJhZG9yIiwiaWF0IjoxNzY0MTM5NDA5LCJleHAiOjE3NjQxNDMwMDl9.bjauka8i5RyLLFdzzyLS0KPFpISVmuOdXVB0708QCz0
Content-Type: application/json

{
  "mode": "token",
  "targetToken": "eg9CzMsHTaWwyWxOeJYXLa:APA91bGxy8NPieT36rp5mLSdpngfpAE294E2WmMAZC9L2LryPes9bVeMbLI8fSFE3PziyOf55I5KDemSo82fIgoAGxp8pbQdN6ErXWw74HtnWIll7agvyu4",
  "data": {
    "tipo": "reto_recibido",
    "retador_nombre": "Juan Sebastian Mejia Lopez",
    "area": "Sociales",
    "reto_id": "335",
    "retador_id": "319",
    "title": "🧪 Prueba Manual",
    "body": "Esta es una notificación de prueba"
  }
}
```

**IMPORTANTE**: 
- Usen `"mode": "token"` con `"targetToken"` (el token completo)
- El payload debe tener **SOLO `data`**, sin `notification`

Si esta prueba manual llega a nuestra app (veremos los logs de FCMService), entonces sabemos que:
- ✅ El token funciona
- ✅ El formato del payload es correcto
- ❌ El problema es que el envío NO se ejecuta automáticamente al crear retos

---

## 📊 LOGS DEL BACKEND SOLICITADOS

Por favor compartan los logs de su servidor cuando:

1. Se creó el reto ID 335 (2025-11-26T07:25:56.571+00:00)
2. Busquen líneas que mencionen:
   - "Enviando notificación FCM"
   - "admin.messaging().send"
   - Errores relacionados con FCM

**¿Hay logs de envío FCM cuando se creó ese reto?**

---

## 🎯 ESCENARIOS POSIBLES

### Escenario A: Token NO está en la base de datos
- **Causa**: El registro del token falló o no se llamó
- **Solución**: Verificar el endpoint de registro de tokens

### Escenario B: El envío FCM NO es automático
- **Causa**: Falta implementar el envío en el método de crear retos
- **Solución**: Agregar llamada a enviar FCM después de guardar el reto

### Escenario C: El envío falla silenciosamente
- **Causa**: Error en Firebase Admin SDK o token inválido
- **Solución**: Revisar logs del backend, verificar configuración Firebase

### Escenario D: Payload incorrecto
- **Causa**: Aún usan `notification` + `data`
- **Solución**: Cambiar a solo `data`

---

## ⏰ URGENCIA

Este problema está impactando directamente la experiencia de usuario. Los estudiantes NO están viendo cuando los retan, lo cual es una funcionalidad crítica de la app.

---

## 📞 RESPUESTA ESPERADA

Por favor respondan con:

1. ✅ / ❌ ¿El token está registrado en la base de datos?
2. ✅ / ❌ ¿El envío FCM es automático al crear retos?
3. ✅ / ❌ ¿Enviaron la notificación de prueba manual?
4. 📋 Logs del backend del reto ID 335
5. 📋 Código actual del método que crea retos (especialmente la parte de FCM)

---

**Gracias por su pronta atención.**

Saludos cordiales,  
**Equipo de Desarrollo Móvil Android - EDUEXCE**

---

**P.D.**: Mientras esperamos su respuesta, mantendremos la app abierta con Logcat activo para capturar cualquier notificación que llegue.

---

## 📱 INFORMACIÓN ADICIONAL

**Token FCM**: `eg9CzMsHTaWwyWxOeJYXLa:APA91bGxy8NPieT36rp5mLSdpngfpAE294E2WmMAZC9L2LryPes9bVeMbLI8fSFE3PziyOf55I5KDemSo82fIgoAGxp8pbQdN6ErXWw74HtnWIll7agvyu4`  
**Usuario ID**: 325  
**Device ID**: 205176cedc31cf98  
**Platform**: Android  
**Timestamp del token**: 2025-11-26 02:35:46  
