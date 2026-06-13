# FitGymConnect

Aplicación móvil Android para la gestión de un gimnasio, desarrollada como Trabajo de Fin de Grado del Ciclo Superior de Desarrollo de Aplicaciones Multiplataforma (DAM).

FitGymConnect conecta alumnos y entrenadores en una misma plataforma: los alumnos pueden reservar clases, seguir rutinas de entrenamiento y gestionar su suscripción, mientras que los entrenadores disponen de herramientas para gestionar su agenda y visualizar la ocupación de sus sesiones.

---

## Características principales

### Alumnos
- Registro con verificación de email
- Exploración de rutinas de entrenamiento con desglose de ejercicios (series, repeticiones, descanso, vídeo)
- Filtrado de rutinas por nivel de dificultad
- Reserva de clases con selector de fecha y hora, consulta de aforo en tiempo real
- Gestión de reservas: visualización y cancelación
- Suscripción mensual con pago mediante Stripe
- Acceso a contenido premium (rutinas y clases) bloqueado hasta tener suscripción activa

### Entrenadores
- Panel de inicio con estadísticas del día: sesiones, alumnos y rutinas publicadas
- Agenda de los próximos 14 días con sesiones y alumnos apuntados por sesión
- Vista de sus clases y rutinas propias
- Ocupación visible por sesión con indicador de aforo completo

### Autenticación y seguridad
- Login y registro con validación de formularios
- Verificación de email obligatoria tras el registro
- Tokens Bearer (Laravel Sanctum) con persistencia local (DataStore)
- Redirección automática según rol al arrancar la app
- Mensaje específico si la cuenta está desactivada por el administrador

---

## Stack tecnológico

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

### Backend
| Tecnología | Versión | Uso |
|---|---|---|
| Laravel | 13 | Framework PHP principal |
| MySQL | 8.0 | Base de datos relacional |
| Laravel Sanctum | — | Autenticación por tokens Bearer |
| Stripe | Sandbox | Pagos y suscripciones |
| Nginx | 1.24 | Servidor web |
| PHP | 8.3 | Lenguaje backend |
| SMTP2GO | — | Envío de emails transaccionales |

---

## Arquitectura

La app sigue el patrón **MVVM** con una separación clara por capas:

```
MVVM
│
├── data/
│   ├── local/          TokenDataStore — sesión persistente (token, rol, userId, email)
│   ├── model/          Data classes (User, Routine, GymClass, Booking, ClassSchedule…)
│   ├── network/        ApiService (Retrofit) + AuthInterceptor (añade Bearer automáticamente)
│   └── repository/     AuthRepository, RoutineRepository, ClassRepository,
│                       BookingRepository, SubscriptionRepository
│
├── di/                 AppModule — provee Retrofit + OkHttpClient via Hilt
│
├── navigation/         NavGraph — splash + routing por rol (student / trainer)
│
└── ui/
    ├── auth/           LoginScreen, RegisterScreen, EmailVerificationScreen, AuthViewModel
    ├── main/           MainViewModel — detecta sesión al arrancar
    ├── shared/         RoutinesScreen, RoutineDetailScreen, ExerciseDetailScreen,
    │                   ClassesScreen, ClassDetailViewModel, ClassViewModel, RoutineViewModel
    ├── student/        StudentMainScreen (5 tabs), StudentHomeScreen,
    │                   MyBookingsScreen, StudentProfileScreen,
    │                   BookingViewModel, SubscriptionViewModel
    ├── trainer/        TrainerMainScreen (4 tabs), TrainerHomeScreen,
    │                   TrainerAgendaScreen, TrainerProfileScreen,
    │                   TrainerAgendaViewModel, TrainerOccupancyViewModel
    └── theme/          Color, Theme, Type, ThemeViewModel
```

### Navegación por roles

```
Splash (lee token + rol del DataStore)
├── Sin sesión  →  Login / Registro / Verificación de email
├── student     →  StudentMainScreen
│                      └── 5 pestañas: Inicio · Rutinas · Clases · Reservas · Perfil
└── trainer     →  TrainerMainScreen
                       └── 4 pestañas: Inicio · Mis Rutinas · Agenda · Perfil
```

---

## Infraestructura

| Parámetro | Valor |
|---|---|
| Proveedor | DigitalOcean VPS — Frankfurt |
| Especificaciones | 1 vCPU / 1 GB RAM / 25 GB SSD |
| Sistema operativo | Ubuntu 24.04 LTS |
| Dominio | https://alumnojmya.me |
| SSL | Let's Encrypt (renovación automática) |
| Email | SMTP2GO — hasta 1.000 emails/mes gratuitos |
| Panel de administración | https://alumnojmya.me/admin |

---

## API REST

### Autenticación
| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/register | Registrar nuevo usuario (envía email de verificación) |
| POST | /api/login | Iniciar sesión |
| POST | /api/logout | Cerrar sesión |
| GET | /api/me | Datos del usuario autenticado |
| POST | /api/email/resend | Reenviar email de verificación |
| GET | /api/email/verify/{id}/{hash} | Verificar email (enlace firmado) |

### Contenido público
| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/trainers | Listado de entrenadores |
| GET | /api/routines | Listado de rutinas |
| GET | /api/classes | Listado de clases |
| GET | /api/classes/{id}/availability | Plazas disponibles para una sesión (`?date=&time_slot=`) |

### Protegidos — Alumno
| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/bookings | Mis reservas |
| POST | /api/bookings | Crear reserva |
| DELETE | /api/bookings/{id} | Cancelar reserva |
| GET | /api/subscription | Mi suscripción |
| POST | /api/subscription/payment-intent | Iniciar pago Stripe |
| POST | /api/subscription | Confirmar suscripción |
| POST | /api/subscription/cancel | Cancelar suscripción |

### Protegidos — Entrenador
| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/trainer/stats | Estadísticas del panel de inicio |
| GET | /api/trainer/classes | Clases asignadas al entrenador |
| GET | /api/trainer/routines | Rutinas publicadas por el entrenador |
| GET | /api/classes/{id}/bookings | Alumnos con reserva en una clase |

### Solo administrador
| Método | Endpoint | Descripción |
|---|---|---|
| POST/PUT/DELETE | /api/trainers | Gestión de entrenadores |
| POST/PUT/DELETE | /api/classes | Gestión de clases |
| POST/PUT/DELETE | /api/routines | Gestión de rutinas |

---

## Modelo de suscripciones

La app contemplaría dos tipos de suscripción:

**Suscripción A — Socio completo (30–40 €/mes)**
Destinada al usuario que asiste físicamente al gimnasio. Se activa manualmente por el administrador desde el panel web. Incluye acceso completo a instalaciones, rutinas premium y reserva de clases sin coste adicional.

**Suscripción B — Acceso digital (10 €/mes)**
Destinada al usuario que accede solo a los servicios digitales. Se contrata directamente desde la app mediante Stripe. Incluye acceso a rutinas premium y opción a acceder a las clases con coste adicional reducido (~7€ por sesión)

Actualmente está implementada la **Suscripción B** con flujo de pago completo (PaymentIntent → PaymentSheet → confirmación). La Suscripción A se gestionaría íntegramente desde el panel de administración.


**Tarjeta de prueba Stripe:** `4242 4242 4242 4242` · cualquier fecha futura · cualquier CVC
