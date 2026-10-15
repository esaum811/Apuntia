# Apuntia: estándares de código

Aplican a todo el código del proyecto. Se refuerzan con `.editorconfig`, ktlint y detekt.

Referencias: guía oficial de estilo de Kotlin, guía de estilo de Android para Kotlin y pautas de arquitectura de Android.

## Reglas

1. El código va en inglés; los comentarios y KDoc, en español.
2. Toda clase, interfaz y función pública lleva KDoc. Los comentarios `//` explican el porqué.
3. Nombres: `PascalCase` (clases y `@Composable`), `camelCase` (funciones y variables), `UPPER_SNAKE_CASE` (constantes), paquetes en minúsculas sin guiones bajos.
4. Ningún texto visible en el código: va en `strings.xml`.
5. Sin números ni textos mágicos: constantes con nombre.
6. Funciones cortas y con una sola responsabilidad (unas 40 líneas como máximo).
7. Nunca llaves, PIN ni datos sensibles en el código o en los logs. Sin `!!` salvo justificación comentada.
8. Capas: UI → ViewModel → dominio → repositorio → datos. La UI no toca la base de datos.
9. Sin código muerto ni comentado.
10. Commits convencionales y pruebas para toda funcionalidad nueva.

## Ejemplos

### Documentación (KDoc)

```kotlin
/**
 * Repositorio de notas y tareas.
 *
 * Es la única puerta de acceso a los datos de [Entry]; la interfaz nunca
 * consulta la base de datos directamente.
 */
interface EntryRepository {

    /**
     * Devuelve las entradas de una categoría, de la más reciente a la más antigua.
     *
     * @param categoryId identificador de la categoría.
     */
    fun observeByCategory(categoryId: String): Flow<List<Entry>>
}
```

### Constantes en lugar de valores mágicos

```kotlin
// Mal
if (title.length > 120) { ... }

// Bien
private const val MAX_TITLE_LENGTH = 120

if (title.length > MAX_TITLE_LENGTH) { ... }
```

### Textos en recursos

```kotlin
// Mal
Text(text = "Nueva nota")

// Bien
Text(text = stringResource(R.string.new_note))
```

### Comentarios útiles

```kotlin
// Mal: repite lo que ya dice el código
// Suma uno al contador
counter++

// Bien: explica la razón
// Se reprograman las alarmas porque Android las borra al reiniciar el teléfono.
rescheduleAlarms()
```

### Seguridad

```kotlin
// Mal
Log.d("AI", "Llave: $apiKey")
val apiKey = "sk-..."

// Bien: la llave se lee del almacén cifrado y nunca se registra
val apiKey = keyStore.readApiKey(provider)
```

## Commits convencionales

```
feat: agrega pantalla de lista de notas
fix: corrige la reprogramación de alarmas tras reiniciar
docs: actualiza la documentación de la base de datos
refactor: separa la lógica de exportación a PDF
test: agrega pruebas del repositorio de notas
```

## Lista de revisión antes de cada commit

- [ ] Compila sin advertencias nuevas.
- [ ] El código nuevo tiene KDoc donde corresponde.
- [ ] No hay textos fijos, llaves ni datos sensibles.
- [ ] Hay pruebas para lo nuevo y todas pasan.
- [ ] Ktlint y detekt no reportan errores (cuando estén configurados).
