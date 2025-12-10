# 📋 RESPUESTA: Endpoint de Detalle de Quiz - ✅ RESUELTO

**Fecha:** 2025-12-08  
**Proyecto:** EDUEXCE/ZAVIRA  
**Estado:** ✅ RESUELTO - Backend corregido y Frontend alineado  

---

## ✅ CONFIRMACIÓN DE CORRECCIÓN

El equipo de backend implementó exitosamente la corrección del endpoint `GET /movil/sesion/{id}/detalle`.

### Cambios realizados por Backend:

1. **`POST /sesion/cerrar`**: Ahora persiste `detalleResumen` en el campo JSONB `detalle_resumen`
2. **`GET /movil/sesion/{id}/detalle`**: Lee desde JSONB cuando la sesión usó preguntas de IA

### Cambios realizados en Frontend Android:

Se actualizó el modelo `ProgresoDetalleResponse.java` para compatibilidad:

1. **`id_pregunta`**: Cambiado de `int` a `Integer` (nullable para preguntas de IA)
2. **`nivelOrden`** y **`nivelActual`**: Cambiados a `Integer` (nullable)
3. **`subtemas_a_mejorar`**: Nuevo campo agregado en `Analisis`
4. Agregadas anotaciones `@SerializedName` para mapeo correcto

---

## ⚠️ NOTA IMPORTANTE

**Sesiones antiguas** (antes de este fix) no tendrán `detalle_resumen` guardado, por lo que retornarán `preguntas: []`. Esto es esperado y no es un bug.

---

## 🧪 PARA PROBAR

1. Crear una nueva sesión de práctica
2. Responder las preguntas y cerrar la sesión
3. Presionar "Ver Detalle"
4. Verificar que las preguntas aparezcan con enunciado, explicación, etc.

---

*Documento actualizado: 2025-12-08*

---

## 🎯 DESCRIPCIÓN DEL PROBLEMA

### Flujo actual:
```
1. Usuario completa quiz (5 preguntas)
          ↓
2. POST /sesion/cerrar → Devuelve resultado ✅
   {
     "aprueba": false,
     "correctas": 3,
     "puntaje": 60,
     "detalleResumen": [
       {"orden":1, "correcta":"D", "marcada":"B", "es_correcta":false},
       {"orden":2, "correcta":"A", "marcada":"C", "es_correcta":false},
       ...
     ]
   }
          ↓
3. Usuario pierde vida, aparece diálogo "Ver Detalle"
          ↓
4. GET /movil/sesion/2692/detalle → Devuelve VACÍO ❌
   {
     "header": {
       "total": 0,        // ❌ Debería ser 5
       "correctas": 0,    // ❌ Debería ser 3
       "incorrectas": 0,  // ❌ Debería ser 2
       "puntaje": 0       // ❌ Debería ser 60
     },
     "preguntas": [],     // ❌ VACÍO - Debería tener 5 preguntas
     "analisis": {
       "fortalezas": [],
       "mejoras": [],
       "recomendaciones": []
     }
   }
          ↓
5. Pantalla de detalle aparece VACÍA
```

---

## 📤 LO QUE ENVÍA EL FRONTEND (Correcto)

### Request: `POST /sesion/cerrar`

```json
{
  "id_sesion": 2692,
  "respuestas": [
    {
      "orden": 1,
      "id_pregunta": 45231,
      "opcion": "B"
    },
    {
      "orden": 2,
      "id_pregunta": 45232,
      "opcion": "C"
    },
    {
      "orden": 3,
      "id_pregunta": 45233,
      "opcion": "C"
    },
    {
      "orden": 4,
      "id_pregunta": 45234,
      "opcion": "D"
    },
    {
      "orden": 5,
      "id_pregunta": 45235,
      "opcion": "A"
    }
  ]
}
```

**⚠️ IMPORTANTE:** El frontend ya envía el `id_pregunta` de cada pregunta respondida. El backend debe usar este ID para:
1. Guardar la relación sesión ↔ pregunta ↔ respuesta
2. Recuperar el enunciado y explicación al consultar el detalle

---

## ✅ LO QUE DEBE DEVOLVER EL BACKEND

### Response: `GET /movil/sesion/{id}/detalle`

```json
{
  "header": {
    "materia": "Sociales",
    "fecha": "2025-12-08T15:30:00.000+00:00",
    "nivel": "Básico",
    "nivelOrden": 4,
    "puntaje": 60,
    "escala": "porcentaje",
    "correctas": 3,
    "incorrectas": 2,
    "total": 5,
    "tiempo_total_seg": 45
  },
  "resumen": {
    "cambio": "igual",
    "mensaje": "Te mantuviste en el nivel",
    "nivelActual": 4
  },
  "preguntas": [
    {
      "orden": 1,
      "id_pregunta": 45231,
      "area": "Sociales",
      "subtema": "Historia de Colombia",
      "enunciado": "¿En qué año se firmó el Acta de Independencia de Colombia?",
      "correcta": "D",
      "marcada": "B",
      "es_correcta": false,
      "explicacion": "El Acta de Independencia de Colombia fue firmada el 20 de julio de 1810 en Santa Fe de Bogotá. La opción correcta es D (1810).",
      "tiempo_empleado_seg": 12
    },
    {
      "orden": 2,
      "id_pregunta": 45232,
      "area": "Sociales",
      "subtema": "Geografía",
      "enunciado": "¿Cuál es el río más largo de Colombia?",
      "correcta": "A",
      "marcada": "C",
      "es_correcta": false,
      "explicacion": "El río Magdalena es el río más largo de Colombia con aproximadamente 1,528 km de longitud.",
      "tiempo_empleado_seg": 8
    },
    {
      "orden": 3,
      "id_pregunta": 45233,
      "area": "Sociales",
      "subtema": "Cultura",
      "enunciado": "¿Qué género musical es originario de la costa atlántica colombiana?",
      "correcta": "C",
      "marcada": "C",
      "es_correcta": true,
      "explicacion": "La cumbia es un género musical y baile folclórico originario de la costa caribe colombiana.",
      "tiempo_empleado_seg": 5
    },
    {
      "orden": 4,
      "id_pregunta": 45234,
      "area": "Sociales",
      "subtema": "Economía",
      "enunciado": "¿Cuál es el principal producto de exportación de Colombia?",
      "correcta": "D",
      "marcada": "D",
      "es_correcta": true,
      "explicacion": "El petróleo es el principal producto de exportación de Colombia, seguido por el carbón y el café.",
      "tiempo_empleado_seg": 10
    },
    {
      "orden": 5,
      "id_pregunta": 45235,
      "area": "Sociales",
      "subtema": "Política",
      "enunciado": "¿En qué año se promulgó la actual Constitución de Colombia?",
      "correcta": "A",
      "marcada": "A",
      "es_correcta": true,
      "explicacion": "La Constitución Política de Colombia fue promulgada el 4 de julio de 1991.",
      "tiempo_empleado_seg": 10
    }
  ],
  "analisis": {
    "fortalezas": [
      "Buen conocimiento en cultura colombiana",
      "Dominio de temas de economía y política"
    ],
    "mejoras": [
      "Reforzar fechas históricas importantes",
      "Repasar geografía física de Colombia"
    ],
    "recomendaciones": [
      "Practicar con ejercicios de línea de tiempo histórica",
      "Revisar mapas hidrográficos de Colombia"
    ]
  }
}
```

---

## 🔧 CAMBIOS REQUERIDOS EN BACKEND

### 1. Modificar `POST /sesion/cerrar`

Al cerrar una sesión, **guardar en base de datos** la relación entre:
- ID de sesión
- ID de pregunta (viene en el request)
- Orden de la pregunta
- Opción marcada por el usuario
- Opción correcta
- Si fue correcta o no
- Tiempo empleado (opcional)

**Tabla sugerida:** `sesion_pregunta_respuesta`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | INT | PK autoincremental |
| id_sesion | INT | FK a tabla sesion |
| id_pregunta | INT | FK a tabla pregunta |
| orden | INT | Orden en que apareció (1-5) |
| marcada | CHAR(1) | Opción marcada: A, B, C, D |
| correcta | CHAR(1) | Opción correcta: A, B, C, D |
| es_correcta | BOOLEAN | true si marcada == correcta |
| tiempo_seg | INT | Segundos que tardó (opcional) |
| created_at | TIMESTAMP | Fecha de creación |

### 2. Modificar `GET /movil/sesion/{id}/detalle`

Hacer JOIN con las tablas necesarias para obtener el detalle completo:

```sql
SELECT 
    s.id as id_sesion,
    s.id_estudiante,
    s.id_area,
    s.nivel,
    s.puntaje,
    s.fecha_inicio,
    s.fecha_fin,
    
    -- Datos de la pregunta
    spr.orden,
    spr.id_pregunta,
    spr.marcada,
    spr.correcta,
    spr.es_correcta,
    spr.tiempo_seg,
    
    -- Datos del enunciado (de tabla pregunta)
    p.enunciado,
    p.explicacion,
    p.subtema,
    a.nombre as area
    
FROM sesion s
LEFT JOIN sesion_pregunta_respuesta spr ON spr.id_sesion = s.id
LEFT JOIN pregunta p ON p.id = spr.id_pregunta
LEFT JOIN area a ON a.id = p.id_area
WHERE s.id = :id_sesion
ORDER BY spr.orden ASC;
```

### 3. Calcular estadísticas en el header

```sql
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN es_correcta = true THEN 1 ELSE 0 END) as correctas,
    SUM(CASE WHEN es_correcta = false THEN 1 ELSE 0 END) as incorrectas,
    SUM(tiempo_seg) as tiempo_total_seg
FROM sesion_pregunta_respuesta
WHERE id_sesion = :id_sesion;
```

---

## 📊 DIAGRAMA DE FLUJO CORREGIDO

```
┌─────────────────────────────────────────────────────────────────┐
│                        FLUJO CORRECTO                           │
└─────────────────────────────────────────────────────────────────┘

FRONTEND                                    BACKEND
    │                                           │
    │  POST /sesion/cerrar                      │
    │  {id_sesion: 2692, respuestas: [...]}     │
    │─────────────────────────────────────────►│
    │                                           │
    │                                      ┌────┴────┐
    │                                      │ GUARDAR │
    │                                      │ en DB:  │
    │                                      │ sesion_ │
    │                                      │ pregunta│
    │                                      │_respues │
    │                                      │   ta    │
    │                                      └────┬────┘
    │                                           │
    │  {aprueba: false, correctas: 3, ...}      │
    │◄─────────────────────────────────────────│
    │                                           │
    │  [Usuario presiona "Ver Detalle"]         │
    │                                           │
    │  GET /movil/sesion/2692/detalle           │
    │─────────────────────────────────────────►│
    │                                           │
    │                                      ┌────┴────┐
    │                                      │  JOIN   │
    │                                      │ sesion  │
    │                                      │    +    │
    │                                      │pregunta │
    │                                      │    +    │
    │                                      │respuesta│
    │                                      └────┬────┘
    │                                           │
    │  {header: {...}, preguntas: [5], ...}     │
    │◄─────────────────────────────────────────│
    │                                           │
    │  [Mostrar detalle con preguntas]          │
    │                                           │
```

---

## 🧪 DATOS DE PRUEBA

Para probar la corrección, usar:

- **ID Sesión:** 2692
- **ID Usuario:** 368
- **Área:** Sociales y ciudadanas (en backend: "Sociales")
- **Nivel:** 4 (Básico)

---

## 📁 ARCHIVOS DEL FRONTEND (Solo referencia)

El frontend está listo y no requiere cambios. Estos archivos muestran cómo espera recibir los datos:

| Archivo | Descripción |
|---------|-------------|
| `ProgresoDetalleResponse.java` | Modelo que parsea la respuesta del backend |
| `FragmentDetalleSimulacro.java` | Fragment que muestra el detalle |
| `QuizActivity.java` | Activity que envía las respuestas y navega al detalle |
| `CerrarRequest.java` | Modelo del request con `id_pregunta` incluido |

---

## ⏰ URGENCIA

Esta funcionalidad es crítica porque:

1. **Impacta la experiencia de usuario:** El usuario espera ver qué preguntas falló
2. **Afecta el sistema de vidas:** Ver el detalle recarga media vida como recompensa
3. **Es parte del flujo de aprendizaje:** Sin ver errores, el usuario no puede mejorar

---

## 📞 CONTACTO

Si tienen dudas sobre la estructura de datos esperada o necesitan más información:

1. Revisar el archivo `ProgresoDetalleResponse.java` para ver el modelo exacto
2. Ver los logs del frontend con tag `DETALLE_SIMU` para debugging
3. Contactar al equipo de frontend para coordinar pruebas

---

**Atentamente,**  
Equipo de Desarrollo Frontend Android  
EDUEXCE/ZAVIRA Mobile App

---

*Documento generado: 2025-12-08*

