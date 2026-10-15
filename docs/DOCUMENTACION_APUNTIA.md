# Apuntia: documentación del proyecto

Versión del documento: 0.3 (14/10/2026). Estado: entorno validado y estándares definidos.

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

## 10. Herramientas de desarrollo

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

Otros requisitos:
- Git y cuenta de GitHub o GitLab.
- PC con 16 GB de RAM (mínimo 8).
- Un teléfono físico con Depuración USB para probar alarmas y biometría; emulador solo para pruebas de interfaz.

## 11. Estándares de programación

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

## 12. Calidad y buenas prácticas

- **Planeación:** requisitos e historias de usuario documentados; decisiones de arquitectura (ADR) en el repositorio.
- **Código:** Kotlin, MVVM, ramas main / develop / feature, commits convencionales, ktlint + detekt, CI (GitHub Actions u otro).
- **Pruebas:** JUnit5, MockK, Turbine, Compose UI Test, Room en memoria.
- **Implementación:** versionado semántico y pruebas en varios dispositivos antes de entregar.
- La normativa de tiendas queda fuera por ser un entorno privado; se mantienen seguridad, privacidad básica y accesibilidad.

## 13. Distribución

- APK firmado con keystore propia (con respaldo: sin ella no se pueden actualizar las instalaciones).
- Instalación desde "fuentes desconocidas" y actualizaciones manuales o por enlace interno.

## 14. Estado actual y pendientes

Hecho:
- Proyecto `Apuntia` creado y renombrado (paquete `com.apuntia.app`, tema `ApuntiaTheme`, Minimum SDK 26).
- Estilo de código, Auto Import, Actions on Save y plugins configurados en Android Studio.
- Entorno validado: la app de prueba compila e instala en un teléfono físico (Xiaomi).
- `.editorconfig` y `.gitignore` definidos con los estándares de programación.

Pendientes:
- Crear el repositorio en **GitHub**, iniciar Git en el proyecto y hacer el primer commit.
- Configurar `libs.versions.toml`, ktlint y detekt.
- Definir el MVP, las pantallas y la navegación.
- Crear la estructura de paquetes (UI, dominio, datos).
- Revisar la actualización menor de Gradle cuando el proyecto esté estable.
