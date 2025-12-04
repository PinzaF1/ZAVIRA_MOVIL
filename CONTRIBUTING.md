# 🤝 Guía de Contribución - EduExce

¡Gracias por tu interés en contribuir a **EduExce**! Esta guía te ayudará a empezar.

---

## 📋 Tabla de Contenidos

1. [Código de Conducta](#código-de-conducta)
2. [¿Cómo Puedo Contribuir?](#cómo-puedo-contribuir)
3. [Proceso de Desarrollo](#proceso-de-desarrollo)
4. [Guías de Estilo](#guías-de-estilo)
5. [Estructura del Proyecto](#estructura-del-proyecto)
6. [Testing](#testing)
7. [Commits](#commits)

---

## 📜 Código de Conducta

Al participar en este proyecto, te comprometes a mantener un ambiente respetuoso y profesional. Se espera:

- ✅ Respeto hacia todos los colaboradores
- ✅ Críticas constructivas
- ✅ Comunicación clara y profesional
- ❌ Lenguaje ofensivo o discriminatorio
- ❌ Ataques personales

---

## 🚀 ¿Cómo Puedo Contribuir?

### 🐛 Reportar Bugs

1. Verifica que el bug no haya sido reportado
2. Abre un nuevo issue con el template
3. Incluye:
   - Descripción detallada
   - Pasos para reproducir
   - Comportamiento esperado vs. actual
   - Screenshots si aplica
   - Versión de Android y dispositivo

### 💡 Sugerir Mejoras

1. Abre un issue con la etiqueta `enhancement`
2. Describe la mejora propuesta
3. Explica por qué sería útil
4. Si es posible, incluye mockups o ejemplos

### 🔧 Contribuir Código

1. Fork el repositorio
2. Crea una rama descriptiva
3. Implementa tus cambios
4. Escribe tests
5. Actualiza documentación
6. Abre un Pull Request

---

## 🛠️ Proceso de Desarrollo

### 1️⃣ Setup Inicial

```bash
# Fork y clonar
git clone https://github.com/tu-usuario/eduexce-movil.git
cd eduexce-movil

# Crear rama
git checkout -b feature/mi-nueva-funcionalidad
```

### 2️⃣ Desarrollo

```bash
# Hacer cambios
# Compilar frecuentemente
./gradlew assembleDebug

# Ejecutar tests
./gradlew test
```

### 3️⃣ Antes de Hacer Commit

```bash
# Lint check
./gradlew lint

# Formatear código
# Usar Android Studio: Code -> Reformat Code (Ctrl+Alt+L)
```

### 4️⃣ Commit y Push

```bash
git add .
git commit -m "feat: descripción clara del cambio"
git push origin feature/mi-nueva-funcionalidad
```

### 5️⃣ Pull Request

1. Ve a GitHub y abre un PR
2. Completa el template del PR
3. Enlaza issues relacionados
4. Espera revisión

---

## 📐 Guías de Estilo

### Java

#### Nomenclatura

```java
// Clases: PascalCase
public class MiClase { }

// Métodos: camelCase
public void miMetodo() { }

// Constantes: UPPER_SNAKE_CASE
private static final int MAX_INTENTOS = 3;

// Variables: camelCase
private int miVariable;
```

#### Formato

```java
// Llaves en nueva línea (estilo K&R)
if (condicion) {
    // código
} else {
    // código
}

// Espacios alrededor de operadores
int resultado = a + b;

// Imports ordenados
import android.content.Context;
import android.util.Log;

import com.example.zavira_movil.model.Usuario;

import java.util.List;
```

#### Comentarios

```java
/**
 * Descripción del método.
 * 
 * @param parametro Descripción del parámetro
 * @return Descripción del retorno
 */
public String miMetodo(String parametro) {
    // Comentario de una línea para lógica compleja
    
    /* 
     * Comentario multilínea
     * para explicaciones largas
     */
}
```

### XML (Layouts)

```xml
<!-- Atributos en orden alfabético -->
<TextView
    android:id="@+id/tvTitulo"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/titulo"
    android:textSize="16sp"
    android:textStyle="bold" />

<!-- IDs descriptivos -->
<!-- Prefijos: tv (TextView), btn (Button), iv (ImageView), etc. -->
```

### Nombres de Recursos

```
// Strings
<string name="area_matematicas">Matemáticas</string>
<string name="error_network">Error de conexión</string>

// Colors
<color name="primary_blue">#3988FF</color>
<color name="text_dark">#1F2937</color>

// Drawables
ic_heart_full.xml
bg_button_primary.xml
```

---

## 🏗️ Estructura del Proyecto

### Organización de Paquetes

```
com.example.zavira_movil/
├── activities/          # Activities principales
├── adapters/           # RecyclerView Adapters
├── fragments/          # Fragments
├── model/             # Modelos de datos
├── remote/            # API y Retrofit
├── local/             # Storage local
├── utils/             # Utilidades
├── services/          # Background services
└── notifications/     # FCM y notificaciones
```

### Agregar Nueva Funcionalidad

1. **Crear paquete** si es una funcionalidad grande
2. **Model** - Definir modelos de datos
3. **API** - Agregar endpoints en `ApiService.java`
4. **Manager** - Crear clase Manager si maneja lógica compleja
5. **UI** - Activity/Fragment + Layout XML
6. **Adapter** - Si usa RecyclerView
7. **Tests** - Agregar tests unitarios

---

## 🧪 Testing

### Tests Unitarios

```java
@Test
public void testMetodo() {
    // Arrange
    int input = 5;
    
    // Act
    int resultado = miClase.miMetodo(input);
    
    // Assert
    assertEquals(10, resultado);
}
```

### Tests Instrumentados

```java
@Test
public void testUI() {
    // Dado
    onView(withId(R.id.btnLogin))
        .check(matches(isDisplayed()));
    
    // Cuando
    onView(withId(R.id.etCorreo))
        .perform(typeText("test@eduexce.com"));
    
    // Entonces
    onView(withId(R.id.btnLogin))
        .perform(click());
}
```

### Coverage Mínimo

- ✅ **70%** para código nuevo
- ✅ Tests para bugs corregidos
- ✅ Tests para lógica crítica (vidas, progreso, etc.)

---

## 📝 Commits

### Formato de Commits (Conventional Commits)

```
<tipo>(<scope>): <descripción corta>

[cuerpo opcional]

[footer opcional]
```

### Tipos

- `feat`: Nueva funcionalidad
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `style`: Formato, sin cambios de código
- `refactor`: Refactorización sin cambios funcionales
- `test`: Agregar o modificar tests
- `chore`: Cambios en build, configs, etc.

### Ejemplos

```bash
# Feature
feat(vidas): agregar recarga automática cada 5 minutos

# Bug fix
fix(retos): corregir notificación duplicada

# Documentación
docs(readme): actualizar instrucciones de instalación

# Refactor
refactor(api): simplificar manejo de errores

# Multiple líneas
feat(quiz): implementar sistema de hints

Se agrega un sistema de pistas que permite al estudiante:
- Ver una pista por pregunta
- Consumir 1 vida por pista usada
- Máximo 2 pistas por quiz

Closes #123
```

---

## 🔍 Code Review

### Checklist del Autor

Antes de solicitar revisión:

- [ ] Código compilado sin errores
- [ ] Tests pasando
- [ ] Lint sin warnings críticos
- [ ] Documentación actualizada
- [ ] Comentarios en código complejo
- [ ] Sin `System.out.println()` o TODOs
- [ ] Variables y métodos con nombres descriptivos

### Checklist del Revisor

Al revisar un PR:

- [ ] El código cumple los estándares
- [ ] La lógica es correcta
- [ ] No hay vulnerabilidades obvias
- [ ] Los tests son suficientes
- [ ] La documentación es clara
- [ ] No hay código duplicado
- [ ] El rendimiento es aceptable

---

## 🎨 UI/UX Guidelines

### Material Design

- ✅ Usar componentes Material
- ✅ Colores del theme
- ✅ Elevaciones consistentes
- ✅ Ripple effects
- ✅ Transiciones suaves

### Accesibilidad

```xml
<!-- Content descriptions para ImageViews -->
<ImageView
    android:contentDescription="@string/heart_icon_description" />

<!-- Tamaños mínimos táctiles (48dp) -->
<Button
    android:minWidth="48dp"
    android:minHeight="48dp" />

<!-- Contraste suficiente para texto -->
```

### Responsive Design

- ✅ Usar ConstraintLayout
- ✅ Soportar orientación landscape
- ✅ Probar en diferentes tamaños de pantalla
- ✅ Usar dimension resources

---

## 🐛 Debugging

### Logs

```java
// Usar Log, no System.out.println
Log.d(TAG, "Debug message");
Log.i(TAG, "Info message");
Log.w(TAG, "Warning message");
Log.e(TAG, "Error message", exception);

// TAG estático
private static final String TAG = "MiClase";
```

### Logging Interceptor

Los logs HTTP están habilitados en desarrollo. Ver `RetrofitClient.java`.

---

## 📚 Recursos

### Documentación

- [Android Developers](https://developer.android.com)
- [Material Design](https://material.io/design)
- [Retrofit](https://square.github.io/retrofit/)
- [Firebase](https://firebase.google.com/docs)

### Herramientas

- **Android Studio** - IDE oficial
- **Scrcpy** - Control de dispositivos
- **Postman** - Testing de API
- **Firebase Console** - Testing de notificaciones

---

## 💬 Comunicación

### Issues

- Usa labels apropiados
- Sé específico y claro
- Incluye contexto suficiente
- Actualiza si encuentras más info

### Pull Requests

- Título descriptivo
- Descripción detallada de cambios
- Referencias a issues
- Screenshots para cambios UI
- Mantén PRs pequeños y enfocados

---

## ❓ Preguntas Frecuentes

### ¿Puedo trabajar en un issue ya asignado?

No, espera a que se libere o contacta al asignado.

### ¿Cuánto tiempo toma la revisión de un PR?

Generalmente 2-5 días hábiles.

### ¿Qué hago si mi PR tiene conflictos?

```bash
# Actualizar tu fork
git checkout main
git pull upstream main
git checkout tu-rama
git merge main
# Resolver conflictos
git push origin tu-rama
```

### ¿Debo crear un issue antes de un PR?

Para features grandes, sí. Para bugs pequeños, no es necesario.

---

## 🎉 ¡Gracias por Contribuir!

Tu contribución hace que **EduExce** sea mejor para todos los estudiantes. 

**¡Feliz codificación!** 🚀

---

<div align="center">

**¿Preguntas?** Abre un issue con la etiqueta `question`

Hecho con ❤️ por la comunidad de EduExce

</div>

