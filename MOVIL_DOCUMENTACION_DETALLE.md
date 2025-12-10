Propósito:
Documento técnico para la parte móvil (Android) que recoge instalación, configuración, arquitectura relevante, despliegue y detalles operativos del cliente Android EDUEXCE/ZAVIRA. Está orientado a desarrolladores móviles, QA técnico y administradores que deben preparar builds y entender cómo se mapean recursos, colores y endpoints críticos del flujo "quiz".

Alcance:
Incluye lo que ya está implementado en el código fuente del repo: identificadores, stack y versiones, firma/keystore (guía), comandos de build, variables y archivos de configuración esperados, integración Firebase/FCM, deep links, permisos, endpoints consumidos por el flujo de quiz (request/responses esperadas), flujos UX críticos, assets y rutas en el repo, métricas/crash reporting y localización soportada.

---

Package / IDs:
- applicationId (package name): revisar `app/build.gradle` (buscar `applicationId`).
- package name usado en código: `com.example.zavira_movil` (revisar carpetas `app/src/main/java/com/example/zavira_movil`).
- versionCode / versionName: revisar `app/build.gradle` (propiedades en `versionCode` y `versionName`).

Stack y versiones:
- Kotlin/Java: el proyecto es mayoritariamente Java (archivos `.java`) con algunas dependencias modernas; revisar `build.gradle.kts` y `gradle.properties` para versiones.
- Gradle: ver `gradle/wrapper/gradle-wrapper.properties` y `gradlew` para la versión exacta.
- Android SDK: revisar `build.gradle` (minSdkVersion, targetSdkVersion) y `compileSdkVersion`. También `gradle.properties` puede contener versiones globales.

Keystore / Firma (guía):
- Nunca subir keystore al repo. Mantener `.jks` en un almacenamiento seguro (Vault, S3 privado, Google Drive restringido).
- Ruta local de ejemplo (no real): `/home/ci/keystores/eduexce_release.jks` o `C:\keystores\eduexce_release.jks`.
- Alias: ejemplo `eduexce_alias` (confirmar con el equipo responsable de release).
- Comandos para firmar (localmente) usando `./gradlew` (release bundle/apk):
  - Generar artefacto (release): `./gradlew assembleRelease` (genera APKs en `app/build/outputs/apk/release/`).
  - Generar bundle: `./gradlew bundleRelease` (genera AAB en `app/build/outputs/bundle/release/`).
  - Firmar y alinear manualmente (si fuese necesario):
    - jarsigner (si ya tienes el keystore y el apk unsigned): `jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore /ruta/a/keystore.jks app-release-unsigned.apk eduexce_alias`
    - zipalign: `zipalign -v -p 4 app-release-unsigned.apk app-release.apk` (ubicado en Android SDK build-tools).
- Nota: Se recomienda usar signingConfigs en `app/build.gradle` con variables CI/secretas (no en VCS). Ejemplo en `gradle.properties` o en variables de entorno en CI:
  - SIGNING_STORE_FILE
  - SIGNING_STORE_PASSWORD
  - SIGNING_KEY_ALIAS
  - SIGNING_KEY_PASSWORD

Build commands (exactos):
- Compilar debug: `./gradlew assembleDebug`
- Compilar release (signed en CI/local): `./gradlew assembleRelease`
- Generar bundle (AAB): `./gradlew bundleRelease`
- Limpiar: `./gradlew clean`
- Ejecutar lint (si aplica): `./gradlew lint` (dependiendo del setup)

Variables / Configs (archivo ejemplo / lista):
- Archivos esperados en `app/src`:
  - `google-services.json` (ubicación: `app/` raíz del módulo `app`).
- Variables de entorno / Configs (ejemplos, NO incluir valores reales):
  - API_BASE_URL=https://api.eduexce.example
  - FIREBASE_PROJECT_ID=eduexce-project
  - GOOGLE_SERVICES_JSON=*(archivo `google-services.json` en `app/`)*
  - MAPA_KEYS (si aplican)
  - OAUTH_CLIENT_ID
  - SIGNING_STORE_FILE (ruta local en CI)
  - SIGNING_STORE_PASSWORD
  - SIGNING_KEY_ALIAS
  - SIGNING_KEY_PASSWORD

Ejemplo de `.env.mobile` (no subir al repo):
- API_BASE_URL=https://api.staging.eduexce
- ENV=staging
- FIREBASE_ENABLED=true
- ANALYTICS_ENABLED=true

Firebase / FCM (configuración mínima):
- `google-services.json` debe colocarse en `app/`.
- El proyecto utiliza Firebase Messaging (en `HomeActivity` hay verificación de token FCM y uso de `FirebaseMessaging.getInstance().getToken()`).
- Pasos para configurar FCM (resumen):
  1. Crear proyecto en Firebase Console.
  2. Añadir app Android con package `com.example.zavira_movil` y descargar `google-services.json`.
  3. Colocar `google-services.json` en `app/` y sincronizar gradle.
  4. Asegurar `com.google.firebase:firebase-messaging` en `build.gradle`.
  5. Revisar código: token se guarda en `SharedPreferences` (`fcm_prefs`, key `fcm_token`).
  6. Revisar `AndroidManifest.xml` y `FirebaseMessagingService` si existe para manejar mensajes.

Deep Links / App Links:
- Buscar en código `intent` que use `Uri` o `intent-filter` en `AndroidManifest.xml`.
- En este repo se usan Intents explícitos para navegación interna (Activities), y `HomeActivity` procesa `Intent` extras `action=show_detalle` para abrir el `FragmentDetalleSimulacro`.
- Ejemplo de intent para abrir detalle desde QuizActivity:
  - Intent a `HomeActivity` con extras:
    - `action = "show_detalle"`
    - `id_sesion` (int)
    - `materia` (string)
    - `initial_tab` (int) (0=overview, 1=preguntas)
- Ejemplo de `intent-filter` (AndroidManifest) si se quiere soportar app links:
  - <intent-filter>
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data android:scheme="https" android:host="app.eduexce.example" android:pathPrefix="/open" />
    </intent-filter>

Permisos:
- Revisar `AndroidManifest.xml` para la lista exacta. En general el app requiere:
  - android.permission.INTERNET (red)
  - android.permission.ACCESS_NETWORK_STATE (estado de red)
  - (Opcional según features) android.permission.CAMERA
  - android.permission.ACCESS_FINE_LOCATION o ACCESS_COARSE_LOCATION (si hay geolocalización; no evidenciado en la parte leída)
- Recomendación: mantener permisos mínimos y solicitar en runtime para peligrosos (cámara, ubicación).

Endpoints consumidos (flujo de quiz) — paths principales y ejemplos:
- POST /sesion/parada
  - Descripción: Crear una "parada" / sesión para iniciar el quiz con parámetros (area, subtema, nivel, etc.).
  - Request (ejemplo JSON):
    {
      "area": "matematicas",
      "subtema": "operaciones",
      "nivel_orden": 2,
      "usa_estilo_kolb": true,
      "intento_actual": 1
    }
  - Response esperado (ejemplo):
    {
      "sesion": { "idSesion": 123, "preguntas": [...], ... },
      "preguntas": [...],
      "preguntasPorSubtema": [...]
    }
  - Uso en app: `QuizActivity.crearParadaYMostrar()` -> `api.crearParada(req)`

- POST /sesion/cerrar
  - Descripción: Enviar respuestas al finalizar la sesión. Puede aceptar formato "nuevo" o fallar y requerir compat (legacy) que use `cerrarSesionCompat`.
  - Request (nuevo formato):
    {
      "id_sesion": 123,
      "respuestas": [ { "orden": 1, "id_pregunta": 456, "opcion": "A" }, ... ]
    }
  - Response esperado: contiene `puntaje`, `correctas`, `aprueba` booleano y otros metadatos.
  - Uso en app: `QuizActivity.enviarTodasLasRespuestas()` -> `api.cerrarSesion(...)` y fallback `cerrarSesionCompat`.

- GET /movil/sesion/{id}/detalle
  - Descripción: Obtener detalle de una sesión ya creada/contestada (usada por el detalle del simulacro).
  - Response esperado: JSON con preguntas, análisis y demás campos que permitan mostrar detalle.
  - Nota importante: Historial en repo muestra que este endpoint tuvo problemas (devolvía vacío) y fue corregido en backend por migraciones. Revisar `VERIFICACION_DETALLE_PREGUNTAS.md` y `FIX_DETALLE_QUIZ_RESUELTO.md`.

- Otros endpoints relacionados (mencionados en docs del repo):
  - POST /sesion/finalizar (o /movil/sesion/finalizar) — finaliza sesión y devuelve resumen (detalleResumen) — revisar `CARTA_RESUMEN_DETALLE_QUIZ.md`.
  - GET /perfil (o endpoint similar) — usado por `getPerfilEstudiante()` en `HomeActivity`.

Formato/contract mínimo (contrato) para cada endpoint del flujo quiz:
- Inputs: area (string), subtema (string), nivel (int), id_sesion (int), respuestas (array ordenado), token auth.
- Outputs: id_sesion, preguntas[] (con id_pregunta opcional), puntaje, correctas, aprueba bool, detalleResumen.
- Modo fallo: 400/500 con body con mensaje; en 400 legacy se detecta mensaje específico `cannot extract elements from an object` y se reintenta con formato compatible.

Flujos UX críticos (pantallas y pasos):
1) Inicio del quiz desde Mapa/Isla (MapaActivity -> nivelhotspot):
   - Usuario toca hotspot del nivel en `MapaActivity`.
   - Se crea Intent a `QuizActivity` con extras: `EXTRA_AREA`, `EXTRA_SUBTEMA`, `EXTRA_NIVEL`.
   - `QuizActivity.crearParadaYMostrar()` -> POST `/sesion/parada` -> recibe `ParadaResponse`.
   - Muestra la primera pregunta en `QuizQuestionsAdapter`.

2) Avanzar preguntas y enviar respuestas:
   - Usuario responde cada pregunta, `siguientePregunta()` guarda respuestas y avanza.
   - Al finalizar, `enviarTodasLasRespuestas()` envía POST `/sesion/cerrar`.
   - Manejo de respuestas: si backend responde `aprueba=true`, desbloquea siguiente nivel; si `aprueba=false`, consume vida y muestra `DialogoVidas`.

3) Ver detalle (recarga media vida):
   - Desde diálogo de vidas el usuario puede elegir "Ver Detalle".
   - `irAlDetalle(idSesion)` realiza `LivesManager.recargarPorDetalle()` y lanza `HomeActivity` con extras `action=show_detalle`, `id_sesion`, `materia`, `initial_tab`.
   - `HomeActivity` al iniciar detecta `action=show_detalle` y crea `FragmentDetalleSimulacro` con `id_sesion` y `materia`.

4) Cambio/mezcla de colores (problema reportado):
   - El app usa colores por área (matemáticas rojo, lectura azul, ciencias verde, inglés morado, etc.).
   - Colores se obtienen con helper `obtenerColorArea()` (en `QuizActivity` y otros) y `colorFor()` (en `SubjectAdapter`).
   - Causa frecuente de mezcla: uso de variables globales o handlers que forzan status bar color sin considerar el fragment actual.
   - Mitigación aplicada: en `HomeActivity` el handler que forzaba constantemente el color fue convertido a campo cancelable y reducido en frecuencia (200ms) y se cancela en `onPause/onDestroy`.

Assets y diseño (rutas en repo):
- Iconos y fondos por área en `app/src/main/res/drawable/`:
  - `fondomatematicas`, `fondiespa`, `fondosociales`, `fondonaturales`, `fondoingles` (usados en `SubjectAdapter.bgFor`).
  - `bg_header_science` y `bg_icon_blue.xml` (ver `DRAWABLES_CREADOS.md`).
- Layouts principales en `app/src/main/res/layout/` (por ejemplo `activity_quiz.xml`, `item_subject_card.xml`, `dialog_vidas_nivel.xml`).
- Rutas en el repo: `app/src/main/res/drawable/*`, `app/src/main/res/layout/*`, `app/src/main/java/com/example/zavira_movil/*`.

Métricas / Crash reporting:
- Firebase Crashlytics: verificar si `com.google.firebase:firebase-crashlytics` está presente en `build.gradle`.
- Sentry: si se usa, revisar inicialización y DSN en código/gradle.
- En `HomeActivity` y `QuizActivity` abundan logs `android.util.Log.d/e` para debugging — útil en QA.
- Asegurar claves/DSNs en CI/secret manager, no en el repo.

Localización:
- Idiomas soportados: revisar `res/values/` y `res/values-xx/` carpetas.
- Archivos típicos: `res/values/strings.xml`, `res/values-es/strings.xml` etc.

Notas de implementación y verificación (no incluir pruebas ni Play store):
- Para reproducir el problema de mezcla de colores: navegar entre `HomeActivity` (status bar azul) -> `MapaActivity` (status bar transparente) -> `QuizActivity` (status bar blanco o con color del área). Verificar que cada Activity/Fragment establezca su color en `onResume` y que no haya handlers activos en background forzando colores.
- Revisar `ProgressLockManager`, `LivesManager` y `LivesManager.recargarPorDetalle()` para entender recargas al abrir detalle.
- Revisar `VERIFICACION_DETALLE_PREGUNTAS.md`, `FIX_DETALLE_QUIZ_RESUELTO.md` y `CARTA_RESUMEN_DETALLE_QUIZ.md` para historial de bugs y cambios aplicados en backend.

Prácticas recomendadas:
- Centralizar mapeo materia→color en un helper único (por ejemplo `AreaColors.java`) que devuelva `@ColorInt` constantes y `ColorStateList` para mantener coherencia.
- Evitar handlers globales que modifiquen UI del sistema sin chequear lifecycle; usar `LifecycleObserver` o `LifecycleScope` para manejar cambios y cancelar en `onPause`/`onStop`.
- Asegurar que `EdgeToEdge` o flags de `WindowInsets` no sobreescriban colores sin intención.
- Mantener `google-services.json` fuera del VCS y documentar cómo obtenerlo desde Firebase Console.

---

Archivos del repo relacionados (puntos de interés):
- `app/src/main/java/com/example/zavira_movil/Home/HomeActivity.java` (gestión status bar, hotspots, navegación a detalle)
- `app/src/main/java/com/example/zavira_movil/niveleshome/QuizActivity.java` (flujo de quiz, enviar/parada, vidas)
- `app/src/main/java/com/example/zavira_movil/niveleshome/MapaActivity.java` (mapa por materia, blur color logic `getBlurColorForSubject`)
- `app/src/main/java/com/example/zavira_movil/niveleshome/SubjectAdapter.java` (mapeo imagenes y color por área)
- `VERIFICACION_DETALLE_PREGUNTAS.md`, `FIX_DETALLE_QUIZ_RESUELTO.md`, `CARTA_RESUMEN_DETALLE_QUIZ.md` (historial y decisiones relacionadas)

Contacto / Siguientes pasos:
- Si quieres, aplico una refactorización para centralizar los mapeos materia→color en un helper y actualizar usos actuales para eliminar inconsistencias.
- También puedo añadir logs estructurados o una clase utilitaria `AreaColorManager` y escribir una prueba rápida para verificar que `obtenerColorArea()` y `getBlurColorForSubject()` devuelven valores consistentes para las mismas materias.

Fin del documento.

