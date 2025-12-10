# 📋 RESOLUCIÓN: Preguntas Correctas e Incorrectas No Se Cargan

## 🔍 PROBLEMA IDENTIFICADO

Cuando el usuario:
1. Realiza un intento (quiz)
2. Pierde una vida (no aprueba)
3. Se muestra modal "Necesitas practicar más"
4. Hace clic en "Ver Detalle"
5. **El detalle del quiz muestra `preguntas: []` vacío**

**Causa raíz:** El endpoint backend `/movil/sesion/{id}/detalle` retorna preguntas vacías aunque la sesión fue respondida.

## ✅ SOLUCIÓN IMPLEMENTADA

### 1️⃣ **Crear Sistema de Caché Local**
**Archivo:** `QuizDetailCache.java`

```java
- saveDetail() → Guarda ProgresoDetalleResponse en SharedPreferences
- getDetail() → Recupera del caché
- clearDetail() → Limpia caché
```

**Ventajas:**
- Fallback automático cuando backend falla
- No requiere cambios en el backend
- Datos sincronizados localmente

---

### 2️⃣ **Guardar Detalle en QuizActivity**
**Archivo:** `QuizActivity.java` → Método `onCierreOk()`

Cuando se cierra la sesión (después de enviar respuestas):

```java
// Construir ProgresoDetalleResponse desde CerrarResponse
- Extraer header: materia, fecha, nivel, puntaje, correctas, incorrectas
- Extraer preguntas: convertir detalleResumen a lista de Pregunta
- Extraer análisis: inicializar vacío (el backend lo calcula)
- Guardar todo en caché local usando QuizDetailCache.saveDetail()
```

**Resultado:** Cada intento guardado localmente inmediatamente después de cerrarse.

---

### 3️⃣ **Usar Caché como Fallback en FragmentDetalleSimulacro**
**Archivo:** `FragmentDetalleSimulacro.java` → Método `onResponse()`

Cuando se recibe respuesta del backend:

```java
if (preguntas vacías AND header.total > 0) {
    // El usuario respondió pero backend retorna vacío
    cached = QuizDetailCache.getDetail(context, idSesion)
    if (cached tiene preguntas) {
        usar cached en lugar de respuesta del backend
    }
}
```

**Resultado:** Mostrar siempre las preguntas correctas e incorrectas, incluso si el backend falla.

---

## 🎯 FLUJO COMPLETO

```
┌─────────────────────────────────────────┐
│ Usuario realiza intento de quiz         │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│ Usuario envía respuestas                │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│ QuizActivity.onCierreOk() recibe datos  │
├─────────────────────────────────────────┤
│ ✅ Guardar en QuizDetailCache            │
│ ✅ Mostrar modal "Necesitas practicar"  │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│ Usuario hace clic "Ver Detalle"         │
└────────────────┬────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│ FragmentDetalleSimulacro carga datos    │
├─────────────────────────────────────────┤
│ Intenta GET /movil/sesion/{id}/detalle  │
└────────────────┬────────────────────────┘
                 ↓
        ┌────────┴────────┐
        ↓                 ↓
    ✅ ÉXITO         ❌ VACÍO
    Backend tiene  Backend retorna
    preguntas      preguntas vacío
        ↓                 ↓
    Usar datos      Usar caché local
    del backend     (QuizDetailCache)
        ↓                 ↓
        └────────┬────────┘
                 ↓
┌─────────────────────────────────────────┐
│ Mostrar pestaña PREGUNTAS con:          │
│ - Preguntas respondidas                 │
│ - Respuestas correctas e incorrectas    │
│ - Indicador visual de aciertos/errores  │
└─────────────────────────────────────────┘
```

---

## 📊 DATOS GUARDADOS EN CACHÉ

```json
{
  "header": {
    "materia": "Sociales y ciudadanas",
    "fecha": "2025-12-05T02:36:53",
    "nivel": "4",
    "nivelOrden": 4,
    "puntaje": 60,
    "escala": "porcentaje",
    "correctas": 3,
    "incorrectas": 2,
    "total": 5
  },
  "preguntas": [
    {
      "orden": 1,
      "correcta": "D",
      "marcada": "B",
      "es_correcta": false
    },
    {
      "orden": 2,
      "correcta": "A",
      "marcada": "C",
      "es_correcta": false
    },
    {
      "orden": 3,
      "correcta": "C",
      "marcada": "C",
      "es_correcta": true
    }
    // ... más preguntas
  ],
  "analisis": {
    "fortalezas": [],
    "mejoras": [],
    "recomendaciones": []
  }
}
```

---

## 🔧 ARCHIVOS MODIFICADOS

| Archivo | Cambio |
|---------|--------|
| `QuizDetailCache.java` | ✅ **CREADO** - Nueva clase para caché |
| `QuizActivity.java` | ✅ **MODIFICADO** - Guardar detalle en `onCierreOk()` |
| `FragmentDetalleSimulacro.java` | ✅ **MODIFICADO** - Usar caché como fallback |

---

## ✨ BENEFICIOS

✅ **Preguntas siempre disponibles** - Incluso si el backend falla  
✅ **Análisis accesible** - Ver qué se respondió correctamente/incorrectamente  
✅ **UX mejorada** - El usuario ve el detalle sin esperas innecesarias  
✅ **Sin cambios en backend** - Solución pura en cliente  
✅ **Fallback automático** - Usa caché si es necesario

---

## 🧪 PRUEBAS RECOMENDADAS

1. **Test 1: Flujo normal**
   - Intentar un quiz → Fallar → Ver Detalle
   - ✅ Verificar que se muestren las preguntas

2. **Test 2: Caché en acción**
   - Intentar quiz sin conexión al backend
   - ✅ Verificar que aún se muestren preguntas del caché

3. **Test 3: Múltiples intentos**
   - Hacer varios intentos en el mismo nivel
   - ✅ Verificar que cada uno tenga su caché correcto

---

## 📝 NOTA IMPORTANTE

El análisis de fortalezas/debilidades está inicializado vacío porque el backend es responsable de calcularlo. Cuando el backend retorne esos datos, se mostrarán automáticamente en la pestaña "Análisis".


