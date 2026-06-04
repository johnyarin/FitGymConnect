# FitGymConnect

Aplicación móvil Android para gestión de gimnasio y entrenadores online, desarrollada como Trabajo de Fin de Grado (TFG) del Ciclo Superior de Desarrollo de Aplicaciones Multiplataforma (DAM).

---

## Descripción

FitGymConnect es una plataforma que conecta alumnos y entrenadores de gimnasio. Los alumnos pueden explorar rutinas de entrenamiento, reservar clases en vivo y gestionar su suscripción mensual. Los entrenadores disponen de un panel propio para visualizar sus clases y rutinas programadas.

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
| GET | /api/classes/{id}/reviews | Valoraciones de una clase |

### Protegidos (requieren token Bearer)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/bookings | Mis reservas |
| POST | /api/bookings | Crear reserva |
| DELETE | /api/bookings/{id} | Cancelar reserva |
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
│   ├── model/          Data classes (User, Routine, GymClass, Booking…)
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
│   ├── shared/         RoutinesScreen, ClassesScreen, ProfileViewModel,
│   │                   RoutineViewModel, ClassViewModel
│   ├── student/        StudentMainScreen (5 tabs), StudentHomeScreen,
│   │                   MyBookingsScreen, StudentProfileScreen,
│   │                   BookingViewModel, SubscriptionViewModel
│   └── trainer/        TrainerMainScreen (4 tabs), TrainerHomeScreen,
│                       TrainerProfileScreen
│
└── utils/              Formatters (fechas, dificultad, estado)
```

---

## Navegación por roles

```
App arranca → Splash (lee token + rol del DataStore)
    ├── Sin sesión → LoginScreen / RegisterScreen
    ├── role = "student" → StudentMainScreen
    │       └── 5 pestañas: Inicio · Rutinas · Clases · Reservas · Perfil
    └── role = "trainer" → TrainerMainScreen
            └── 4 pestañas: Inicio · Mis Rutinas · Mis Clases · Perfil
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
│   │   ├── ClassViewModel.kt
│   │   ├── ProfileViewModel.kt
│   │   ├── RoutinesScreen.kt
│   │   └── RoutineViewModel.kt
│   ├── student/
│   │   ├── BookingViewModel.kt
│   │   ├── MyBookingsScreen.kt
│   │   ├── StudentHomeScreen.kt
│   │   ├── StudentMainScreen.kt
│   │   ├── StudentProfileScreen.kt
│   │   ├── SubscriptionViewModel.kt
│   ├── trainer/
│   │   ├── TrainerHomeScreen.kt
│   │   ├── TrainerMainScreen.kt
│   │   └── TrainerProfileScreen.kt
│   └── home/HomeScreen.kt
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

## Mejoras futuras

- Detalle de clase con lista de alumnos apuntados (requiere endpoint `GET /api/classes/{id}/bookings` en el backend)
- Tabla `trainer_student` para asignación directa de rutinas de un entrenador a un alumno específico
- Notificaciones push antes de una clase reservada
- Sistema de valoraciones y reseñas desde la app
- Modo oscuro
- Soporte offline con caché local
- Control de acceso al enlace de videollamada verificado en el backend
- Suscripción que permanece activa hasta la fecha de vencimiento tras cancelar
