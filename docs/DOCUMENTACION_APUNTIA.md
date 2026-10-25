# Apuntia: documentación del proyecto

Versión del documento: 0.6 (25/10/2026). Estado: entorno validado, estándares definidos, diseño "Papel de colores" adoptado y tema implementado, capa de datos compilando y plan de rendimiento definido.

## 1. Resumen

Apuntia es una app Android de notas y tareas enfocada en el trabajo y la escuela. Funciona sin servidor y sin conexión; solo usa internet para la IA y para compartir archivos. Se instala por APK en un entorno local o privado (no se publica en Play Store).

## 2. Problema que resuelve

La información laboral y escolar está dispersa entre la app de notas, los recordatorios, Word/Excel y un chat de IA, y las notas confidenciales suelen guardarse sin protección. Apuntia reúne todo en un lugar, protege lo sensible y convierte las notas en documentos sin salir de la app.

## 3. Enfoques principales

1. **Autónoma:** sin servidor ni cuentas. Internet solo para IA y mensajería/compartir.
2. **Funciones principales:** recordatorios con alarma y timbre, notas por categorías, creación de archivos, IA con acceso a la app, importar y exportar archivos e imágenes.
3. **Interfaz fluida e intuitiva**, sin complejidad.
4. **Usable por cualquier persona sin manual** (máximo 3 toques por acción, onboarding de 2 o 3 pantallas).
5. **Buenas prácticas** en planeación, código, desarrollo e implementación.

## 4. Funciones

| Función | Descripción |
|---|---|
| Notas y tareas | Entradas con categorías (Escuela, Trabajo, Personal y las que cree el usuario). Las tareas tienen fecha límite y estado. |
| Notas confidenciales | Cifrado adicional; se liberan con PIN o huella. Quedan fuera de la búsqueda y de la IA hasta desbloquearlas. |
| Recordatorios | Alarmas exactas con timbre, aun con la app cerrada; se reprograman al reiniciar el teléfono. |
| Archivos | Exportar notas a Word, PDF y Excel; importar archivos e imágenes. |
| IA | Claude, Gemini y ChatGPT con la llave API propia de cada usuario. La IA puede crear, buscar, editar y poner recordatorios, siempre con confirmación. Gemini Nano opcional (en el teléfono). |
| Ayudas | Búsqueda global, widget de nota rápida, modo oscuro, dictado por voz, OCR de documentos, papelera, varios idiomas. |
| Respaldo | Archivo `.apuntia` cifrado con contraseña del usuario (BD + adjuntos). |

## 5. Arquitectura

App 100 % local con arquitectura MVVM por capas (UI, dominio, datos).

```
┌──────────────────────────────┐
│     APP ANDROID (todo local) │
│ UI (Compose) · Notas · Tareas│
│ Alarmas · Exportar/Importar  │
│ Acceso PIN/huella · Cifrado  │
│ BD local cifrada             │
│ Módulo IA (AIProvider)       │
└──────────────┬───────────────┘
               │ solo si el usuario lo pide
               ▼
     API de IA (HTTPS, llave del usuario)
```

**Cliente inteligente:** sí. Crea, edita y busca sin internet; cifra y valida acceso; programa alarmas; genera Word/PDF/Excel en el teléfono; y llama a la IA bajo demanda.

**Mensajería:** REST sobre HTTPS con JSON, únicamente hacia la API de IA elegida.
- Consulta a la IA (prompt y respuesta): **síncrona**.
- Alarmas y recordatorios: locales, sin push ni servidor.

## 6. Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| IDE | **Android Studio** (único entorno de desarrollo y edición) |
| Compilación | Gradle con Kotlin DSL y catálogo de versiones (`libs.versions.toml`) |
| SDK | Mínimo API 26 (Android 8.0); objetivo la versión estable más reciente |
| JDK | El que incluye Android Studio (el proyecto actual usa la versión 25) |
| Interfaz | Jetpack Compose + Material 3, Navigation Compose |
| Arquitectura | MVVM, ViewModel, StateFlow, Hilt |
| Asincronía | Coroutines + Flow |
| Datos | Room + SQLCipher, DataStore |
| Seguridad | Android Keystore, EncryptedSharedPreferences, BiometricPrompt |
| Alarmas | AlarmManager (exactas) + BroadcastReceiver de arranque |
| Segundo plano | WorkManager (respaldos) |
| Archivos | Apache POI (Word/Excel), PdfDocument (PDF), Storage Access Framework, Photo Picker |
| Imágenes y OCR | Coil, ML Kit (OCR sin conexión) |
| IA | Ktor Client + kotlinx.serialization; interfaz `AIProvider` con un adaptador por proveedor; ML Kit GenAI para Gemini Nano |
| Voz | SpeechRecognizer de Android |

Nota de licencias: iText es AGPL; usar PdfDocument nativo o validar la licencia antes de adoptarlo.

## 7. Base de datos

Room sobre SQLCipher (cifrada completa). Las llaves de IA y el hash del PIN **no** van en la BD, sino en Android Keystore.

- **categories:** id, name, icon, color, sort_order, is_default.
- **entries:** id, category_id, type (nota/tarea), title, body, is_done, due_at, is_confidential, body_encrypted, is_pinned, created_at, updated_at, deleted_at.
- **reminders:** id, entry_id, trigger_at, repeat_rule, ringtone_uri, is_enabled.
- **attachments:** id, entry_id, type, file_name, mime_type, size, internal_path.
- **ai_actions:** id, provider, action, entry_id, status (propuesta/aprobada/rechazada), created_at.
- **settings:** key, value (nada sensible).

Decisiones: IDs UUID, fechas en milisegundos UTC, búsqueda con FTS4 (solo notas no confidenciales), migraciones de Room versionadas desde la v1.

## 8. Integración con IA

- **Llave propia por usuario** (decisión confirmada): sin servidor ni proxy. Se guarda cifrada en Keystore y nunca entra en respaldos.
- Flujo de enlace: obtener la llave en el sitio del proveedor, pegarla en Ajustes → IA, la app la valida con una petición mínima y muestra "Conectado" o un error claro.
- Toda acción de la IA se muestra antes de aplicarse; el borrado siempre se confirma.
- Las notas confidenciales nunca se envían sin desbloqueo y confirmación.
- La app avisa qué texto sale y a qué empresa; el costo corre por cuenta del usuario.
- Precios, modelos y límites cambian seguido: revisarlos al iniciar el desarrollo.

## 9. Seguridad

- BD cifrada (SQLCipher) y doble cifrado en notas confidenciales.
- PIN y huella; sin recuperación de PIN (no hay cuentas), y la app debe advertirlo.
- Respaldo cifrado con contraseña elegida por el usuario; sin ella no se puede restaurar.
- Logs (Timber) sin datos sensibles.
- Guía de referencia: OWASP MASVS.

## 10. Plan de desarrollo: tres entregas

Cada entrega es usable por sí sola y se apoya en la anterior.

**Entrega 1 (MVP): el núcleo**
- Crear, editar, buscar y borrar notas y tareas (con papelera).
- Categorías: Escuela, Trabajo y Personal, más las que cree el usuario.
- Tareas con fecha límite y marca de hecha.
- Alarmas con timbre que suenan con la app cerrada y se reprograman al reiniciar el teléfono.
- Notas confidenciales con PIN o huella y cifrado.
- Base de datos cifrada (Room y SQLCipher).
- Onboarding de 2 o 3 pantallas, modo oscuro y textos listos para traducir.
- Criterio de terminado: alguien nuevo crea una nota, la protege, programa una alarma y suena con la app cerrada, sin ayuda ni manual.

**Entrega 2: archivos y respaldo**
- Adjuntar e importar imágenes y archivos.
- Exportar notas a PDF, Word y Excel.
- Respaldo y restauración con el archivo `.apuntia` cifrado.

**Entrega 3: IA**
- Elegir entre Claude, Gemini y ChatGPT e ingresar la llave propia.
- La IA puede crear, buscar, editar y programar recordatorios, con confirmación previa.
- Gemini Nano en los teléfonos compatibles.
- Registro de acciones en `ai_actions`.

**Después:** widget de nota rápida, dictado por voz y OCR de documentos.

Cada entrega cumple los estándares: ktlint y detekt sin errores y pruebas pasando. El diseño de cada entrega se hace justo antes de construirla.

## 11. Diseño de la interfaz (Entrega 1)

Diseño vigente desde el 25/10/2026: propuesta **"Papel de colores"** (lienzo "Apuntia: propuesta Papel de colores"), que reemplaza al diseño aprobado el 19/10/2026 (azul y naranja). El cambio retira el naranja de la paleta, que estaba en el requisito inicial; se hizo por decisión del autor.

**Pantallas (8, en claro y oscuro):** Bienvenida, Notas (inicio), Nueva nota, Desbloqueo, Tareas, Alarmas, Alarma sonando y Ajustes. La barra inferior flotante (Notas, Tareas, Alarmas, Ajustes) con el botón "+" incrustado al centro permite llegar a cualquier sección con 1 toque.

**Recorrido (máximo 3 toques por acción):**
1. Primer uso: Bienvenida (3 pasos) → Notas. El PIN se crea al guardar la primera nota confidencial.
2. Crear nota o tarea: Notas → botón + → Nueva nota → Guardar (2 toques). Nota o tarea se elige en el mismo editor.
3. Abrir nota confidencial: Notas → Desbloqueo (PIN o huella) → Nota abierta. En la lista se ve el título, nunca el cuerpo.
4. Alarma: Alarmas → elegir hora y guardar → suena aunque la app esté cerrada → Terminar o Posponer 10 minutos.

**Estilo:** tarjetas de color con forma de carpeta (pestaña con la categoría), títulos grandes y condensados, y un único color de acción (verde menta).

**Categorías** (mismo color en claro y oscuro; el texto sobre ellas es siempre `#1C1A3D`). Cada una tiene también una forma, para no depender solo del color:

| Categoría | Color | Forma |
|---|---|---|
| Escuela | `#FFD84D` | Círculo |
| Trabajo | `#8FA8FF` | Cuadrado |
| Personal | `#FF9DBB` | Rombo |

Las categorías creadas por el usuario guardan nombre, forma (`shape`) y color (ARGB).

| Uso | Claro | Oscuro |
|---|---|---|
| Fondo de pantalla | `#F5F4F9` | `#1C1A3D` |
| Superficies (chips, selectores) | `#E8E5F4` | `#2B2866` |
| Texto principal | `#1C1A3D` | `#FAF9FC` |
| Texto secundario | `#4E4A85` | `#C4C0EA` |
| Contorno | `#6A65B8` | `#8A85D6` |
| Barra inferior | `#1C1A3D` | `#2B2866` |
| Acción principal (menta) | `#62E6B7` | `#62E6B7` |
| Texto sobre menta | `#1C1A3D` | `#1C1A3D` |

**Tema en código:** `ui/theme/Color.kt` (colores), `Type.kt` (tipografía) y `Theme.kt` (`ApuntiaTheme`, con `ApuntiaTheme.colors` para los colores que Material 3 no tiene; sin colores dinámicos de Android para conservar la identidad). La tipografía es **Bricolage Grotesque** (fuente variable, licencia OFL) incluida en `res/font/bricolage_grotesque.ttf`, sin descargas, para funcionar sin internet; títulos en ancho 75 % y texto en ancho 100 %. Su licencia se guarda en `docs/licenses/`.

**Componentes propios** (`ui/components`): `CategoryCard`, `CategoryChip`, `CategoryShapeIcon`, `PrimaryButton`, `ApuntiaDock` y `ApuntiaIcons` (íconos de trazo creados en código, sin la librería `material-icons-extended`).

**Reglas de interfaz:** botones de al menos 44 dp, texto con contraste alto, categorías identificables por forma además de color, íconos de trazo simple (sin emojis), botones con etiqueta clara y animaciones que respetan la opción "reducir movimiento" del sistema.

**Pendientes de diseño:** las otras dos pantallas de la bienvenida, el selector de hora de la alarma y la pantalla de la papelera.

## 12. Estructura de paquetes

Dentro de `com.apuntia.app`:

```
core/            utilidades y constantes comunes
di/              módulos de Hilt
security/        PIN, huella, Keystore, cifrado
alarm/           alarmas y receptor de arranque
data/
  local/         database, dao, entity (Room)
  repository/    implementaciones de los repositorios
  file/          exportar e importar (Word, PDF, Excel, imágenes)
  ai/            proveedores de IA (Claude, Gemini, ChatGPT)
domain/
  model/         modelos de negocio
  repository/    interfaces de los repositorios
  usecase/       casos de uso
ui/
  navigation/    rutas y navegación
  theme/         tema
  components/    componentes reutilizables
  feature/       notes, tasks, reminders, ai, settings, onboarding
```

## 13. Herramientas de desarrollo

**Android Studio es la única herramienta de código y documentación.** Visual Studio Code queda fuera del proyecto: no se usa para código, documentación ni diagramas.

Qué cambia al quitar VS Code:
- Documentación (requisitos, ADR, este archivo) en Markdown, editada con el plugin **Markdown** de Android Studio (incluido).
- Diagramas con **Mermaid** dentro de Android Studio (soporte disponible en el IDE).
- Se descartan las extensiones de VS Code (Markdown All in One, GitLens, Mermaid).
- Se agrega `.vscode/` al `.gitignore` para que nunca entre al repositorio.
- Git se usa desde el panel de Git de Android Studio (con GitToolBox opcional).

Configuración aplicada en Android Studio:
- Estilo de código: Kotlin style guide, 4 espacios, columna 120.
- Auto Import y Actions on Save (reformatear y optimizar imports).
- Plugins: Ktlint, Detekt, SonarQube for IDE; opcionales Rainbow Brackets, Key Promoter X, GitToolBox.

Análisis de código en el proyecto (Gradle):
- **ktlint** con el plugin `org.jlleitschuh.gradle.ktlint` 14.0.1.
- **detekt** 2.0.0-alpha.5 (plugin `dev.detekt`). Se usa la serie 2 porque el proyecto usa AGP 9; la serie 1.23 es para AGP 8. Al ser versión alfa, conviene revisar cuando salga la estable.
- Reglas propias en `config/detekt/detekt.yml`. En detekt 2 la regla `LongMethod` usa `allowedLines`.
- Comando de verificación: `.\gradlew ktlintCheck detekt`; para corregir formato: `.\gradlew ktlintFormat`.

Otros requisitos:
- Git y cuenta de GitHub o GitLab.
- PC con 16 GB de RAM (mínimo 8).
- Un teléfono físico con Depuración USB para probar alarmas y biometría; emulador solo para pruebas de interfaz.

## 14. Estándares de programación

Aplican a todo el código del proyecto. Detalle y ejemplos en `ESTANDARES_CODIGO.md`.

**Referencias:** guía oficial de estilo de Kotlin, guía de estilo de Android para Kotlin y pautas de arquitectura de Android (MVVM por capas).

1. **Idioma:** el código (clases, funciones, variables) en inglés; comentarios y KDoc en español.
2. **Documentación:** toda clase, interfaz y función pública lleva KDoc. Los comentarios `//` explican el porqué, no lo obvio.
3. **Nombres:** clases en `PascalCase`, funciones y variables en `camelCase`, constantes en `UPPER_SNAKE_CASE`, funciones `@Composable` en `PascalCase`, paquetes en minúsculas y sin guiones bajos.
4. **Sin textos fijos:** todo texto visible va en `strings.xml`.
5. **Sin números ni textos mágicos:** se usan constantes con nombre.
6. **Funciones cortas** con una sola responsabilidad (si pasan de unas 40 líneas, se dividen).
7. **Seguridad:** nunca llaves, PIN ni datos sensibles en el código ni en los logs; no usar `!!` sin justificación comentada.
8. **Capas:** la UI no accede a la base de datos; siempre ViewModel → dominio → repositorio.
9. **Sin código muerto ni comentado:** Git guarda el historial.
10. **Commits convencionales** (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`) y pruebas para toda funcionalidad nueva.

**Reglas automáticas:** `.editorconfig` (estilo android_studio, 4 espacios, 120 columnas) y, más adelante, ktlint y detekt en el CI.

## 15. Calidad y buenas prácticas

- **Planeación:** requisitos e historias de usuario documentados; decisiones de arquitectura (ADR) en el repositorio.
- **Código:** Kotlin, MVVM, ramas main / develop / feature, commits convencionales, ktlint + detekt, CI (GitHub Actions u otro).
- **Pruebas:** JUnit5, MockK, Turbine, Compose UI Test, Room en memoria.
- **Implementación:** versionado semántico y pruebas en varios dispositivos antes de entregar.
- La normativa de tiendas queda fuera por ser un entorno privado; se mantienen seguridad, privacidad básica y accesibilidad.

### Rendimiento

| Área | Meta | Medidas |
|---|---|---|
| Arranque | App usable en menos de 1 s | La base de datos se abre fuera del hilo principal; más adelante, perfil base (Baseline Profile) y medición con Macrobenchmark |
| Listas | 60 fps con miles de notas | `LazyColumn` con `key` y `contentType`; la lista lee título y un resumen, no el cuerpo completo; búsqueda con consulta SQL, no filtrando en memoria |
| Base de datos | Ninguna consulta bloquea la pantalla | Room con `Dispatchers.IO`, índices en `categoryId`, `deletedAt` y `updatedAt`, y `Flow` en las consultas de pantalla |
| Tamaño | APK pequeño | R8 y `shrinkResources` en la versión de lanzamiento; sin `material-icons-extended`; una sola fuente variable |
| Batería | Casi cero consumo en segundo plano | Alarmas con `AlarmManager`, sin servicios permanentes; exportar y copias de seguridad con `WorkManager` |
| Adjuntos | Sin tirones al abrir imágenes | Reducir las imágenes al tamaño en que se muestran antes de cargarlas |
| Vigilancia | Detectar problemas pronto | `StrictMode` en depuración (clase `ApuntiaApplication`); LeakCanary para fugas de memoria |

Nota: SQLCipher añade algo de costo al abrir y leer la base de datos; por eso la apertura es perezosa y fuera del hilo principal.

## 16. Distribución

- APK firmado con keystore propia (con respaldo: sin ella no se pueden actualizar las instalaciones).
- Instalación desde "fuentes desconocidas" y actualizaciones manuales o por enlace interno.

## 17. Estado actual y pendientes

Hecho:
- Proyecto `Apuntia` creado y renombrado (paquete `com.apuntia.app`, tema `ApuntiaTheme`, Minimum SDK 26).
- Estilo de código, Auto Import, Actions on Save y plugins configurados en Android Studio.
- Entorno validado: la app de prueba compila e instala en un teléfono físico (Xiaomi).
- `.editorconfig` y `.gitignore` definidos con los estándares de programación.
- Repositorio en GitHub (privado) con rama `main`, primer commit y README.
- ktlint y detekt configurados.
- MVP definido en tres entregas.
- Diseño "Papel de colores" adoptado (8 pantallas, claro y oscuro), con tema y componentes base verificados en el teléfono.
- Capa de datos (Room 3 + SQLCipher) compilando.

Pendientes:
- Crear la estructura de paquetes en el proyecto, si falta.
- Repositorios y casos de uso, y carga de las categorías por defecto (Entrega 1).
- Activar R8 y StrictMode (plan de rendimiento).
- Pantallas de notas y tareas, y luego alarmas.
- Pruebas con una persona real y ajustes.
- Revisar la actualización menor de Gradle y el paso de detekt a versión estable cuando el proyecto esté estable.
- Aclarar en la política de respaldo de Android (`backup_rules.xml` y `data_extraction_rules.xml`) que no se respalda la BD ni las llaves.
