# 📚 EduExce - Plataforma Educativa Móvil

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white)

**Plataforma educativa móvil diseñada para preparación de exámenes ICFES**

[Características](#-características) • [Instalación](#-instalación) • [Arquitectura](#-arquitectura) • [Contribuir](#-contribuir)

</div>

---

## 📖 Descripción

**EduExce** es una aplicación móvil Android completa para la preparación de exámenes ICFES, diseñada con las mejores prácticas de desarrollo y una experiencia de usuario excepcional. La aplicación combina gamificación, inteligencia artificial y seguimiento detallado del progreso académico.

### 🎯 Objetivo

Proporcionar a los estudiantes una plataforma interactiva y efectiva para prepararse para el ICFES, con preguntas generadas por IA alimentada con información oficial del ICFES, sistema de vidas, retos 1vs1, y análisis detallado del progreso.

---

## ✨ Características

### 🎮 Sistema de Gamificación

#### **Mapa de Islas del Conocimiento**
- 🗺️ Navegación visual por áreas de conocimiento
- 🏝️ 5 islas temáticas: Matemáticas, Lectura Crítica, Ciencias Naturales, Sociales y Ciudadanas, Inglés
- 🔓 Sistema de desbloqueo progresivo de niveles
- 📊 5 niveles por área + Examen Final

#### **Sistema de Vidas** ⭐ COMPLETO
- ❤️ 3 vidas por nivel (niveles 2+)
- ⏱️ **Recarga automática:** 5 minutos por vida
- 🎁 **Recarga por detalle:** Media vida al ver historial (una vez por intento)
- 🔄 Sistema de timestamps inteligente
- 🎨 UI en tiempo real con actualización cada segundo
- 📱 Diálogos informativos y visualización de corazones
- 🔐 Sistema anti-bugs con validación de flags

### 🤖 Preguntas con IA

- 🧠 **Generación inteligente** con OpenAI (GPT-4o-mini)
- 📚 Alimentada con información oficial del ICFES
- 🎲 Fallback automático a banco local
- 🏷️ Clasificación por área, subtema y nivel
- ✅ Diagnóstico automático del origen de preguntas
- 📊 Análisis de contenido IA vs. Banco local

### 🎯 Retos 1vs1

- 🤝 Sistema de retos en tiempo real
- 📨 Notificaciones push con Firebase Cloud Messaging (FCM)
- 🔔 Indicador de retos pendientes con badge
- ⚡ Lobby de espera con timeout
- 🏆 Sistema de puntuación competitiva
- 📝 Estados: Pendiente, Aceptado, Rechazado, Expirado

### 📈 Análisis y Progreso

#### **Historial Detallado**
- 📊 Estadísticas por sesión
- ✅ Correctas vs. Incorrectas
- ⏱️ Tiempo empleado
- 📈 Evolución de nivel (subió/bajó/igual)
- 🎯 Análisis por subtemas
- 💡 Recomendaciones personalizadas
- 🔄 **Actualización en tiempo real** (Broadcast system)

#### **Progreso Académico**
- 📱 Sincronización automática con backend
- 💾 Persistencia local con SharedPreferences
- 🔄 Sincronización bidireccional
- 🎨 Visualización de estadísticas
- 📊 Gráficos de rendimiento

### 🔐 Sistema de Autenticación

- 👤 Login seguro con JWT
- 🔑 Recuperación de contraseña
- 👨‍🎓 Perfiles de estudiante con foto
- 📝 Información institucional completa
- 🔄 Manejo automático de sesiones expiradas (401)

### 🌐 Integración Backend

- 🚀 **API REST** con Retrofit 2
- 📡 Interceptores personalizados (Auth, Logging, Session)
- 🔍 Sistema de Request-ID para debugging
- 📊 Manejo robusto de errores
- ⚡ Timeouts configurados (30s)
- 🔄 Sincronización asíncrona

---

## 🛠️ Tecnologías

### **Lenguaje y Framework**
- ☕ **Java 11**
- 📱 **Android SDK** (minSdk: 24, targetSdk: 34)
- 🎨 **Material Design 3**
- 🖼️ **View Binding**

### **Networking**
- 🌐 **Retrofit 2.9.0** - Cliente HTTP
- 📦 **Gson** - Serialización JSON
- 📝 **OkHttp Logging Interceptor** - Debugging

### **Firebase**
- 🔔 **Firebase Cloud Messaging (FCM)** - Notificaciones push
- 📊 **Firebase BOM 33.7.0**

### **UI/UX**
- 🎨 **Material Components 1.12.0**
- 🖼️ **Glide 4.16.0** - Carga de imágenes
- 🎭 **RecyclerView & CardView**
- 🎪 **ViewPager2** - Navegación de tabs

### **Utilidades**
- 💾 **SharedPreferences** - Persistencia local
- 📢 **LocalBroadcastManager** - Comunicación entre componentes
- ⏰ **Handler & Runnable** - Actualizaciones en tiempo real

---

## 🏗️ Arquitectura

### **Estructura del Proyecto**

```
app/src/main/java/com/example/zavira_movil/
│
├── 📱 Activities
│   ├── MainActivity.java              # Pantalla principal
│   ├── LoginActivity.java             # Autenticación
│   ├── TestActivity.java              # Exámenes
│   └── ResultActivity.java            # Resultados
│
├── 🎮 Niveleshome/
│   ├── QuizActivity.java              # Sistema de preguntas
│   ├── MapaActivity.java              # Mapa de islas
│   ├── LivesManager.java              # ⭐ Sistema de vidas
│   ├── ProgressLockManager.java       # Control de desbloqueo
│   └── MapeadorArea.java              # Mapeo áreas
│
├── 🏆 Retos1vs1/
│   ├── RetosFragment.java             # Lista de retos
│   ├── CrearRetoActivity.java         # Crear reto
│   ├── LobbyEsperaActivity.java       # Sala de espera
│   └── RetosPollingService.java       # Polling de retos
│
├── 📊 Progreso/
│   ├── FragmentHistorial.java         # Historial con actualización real-time
│   └── FragmentDetalleSimulacro.java  # Detalles de sesión
│
├── 🔄 Sincronizacion/
│   └── ProgresoSincronizador.java     # Sincronización backend
│
├── 🌐 Remote/
│   ├── RetrofitClient.java            # Cliente HTTP configurado
│   └── ApiService.java                # Endpoints API
│
├── 💾 Local/
│   └── TokenManager.java              # Gestión de tokens JWT
│
├── 🔔 Notifications/
│   └── MyFirebaseMessagingService.java # FCM Handler
│
└── 🛠️ Utils/
    └── ErrorHandler.java               # Manejo centralizado de errores
```

### **Patrones de Diseño**

- 🏛️ **Singleton** - RetrofitClient, Sincronizadores
- 🏭 **Factory** - Adaptadores y ViewHolders
- 👀 **Observer** - BroadcastReceivers para actualizaciones en tiempo real
- 🎯 **Repository** - Capa de abstracción de datos
- 🔌 **Interceptor** - Manejo de headers, auth y logging

---

## 📥 Instalación

### **Prerrequisitos**

- ☕ **JDK 11+**
- 🤖 **Android Studio Hedgehog (2023.1.1) o superior**
- 📱 **Android SDK 24+**
- 🔥 **Cuenta Firebase** (para notificaciones)

### **Pasos de Instalación**

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/eduexce-movil.git
   cd eduexce-movil
   ```

2. **Abrir en Android Studio**
   - File → Open → Seleccionar carpeta del proyecto

3. **Configurar Firebase**
   - Descargar `google-services.json` desde Firebase Console
   - Colocar en `app/google-services.json`

4. **Configurar Backend URL**
   - Editar `RetrofitClient.java`
   ```java
   private static final String BASE_URL = "https://tu-backend.com/";
   ```

5. **Sync Gradle**
   - Click en "Sync Now" cuando aparezca la notificación

6. **Ejecutar**
   - Click en Run ▶️ o `Shift + F10`

---

## 🔧 Configuración

### **Backend URL**

Editar en `app/src/main/java/com/example/zavira_movil/remote/RetrofitClient.java`:

```java
// PRODUCCIÓN
private static final String BASE_URL = "https://eduexce-backend.ddns.net/";

// EMULADOR
// private static final String BASE_URL = "http://10.0.2.2:3333/";

// DISPOSITIVO FÍSICO
// private static final String BASE_URL = "http://192.168.X.X:3333/";
```

### **Firebase FCM**

1. Ir a Firebase Console
2. Agregar app Android con package name: `com.example.zavira_movil`
3. Descargar `google-services.json`
4. Colocar en `app/`

---

## 🚀 Compilar APK

### **Debug APK**

```bash
./gradlew assembleDebug
```

**Output:** `app/build/outputs/apk/debug/app-debug.apk`

### **Release APK**

```bash
./gradlew assembleRelease
```

**Output:** `app/build/outputs/apk/release/app-release.apk`

### **Script PowerShell Incluido**

```powershell
.\compile.ps1
```

---

## 📚 Documentación Adicional

El proyecto incluye documentación detallada en archivos `.md`:

### **Sistema de Vidas**
- 📄 `SISTEMA_VIDAS_ANALISIS_FINAL.md` - Análisis completo del sistema de vidas
- 📄 `DIAGNOSTICO_BUGS_VIDAS.md` - Bugs corregidos
- 📄 `CORRECCIONES_VIDAS_IMPLEMENTADAS.md` - Implementación

### **Notificaciones y Retos**
- 📄 `FIX_NOTIFICACIONES_FCM.md` - Correcciones FCM
- 📄 `SOLUCION_NOTIFICACIONES_RETOS.md` - Sistema de retos
- 📄 `MEJORAS_UX_NOTIFICACIONES_COMPLETO.md` - UX mejorada

### **Integración IA**
- 📄 `RESUMEN_INTEGRACION_IA_OPENAI.md` - OpenAI integration
- 📄 `CARTA_BACKEND_IA.md` - Confirmación backend
- 📄 `TESTING_INTEGRACION_IA.md` - Testing

### **Backend**
- 📄 `BACKEND_AWS_CONFIGURADO.md` - Configuración AWS
- 📄 `EJEMPLO_BACKEND_NOTIFICACIONES_RETOS.md` - Endpoints

---

## 🧪 Testing

### **Comandos de Testing**

```bash
# Tests unitarios
./gradlew test

# Tests instrumentados (requiere emulador/dispositivo)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

### **Testing Manual**

Ver `GUIA_TESTING_HISTORIAL.md` para guías de testing específicas.

---

## 📱 Características del Sistema

### **Mínimos Requerimientos**

- 📱 **Android 7.0 (API 24)** o superior
- 💾 **50 MB** de espacio libre
- 🌐 **Conexión a Internet** (WiFi o datos móviles)
- 📶 **Firebase Cloud Messaging** habilitado

### **Recomendado**

- 📱 **Android 10.0 (API 29)** o superior
- 💾 **100 MB** de espacio libre
- 📶 **Conexión WiFi estable**

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor sigue estos pasos:

1. 🍴 **Fork** el proyecto
2. 🌿 Crea una rama para tu feature (`git checkout -b feature/NuevaCaracteristica`)
3. 💾 Commit tus cambios (`git commit -m 'Agregar nueva característica'`)
4. 📤 Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. 🔀 Abre un Pull Request

### **Guías de Estilo**

- ☕ Seguir convenciones Java estándar
- 📝 Comentarios en español
- 🧹 Código limpio y legible
- ✅ Tests para nuevas funcionalidades

---

## 🐛 Reportar Bugs

Si encuentras un bug, por favor crea un issue con:

1. 📝 Descripción detallada del problema
2. 📱 Dispositivo y versión de Android
3. 🔄 Pasos para reproducir
4. 📸 Screenshots (si aplica)
5. 📋 Logs relevantes

---

## 📋 Roadmap

### ✅ Completado

- ✅ Sistema de autenticación
- ✅ Mapa de islas del conocimiento
- ✅ Sistema de vidas completo
- ✅ Integración con IA (OpenAI)
- ✅ Retos 1vs1 con notificaciones
- ✅ Historial en tiempo real
- ✅ Sincronización con backend

### 🚧 En Desarrollo

- 🔄 Sistema de logros e insignias
- 🔄 Ranking global de estudiantes
- 🔄 Modo offline con sincronización diferida
- 🔄 Analytics avanzado

### 🎯 Planeado

- 📊 Dashboard para profesores
- 👥 Sistema de grupos/clases
- 📚 Biblioteca de recursos educativos
- 🎥 Video tutoriales integrados

---

## 👥 Equipo

### **Desarrolladores**

- 👨‍💻 **Bryan Hurtado** - Developer Principal
- 🤖 **GitHub Copilot** - AI Assistant

### **Agradecimientos**

- 🏫 **ICFES** - Por proporcionar información oficial
- 🤖 **OpenAI** - Por la generación inteligente de preguntas
- 🔥 **Firebase** - Por servicios de notificaciones
- 🌐 **Retrofit** - Por el excelente cliente HTTP

---

## 📄 Licencia

Este proyecto es parte de un proyecto educativo privado.

**Nota:** El código es propietario y no está disponible para uso comercial sin autorización.

---

## 📞 Contacto

- 📧 **Email:** soporte@eduexce.com
- 🌐 **Website:** [eduexce.com](https://eduexce.com)
- 💬 **Issues:** [GitHub Issues](https://github.com/tu-usuario/eduexce-movil/issues)

---

## 🎓 Sobre EduExce

**EduExce** nace de la necesidad de democratizar el acceso a la preparación de alta calidad para el examen ICFES en Colombia. Nuestra misión es proporcionar herramientas tecnológicas innovadoras que permitan a todos los estudiantes alcanzar su máximo potencial académico.

### **Valores**

- 🎯 **Excelencia** - Calidad en cada aspecto
- 🤝 **Accesibilidad** - Educación para todos
- 🚀 **Innovación** - Tecnología de vanguardia
- 📊 **Transparencia** - Análisis claro y detallado

---

<div align="center">

**⭐ Si este proyecto te ayudó, considera darle una estrella en GitHub ⭐**

Hecho con ❤️ por el equipo de EduExce

</div>

---

## 🔄 Última Actualización

**Versión:** 1.0.0  
**Fecha:** Diciembre 4, 2025  
**Estado:** ✅ Producción

### **Cambios Recientes (v1.0.0)**

- ✅ Sistema de vidas completamente implementado y probado
- ✅ Integración completa con OpenAI para generación de preguntas
- ✅ Sistema de retos 1vs1 con notificaciones FCM
- ✅ Historial con actualización en tiempo real
- ✅ Corrección de bugs críticos y optimizaciones
- ✅ Documentación completa del proyecto

---

## 📖 Apéndice: Estructura de Datos

### **Usuario**
```json
{
  "id_usuario": "325",
  "nombre": "Estudiante",
  "apellido": "Demo",
  "correo": "estudiante@eduexce.com",
  "grado": "11",
  "curso": "A",
  "jornada": "mañana"
}
```

### **Sesión de Quiz**
```json
{
  "idSesion": 2556,
  "area": "Ciencias Naturales",
  "nivel": 4,
  "correctas": 4,
  "incorrectas": 1,
  "puntaje": 80,
  "tiempo_total_seg": 300
}
```

### **Reto 1vs1**
```json
{
  "id_reto": 123,
  "creador": "Usuario1",
  "oponente": "Usuario2",
  "area": "Matemáticas",
  "estado": "pendiente",
  "fecha_creacion": "2025-12-04T10:00:00Z"
}
```

---

**🎓 EduExce - Excelencia en Educación Digital** 🚀

