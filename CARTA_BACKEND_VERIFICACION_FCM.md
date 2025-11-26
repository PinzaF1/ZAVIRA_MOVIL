# 📱 Carta al Backend - Verificación de Payload FCM

**Fecha**: 2025-11-26  
**De**: Equipo Móvil (Android)  
**Para**: Equipo Backend  
**Asunto**: Verificación urgente de notificaciones de retos  

---

## 🎯 SITUACIÓN ACTUAL

Hemos implementado el manejo de notificaciones de retos según el formato que nos compartieron en su carta anterior. Sin embargo, **las notificaciones de reto NO aparecen en el historial de la app**.

## 🔧 LO QUE HICIMOS

✅ Implementamos el código para recibir notificaciones FCM  
✅ Implementamos el guardado en historial local  
✅ Implementamos la UI con chip morado para mostrar retador  
✅ **AGREGAMOS LOGS EXHAUSTIVOS** para diagnosticar el problema  

## 📊 FORMATO QUE ESPERAMOS (según su carta)

```json
{
  "notification": {
    "title": "¡Nuevo Reto!",
    "body": "Te han retado en Matemáticas"
  },
  "data": {
    "tipo": "reto_recibido",
    "retador_nombre": "Carlos Pérez",
    "area": "Matemáticas",
    "reto_id": "789",
    "retador_id": "123"
  }
}
```

## ✅ CAMPOS QUE NUESTRA APP NECESITA OBLIGATORIAMENTE

| Campo | Tipo | Ejemplo | ¿Obligatorio? |
|-------|------|---------|---------------|
| `tipo` | string | `"reto_recibido"` | ✅ SÍ |
| `retador_nombre` | string | `"Carlos Pérez"` | ✅ SÍ |
| `area` | string | `"Matemáticas"` | ✅ SÍ |
| `reto_id` | string | `"789"` | ⚠️ Recomendado |
| `retador_id` | string | `"123"` | ⚠️ Recomendado |

## 🔍 LO QUE NECESITAMOS DE USTEDES

### 1️⃣ CONFIRMAR que el backend está enviando el payload EXACTAMENTE como lo prometieron

Específicamente en el endpoint: `POST /movil/retos` (al crear un reto)

### 2️⃣ VERIFICAR que los 3 campos obligatorios están presentes:
- ✅ `data.tipo = "reto_recibido"`
- ✅ `data.retador_nombre = "[nombre del retador]"`
- ✅ `data.area = "[nombre del área]"`

### 3️⃣ REVISAR los logs de su servidor cuando envían una notificación

Busquen en sus logs del `RetosService.crearReto()` algo como:
```
✅ Enviando notificación FCM a token: xxxxx
📦 Payload: { ... }
```

### 4️⃣ Si es posible, enviarnos un ejemplo REAL del payload FCM que están enviando

## 📋 CÓMO PROBAR DE SU LADO

1. Crear un reto desde su backend/Postman
2. Verificar en los logs del servidor que se envió la notificación FCM
3. Copiar el payload EXACTO que enviaron
4. Compartirlo con nosotros

## 🚨 SÍNTOMAS DEL PROBLEMA

En nuestra app móvil:
- ❌ NO aparece ninguna notificación en el historial después de recibir un reto
- ❌ NO se muestra el chip morado del retador
- ✅ El token FCM sí está registrado correctamente

**Esto sugiere que:**
- O NO está llegando la notificación FCM
- O está llegando SIN los campos correctos en `data`

## 🎯 PRUEBA RÁPIDA QUE PUEDEN HACER

**Endpoint de Debug** (si lo tienen):
```
POST /debug/send-notification
Authorization: Bearer [JWT_ADMIN]
Content-Type: application/json

{
  "mode": "user",
  "targetUserId": 123,
  "title": "¡Nuevo Reto!",
  "body": "Te han retado en Matemáticas",
  "data": {
    "tipo": "reto_recibido",
    "retador_nombre": "Test Backend",
    "area": "Matemáticas",
    "reto_id": "999",
    "retador_id": "456"
  }
}
```

Si este endpoint existe, pueden usarlo para enviar una notificación de prueba directamente a nuestro dispositivo.

## 📞 PRÓXIMOS PASOS

1. **NOSOTROS**: Instalaremos la app con logs y les compartiremos el output completo
2. **USTEDES**: Crean un reto de prueba contra nuestra cuenta
3. **NOSOTROS**: Les enviamos los logs que muestren qué llegó (o qué NO llegó)
4. **JUNTOS**: Identificamos el problema

## 🙏 SOLICITUD URGENTE

Por favor confirmen **ANTES de las pruebas** que el payload FCM que están enviando es exactamente:

```json
{
  "notification": { ... },
  "data": {
    "tipo": "reto_recibido",           ← ¿SÍ o NO?
    "retador_nombre": "...",            ← ¿SÍ o NO?
    "area": "...",                      ← ¿SÍ o NO?
    "reto_id": "...",                   ← ¿SÍ o NO?
    "retador_id": "..."                 ← ¿SÍ o NO?
  }
}
```

---

**Gracias por su colaboración.**  
**Equipo Móvil Android - EDUEXCE**  

P.D. Tenemos logs exhaustivos implementados. En cuanto hagamos la prueba, podremos ver EXACTAMENTE qué está llegando a nuestra app.

