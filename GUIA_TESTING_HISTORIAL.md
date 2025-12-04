# 🔍 GUÍA DE TESTING - PROBLEMA DEL HISTORIAL

**Fecha:** 2025-12-03  
**Commit:** 2d9442f  
**Problema:** Historial no muestra correctas, incorrectas ni demás información

---

## 📋 RESUMEN DEL PROBLEMA

El usuario reporta que al ver el detalle del historial:
- ❌ No se muestran las correctas
- ❌ No se muestran las incorrectas  
- ❌ No se muestra demás información (tiempo, porcentaje, etc.)

---

## 🔧 DEBUGGING IMPLEMENTADO

Se agregaron logs detallados en `FragmentDetalleSimulacro.java` para identificar el problema:

### **1. Logs en `onResponse()` - Recepción de Datos**

```
========================================
📥 RESPUESTA RECIBIDA DEL BACKEND
========================================
HTTP Status: 200/201/4XX/5XX
Successful: true/false

========================================
📊 DATOS RECIBIDOS:
========================================
✅ HEADER:
  - Materia: Matemáticas
  - Fecha: 2025-12-03
  - Nivel: Nivel 2
  - Correctas: 4
  - Incorrectas: 1
  - Total: 5
  - Puntaje: 80
  - Tiempo Total (seg): 300
  - Escala: porcentaje

✅ PREGUNTAS: 5 preguntas
  - Pregunta 1: ¿Cuál es la capital de...
  - Pregunta 2: ¿Qué valor tiene X en...
  - Pregunta 3: ¿Cuántos lados tiene...

✅ ANÁLISIS:
  - Por Tema: 3
  - Por Dificultad: 3
```

### **2. Logs en `bindHeader()` - Actualización de UI**

```
========================================
🔄 BINDING DATOS A LA UI
========================================
🎨 BIND HEADER - Iniciando...

✅ tvMateria actualizado: Matemáticas
✅ tvFecha actualizado: 3 de diciembre, 2025
✅ tvNivel actualizado: Nivel 2
✅ tvTiempo actualizado: 5:00 (seg: 300)
✅ tvCorr (correctas) actualizado: 4
✅ tvInc (incorrectas) actualizado: 1

📊 Cálculo porcentaje:
  - Escala: porcentaje
  - Puntaje: 80
  - Correctas: 4 / Total: 5
  - Porcentaje calculado: 80%

✅ tvPuntaje actualizado: 80%
✅ ProgressBar actualizado: 80/100

🎨 BIND HEADER - Completado
```

---

## 🧪 PASOS PARA TESTING

### **PASO 1: Ejecutar la App**

1. Abrir Android Studio
2. Conectar dispositivo o iniciar emulador
3. Run → Run 'app' (Shift+F10)

### **PASO 2: Navegar al Historial**

1. Completar un quiz (cualquier área/nivel)
2. Al finalizar, presionar "Ver Detalle"
3. Debería navegar a `FragmentDetalleSimulacro`

### **PASO 3: Abrir Logcat**

1. En Android Studio: View → Tool Windows → Logcat
2. En el filtro, escribir: `DETALLE_SIMU`
3. Seleccionar "Regex" si está disponible

### **PASO 4: Analizar los Logs**

Buscar las secciones con emojis:

#### **A) Verificar Recepción de Datos**

```
📥 RESPUESTA RECIBIDA DEL BACKEND
HTTP Status: ???
✅ HEADER: ???
```

**Posibles Escenarios:**

| Escenario | Logs | Diagnóstico | Solución |
|-----------|------|-------------|----------|
| **Backend OK** | `✅ HEADER: Correctas: 4, Incorrectas: 1` | Datos llegan correctamente | Verificar UI binding |
| **Backend Vacío** | `❌ ERROR: Header es NULL` | Backend no envía datos | Revisar endpoint backend |
| **HTTP Error** | `❌ ERROR: HTTP 404/500` | Problema de red/servidor | Verificar URL/conectividad |

#### **B) Verificar Binding de UI**

```
🎨 BIND HEADER - Iniciando...
✅ tvCorr actualizado: 4
✅ tvInc actualizado: 1
```

**Posibles Escenarios:**

| Escenario | Logs | Diagnóstico | Solución |
|-----------|------|-------------|----------|
| **UI OK** | `✅ tvCorr actualizado: 4` | Vista se actualiza | Problema resuelto |
| **Vista NULL** | `❌ tvCorr es NULL` | Vista no existe en layout | Revisar XML layout |
| **Datos 0** | `✅ tvCorr actualizado: 0` | Backend envía 0 | Revisar lógica backend |

---

## 🎯 DIAGNÓSTICO RÁPIDO

### **Caso 1: Backend No Envía Datos**

**Síntomas:**
```
❌ ERROR: Header es NULL
⚠️ PREGUNTAS es NULL
```

**Causa:** Problema en el backend o endpoint incorrecto

**Solución:**
1. Verificar URL del endpoint: `/movil/sesion/{id}/detalle`
2. Revisar respuesta del backend en navegador/Postman
3. Verificar que el `idSesion` sea correcto

### **Caso 2: Vistas NULL (Layout Incorrecto)**

**Síntomas:**
```
✅ HEADER: Correctas: 4, Incorrectas: 1
❌ tvCorr es NULL
❌ tvInc es NULL
```

**Causa:** IDs del layout no coinciden con el código

**Solución:**
1. Abrir `fragment_detalle_simulacro.xml`
2. Verificar que existan:
   - `android:id="@+id/tvCorrectas"`
   - `android:id="@+id/tvIncorrectas"`
   - `android:id="@+id/tvPuntaje"`
   - etc.

### **Caso 3: Datos Llegan pero No Se Muestran**

**Síntomas:**
```
✅ HEADER: Correctas: 4
✅ tvCorr actualizado: 4
(Pero en pantalla no se ve)
```

**Causa:** Vista oculta, color de texto invisible, o layout issue

**Solución:**
1. Verificar visibilidad en XML: `android:visibility="visible"`
2. Verificar color de texto: no sea transparente
3. Verificar tamaño de texto: no sea 0dp
4. Revisar constraints/layout que puedan ocultar la vista

---

## 📸 SCREENSHOTS ESPERADOS

### **Pantalla del Historial (Header)**

Debería mostrar:
- ✅ Materia (ej: "Matemáticas")
- ✅ Fecha (ej: "3 de diciembre, 2025")
- ✅ Nivel (ej: "Nivel 2")
- ✅ Tiempo (ej: "5:00")
- ✅ Correctas (ej: "4" en verde)
- ✅ Incorrectas (ej: "1" en rojo)
- ✅ Porcentaje (ej: "80%" con barra de progreso)

---

## 🔄 PRÓXIMOS PASOS SEGÚN DIAGNÓSTICO

### **Si Backend No Envía Datos:**
1. Reportar problema al equipo de backend
2. Proveer ID de sesión que falla
3. Compartir logs completos de `DETALLE_SIMU`

### **Si Vistas Son NULL:**
1. Revisar y corregir `fragment_detalle_simulacro.xml`
2. Asegurar que todos los IDs existen
3. Rebuild proyecto (Build → Rebuild Project)

### **Si Datos Llegan Pero No Se Ven:**
1. Revisar propiedades de visibilidad en XML
2. Verificar colores y tamaños de texto
3. Inspeccionar layout con Layout Inspector:
   - View → Tool Windows → Layout Inspector

---

## ✅ CHECKLIST DE VERIFICACIÓN

Antes de reportar el bug, verificar:

- [ ] App ejecutándose sin crashes
- [ ] Logcat abierto con filtro `DETALLE_SIMU`
- [ ] Se ve la sección `📥 RESPUESTA RECIBIDA`
- [ ] Se ve la sección `🎨 BIND HEADER`
- [ ] Captura de pantalla del problema
- [ ] Captura de logs completos de Logcat

---

## 📞 REPORTE DE BUG

Si después del testing el problema persiste, reportar con:

1. **Logs completos** de `DETALLE_SIMU` (copiar/pegar)
2. **Screenshot** de la pantalla del historial
3. **Valores esperados** vs **valores mostrados**
4. **Pasos exactos** para reproducir

---

**Commit:** 2d9442f  
**Branch:** feature/telemetria-ia-reportes  
**Fecha:** 2025-12-03  
**Estado:** ✅ Debugging implementado - Listo para testing

