# CloudNative01 — Sistema Hotelero (DSY1107 / EP1)

Plataforma de gestión hotelera tipo microservicios: 3 microservicios de dominio y un BFF en Spring Boot 4 que validan tokens JWT emitidos por Microsoft Entra ID, una base de datos MySQL y un frontend React 19 (lenguaje autorizado por el docente en reemplazo de Angular) que se autentica contra Entra ID con la librería MSAL.

**Cómo cumple la rúbrica EP1:**
- **Indicador 1 — MSAL (60%):** login/logout por redirección de Entra ID, guard de rutas por autenticación y rol (`RequireAuth`), inyección del `Bearer` token en cada petición HTTP, roles/scopes leídos desde los claims del JWT, vistas funcionales.
- **Indicador 2 — BFF (40%):** microservicio `bff` (Spring Boot 4.1.1, `:8084`) que valida el JWT de Entra ID (`issuer-uri`, `audience`, firma JWKS y vigencia), exige autenticación en todas sus rutas, aplica autorización por rol con `@PreAuthorize` y responde `401 AUTH-001` o `403 AUTH-003`. Agrega usuario + reservas + servicios del usuario del token (`GET /api/v1/bff/dashboard`) reenviando el `Bearer` al API Gateway. La vista "Mi cuenta" del frontend lo consume.

## Arquitectura

| Capa | Tecnología | Puerto |
|------|-----------|--------|
| Frontend | React 19 + Vite 8 + MSAL (Microsoft Entra ID) | `5173` |
| Microservicio usuario | Spring Boot 4.1.1 + OAuth2 Resource Server | `8083` |
| Microservicio reserva | Spring Boot 4.1.1 + OAuth2 Resource Server | `8081` |
| Microservicio servicio | Spring Boot 4.1.1 + OAuth2 Resource Server | `8082` |
| BFF | Spring Boot 4.1.1 + OAuth2 Resource Server | `8084` |
| Base de datos | MySQL 8 (Docker) | `3306` |

Cada microservicio lee su configuración por variables de entorno (todas con default en `application.properties`) y valida JWT con el issuer de Entra ID del tenant `tenaninternoemihormazabal.onmicrosoft.com` y la audience del client ID de la app.

## Estructura del repositorio

```
├── Frontend/            # React 19 + Vite + MSAL
│   └── src/
│       ├── auth/        # MSAL (authConfig, msalInstance, authUtils), interceptor HTTP y RequireAuth
│       ├── components/  # Tarjeta_servicios, Formulario_servicios, Modificar_servicios
│       ├── pages/       # Inicio, Login, Registrarse, Mi_cuenta, Servicios, Reservas, Panel_admin, etc.
│       ├── routes/      # RouterConfig (rutas protegidas por RequireAuth)
│       ├── services/    # servicioService y bffService (consumo vía apiClient)
│       └── utils/       # enviroment.ts (tenant, clientId, redirectUri y base URL del API Gateway)
├── usuario/             # Microservicio: usuarios  (Spring Boot 4, :8083)
├── reserva/             # Microservicio: reservas  (Spring Boot 4, :8081)
├── servicio/            # Microservicio: servicios (Spring Boot 4, :8082)
├── bff/                 # BFF: dashboard del frontend (Spring Boot 4, :8084)
├── db-init/             # init.sql — crea h_usuario, h_reserva y h_servicio (solo DDL de infraestructura)
├── seeds/               # seed_usuarios.sql + seed_servicios.sql (idempotentes, se aplican a mano)
├── infra/               # Terraform (AWS): VPC propia + 5 EC2 t2.micro + SG + API Gateway con JWT
├── scripts/             # run_token_tests.ps1 (E2E con JWT real: 200, CRUD y carga 200 req) + token_e2e.ps1 (wrapper del reporte)
├── docker-compose.yml   # MySQL 8 con datos persistidos en volumen
└── .env.example         # Plantilla de variables de entorno (DB, puertos, tenant de Entra)
```

## Requisitos previos

- **Docker Desktop** (para MySQL) o un MySQL 8.0 local en `localhost:3306`
- **Java 21** — los microservicios compilan a bytecode 21 (`mvnw.cmd test` con JAVA_HOME apuntando a JDK 21+)
- **Node.js 20.19+ o 22.12+** (requisito real de Vite 8; Node 18 no compila este proyecto)
- Una cuenta de usuario en el tenant de Entra ID `tenaninternoemihormazabal.onmicrosoft.com`

## Puesta en marcha (paso a paso)

### 1. Base de datos

```bash
docker compose up -d
```

Levanta el contenedor `hotel-mysql` (usuario `root`, contraseña `root`) y, en la primera inicialización, ejecuta `db-init/init.sql` (crea `h_usuario`, `h_reserva` y `h_servicio`).

> Las **tablas** no las crea MySQL: las crea Hibernate (`ddl-auto=update`) al arrancar cada microservicio. Por eso el **seed se aplica manualmente y siempre después del primer arranque** de los microservicios. Es idempotente:
>
> ```bash
> docker exec -i hotel-mysql mysql -uroot -proot < seeds/seed_usuarios.sql
> docker exec -i hotel-mysql mysql -uroot -proot < seeds/seed_servicios.sql
> ```
>
> **En PowerShell (Windows)** la redirección `<` no existe; usa `cmd`:
>
> ```powershell
> cmd /c "docker exec -i hotel-mysql mysql -uroot -proot < seeds\seed_usuarios.sql"
> cmd /c "docker exec -i hotel-mysql mysql -uroot -proot < seeds\seed_servicios.sql"
> ```

### 2. Microservicios

Compilar y levantar cada microservicio en su terminal (usa el wrapper inclusivo, no el `mvn` global):

```bash
cd usuario && .\mvnw.cmd package -DskipTests
java -jar target\usuario-0.0.1-SNAPSHOT.jar
```

```bash
cd reserva && .\mvnw.cmd package -DskipTests
java -jar target\reserva-0.0.1-SNAPSHOT.jar
```

```bash
cd servicio && .\mvnw.cmd package -DskipTests
java -jar target\servicio-0.0.1-SNAPSHOT.jar
```

```bash
cd bff && mvn package -DskipTests
java -jar target\bff-0.0.1-SNAPSHOT.jar
```

> El módulo `bff` no trae wrapper de Maven, se compila con el `mvn` global (3.9.14).

Variables de entorno que leen los microservicios (todas con default):

| Variable | Default | Uso |
|----------|---------|-----|
| `DB_HOST` / `DB_PORT` | `localhost` / `3306` | Servidor MySQL |
| `DB_NAME` | `h_usuario` / `h_reserva` / `h_servicio` | Base de cada microservicio |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / *(vacío)* | Credenciales MySQL |
| `SERVER_PORT` | `8083` / `8081` / `8082` / `8084` | Puerto del API |
| `AZURE_TENANT_ID` | `e34a6311-…5455a0` | Emisor del JWT (issuer) |

**Importante:** si se levanta el jar sin `DB_PASSWORD` y MySQL exige clave, el arranque falla con `Access denied using password: NO`. En Windows:

```powershell
$env:DB_PASSWORD = "root"
java -jar target\usuario-0.0.1-SNAPSHOT.jar
```

**Seeds (después del primer arranque de los 3 microservicios):** usuarios demo + 4 servicios de ejemplo.

```bash
docker exec -i hotel-mysql mysql -uroot -proot < seeds/seed_usuarios.sql
docker exec -i hotel-mysql mysql -uroot -proot < seeds/seed_servicios.sql
```

### 3. Frontend

```bash
cd Frontend
npm install
npm run dev
```

Abrir [http://localhost:5173](http://localhost:5173) — el `redirectUri` de MSAL está configurado a este puerto. El client ID/tenant y la base URL del API se definen en `Frontend/src/utils/enviroment.ts`.

> **Modo actual (demo):** `enviroment.ts` apunta al API Gateway de AWS, así el frontend local consume la nube. Para desarrollo 100% local: `apiBaseUrl_01` → BFF (`http://localhost:8084`), `apiBaseUrl_02` → servicio (`8082`), `apiBaseUrl_03` → usuario (`8083`), y en el BFF exportar `MICROSERVICIOS_USUARIO_URL`, `MICROSERVICIOS_RESERVA_URL` y `MICROSERVICIOS_SERVICIO_URL` apuntando a los microservicios locales.

## Manual de usuario

### Iniciar sesión

1. En la barra superior hacer clic en **Iniciar sesión**.
2. MSAL redirige a la página de Microsoft Entra ID.
3. Entrar con una cuenta del tenant. **Credenciales de prueba:**

| Cuenta | Rol | Acceso |
|--------|-----|--------|
| `admin.prueba@tenaninternoemihormazabal.onmicrosoft.com` | Admin | Panel de administración y CRUD de servicios |
| `cliente.prueba@tenaninternoemihormazabal.onmicrosoft.com` | Cliente | Solo lectura de servicios (reservas pendientes) |

### Navegación y roles

| Ruta | Vista | Requiere |
|------|-------|----------|
| `/` | Inicio | Público |
| `/registro` | Registro | **Pendiente** — vista placeholder vacía |
| `/login` | Iniciar / cerrar sesión | Público |
| `/servicios` | Listado de servicios (desde BD) | Autenticado |
| `/servicios/detalle` | Detalle de un servicio | Autenticado |
| `/mi_cuenta` | Datos del JWT + resumen agregado por el BFF (usuario, reservas, servicios) | Autenticado |
| `/mis_reservas` | Reservas del usuario | Autenticado |
| `/mis_reservas/detalle` | Detalle de reserva | Autenticado |
| `/PanelAdministradores` | Panel admin (alta/baja/edición de servicios) | Rol `Admin` |

- Usuario autenticado que accede a `PanelAdministradores` sin rol `Admin` ve **"Acceso denegado"**.
- Usuario no autenticado es redirigido a `/login` desde cualquier ruta protegida.
- Los roles se leen del claim `roles` del JWT (no de la base de datos).
- **Pendientes de implementar:** el alta de usuarios (`/registro`) y las vistas del flujo de reservas (`/mis_reservas`, `/mis_reservas/detalle`) — son placeholders vacíos. El microservicio **reserva ya expone todos sus endpoints** (ver sección API).

### Flujo de uso (inicio a fin)

1. Abrir `http://localhost:5173`. Vista Inicio.
2. **Iniciar sesión** con una cuenta demo (Admin o Cliente).
3. En `/mi_cuenta` se muestran el correo, el rol y los scopes del token, más el resumen agregado por el BFF (usuario, reservas y servicios).
4. Consultar `/servicios` para ver los 4 servicios sembrados (carga vía API Gateway desde el microservicio en AWS, con Bearer).
5. **Como Admin:** entrar a `/PanelAdministradores` y probar crear / editar / eliminar servicios.
6. Cerrar sesión con el botón correspondiente.

> El flujo **Cliente → reservar → `/mis_reservas`** está pendiente de implementar (ver nota de pendientes).

## API (endpoints)

Salvo `register` y `login`, **todos** los endpoints requieren un JWT de Entra ID válido (`Authorization: Bearer <token>`); sin token responden `401` y con rol insuficiente `403`. Los roles se aplican con `@PreAuthorize` (escrituras reservadas a `ADMIN`).

> `register` y `login/{correo}/{contra}` quedaron legacy del contrato inicial: el frontend no los usa —el acceso real es Microsoft Entra ID (MSAL)— y el campo `contrasenia` de la base de datos no participa del flujo de autenticación.

### Microservicio usuario — `http://localhost:8083/api/v1/usuario`

| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/register` | Registrar usuario | público |
| GET | `/login/{correo}/{contra}` | Login por correo/contraseña | público |
| GET | `/me` | Usuario autenticado resuelto por el claim del token (se crea en su primer acceso) | autenticado |
| GET | `/get/{id}` | Usuario por id | ADMIN |
| GET | `/list` | Listar usuarios | ADMIN |
| PUT | `/put/{id}` | Actualizar usuario | ADMIN |
| DELETE | `/delete/{id}` | Eliminar usuario | ADMIN |

### Microservicio reserva — `http://localhost:8081/api/v1/reserva`

| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/post` | Crear reserva | ADMIN |
| GET | `/get/{id}` | Reserva por id | autenticado |
| GET | `/list` | Listar reservas | autenticado |
| GET | `/usuario/{idUsuario}` | Reservas de un usuario | autenticado |
| PUT | `/put/{id}` | Actualizar reserva | ADMIN |
| DELETE | `/delete/{id}` | Eliminar reserva | ADMIN |

### Microservicio servicio — `http://localhost:8082/api/v1/servicio`

| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/post` | Crear servicio | ADMIN |
| GET | `/get/{id}` | Servicio por id | autenticado |
| GET | `/list` | Listar servicios | autenticado |
| PUT | `/put/{id}` | Actualizar servicio | ADMIN |
| DELETE | `/delete/{id}` | Eliminar servicio | ADMIN |

### BFF — `http://localhost:8084/api/v1/bff`

| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| GET | `/dashboard` | Agrega usuario + reservas + servicios del usuario del token | autenticado |
| GET | `/admin/resumen` | Resumen global: usuarios + reservas + servicios | ADMIN |

## Pruebas

```bash
.\mvnw.cmd test     # en cada microservicio (usuario / reserva / servicio)
mvn test            # en bff (usa Maven global)
npm run lint        # en Frontend (oxlint)
npm run build       # en Frontend (build de producción)
```

Los microservicios suman **30 tests** (usuario 14, reserva 8, servicio 8) y el BFF **14** = **44 tests**: arranque de contexto, mapeo de claims JWT→roles, control de acceso con cuerpos `AUTH-001`/`AUTH-003`, rutas inexistentes y métodos no permitidos (`404 SYS-002` / `405 SYS-003`), resolución por token del endpoint `/me` (con creación del usuario en su primer acceso) y dashboard/resumen por token en el BFF. Para una validación end-to-end contra los microservicios en ejecución, usar `scripts/run_token_tests.ps1` (valida JWT real contra los 3, hace un CRUD completo y una carga de 200 peticiones). `scripts/token_e2e.ps1` es un wrapper que pide el token que el snippet de la consola dejó en el portapapeles y genera un reporte local.

### Análisis estático (SonarQube)

Los **44 tests automatizados** (JUnit 5 + MockMvc) fueron revisados con **SonarQube** (scanner del IDE IntelliJ IDEA), fuente fidedigna de análisis estático, junto con el código de los 4 módulos Java, el frontend y el IaC de Terraform. Los avisos remanentes son **convenciones deliberadas del proyecto, no defectos**: los campos `snake_case` de las entidades (`s_nombre`, `a_paterno`, `dv_rut`, …) mapean las columnas MySQL del modelo de datos ya definido, y los prefijos de los métodos de servicio (`uc_`, `u_`, `sn_`) identifican su capa. El único hallazgo real (índice del array como `key` en las listas de React) fue corregido. En el IaC, la IP pública de las EC2 es deliberada —el API Gateway integra por IP y el laboratorio exige SSH— y quedó explícita en `main.tf`.

## Infraestructura (AWS)

`infra/main.tf` despliega en `us-east-1`, en VPC propia (`10.0.0.0/16` + subred pública):

- **5 EC2 `t2.micro`** (AL2023, key `vockey`): frontend (nginx), `usuario` (microservicio + MySQL 8 con `h_usuario`, `h_reserva`, `h_servicio`), `reserva`, `servicio` y `bff` (microservicio + JDK 21). Cada microservicio tiene su servicio `systemd` (`cn-*`).
- **Security Group**: 8081–8084 y 80 públicos, SSH 22 (lab), 3306 solo entre EC2 del grupo.
- **API Gateway HTTP API v2** con authorizer **JWT Entra** (valida, no solo pasa el token), CORS y rutas `OPTIONS` sin auth para que el preflight no caiga en el authorizer. Proxy `/{proxy+}` hacia cada microservicio.

```bash
cd infra
terraform init
terraform plan
terraform apply   # responde yes
```

Post-apply manual: SCP de los 4 jars a `/opt/apps/` + `systemctl start cn-*`, seeds de usuarios y servicios en MySQL (`seeds/seed_usuarios.sql`, `seeds/seed_servicios.sql`), y `dist/` del frontend a `/var/www/grandhotel`. Outputs: `frontend_public_ip`, `usuario_public_ip`, `microservicios_public_ip`, `api_url` y `db_password` (sensitive).

> Si las EC2 se reinician cambian sus IPs públicas: correr `terraform apply` de nuevo para refrescar las integrations. En AWS Academy el lab se corta a las 4 horas y detiene las EC2, así que al volver hay que arrancarlas de nuevo. Tras la demo: `terraform destroy`.

## Credenciales y seguridad

- Las claves públicas de Entra ID (clientId, tenant) quedan definidas en `Frontend/src/utils/enviroment.ts`, y el issuer/audience en el `application.properties` de cada microservicio. No hay secretos comprometidos en el repositorio; la contraseña de MySQL (`root`) y los identificadores de Entra son solo para el entorno local de desarrollo.
- `.env.example` es plantilla **solo para los microservicios** (DB, puertos, tenant); el frontend no lee `.env` — su configuración está hardcodeada en `enviroment.ts`. `.env` real **no se sube** a Git.
- El `.gitignore` raíz excluye `.env`, `node_modules`, `target/`, `*.log` y artefactos de compilación; `infra/.gitignore` excluye `.terraform/`, `.tfstate` y `.tfvars`.
