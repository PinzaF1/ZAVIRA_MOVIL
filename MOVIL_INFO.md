# Móvil (Android) — Información necesaria

Archivo: `MOVIL_INFO.md`
Generado: 2025-12-09

## 1. Package / IDs
- applicationId / package name / versión:
  - Revisar en `app/build.gradle` (o `app/build.gradle.kts`). Ejemplo:
    applicationId "com.example.app"
    versionCode 12
    versionName "1.2.0"
  - Si hay parámetros en `gradle.properties`, copiar los valores reales allí.

## 2. Stack y versiones
- Kotlin: ver `build.gradle` (ej: `org.jetbrains.kotlin:kotlin-stdlib:1.8.x`).
- Java: comprobar `compileOptions` / `javaCompatibility` (11 o 17 según proyecto).
- Gradle Wrapper: revisar `gradle/wrapper/gradle-wrapper.properties` (ej: 7.6).
- Android SDK:
  - `minSdkVersion`: ver `app/build.gradle`.
  - `targetSdkVersion`: ver `app/build.gradle`.

> Copiar los valores concretos desde los archivos del proyecto al documento final.

## 3. Keystore / Firma
- Ruta sugerida (no subir al repositorio): `keystore/release.jks` (NO commitear ni compartir).
- Alias sugerido: `release_alias`.
- Comando ejemplo para generar localmente:
  keytool -genkey -v -keystore keystore/release.jks -alias release_alias -keyalg RSA -keysize 2048 -validity 10000

- Ejemplo de `signingConfigs` en `app/build.gradle` (usar variables/CI para contraseñas):
  signingConfigs {
    release {
      storeFile file("keystore/release.jks")
      storePassword System.getenv("KEYSTORE_PASSWORD")
      keyAlias "release_alias"
      keyPassword System.getenv("KEY_PASSWORD")
    }
  }

- Nota: usar secret manager o variables de entorno en CI; nunca guardar contraseñas ni .jks en el repo.

## 4. Build commands (Windows)
- Debug APK:
  .\gradlew.bat assembleDebug
- Release APK:
  .\gradlew.bat assembleRelease
- Release AAB (bundle):
  .\gradlew.bat bundleRelease
- Limpieza y build completo:
  .\gradlew.bat clean build

## 5. Variables / Configs (ejemplo)
- Archivos y ubicación:
  - `app/google-services.json` (Firebase)
  - `app/src/main/assets/config.json` (opcional)
- Variables/keys (lista, sin valores reales):
  - API_BASE_URL (ej: https://api.example.com)
  - SENTRY_DSN
  - GOOGLE_MAPS_KEY
  - FCM_SERVER_KEY (solo backend)
  - ENV (development|staging|production)
  - ANALYTICS_KEY
- Ejemplo de `.env.mobile` (no incluir en repo):
  API_BASE_URL=https://api.staging.example.com
  SENTRY_DSN=
  GOOGLE_MAPS_KEY=
  ENV=staging

## 6. Firebase / FCM
- `google-services.json` debe colocarse en: `app/google-services.json`.
- Pasos rápidos de configuración:
  1. Crear proyecto en Firebase Console.
  2. Registrar la aplicación con el mismo `applicationId`.
  3. Descargar `google-services.json` y colocarlo en `app/`.
  4. Habilitar Cloud Messaging; copiar la server key al backend (en secreto).
  5. Verificar que la app recibe y registra el token FCM y lo envía al backend.
- Verificar `com.google.gms:google-services` en `build.gradle`.

## 7. Deep Links / App Links
- Ejemplo de `intent-filter` para `AndroidManifest.xml` (añadir en el `Activity` correspondiente):
  <intent-filter android:autoVerify="true">
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data android:scheme="https" android:host="app.example.com" android:pathPrefix="/quiz" />
  </intent-filter>

- Rutas comunes usadas por la app (ejemplos):
  - https://app.example.com/quiz/{sessionId}
  - eduapp://quiz/{sessionId} (scheme custom)

## 8. Permisos necesarios
- INTERNET: `android.permission.INTERNET` (obligatorio).
- Almacenamiento (si aplica): `android.permission.READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`.
- Cámara (si usa captura): `android.permission.CAMERA`.
- Ubicación (si aplica): `android.permission.ACCESS_FINE_LOCATION`.
- Notificaciones (Android 13+): `android.permission.POST_NOTIFICATIONS` (solicitar en runtime si corresponde).

## 9. Endpoints consumidos (flujo de quiz)
Base: `API_BASE_URL` (reemplazar por la variable real).

1) Obtener detalle de sesión (incluye `analisis`)
- Método: GET
- Path: /movil/sesion/{id}/detalle
- Headers: Authorization: Bearer <token>
- Response relevante:
  {
    "header": { "materia": "sociales", "puntaje": 40, "correctas": 2, "incorrectas": 3, "total": 5 },
    "preguntas": [
      { "orden":1, "enunciado":"...", "es_correcta": false, "subtema":"hidrografía de colombia", "explicacion":"..." }
    ],
    "analisis": {
      "fortalezas": ["..."],
      "subtemas_a_mejorar": ["..."],
      "mejoras": ["..."],
      "recomendaciones": ["..."]
    }
  }

2) Crear/iniciar sesión de intento
- Método: POST
- Path: /movil/sesion
- Payload ejemplo:
  { "usuario_id": "u123", "materia": "sociales", "tipo": "evaluacion" }
- Response: { "sessionId": "s123", "inicio": "2025-12-08T12:00:00Z" }

3) Enviar respuesta a pregunta
- Método: POST
- Path: /movil/sesion/{id}/respuesta
- Payload ejemplo:
  { "pregunta_id": "p456", "opcion": "A", "tiempo_ms": 12000 }
- Response: { "correcto": true, "puntaje_obtenido": 2 }

4) Obtener perfil / progreso (opcional)
- Método: GET
- Path: /movil/usuario/{id}/progreso
- Response: resumen de estadísticas por áreas.

## 10. Flujos UX críticos
- Cerrar quiz (abandono): confirmar salida → guardar estado parcial → mostrar resumen.
- Perder vida / fin de vidas: animación de vida perdida → modal "necesitas practicar más" → opciones (ver detalle, recargar vida si aplica).
- Ver detalle / pestaña "Análisis": mostrar `header`, `preguntas` y `analisis` (fortalezas, subtemas, mejoras, recomendaciones). Asegurar que el backend envíe listas (aunque vacías) para evitar NPEs.

## 11. Assets y diseño
- Íconos: `app/src/main/res/mipmap-*` (ic_launcher)
- Drawables y gráficos: `app/src/main/res/drawable/`
- Recursos extra (SVG/PNG): `app/src/main/assets/images/` o `assets/images/`
- Si existe, guía de estilo o brand assets en `design/` o `docs/assets/`.

## 12. Métricas / Crash reporting
- Firebase Crashlytics: asegurar plugin y `google-services.json` configurado.
- Sentry: DSN en variable `SENTRY_DSN` y habilitar en inicialización de la app según `ENV`.
- Logs: usar logs estructurados y enviar solo en staging/production según configuración.

## 13. Localización
- Idiomas soportados: revisar carpetas `res/values-*/` en el proyecto.
  - `res/values/` (es)
  - `res/values-en/` (en)
  - Otros: listar según directorio `res/`.

---

Notas finales:
- No incluir secretos en el repo; usar secret manager o variables de entorno en CI.
- Antes de publicar la documentación final, copiar los valores concretos (applicationId, minSdk, targetSdk, versiones) desde los archivos del proyecto e insertarlos en este documento.

