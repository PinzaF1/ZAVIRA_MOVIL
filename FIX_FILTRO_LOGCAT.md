# 🔧 FIX: Filtro de Logcat No Muestra Nada

## 🚨 PROBLEMA IDENTIFICADO

El filtro `FCMService` no muestra nada porque **`onMessageReceived()` NUNCA se está ejecutando**.

Sin embargo, veo que:
- ✅ La app está corriendo (logs de HomeActivity, RetosFragment, etc.)
- ✅ Hay un nuevo reto (ID 335, área Sociales, de Juan Sebastian)
- ❌ NO hay NINGÚN log de FCMService
- ❌ NO hay NINGÚN log de AppInit con el token FCM

**Esto significa que la notificación FCM NO está llegando a tu dispositivo.**

---

## ✅ SOLUCIÓN INMEDIATA

### Paso 1: Cambia el Filtro de Logcat

En Logcat, **QUITA el filtro** o usa uno más amplio:

#### Opción A: Sin Filtro
```
(deja el campo vacío para ver TODOS los logs)
```

#### Opción B: Filtro Amplio
```
package:com.example.zavira_movil
```

#### Opción C: Filtro por Tags Específicos
```
tag:AppInit|FCMService|NotificationStorage
```

---

### Paso 2: Reinicia la App y Busca el Token

1. **Cierra completamente la app** (Force Stop)
2. **Limpia Logcat** (icono de escoba 🧹)
3. **Abre la app**
4. **Busca en Logcat**:

```
D/AppInit: 🔥 TOKEN FCM AL INICIAR APP
D/AppInit: 📱 Token completo: [tu_token]
```

**SI NO VES ESTE LOG**, entonces hay un problema con la inicialización de Firebase.

---

### Paso 3: Si NO Aparece el Token

Si después de reiniciar la app NO aparece el log del token FCM, entonces:

#### Verificación 1: Firebase Inicializado
Busca en los logs:
```
D/AppInit: ✅ Firebase inicializado correctamente
```

#### Verificación 2: Error al Obtener Token
Busca:
```
E/AppInit: ❌ ERROR al obtener token FCM
```

---

## 🧪 PRUEBA ALTERNATIVA: Usa el Endpoint de Debug del Backend

Ya que el backend hizo el cambio, **pídeles que te envíen una notificación MANUALMENTE** usando su endpoint de debug:

```bash
POST https://churnable-nimbly-norbert.ngrok-free.dev/debug/send-notification
Authorization: Bearer [JWT_ADMIN_que_te_dieron]
Content-Type: application/json

{
  "mode": "user",
  "targetUserId": 325,
  "data": {
    "tipo": "reto_recibido",
    "retador_nombre": "Juan Sebastian Mejia Lopez",
    "area": "Sociales",
    "reto_id": "335",
    "retador_id": "319",
    "title": "¡Nuevo Reto!",
    "body": "Te han retado en Sociales"
  }
}
```

**IMPORTANTE**: El payload debe tener **SOLO `data`**, sin `notification`.

---

## 🔍 DIAGNÓSTICO DE TUS LOGS

De los logs que compartiste:

```
✅ App funcionando: SÍ
✅ Nuevo reto creado: SÍ (ID 335, Sociales)
✅ Reto aparece en "Recibidos": SÍ
❌ Log de AppInit con token: NO
❌ Log de FCMService: NO
❌ onMessageReceived() ejecutado: NO
```

**Conclusión**: La notificación FCM **NO está llegando** a tu dispositivo.

---

## 🎯 ACCIONES INMEDIATAS

### 1️⃣ Verifica el Token FCM

```
1. Force Stop de la app
2. Limpia Logcat
3. QUITA el filtro (o pon: package:com.example.zavira_movil)
4. Abre la app
5. Busca: "TOKEN FCM AL INICIAR APP"
6. Copia el token completo
```

### 2️⃣ Envía el Token al Backend

```
Una vez que tengas el token, envíaselo al backend y pídeles:

"Mi token FCM es: [token_completo]

¿Pueden verificar que este token está registrado en la base de datos?

SELECT * FROM fcm_tokens WHERE token = '[token_completo]';

Y por favor envíenme una notificación de prueba usando el endpoint de debug."
```

### 3️⃣ Verifica que el Backend Envía FCM Automáticamente

```
Pregunta al backend:

"¿El código que envía notificaciones FCM se ejecuta automáticamente 
cuando se crea un reto? ¿O solo funciona con el endpoint de debug?"

Deben confirmar que en el método que crea retos (crearReto()), 
también envían la notificación FCM.
```

---

## 📋 CHECKLIST DE VERIFICACIÓN

- [ ] Logcat sin filtro o con filtro amplio
- [ ] App reiniciada completamente
- [ ] Log de "TOKEN FCM AL INICIAR APP" visible
- [ ] Token FCM copiado
- [ ] Token enviado al backend para verificación
- [ ] Backend confirma que token está registrado
- [ ] Backend envía notificación de prueba manual
- [ ] Backend confirma que envío automático está implementado

---

## 🚨 SI AÚN NO APARECE EL TOKEN

Si después de reiniciar la app **NO aparece** el log del token FCM:

```
1. Verifica que google-services.json está en app/
2. Sincroniza Gradle: File > Sync Project with Gradle Files
3. Limpia el proyecto: Build > Clean Project
4. Rebuild: Build > Rebuild Project
5. Desinstala completamente la app del dispositivo
6. Instala de nuevo desde Android Studio
```

---

## 💡 NOTA IMPORTANTE

**El hecho de que NO veas logs de FCMService significa que:**

- El backend puede haber hecho el cambio en el código
- Pero la notificación FCM NO está llegando a tu dispositivo
- Esto puede ser porque:
  1. El backend no está enviando FCM automáticamente al crear retos
  2. O tu token FCM no está registrado correctamente
  3. O hay un problema de configuración en Firebase

**Por eso necesitamos ver el token FCM primero.**

---

**ACCIÓN INMEDIATA**: 
1. Quita el filtro de Logcat
2. Reinicia la app
3. Busca el log del token FCM
4. Compártelo con el backend
