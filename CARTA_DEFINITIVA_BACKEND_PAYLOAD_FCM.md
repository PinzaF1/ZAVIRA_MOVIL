# 📧 CARTA DEFINITIVA AL BACKEND - Cambio Urgente en Payload FCM

**Fecha**: 2025-11-26  
**De**: Equipo Móvil Android  
**Para**: Equipo Backend  
**Asunto**: 🚨 URGENTE - Cambio requerido en payload FCM de notificaciones de retos  
**Prioridad**: ALTA  

---

## 🎯 RESUMEN EJECUTIVO

Hemos identificado y **CONFIRMADO** que las notificaciones de retos NO aparecen en el historial de notificaciones de la app debido al formato del payload FCM que están enviando.

**Problema**: El payload actual usa `notification` + `data`, lo cual hace que Android maneje la notificación automáticamente sin ejecutar nuestro código cuando la app está en segundo plano.

**Solución**: Cambiar el payload a **SOLO `data`** (eliminar el campo `notification`).

---

## 📊 EVIDENCIA DEL PROBLEMA

### ✅ Lo que FUNCIONA:
- Los retos se crean correctamente en el backend
- Los retos aparecen en el apartado "Retos Recibidos" de la app
- El token FCM del usuario está registrado correctamente
- Las notificaciones de prueba del endpoint de debug SÍ se guardan

### ❌ Lo que NO FUNCIONA:
- Cuando un usuario es retado, la notificación **NO aparece** en el historial de Notificaciones
- Los usuarios NO ven el chip morado del retador en Notificaciones
- La app NO ejecuta `onMessageReceived()` cuando llega la notificación

### 🔍 Causa Confirmada:
El payload FCM actual tiene este formato:
```json
{
  "notification": {  ← ESTE ES EL PROBLEMA
    "title": "¡Nuevo Reto!",
    "body": "Te han retado en Matemáticas"
  },
  "data": {
    "tipo": "reto_recibido",
    "retador_nombre": "...",
    "area": "..."
  }
}
```

Según la documentación oficial de Firebase:
> "When your app is in the background, Android directs notification messages to the system tray. A user tap on the notification opens the app launcher by default."
> 
> "Messages containing both notification and data payloads are treated as notification messages, so the `onMessageReceived()` callback will not be called when the app is in the background."

**Fuente**: https://firebase.google.com/docs/cloud-messaging/android/receive#handling_messages

---

## ✅ SOLUCIÓN REQUERIDA

Cambien el payload FCM a **SOLO `data`** (sin el campo `notification`):

### ❌ Código Actual (NO FUNCIONA en segundo plano):

```javascript
// En RetosService.crearReto() o donde envíen las notificaciones FCM

const message = {
  notification: {  // ← ELIMINAR ESTE OBJETO COMPLETO
    title: "¡Nuevo Reto!",
    body: `Te han retado en ${reto.area}`
  },
  data: {
    tipo: "reto_recibido",
    retador_nombre: retador.nombre,
    area: reto.area,
    reto_id: String(reto.id),
    retador_id: String(retador.id)
  },
  token: tokenFCM
};

await admin.messaging().send(message);
```

### ✅ Código Requerido (FUNCIONA SIEMPRE):

```javascript
// En RetosService.crearReto() o donde envíen las notificaciones FCM

const message = {
  data: {  // ← TODO EN DATA, SIN NOTIFICATION
    tipo: "reto_recibido",
    retador_nombre: retador.nombre,
    area: reto.area,
    reto_id: String(reto.id),
    retador_id: String(retador.id),
    title: "¡Nuevo Reto!",  // ← MOVER AQUÍ
    body: `Te han retado en ${reto.area}`  // ← MOVER AQUÍ
  },
  token: tokenFCM
};

await admin.messaging().send(message);
```

**Cambio simple**: Mover `title` y `body` del objeto `notification` al objeto `data`, y eliminar el objeto `notification` completamente.

---

## ⚡ BENEFICIOS DE ESTE CAMBIO

Con el payload correcto:

1. ✅ **Funcionará SIEMPRE**:
   - App en primer plano: ✅ Funciona
   - App en segundo plano: ✅ Funciona
   - App cerrada: ✅ Funciona

2. ✅ **onMessageReceived() se ejecutará SIEMPRE**:
   - Nuestro código se ejecuta en todos los escenarios
   - Las notificaciones se guardan en el historial local
   - Los usuarios ven el chip morado del retador

3. ✅ **Mejor experiencia de usuario**:
   - Los usuarios ven todas sus notificaciones de retos
   - Pueden revisar el historial completo
   - La app puede personalizar la notificación visual

---

## 🧪 PRUEBA DE VALIDACIÓN

Una vez realizado el cambio, haremos esta prueba:

1. **Cerrar la app** en el dispositivo del usuario 325
2. **Crear un reto** desde otro usuario contra el usuario 325
3. **Verificar que**:
   - ✅ La notificación aparece en el dispositivo
   - ✅ Al abrir la app, la notificación está en el historial
   - ✅ Se muestra el chip morado con el nombre del retador

---

## 📝 ARCHIVOS A MODIFICAR EN EL BACKEND

Busquen en su código el lugar donde envían notificaciones FCM de retos:

Posibles ubicaciones:
- `services/RetosService.js` o `services/RetosService.ts`
- `controllers/RetosController.js` o `controllers/RetosController.ts`
- `utils/fcm.js` o `utils/notifications.js`

Busquen código similar a:
```javascript
admin.messaging().send({
  notification: { ... },  // ← Esta parte
  data: { ... }
});
```

Y reemplacen con:
```javascript
admin.messaging().send({
  data: {
    ...todosLosCampos,  // Incluir title y body aquí
  }
});
```

---

## ⏰ URGENCIA

**Este cambio es crítico** para que las notificaciones de retos funcionen correctamente. Actualmente los usuarios NO están viendo las notificaciones en su historial, lo cual afecta la experiencia de usuario significativamente.

---

## 🔄 RETROCOMPATIBILIDAD

Este cambio **NO afectará** las notificaciones existentes porque:
- Los usuarios que ya recibieron notificaciones las verán igual
- Las nuevas notificaciones funcionarán correctamente desde el primer envío
- No requiere actualización de la app móvil (ya está preparada para recibir ambos formatos)

---

## 📞 COORDINACIÓN

Una vez realizado el cambio:
1. Por favor notifiquen al equipo móvil
2. Haremos pruebas inmediatas para validar
3. Confirmaremos que todo funciona correctamente
4. Actualizaremos la documentación

---

## 📚 REFERENCIAS

- **Firebase Cloud Messaging - Android**: https://firebase.google.com/docs/cloud-messaging/android/receive
- **Notification vs Data Messages**: https://firebase.google.com/docs/cloud-messaging/concept-options#notifications_and_data_messages
- **Best Practices**: https://firebase.google.com/docs/cloud-messaging/android/first-message

---

## ✅ CONFIRMACIÓN ESPERADA

Por favor respondan confirmando:
- [ ] ¿Dónde está el código que envía las notificaciones FCM de retos?
- [ ] ¿Cuándo pueden realizar el cambio?
- [ ] ¿Necesitan alguna aclaración adicional?

---

**Agradecemos su pronta atención a este tema.**

Saludos cordiales,  
**Equipo de Desarrollo Móvil Android - EDUEXCE**

---

**P.D.**: Si tienen dudas sobre la implementación, estamos disponibles para una reunión rápida o videollamada para explicar en detalle.
