# FitGymConnect

Aplicación móvil Android para gestión de gimnasio y entrenadores online, desarrollada como Trabajo de Fin de Grado (TFG) del Ciclo Superior de Desarrollo de Aplicaciones Multiplataforma (DAM).

---

## Descripción

FitGymConnect es una plataforma que conecta alumnos y entrenadores de gimnasio. Los alumnos pueden explorar rutinas de entrenamiento con su desglose de ejercicios, reservar clases en vivo con selector de fecha y hora, y gestionar su suscripción mensual. Los entrenadores disponen de un panel propio para visualizar sus clases y rutinas, consultar la agenda de los próximos 14 días y ver los alumnos apuntados a cada sesión.

---

## Stack tecnológico

### Backend
| Tecnología | Versión | Uso |
|---|---|---|
| Laravel | 13 | Framework PHP principal |
| MySQL | 8.0 | Base de datos relacional |
| Laravel Sanctum | — | Autenticación por tokens Bearer |
| Stripe | Sandbox | Pagos y suscripciones |
| Nginx | 1.24 | Servidor web |
| PHP | 8.3 | Lenguaje backend |
| Ubuntu | 24.04 LTS | Sistema operativo del servidor |

### App Android
| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 2.0.21 | Lenguaje principal |
| Jetpack Compose | BOM 2024.09 | UI declarativa |
| Hilt | 2.52 | Inyección de dependencias |
| Retrofit | 2.11 | Cliente HTTP / llamadas a la API |
| OkHttp | 4.12 | Interceptor de autenticación |
| Navigation Compose | 2.8.4 | Navegación entre pantallas |
| DataStore | 1.1.1 | Persistencia local del token y sesión |
| Stripe Android | 20.50 | Pagos con tarjeta (PaymentSheet) |
| Coil | 2.7 | Carga de imágenes |

---

## Infraestructura

- **Servidor:** VPS DigitalOcean — Frankfurt — 1 vCPU / 1 GB RAM / 25 GB SSD
- **IP:** 209.38.233.231
- **Dominio:** https://alumnojmya.me
- **SSL:** Let's Encrypt (válido hasta 30/08/2026)
- **Email relay:** SMTP2GO (hasta 1.000 emails/mes gratuitos)
- **Panel admin:** https://alumnojmya.me/admin

---

## Credenciales de prueba

| Rol | Email | Contraseña |
|---|---|---|
| Admin | admin@fitgymconnect.com | password |
| Entrenador | trainer@fitgymconnect.com | password |
| Alumno | student@fitgymconnect.com | password |

**Tarjeta de prueba Stripe:** `4242 4242 4242 4242` — cualquier fecha futura — cualquier CVC

---

## API REST — Endpoints principales

### Autenticación
| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/login | Iniciar sesión |
| POST | /api/register | Registrar nuevo usuario |
| POST | /api/logout | Cerrar sesión |
| GET | /api/me | Datos del usuario autenticado |

### Contenido público
| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/trainers | Listado de entrenadores |
| GET | /api/routines | Listado de rutinas |
| GET | /api/classes | Listado de clases |

### Protegidos (requieren token Bearer)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/bookings | Mis reservas |
| POST | /api/bookings | Crear reserva (con `booking_date` y `time_slot`) |
| DELETE | /api/bookings/{id} | Cancelar reserva |
| GET | /api/classes/{id}/availability?date=YYYY-MM-DD&time_slot=HH:MM | Plazas disponibles para una sesión concreta |
| GET | /api/classes/{id}/bookings | Lista de alumnos con reserva para esa clase |
| GET | /api/subscription | Mi suscripción |
| POST | /api/subscription/payment-intent | Iniciar pago Stripe |
| POST | /api/subscription | Confirmar suscripción |
| POST | /api/subscription/cancel | Cancelar suscripción |

### Solo administrador
| Método | Endpoint | Descripción |
|---|---|---|
| POST/PUT/DELETE | /api/trainers | Gestión de entrenadores |
| POST/PUT/DELETE | /api/classes | Gestión de clases |
| POST/PUT/DELETE | /api/routines | Gestión de rutinas |

---

## Arquitectura de la app Android

```
MVVM (Model - View - ViewModel)
│
├── data/
│   ├── local/          TokenDataStore — sesión persistente
│   ├── model/          Data classes (User, Routine, GymClass, Booking, ClassSchedule…)
│   ├── network/        ApiService (Retrofit) + AuthInterceptor
│   └── repository/     AuthRepository, RoutineRepository, ClassRepository,
│                       BookingRepository, SubscriptionRepository
│
├── di/                 AppModule — provee Retrofit + OkHttpClient con Hilt
│
├── navigation/         NavGraph — splash + routing por rol (student/trainer)
│
├── ui/
│   ├── auth/           LoginScreen, RegisterScreen, AuthViewModel
│   ├── main/           MainViewModel — detecta sesión al arrancar
│   ├── shared/         RoutinesScreen, RoutineDetailScreen, ExerciseDetailScreen,
│   │                   ClassesScreen, ClassDetailViewModel,
│   │                   ProfileViewModel, RoutineViewModel, ClassViewModel
│   ├── student/        StudentMainScreen (5 tabs), StudentHomeScreen,
│   │                   MyBookingsScreen, StudentProfileScreen,
│   │                   BookingViewModel, SubscriptionViewModel
│   ├── trainer/        TrainerMainScreen (5 tabs), TrainerHomeScreen,
│   │                   TrainerAgendaScreen, TrainerProfileScreen,
│   │                   TrainerAgendaViewModel, TrainerOccupancyViewModel
│   └── theme/          Theme, Color, Type, ThemeViewModel, ThemeLocals
│
└── utils/              Constants (Stripe key, base URL), Formatters (fechas, dificultad)
```

---

## Navegación por roles

```
App arranca → Splash (lee token + rol del DataStore)
    ├── Sin sesión → LoginScreen / RegisterScreen
    ├── role = "student" → StudentMainScreen
    │       └── 5 pestañas: Inicio · Rutinas · Clases · Reservas · Perfil
    └── role = "trainer" → TrainerMainScreen
            └── 5 pestañas: Inicio · Mis Rutinas · Mis Clases · Agenda · Perfil
```

---

## Diario de desarrollo

### Día 1 — Infraestructura y servidor
- Creación del VPS en DigitalOcean (Frankfurt, Ubuntu 24.04 LTS)
- Instalación del stack LEMP: Nginx 1.24 + MySQL 8.0 + PHP 8.3 + Composer
- Instalación y configuración de Laravel 13
- Registro del dominio `alumnojmya.me` en Namecheap
- Configuración de registros DNS (registros A apuntando a la IP del servidor)
- Instalación de certificado SSL/TLS con Let's Encrypt + Certbot (HTTPS automático)
- Verificación: Laravel respondiendo correctamente en https://alumnojmya.me

---

### Día 2 — Base de datos, modelos y seeders
- Diseño y creación de 7 tablas mediante migraciones de Laravel:
  `users`, `trainers`, `routines`, `classes`, `bookings`, `subscriptions`, `reviews`
- Creación de modelos Eloquent con relaciones (hasOne, hasMany, belongsTo)
- Seeders con datos de prueba: admin, entrenador (con 2 rutinas y 1 clase) y alumno
- Campo `role` en users (admin / trainer / student) y campo `active` para activar/desactivar cuentas

---

### Día 3 — Autenticación, roles y email de verificación
- Instalación y configuración de Laravel Sanctum (tokens Bearer)
- Endpoints: `/api/register`, `/api/login`, `/api/logout`, `/api/me`
- Verificación de email con enlace firmado (`/api/email/verify/{id}/{hash}`)
- Middlewares de roles: `IsAdmin` e `IsTrainer`
- Configuración de SMTP2GO como relay de email (puerto 2525, esquiva el bloqueo de DigitalOcean)
- Verificación del dominio en SMTP2GO con registros CNAME en Namecheap
- Emails enviados desde `noreply@alumnojmya.me`

---

### Día 4 — API REST completa
- Creación de 6 controladores API: TrainerController, RoutineController, GymClassController, BookingController, ReviewController, SubscriptionController
- Implementación de todos los endpoints REST (públicos, protegidos y de administrador)
- Validaciones de negocio: no reservar clase llena, no reservar dos veces la misma clase, no valorar dos veces, fechas futuras obligatorias
- Prueba completa de todos los endpoints con Postman

---

### Día 5 — Stripe y panel de administración
- Integración de Stripe en modo sandbox para suscripciones de 9,99€/mes
- Flujo completo: PaymentIntent → formulario de pago → confirmación con el servidor → activación de suscripción
- Panel de administración web en https://alumnojmya.me/admin con:
  - Login exclusivo para administradores
  - Dashboard con estadísticas en tiempo real
  - Gestión de usuarios, clases, rutinas y suscripciones

---

### Día 6 — App Android: base del proyecto
- Creación del proyecto Android con Kotlin + Jetpack Compose
- Configuración de dependencias: Hilt (KSP), Retrofit, DataStore, Navigation Compose
- **Nota:** migración de `kapt` a `KSP` necesaria por incompatibilidad entre Hilt 2.52 y Kotlin 2.0.x
- Implementación de la capa de datos base:
  - `TokenDataStore` — persiste token, rol, userId y nombre de usuario
  - `ApiService` — interfaz Retrofit apuntando a `https://alumnojmya.me/`
  - `AuthRepository` — login, registro, logout
- Patrón MVVM: `AuthViewModel` con estados `Idle / Loading / Success / Error`
- `LoginScreen` y `RegisterScreen` conectadas a la API real
- `NavGraph` con splash screen que detecta sesión y redirige por rol
- **Fix:** campo `active` del usuario es `Boolean` en el JSON, no `Int`

---

### Día 7 — App Android: contenido y navegación
- Modelos añadidos: `Routine`, `GymClass`, `TrainerProfile`
- Nuevos endpoints en `ApiService`: `getRoutines()`, `getClasses()`
- `RoutineRepository` y `ClassRepository`
- `RoutineViewModel` y `ClassViewModel` con estados Loading/Success/Error
- `MainViewModel` — detecta token + rol al arrancar para el splash
- `ProfileViewModel` — nombre, rol y userId compartidos entre pantallas
- **Estudiante:** bottom bar con 4 pestañas (Inicio, Rutinas, Clases, Perfil)
- **Entrenador:** bottom bar con 4 pestañas (Inicio, Mis Rutinas, Mis Clases, Perfil)
- `RoutinesScreen` con tarjetas: título, dificultad, duración, badge Premium
- `ClassesScreen` con tarjetas: título, tipo online/presencial, fecha, plazas disponibles

---

### Día 8 — App Android: reservas, pagos y mejoras
- **AuthInterceptor** — OkHttp interceptor que añade `Authorization: Bearer <token>` automáticamente a todas las peticiones protegidas
- Refactorización de `AppModule` para proveer el cliente HTTP completo con Hilt
- **Sistema de reservas:**
  - `BookingRepository` + `BookingViewModel` con estados por tarjeta (processing)
  - `ClassesScreen` actualizado: botón "Reservar plaza" solo si hay suscripción activa
  - Pantalla "Mis Reservas" con enlace de videollamada destacado y diálogo de confirmación antes de cancelar
  - `BookingViewModel` compartido entre Clases y Reservas para mantener estado consistente
- **Suscripción con Stripe:**
  - `SubscriptionRepository` y `SubscriptionViewModel`
  - `PaymentSheet` de Stripe integrado en el perfil del alumno
  - Flujo completo: payment-intent → PaymentSheet → confirmación con el servidor
  - Diálogo de confirmación antes de cancelar con aviso de fecha de vencimiento
- **Mejoras de UX:**
  - Precio eliminado de las tarjetas de clases (incluido en la suscripción)
  - Clases bloqueadas con icono de candado para usuarios sin suscripción activa
  - `SubscriptionViewModel` compartido entre `StudentMainScreen` y `StudentProfileScreen`: al suscribirse, las clases se desbloquean automáticamente sin reiniciar la app
  - Formato de fechas legible (`2 Jul 2026`) en lugar de ISO 8601
  - Filtrado de rutinas y clases del entrenador por su `user_id`
- **Fixes:**
  - Campo `class_id` en `BookingRequest` (el backend usa `class_id`, no `gym_class_id`)
  - Detección correcta de suscripción vacía (objeto `{}` con `id = 0` tratado como sin suscripción)
  - Student bottom bar ampliado a 5 pestañas: Inicio · Rutinas · Clases · Reservas · Perfil

---

### Día 9 — App Android: pulido visual, dashboards y perfiles

#### Control de acceso a contenido premium
- Rutinas marcadas como `is_premium` bloqueadas visualmente para alumnos sin suscripción activa
- La tarjeta aparece en tono apagado con icono de candado y mensaje "Requiere suscripción activa"
- Los entrenadores ven siempre el catálogo completo sin restricciones
- El desbloqueo es reactivo: al suscribirse, las rutinas se desbloquean sin reiniciar la app

#### Rediseño visual de tarjetas (consistencia en toda la app)
- **Tarjetas de clases:** badge de tipo coloreado (azul Online / verde Presencial), iconos en todas las filas (calendario, persona, asiento), fila de fecha siempre visible ("Fecha por confirmar" si es null), plazas en rojo si están agotadas, divisor visual antes del botón de reserva
- **Tarjetas de reservas:** mismo badge de tipo, fecha con icono, nombre del entrenador, badge de estado ("Confirmada"), enlace de videollamada clickable que abre el navegador con icono de apertura externa
- **Tarjetas de rutinas:** badges coloreados por dificultad (verde Principiante / azul Intermedio / rojo Avanzado) con icono de mancuerna, badge de duración con icono de reloj, badge Premium como Surface badge consistente con el resto de la app, icono de persona para el entrenador

#### Dashboard de Inicio — Alumno
- Saludo personalizado con el primer nombre del usuario
- Banner "Hazte Premium" visible solo si no tiene suscripción activa, con acceso directo al Perfil
- Sección "Próxima clase" con la primera reserva activa (fecha, entrenador, tipo) o estado vacío con CTA "Explorar clases"
- Sección "Rutinas para ti" con hasta 3 rutinas en tarjetas compactas (filtra las premium si no tiene suscripción), con acceso "Ver todas"
- Todos los encabezados de sección navegan directamente a la pestaña correspondiente

#### Dashboard de Inicio — Entrenador
- Saludo personalizado
- Card de stats rápidos: Clases / Rutinas / Total alumnos con plazas ocupadas
- Sección "Próxima clase" con la primera clase asignada al entrenador, incluyendo plazas ocupadas
- Sección "Mis rutinas" con preview de hasta 3 rutinas propias en tarjetas compactas
- Navegación directa a las pestañas de Mis Clases y Mis Rutinas

#### Perfiles mejorados y sin redundancias
- Email del usuario añadido al `TokenDataStore` y guardado en login/registro — visible en ambos perfiles
- **Perfil alumno:** email con icono, único stat centrado "Reservas activas" (eliminada la duplicación del estado de suscripción que ya aparece en la card de abajo)
- **Perfil trainer:** email con icono, card "Especialidad" (si está rellena en el backend), card "Sobre mí" con la bio del entrenador — información propia del perfil no visible en ninguna otra pantalla
- Badge "Alumno" y "Entrenador" actualizados al estilo Surface badge consistente con toda la app
- Eliminado el `Spacer(weight(1f))` que dejaba espacio vacío artificial en los perfiles

---

### Día 10 — App Android: consistencia visual en todas las pantallas

#### Chips de filtro en listas
- **Rutinas:** fila de chips `Todos / Principiante / Intermedio / Avanzado` generada dinámicamente según los niveles presentes en los datos — si no hay rutinas avanzadas, el chip no aparece
- **Clases:** fila de chips `Todas / Online / Presencial` con la misma lógica dinámica
- Los chips son toggleables: seleccionar el activo vuelve a mostrar todos

#### Empty states con icono centrado
- Sustituyen los mensajes de texto plano o cards vacías por un estado visual completo: icono grande semitransparente + título + subtítulo explicativo
- **Rutinas vacías:** icono de mancuerna + "Aún no hay rutinas disponibles" + "Los entrenadores irán añadiendo rutinas pronto"
- **Rutinas filtradas sin resultado:** mensaje contextual "No hay rutinas de nivel Principiante" (según el chip activo)
- **Clases vacías / filtradas:** icono de videollamada + mensaje equivalente según filtro activo
- **Reservas vacías:** icono de calendario + "No tienes reservas activas" + "Explora las clases disponibles y reserva tu plaza"

#### Color de fondo en tarjetas por tipo o nivel
Aplicado de forma consistente en todas las pantallas:
- **Tarjetas de rutinas** (lista y compactas en Inicio): verde suave para Principiante, morado para Intermedio, rojo para Avanzado (30% de opacidad sobre el `containerColor` del tema)
- **Tarjetas de clases:** azul suave para Online, morado suave para Presencial
- **Tarjetas de reservas:** mismo esquema de color que las clases (heredado del tipo de clase reservada)
- El color está coordinado con los badges de nivel/tipo ya existentes en cada tarjeta

---

### Día 11 — Refactorización y nuevas funcionalidades

#### Refactorización del sistema de clases

El modelo de datos de clases fue rediseñado completamente para pasar de un sistema de clases con fecha fija a un **sistema de horarios recurrentes**, más acorde con el funcionamiento real de un gimnasio.

**Modelo anterior:** cada clase tenía una fecha y hora concreta (`scheduled_at`). Para tener Yoga los lunes y miércoles durante un mes era necesario crear 8 registros manualmente.

**Modelo nuevo:** las clases son plantillas (`GymClass`) con un array de horarios recurrentes (`ClassSchedule`), donde cada schedule define el día de la semana (`day_of_week`, 0=Lunes a 6=Domingo) y la hora (`time_slot`). Las reservas almacenan la fecha concreta elegida por el alumno.

#### Nuevos endpoints de la API

| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/classes/{id}/availability?date=YYYY-MM-DD&time\_slot=HH:MM | Plazas disponibles para una sesión concreta |
| GET | /api/classes/{id}/bookings | Lista de alumnos con reserva confirmada para esa clase |

#### Nuevo modelo de Booking

```json
{
  "id": 1,
  "user_id": 3,
  "class_id": 1,
  "booking_date": "2026-06-10",
  "time_slot": "09:00:00",
  "status": "confirmed",
  "gym_class": { ... }
}
```

#### Nuevas pantallas y ViewModels

| Archivo | Descripción |
|---|---|
| `ClassDetailViewModel` | Gestiona la selección de fecha, hora y consulta de disponibilidad en el bottom sheet |
| `TrainerAgendaViewModel` | Genera la agenda de los próximos 14 días y carga alumnos por sesión |
| `TrainerAgendaScreen` | Vista de agenda del entrenador con lista de alumnos por sesión |
| `TrainerOccupancyViewModel` | Carga ocupación por schedule para el panel del entrenador |
| `RoutineDetailScreen` | Detalle de rutina con lista de ejercicios (nombre, series, reps, descanso, enlace a vídeo) |
| `ExerciseDetailScreen` | Detalle individual de un ejercicio |

#### Mejoras en el flujo de reserva (alumno)

- **Bottom sheet de reserva:** al pulsar una clase se abre un menú inferior con selector de fecha (próximas 4 semanas, solo días disponibles) y selector de hora. Ambos visibles simultáneamente.
- **Validación temporal:** fechas y horas ya pasadas aparecen deshabilitadas (en gris) automáticamente.
- **Consulta de disponibilidad en tiempo real:** al seleccionar fecha y hora se consulta el endpoint de disponibilidad y se muestra el número de plazas restantes.
- **Bloqueo de plazas completas:** el botón de reserva se deshabilita si la sesión está llena.
- **Validación de formularios:** campos vacíos en login y registro se validan antes de llamar a la API.
- **Pull-to-refresh** implementado en las listas de rutinas, clases y reservas.

#### Mejoras en la pantalla de Inicio (alumno)

- Sección "Próximas clases" muestra las **2 reservas más cercanas a partir de hoy** ordenadas cronológicamente, ignorando reservas pasadas.
- Eliminados botones de navegación redundantes ("Ver clases", "Explorar clases") para una interfaz más limpia.

#### Mejoras en Mis Reservas (alumno)

- Las tarjetas de reserva muestran la **fecha y hora concreta** de la sesión reservada.
- Botón "Cancelar" compacto integrado en la misma línea que el título de la clase.
- Eliminado el chip de estado "Confirmada" (redundante).

#### Nuevo Home del entrenador

- Eliminada la sección "Mis clases con ocupación" (redundante con la pestaña Agenda).
- Nueva sección **"Hoy"** que muestra las sesiones del día actual con hora, nombre y alumnos apuntados. Si no hay sesiones hoy, muestra las 2 próximas.
- Navegación correcta desde los enlaces del home hacia las pestañas del bottom bar.

#### Vista de Agenda del entrenador

- Lista de sesiones de los **próximos 14 días** ordenada por fecha y hora.
- Cabeceras de fecha con etiquetas contextuales ("Hoy", "Mañana", "Jueves 12 Jun…").
- Ocupación visible en cada sesión (alumnos apuntados / plazas máximas) con indicador rojo si está llena.
- Al pulsar una sesión se abre un bottom sheet con la **lista de alumnos** apuntados (nombre e inicial).
- El ViewModel se comparte entre el Home y la pestaña Agenda para evitar llamadas duplicadas a la API.
- Bottom bar del entrenador ampliado a 5 pestañas: Inicio · Mis Rutinas · Mis Clases · Agenda · Perfil

#### Nuevo Perfil del entrenador

- **Avatar** con la inicial del nombre en lugar del icono genérico.
- **Especialidad** como chip junto al badge de rol, en lugar de una card independiente.
- Eliminada la sección "Sobre mí" (irrelevante para el entrenador en su propio perfil).
- **Stats diarios en tiempo real:**
  - Sesiones restantes hoy
  - Alumnos para hoy (suma de alumnos en sesiones restantes)
  - Rutinas publicadas

#### Corrección de errores

- **Campos mal mapeados:** `date` → `scheduled_at`, `max_students` → `max_capacity`, `speciality` → `specialty` corregidos con `@SerializedName`.
- **"Próxima clase" del alumno** ordenada cronológicamente en lugar de mostrar el primer elemento de la lista.
- **Error de suscripción silenciado:** un error de red ya no se muestra como "sin suscripción activa".
- **BookingViewModel compartido** entre pestañas para mantener el contador de reservas activas sincronizado.
- **Navegación del home** corregida para usar las mismas opciones que el bottom bar (`popUpTo`, `launchSingleTop`, `restoreState`).
- **Elevación de cards** reducida a 0dp para eliminar el efecto de borde grueso causado por el overlay tonal de Material You.

---

## Estructura del proyecto Android

```
app/src/main/java/com/example/fitgymconnect/
├── data/
│   ├── local/TokenDataStore.kt
│   ├── model/Models.kt
│   ├── network/
│   │   ├── ApiService.kt
│   │   ├── AuthInterceptor.kt
│   │   └── RetrofitClient.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── BookingRepository.kt
│       ├── ClassRepository.kt
│       ├── RoutineRepository.kt
│       └── SubscriptionRepository.kt
├── di/AppModule.kt
├── navigation/NavGraph.kt
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   ├── main/MainViewModel.kt
│   ├── shared/
│   │   ├── ClassesScreen.kt
│   │   ├── ClassDetailViewModel.kt
│   │   ├── ClassViewModel.kt
│   │   ├── ExerciseDetailScreen.kt
│   │   ├── ProfileViewModel.kt
│   │   ├── RoutineDetailScreen.kt
│   │   ├── RoutinesScreen.kt
│   │   └── RoutineViewModel.kt
│   ├── student/
│   │   ├── BookingViewModel.kt
│   │   ├── MyBookingsScreen.kt
│   │   ├── StudentHomeScreen.kt
│   │   ├── StudentMainScreen.kt
│   │   ├── StudentProfileScreen.kt
│   │   └── SubscriptionViewModel.kt
│   ├── trainer/
│   │   ├── TrainerAgendaScreen.kt
│   │   ├── TrainerAgendaViewModel.kt
│   │   ├── TrainerHomeScreen.kt
│   │   ├── TrainerMainScreen.kt
│   │   ├── TrainerOccupancyViewModel.kt
│   │   └── TrainerProfileScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       ├── ThemeLocals.kt
│       ├── ThemeViewModel.kt
│       └── Type.kt
├── utils/
│   ├── Constants.kt
│   └── Formatters.kt
├── FitGymApp.kt
└── MainActivity.kt
```

---

## Configuración para ejecutar

### Requisitos
- Android Studio Hedgehog o superior
- JDK 11
- Android API 28+ (Android 9.0)

### Pasos
1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. En `utils/Constants.kt`, reemplazar `STRIPE_PUBLISHABLE_KEY` con tu clave pública de Stripe (modo test)
4. Hacer **Sync Project with Gradle Files**
5. Ejecutar en emulador o dispositivo físico (API 28+)

### Clave Stripe
La clave pública de Stripe se obtiene en: **Stripe Dashboard → Developers → API keys → Publishable key**  
Formato: `pk_test_...`

---

## Mejoras futuras

- Tabla `trainer_student` para asignación directa de rutinas de un entrenador a un alumno específico
- Notificaciones push antes de una clase reservada
- Sistema de valoraciones y reseñas desde la app
- Modo oscuro (infraestructura `ThemeViewModel` preparada; falta exponer el toggle en la UI)
- Soporte offline con caché local
- Confirmación de contraseña en el registro
- Implementación completa del modelo de suscripciones de dos niveles (ver sección correspondiente)
- Flujo de pago por reserva individual de clase para suscriptores Sub B
- Pantalla de selección de plan con comparativa visual entre Sub A y Sub B

---

## Modelo de Suscripciones

### Diseño del sistema

La aplicación FitGymConnect contempla un modelo de suscripciones de dos niveles, diseñado para dar cabida tanto a los socios presenciales del gimnasio como a usuarios que deseen acceder únicamente a los servicios digitales.

### Tipos de suscripción

**Suscripción A — Socio completo (30-40€/mes)**

Orientada al usuario que acude físicamente al gimnasio. Esta suscripción se contrata de forma presencial en las instalaciones y es activada manualmente por el administrador desde el panel de gestión. Incluye:

- Acceso total a las instalaciones y maquinaria del gimnasio
- Acceso completo a la aplicación móvil
- Visualización de todas las rutinas, incluyendo las marcadas como premium
- Reserva de clases en vivo sin coste adicional por sesión

**Suscripción B — Acceso digital (10€/mes)**

Orientada al usuario que desea acceder a los servicios digitales sin necesidad de asistir presencialmente al gimnasio. Esta suscripción puede contratarse directamente desde la aplicación móvil mediante pasarela de pago Stripe. Incluye:

- Acceso completo a la aplicación móvil
- Visualización de todas las rutinas, incluyendo las marcadas como premium
- Acceso a clases en vivo con un coste adicional por reserva

### Estado actual de implementación

En la versión actual de la aplicación se encuentra implementada la **Suscripción B**, con el flujo de pago completo integrado mediante Stripe (PaymentIntent → PaymentSheet → confirmación con el servidor). El usuario puede suscribirse, consultar el estado de su suscripción y cancelarla en cualquier momento desde su perfil.

La **Suscripción A** se gestiona íntegramente desde el panel de administración web, sin requerir ninguna acción adicional por parte de la app — el backend simplemente marca al usuario como suscriptor activo y la aplicación refleja ese estado automáticamente.

### Mejoras futuras planificadas para suscripciones

**1. Distinción de tipo de suscripción en el backend**
Añadir un campo `type` (`"gym"` / `"app"`) al modelo `Subscription` para que la aplicación pueda diferenciar qué tipo de suscripción tiene el usuario.

**2. Flujo de pago por reserva de clase (Sub B)**
Cuando un usuario con Suscripción B intente reservar una clase en vivo:
1. La app detecta que el usuario tiene Sub B
2. Se lanza un PaymentIntent específico para esa reserva
3. El usuario completa el pago mediante PaymentSheet de Stripe
4. Tras la confirmación del pago, el backend registra la reserva

**3. Pantalla de selección de plan**
Mostrar al usuario una comparativa visual entre Sub A y Sub B antes de suscribirse.
