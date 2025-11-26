# 🔧 GUÍA RÁPIDA: GENERAR APK (Solución a Errores de Testing)

**Problema Resuelto:** Errores de `package org.junit does not exist`  
**Solución Aplicada:** ✅ Dependencias de testing agregadas a `build.gradle.kts`

---

## ✅ CAMBIOS APLICADOS

### **Archivo Modificado:** `app/build.gradle.kts`

**Dependencias Agregadas:**
```kotlin
// Testing dependencies
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
```

Estas dependencias permiten que el archivo `ExampleUnitTest.java` compile correctamente.

---

## 🚀 PASOS PARA GENERAR EL APK

### **Opción 1: Desde Android Studio (RECOMENDADO)** ⭐

#### **1. Sincronizar Gradle**
```
1. Abre el proyecto en Android Studio
2. Espera a que se indexe
3. Verás un banner "Gradle files have changed"
4. Haz clic en "Sync Now"
5. Espera a que termine (1-2 minutos)
```

**Indicadores de éxito:**
- ✅ Barra de progreso en la parte inferior completa
- ✅ Sin errores en la pestaña "Build"
- ✅ Mensaje "BUILD SUCCESSFUL" en el log

#### **2. Limpiar el Proyecto**
```
Menu: Build → Clean Project
Espera a que termine (~30 segundos)
```

#### **3. Generar APK de Debug**
```
Menu: Build → Build Bundle(s) / APK(s) → Build APK(s)
```

**Tiempo estimado:** 2-5 minutos (primera vez)

#### **4. Ubicar el APK Generado**
Cuando termine, verás una notificación en la esquina inferior derecha:

```
APK(s) generated successfully
[locate] [analyze]
```

Haz clic en **"locate"** para abrir la carpeta.

**Ubicación exacta:**
```
C:\Users\bryan\StudioProjects\EDUEXCE_MOVIL\app\build\outputs\apk\debug\app-debug.apk
```

---

### **Opción 2: APK de Release (Para Distribución)** 🚀

Si necesitas un APK firmado para distribuir:

#### **1. Ir al menú Build**
```
Menu: Build → Generate Signed Bundle / APK
```

#### **2. Seleccionar APK**
```
Opción: APK (no AAB)
Click: Next
```

#### **3. Crear o usar Keystore**

**Si es la primera vez:**
```
Click: "Create new..."

Datos a llenar:
- Key store path: C:\Users\bryan\eduexce-keystore.jks
- Password: [tu contraseña segura]
- Alias: eduexce-key
- Validity: 25 años (por defecto)
- First and Last Name: EDUEXCE
- Organizational Unit: Development
- Organization: EDUEXCE
- City: [tu ciudad]
- State: [tu departamento]
- Country Code: EC (o tu código)

Click: OK
```

**Si ya tienes keystore:**
```
- Selecciona el archivo .jks
- Ingresa la contraseña
- Selecciona el alias
- Ingresa la contraseña del alias
Click: Next
```

#### **4. Seleccionar Build Variant**
```
Destination Folder: [dejar por defecto o elegir]
Build Variants: ✅ release
Signature Versions: ✅ V1 (Jar Signature)
                    ✅ V2 (Full APK Signature)
Click: Create
```

**APK Release generado en:**
```
C:\Users\bryan\StudioProjects\EDUEXCE_MOVIL\app\release\app-release.apk
```

---

## 🐛 SOLUCIÓN A ERRORES COMUNES

### **Error 1: "package org.junit does not exist"**
✅ **SOLUCIONADO** - Dependencias agregadas en este commit

### **Error 2: "Gradle sync failed: JAVA_HOME not set"**
**Solución:**
```
En Android Studio:
File → Project Structure → SDK Location
Verifica que "JDK location" esté configurado

Si no:
- Download → Descargar JDK 11 o 17
- Reinicia Android Studio
```

### **Error 3: "Unresolved reference: ErrorHandler"**
**Causa:** Archivo nuevo no sincronizado

**Solución:**
```
1. Build → Clean Project
2. Build → Rebuild Project
3. Invalidate Caches (si persiste):
   File → Invalidate Caches / Restart → Invalidate and Restart
```

### **Error 4: "Execution failed for task ':app:mergeDebugResources'"**
**Causa:** Recursos duplicados o conflictos

**Solución:**
```
1. Build → Clean Project
2. Borrar carpeta build manualmente:
   C:\Users\bryan\StudioProjects\EDUEXCE_MOVIL\app\build
3. Build → Rebuild Project
```

### **Error 5: "Failed to resolve: com.google.firebase:firebase-..."**
**Causa:** Problema de red o repositorio

**Solución:**
```
1. Verifica conexión a Internet
2. En build.gradle.kts (root), verifica que tengas:
   
   repositories {
       google()
       mavenCentral()
   }

3. Sincroniza de nuevo: Sync Now
```

---

## ⚡ ATAJOS ÚTILES EN ANDROID STUDIO

| Acción | Atajo |
|--------|-------|
| **Sync Now** | Ctrl + Shift + O (después de cambios en gradle) |
| **Build Project** | Ctrl + F9 |
| **Rebuild Project** | Ctrl + Shift + F9 |
| **Run App** | Shift + F10 |
| **Clean Project** | - (usar menú) |
| **Generate APK** | - (usar menú) |

---

## 📊 CHECKLIST PARA GENERAR APK

### **Antes de Generar:**
- [ ] Todos los errores de compilación resueltos
- [ ] Gradle sync completado exitosamente
- [ ] Clean Project ejecutado
- [ ] Cambios recientes guardados

### **Durante la Generación:**
- [ ] Ver progreso en la barra inferior
- [ ] Revisar pestaña "Build" por errores
- [ ] Esperar mensaje "BUILD SUCCESSFUL"

### **Después de Generar:**
- [ ] APK encontrado en carpeta outputs
- [ ] Tamaño del APK razonable (20-80 MB típico)
- [ ] (Opcional) Probar en dispositivo/emulador

---

## 🔍 VERIFICAR QUE EL APK FUNCIONA

### **1. Instalar en Emulador:**
```
En Android Studio:
1. Run → Run 'app' (Shift+F10)
2. Selecciona emulador
3. Verifica que la app inicie correctamente
```

### **2. Instalar en Dispositivo Físico:**
```
1. Habilita "Opciones de Desarrollador" en tu teléfono:
   - Ajustes → Acerca del teléfono
   - Toca "Número de compilación" 7 veces
   
2. Habilita "Depuración USB":
   - Ajustes → Opciones de desarrollador
   - Activa "Depuración USB"

3. Conecta el teléfono por USB

4. En Android Studio:
   - Run → Run 'app'
   - Selecciona tu dispositivo
```

### **3. Instalar APK Manualmente:**
```
1. Copia el APK a tu teléfono (USB o email)
2. En el teléfono, abre el archivo APK
3. Permite "Instalar desde fuentes desconocidas"
4. Instala la app
5. Abre y prueba
```

---

## 📝 NOTAS IMPORTANTES

### **Diferencia Debug vs Release:**

**APK Debug:**
- ✅ Más rápido de generar
- ✅ No requiere keystore
- ❌ Más grande (sin minificar)
- ❌ No optimizado
- **Usar para:** Pruebas internas

**APK Release:**
- ✅ Optimizado (ProGuard)
- ✅ Más pequeño
- ✅ Firmado (seguro)
- ❌ Tarda más en generar
- **Usar para:** Distribución, Play Store

### **Versión de la App:**
Ubicado en `app/build.gradle.kts`:
```kotlin
versionCode = 1        // Incrementar en cada release
versionName = "1.0"    // Versión visible para usuarios
```

### **Tamaño Esperado del APK:**
- **Debug:** 30-60 MB
- **Release (sin ProGuard):** 30-60 MB
- **Release (con ProGuard):** 20-40 MB

Si tu APK es >100 MB, revisa:
- Recursos innecesarios (imágenes grandes)
- Librerías duplicadas
- Assets sin comprimir

---

## 🎯 COMANDOS ÚTILES (Si tienes Java configurado)

```powershell
# Limpiar proyecto
.\gradlew clean

# Compilar sin tests
.\gradlew assembleDebug -x test

# Generar APK de Debug
.\gradlew assembleDebug

# Generar APK de Release (requiere keystore)
.\gradlew assembleRelease

# Ver todas las tareas disponibles
.\gradlew tasks
```

---

## ✅ RESUMEN

**Problema Original:** ❌ Errores de compilación en tests  
**Solución Aplicada:** ✅ Dependencias de testing agregadas  
**Estado Actual:** ✅ Listo para generar APK  

**Pasos Siguientes:**
1. Abrir Android Studio
2. Sync Now (sincronizar Gradle)
3. Build → Build APK(s)
4. ¡Listo! Tu APK estará en `app/build/outputs/apk/debug/`

---

**¿Dudas o errores?** Revisa la sección "Solución a Errores Comunes" arriba. 👆

**¡Éxito generando tu APK! 🎉**

