# Apuntia

App Android de notas y tareas enfocada en el trabajo y la escuela. Funciona sin servidor y sin conexión; solo usa internet para conectarse a la IA que el usuario elija.

> **Estado:** en desarrollo (planeación y proyecto base). Aún no hay una versión instalable con funciones.

## Qué hace

- **Notas y tareas** organizadas por categorías (Escuela, Trabajo, Personal y las que cree el usuario).
- **Notas confidenciales** protegidas con PIN o huella y cifrado adicional.
- **Recordatorios** con alarma y timbre, incluso con la app cerrada.
- **Exportar** notas a Word, PDF y Excel, e **importar** archivos e imágenes.
- **IA integrada** (Claude, Gemini y ChatGPT) con la llave API de cada usuario. La IA puede crear, buscar y programar cosas, siempre con confirmación.
- **Respaldo manual** en un archivo `.apuntia` cifrado.

## Tecnologías

Kotlin, Jetpack Compose (Material 3), MVVM, Hilt, Room con SQLCipher, Android Keystore, AlarmManager, WorkManager, Ktor y kotlinx.serialization. El detalle completo está en la documentación.

## Requisitos para trabajar con el proyecto

- [Git](https://git-scm.com)
- [Android Studio](https://developer.android.com/studio) (versión estable más reciente). Incluye el JDK necesario.
- Android SDK instalado desde el SDK Manager (la versión más reciente de la plataforma).
- Un teléfono Android con **Depuración USB** activada (recomendado, para probar alarmas y biometría) o un emulador.
- Mínimo soportado por la app: Android 8.0 (API 26).

## Cómo descargar el proyecto

El repositorio es privado, así que primero necesitas acceso a él con tu cuenta de GitHub.

### Opción 1: desde Android Studio

1. En la pantalla de bienvenida pulsa **Get from VCS** (o, con un proyecto abierto, **Git → Clone...**).
2. En **URL** pega:
   ```
   https://github.com/esaum811/Apuntia.git
   ```
3. Elige la carpeta de destino y pulsa **Clone**.
4. Si pide iniciar sesión, autoriza con tu cuenta de GitHub en el navegador.
5. Espera a que termine la sincronización de Gradle.

### Opción 2: desde la terminal

```bash
git clone https://github.com/esaum811/Apuntia.git
cd Apuntia
```

Después abre la carpeta con **File → Open** en Android Studio.

### Opción 3: descargar un ZIP

En la página del repositorio pulsa **Code → Download ZIP**, descomprime y abre la carpeta en Android Studio. Con esta opción no tendrás historial de Git.

## Cómo ejecutarlo

1. Abre el proyecto en Android Studio y espera a que termine el **Gradle Sync**.
2. Conecta tu teléfono por USB (con Depuración USB activada) o crea un emulador.
3. Elige el dispositivo en la barra superior y pulsa **Run** (▶).

Si el teléfono no aparece, revisa el cable (que transmita datos), el modo "Transferencia de archivos" y la autorización de depuración en la pantalla del teléfono. En algunos Xiaomi hay que activar también **Instalar vía USB** en Opciones de desarrollador.

## Estructura del repositorio

```
Apuntia/
├── app/                # Código de la app Android
├── docs/               # Documentación del proyecto
├── gradle/             # Wrapper y catálogo de versiones
├── .editorconfig       # Reglas de formato
├── .gitignore
└── build.gradle.kts
```

## Documentación

- [Documentación del proyecto](docs/DOCUMENTACION_APUNTIA.md): alcance, arquitectura, tecnologías, base de datos y seguridad.
- [Estándares de código](docs/ESTANDARES_CODIGO.md): reglas de programación y ejemplos.

## Cómo contribuir

- Se trabaja con ramas `main`, `develop` y `feature/*`.
- Los commits siguen el formato convencional: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`.
- Antes de cada commit hay que cumplir los estándares de código y que las pruebas pasen.

## Seguridad

No se suben al repositorio llaves de API, keystores (`*.jks`), `local.properties` ni respaldos `.apuntia`. El `.gitignore` ya los excluye. Si encuentras una llave en el código, avísalo y rótala de inmediato.

## Licencia

Proyecto privado de uso interno. Todos los derechos reservados.
