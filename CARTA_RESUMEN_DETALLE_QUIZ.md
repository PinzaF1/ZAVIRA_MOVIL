# 📋 CARTA RESUMEN: PROBLEMA CON DETALLE DEL QUIZ

**Fecha:** 2025-12-08  
**Proyecto:** EDUEXCE_MOVIL (App Android)  
**Estado:** 🔴 Requiere corrección en BACKEND

---

## 🎯 DESCRIPCIÓN DEL PROBLEMA

Cuando un usuario pierde una vida en un quiz (no aprueba), aparece un diálogo con la opción **"Ver Detalle (Recarga media vida)"**. Al presionar este botón, debería navegar a una pantalla que muestre:

1. ✅ Las preguntas respondidas (correctas e incorrectas)
2. ✅ El análisis de la IA con fortalezas y áreas de mejora
3. ✅ Detalle de cada pregunta con la respuesta marcada vs la correcta

**Problema actual:** La pantalla de detalle aparece **VACÍA** (sin preguntas ni análisis).

---

## 🔍 EVIDENCIA DEL PROBLEMA (Logs del Usuario)

### 1️⃣ Respuesta del endpoint `/movil/sesion/finalizar` (CORRECTA ✅)

El endpoint de finalización SÍ devuelve el detalle correctamente:

```json
{
  "aprueba": false,
  "correctas": 3,
  "puntaje": 60,
  "puntajePorcentaje": 60,
  "duracionSegundos": 14,
  "resultado": "no_aprobado",
  "detalleResumen": [
    {"orden":1, "correcta":"D", "marcada":"B", "es_correcta":false},
    {"orden":2, "correcta":"A", "marcada":"C", "es_correcta":false},
    {"orden":3, "correcta":"C", "marcada":"C", "es_correcta":true},
    {"orden":4, "correcta":"D", "marcada":"D", "es_correcta":true},
    {"orden":5, "correcta":"A", "marcada":"A", "es_correcta":true}
  ]
}
```

### 2️⃣ Respuesta del endpoint `/movil/sesion/{id}/detalle` (INCORRECTA ❌)

Cuando el usuario presiona "Ver Detalle", la app llama a este endpoint y devuelve datos VACÍOS:

```json
{
  "header": {
    "materia": "sociales",
    "fecha": "2025-12-05T02:36:53.985+00:00",
    "nivel": "Básico",
    "nivelOrden": 4,
    "puntaje": 0,        // ❌ Debería ser 60
    "correctas": 0,      // ❌ Debería ser 3
    "incorrectas": 0,    // ❌ Debería ser 2
    "total": 0,          // ❌ Debería ser 5
    "tiempo_total_seg": 0
  },
  "resumen": {
    "cambio": "igual",
    "mensaje": "Te mantuviste",
    "nivelActual": 4
  },
  "preguntas": [],       // ❌ VACÍO - Debería tener las 5 preguntas con detalle
  "analisis": {
    "fortalezas": [],
    "subtemas_a_mejorar": [],
    "mejoras": [],
    "recomendaciones": []
  }
}
```

---

## 📊 FLUJO DEL PROBLEMA

```
Usuario completa quiz
         ↓
POST /movil/sesion/finalizar → Devuelve detalleResumen ✅
         ↓
Usuario pierde vida, aparece diálogo "Ver Detalle"
         ↓
GET /movil/sesion/{id_sesion}/detalle → Devuelve preguntas: [] ❌
         ↓
Pantalla de detalle aparece VACÍA
```

---

## 📁 ARCHIVOS DEL APP INVOLUCRADOS

### 1. QuizActivity.java (Línea ~1220)
Navega al detalle cuando el usuario presiona "Ver Detalle":

```java
private void irAlDetalle(int idSesion) {
    Intent intent = new Intent(this, HomeActivity.class);
    intent.putExtra("action", "show_detalle");
    intent.putExtra("id_sesion", idSesion);  // <- ID de la sesión
    intent.putExtra("materia", areaUi);
    intent.putExtra("nivel", nivel);
    intent.putExtra("initial_tab", 1); // Abrir en pestaña "Preguntas"
    startActivity(intent);
}
```

### 2. FragmentDetalleSimulacro.java (Línea ~176)
Hace la llamada al backend:

```java
interface ProgresoService {
    @GET("movil/sesion/{id}/detalle")
    Call<ProgresoDetalleResponse> getDetalleSesion(@Path("id") int id);
}
```

### 3. ProgresoDetalleResponse.java
Estructura de respuesta esperada:

```java
public class ProgresoDetalleResponse {
    public Header header;
    public Resumen resumen;
    public List<Pregunta> preguntas;  // <- Debería tener las preguntas
    public Analisis analisis;

    public static class Pregunta {
        public int orden;
        public int id_pregunta;
        public String area;
        public String subtema;
        public String enunciado;
        public String correcta;          // "A"|"B"|"C"|"D"
        public String marcada;           // "A"|"B"|"C"|"D"
        public boolean es_correcta;
        public String explicacion;
        public Integer tiempo_empleado_seg;
    }
}
```

---

## ✅ LO QUE NECESITA EL BACKEND

El endpoint `GET /movil/sesion/{id}/detalle` debe devolver:

```json
{
  "header": {
    "materia": "sociales",
    "fecha": "2025-12-05T02:36:53.985+00:00",
    "nivel": "Básico",
    "nivelOrden": 4,
    "puntaje": 60,
    "escala": "porcentaje",
    "correctas": 3,
    "incorrectas": 2,
    "total": 5,
    "tiempo_total_seg": 14
  },
  "resumen": {
    "cambio": "igual",
    "mensaje": "Te mantuviste",
    "nivelActual": 4
  },
  "preguntas": [
    {
      "orden": 1,
      "id_pregunta": 12345,
      "area": "Sociales",
      "subtema": "Historia de Colombia",
      "enunciado": "¿En qué año se firmó la independencia de Colombia?",
      "correcta": "D",
      "marcada": "B",
      "es_correcta": false,
      "explicacion": "La independencia de Colombia fue el 20 de julio de 1810...",
      "tiempo_empleado_seg": 8
    },
    {
      "orden": 2,
      "id_pregunta": 12346,
      "area": "Sociales",
      "subtema": "Geografía",
      "enunciado": "¿Cuál es la capital de Colombia?",
      "correcta": "A",
      "marcada": "C",
      "es_correcta": false,
      "explicacion": "La capital de Colombia es Bogotá...",
      "tiempo_empleado_seg": 5
    }
    // ... más preguntas
  ],
  "analisis": {
    "fortalezas": ["Buen manejo de geografía básica"],
    "mejoras": ["Repasar fechas históricas importantes"],
    "recomendaciones": ["Practicar más ejercicios de historia"]
  }
}
```

---

## 🔧 POSIBLES CAUSAS EN BACKEND

1. **Las preguntas no se guardan:** El endpoint `/finalizar` recibe las respuestas pero no las guarda en la tabla de detalle de sesión.

2. **Relación incorrecta:** El `id_sesion` que se usa para consultar el detalle no tiene las preguntas asociadas.

3. **Timing issue:** Las preguntas se guardan de forma asíncrona y cuando se consulta el detalle aún no están listas.

4. **Bug en query:** El SQL/query que obtiene las preguntas tiene un filtro incorrecto.

---

## 📝 NOTAS ADICIONALES

- El ID de sesión usado en el ejemplo: **2692**
- El usuario: ID **368**
- El área: **Sociales y ciudadanas** (backend: "Sociales")
- Nivel: **4** (Básico)

---

## 🚀 ACCIÓN REQUERIDA

El equipo de **BACKEND** debe revisar y corregir el endpoint:

```
GET /movil/sesion/{id}/detalle
```

Para que devuelva las preguntas con su detalle completo (enunciado, respuesta marcada, respuesta correcta, explicación).

---

## 📞 CONTACTO

Si necesitan más información sobre la estructura esperada o los datos que envía la app, revisar los siguientes archivos:

1. `app/src/main/java/com/example/zavira_movil/detalleprogreso/ProgresoDetalleResponse.java`
2. `app/src/main/java/com/example/zavira_movil/detalleprogreso/FragmentDetalleSimulacro.java`
3. `app/src/main/java/com/example/zavira_movil/niveleshome/QuizActivity.java`

---

**Creado por:** GitHub Copilot  
**Última actualización:** 2025-12-08

