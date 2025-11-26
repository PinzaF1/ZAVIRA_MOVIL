# 🔥 EJEMPLO: Envío de Notificación de Reto desde el Backend

## 📤 Formato del Payload FCM

### **Opción 1: Usando Node.js con Firebase Admin SDK**

```javascript
const admin = require('firebase-admin');

async function enviarNotificacionReto(usuarioRetado, usuarioRetador, retoInfo) {
  const message = {
    // Notificación que aparece en la bandeja del sistema
    notification: {
      title: '¡Nuevo Reto! 🎮',
      body: `${usuarioRetador.nombre} te ha retado en ${retoInfo.area}`
    },
    
    // Datos adicionales para la app
    data: {
      tipo: 'reto_recibido',                    // OBLIGATORIO: identifica el tipo
      area: retoInfo.area,                       // Ej: "Matemáticas"
      retador_id: String(usuarioRetador.id),     // ID del usuario retador
      retador_nombre: usuarioRetador.nombre,     // OBLIGATORIO: nombre del retador
      retador_foto: usuarioRetador.foto_url || '',  // URL de la foto (opcional)
      reto_id: String(retoInfo.id)               // ID del reto para navegación
    },
    
    // Token FCM del usuario retado
    token: usuarioRetado.fcm_token
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('✅ Notificación enviada:', response);
    return response;
  } catch (error) {
    console.error('❌ Error al enviar notificación:', error);
    throw error;
  }
}

// Ejemplo de uso
const usuarioRetado = {
  id: 456,
  nombre: 'María García',
  fcm_token: 'dXyZ_AbC123...'
};

const usuarioRetador = {
  id: 123,
  nombre: 'Carlos Pérez',
  foto_url: 'https://ejemplo.com/fotos/carlos.jpg'
};

const retoInfo = {
  id: 789,
  area: 'Matemáticas',
  nivel: 'Intermedio'
};

await enviarNotificacionReto(usuarioRetado, usuarioRetador, retoInfo);
```

---

### **Opción 2: Usando Python con Firebase Admin SDK**

```python
import firebase_admin
from firebase_admin import credentials, messaging

# Inicializar Firebase (hacer esto una vez al inicio)
cred = credentials.Certificate('path/to/serviceAccountKey.json')
firebase_admin.initialize_app(cred)

def enviar_notificacion_reto(usuario_retado, usuario_retador, reto_info):
    """
    Envía una notificación de reto a un usuario.
    
    Args:
        usuario_retado: Dict con {id, nombre, fcm_token}
        usuario_retador: Dict con {id, nombre, foto_url}
        reto_info: Dict con {id, area}
    """
    
    message = messaging.Message(
        notification=messaging.Notification(
            title='¡Nuevo Reto! 🎮',
            body=f'{usuario_retador["nombre"]} te ha retado en {reto_info["area"]}'
        ),
        data={
            'tipo': 'reto_recibido',
            'area': reto_info['area'],
            'retador_id': str(usuario_retador['id']),
            'retador_nombre': usuario_retador['nombre'],
            'retador_foto': usuario_retador.get('foto_url', ''),
            'reto_id': str(reto_info['id'])
        },
        token=usuario_retado['fcm_token']
    )
    
    try:
        response = messaging.send(message)
        print(f'✅ Notificación enviada: {response}')
        return response
    except Exception as error:
        print(f'❌ Error al enviar notificación: {error}')
        raise

# Ejemplo de uso
usuario_retado = {
    'id': 456,
    'nombre': 'María García',
    'fcm_token': 'dXyZ_AbC123...'
}

usuario_retador = {
    'id': 123,
    'nombre': 'Carlos Pérez',
    'foto_url': 'https://ejemplo.com/fotos/carlos.jpg'
}

reto_info = {
    'id': 789,
    'area': 'Matemáticas'
}

enviar_notificacion_reto(usuario_retado, usuario_retador, reto_info)
```

---

### **Opción 3: Usando REST API de FCM directamente**

```bash
curl -X POST https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "token": "USER_FCM_TOKEN",
      "notification": {
        "title": "¡Nuevo Reto! 🎮",
        "body": "Carlos Pérez te ha retado en Matemáticas"
      },
      "data": {
        "tipo": "reto_recibido",
        "area": "Matemáticas",
        "retador_id": "123",
        "retador_nombre": "Carlos Pérez",
        "retador_foto": "https://ejemplo.com/fotos/carlos.jpg",
        "reto_id": "789"
      }
    }
  }'
```

---

## 🎯 Flujo Completo en el Backend

### **Cuando un usuario envía un reto:**

```javascript
// 1. Crear el reto en la base de datos
async function crearReto(idUsuarioRetador, idUsuarioRetado, area) {
  // Crear el reto
  const reto = await db.retos.create({
    id_usuario_retador: idUsuarioRetador,
    id_usuario_retado: idUsuarioRetado,
    area: area,
    estado: 'pendiente',
    fecha_creacion: new Date()
  });

  // 2. Obtener información de los usuarios
  const retador = await db.usuarios.findById(idUsuarioRetador);
  const retado = await db.usuarios.findById(idUsuarioRetado);

  // 3. Enviar notificación FCM
  if (retado.fcm_token) {
    await enviarNotificacionReto(
      {
        id: retado.id,
        nombre: retado.nombre,
        fcm_token: retado.fcm_token
      },
      {
        id: retador.id,
        nombre: retador.nombre,
        foto_url: retador.foto_perfil
      },
      {
        id: reto.id,
        area: reto.area
      }
    );
  }

  return reto;
}
```

---

## 📋 Campos del Payload

| Campo | Tipo | ¿Obligatorio? | Descripción | Ejemplo |
|-------|------|---------------|-------------|---------|
| `tipo` | string | ✅ SÍ | Debe ser **"reto_recibido"** | `"reto_recibido"` |
| `retador_nombre` | string | ✅ SÍ | Nombre del usuario que retó | `"Carlos Pérez"` |
| `area` | string | ✅ SÍ | Área del reto | `"Matemáticas"` |
| `reto_id` | string | ✅ SÍ | ID del reto (para navegación) | `"789"` |
| `retador_id` | string | ⚠️ Recomendado | ID del usuario retador | `"123"` |
| `retador_foto` | string | ❌ Opcional | URL de la foto del retador | `"https://..."` |

---

## 🧪 Prueba Rápida desde Firebase Console

### **Pasos:**

1. Ir a **Firebase Console** → **Cloud Messaging**
2. Click en **"Send test message"** o **"New notification"**
3. Llenar los campos:
   - **Notification title:** `¡Nuevo Reto! 🎮`
   - **Notification text:** `Carlos Pérez te ha retado en Matemáticas`
4. En **"Additional options"** → **"Custom data"**, agregar:
   ```
   tipo: reto_recibido
   retador_nombre: Carlos Pérez
   area: Matemáticas
   reto_id: 123
   retador_id: 456
   ```
5. Seleccionar el token FCM del dispositivo de prueba
6. Enviar

---

## 🔍 Verificación en Logs

### **En el servidor:**
```javascript
console.log('📤 Enviando notificación de reto:', {
  destinatario: usuarioRetado.nombre,
  retador: usuarioRetador.nombre,
  area: retoInfo.area,
  reto_id: retoInfo.id
});
```

### **En la app Android (Logcat):**
```
D/FCMService: Mensaje recibido de: ...
D/FCMService: Datos del mensaje: {tipo=reto_recibido, retador_nombre=Carlos Pérez, ...}
D/FCMService: ✅ Notificación guardada en el historial (tipo: reto_recibido)
```

---

## ⚠️ Errores Comunes y Soluciones

### **1. El nombre del retador no aparece**
❌ **Problema:** No se envía `retador_nombre` o está vacío
✅ **Solución:** Verificar que el payload incluye `retador_nombre` con un valor

### **2. La notificación no se muestra como reto**
❌ **Problema:** El campo `tipo` no es `"reto_recibido"`
✅ **Solución:** Asegurarse de que `tipo: "reto_recibido"` (exactamente así)

### **3. Token FCM no válido**
❌ **Problema:** El usuario no tiene token o está desactualizado
✅ **Solución:** 
- Verificar que el usuario tiene un token FCM guardado
- Actualizar el token cuando el usuario hace login
- Manejar errores de token inválido y eliminar tokens viejos

### **4. La notificación no llega**
❌ **Problema:** Múltiples causas posibles
✅ **Solución:**
- Verificar que Firebase Cloud Messaging está habilitado
- Confirmar que el dispositivo tiene conexión a internet
- Revisar los permisos de notificaciones en la app
- Verificar que el proyecto de Firebase está configurado correctamente

---

## 📱 Resultado Final en la App

Cuando todo está configurado correctamente, el usuario verá:

```
┌─────────────────────────────────────────────┐
│  🎮  ¡Nuevo Reto!                       ●   │
│  Carlos Pérez te ha retado en Matemáticas  │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │ 🎮 Carlos Pérez te ha retado        │   │
│  └─────────────────────────────────────┘   │
│  📚 Matemáticas                             │
│  Ahora                                      │
└─────────────────────────────────────────────┘
```

---

## 🚀 Próximos Pasos

1. **Implementar la función de envío** en tu backend
2. **Probar con Firebase Console** primero
3. **Integrar con el flujo de crear reto** en tu API
4. **Monitorear los logs** para verificar que funciona
5. **Implementar navegación** al hacer clic en la notificación (opcional)

---

**¡Listo para usar!** 🎉

Ahora tu backend puede enviar notificaciones de retos que se mostrarán correctamente en la app móvil con el nombre del retador.

