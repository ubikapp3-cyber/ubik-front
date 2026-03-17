# Ubik — Stack de Observabilidad

## Componentes

| Herramienta      | Puerto | Función                                   |
|------------------|--------|-------------------------------------------|
| **Prometheus**   | 9090   | Recolección y almacenamiento de métricas  |
| **Grafana**      | 3000   | Dashboards y visualización                |
| **Loki**         | 3100   | Almacenamiento de logs                    |
| **Promtail**     | —      | Agente que envía logs Docker → Loki       |
| **Alertmanager** | 9093   | Despacho de alertas (email, Slack, etc.)  |
| **cAdvisor**     | 8086   | Métricas de CPU/RAM de contenedores       |

---

## Requisitos en cada microservicio

### 1. Dependencias Maven (`pom.xml`)

```xml
<!-- Actuator (si no está ya) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus — OBLIGATORIO -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Logs estructurados JSON para Loki -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### 2. `application.yml` de cada servicio

Añadir el bloque de `APPLICATION_YML_SNIPPET.yml` y cambiar el valor de
`management.metrics.tags.service` al nombre del servicio correspondiente:

| Módulo                | Valor del tag `service`  |
|-----------------------|--------------------------|
| gateway               | `gateway`                |
| userManagement        | `usermanagement`         |
| motelManegement       | `motel-management`       |
| notificationService   | `notification`           |
| paymentService        | `payment`                |

### 3. Logback JSON (`logback-spring.xml`)

Copiar `LOGBACK_CONFIG.xml` a `src/main/resources/logback-spring.xml` en
cada microservicio. Ajusta el `springProperty` `SERVICE_NAME` al nombre del servicio.

---

## Despliegue

```bash
# 1. Crear la red si no existe
docker network create ubik-network

# 2. Añadir variables al .env principal
cat >> .env << 'EOF'
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=<contraseña_segura>
GRAFANA_ROOT_URL=http://localhost:3000
ALERT_EMAIL=ops@tudominio.com
ALERT_EMAIL_CRITICAL=oncall@tudominio.com
ALERT_EMAIL_PAYMENT=payments@tudominio.com
EOF

# 3. Levantar stack completo
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d

# 4. Solo monitoreo (si los microservicios ya están corriendo)
docker compose -f docker-compose.monitoring.yml up -d
```

## Verificación

```bash
# Estado de todos los contenedores
docker compose -f docker-compose.monitoring.yml ps

# Prometheus targets (deben aparecer UP)
open http://localhost:9090/targets

# Grafana
open http://localhost:3000   # admin / <contraseña>

# Alertmanager
open http://localhost:9093

# Recargar config de Prometheus sin reiniciar
curl -X POST http://localhost:9090/-/reload
```

## Estructura de archivos

```
monitoring/
├── prometheus/
│   ├── prometheus.yml          # Config principal de scraping
│   └── rules/
│       ├── microservices.yml   # Alertas de disponibilidad y errores
│       └── jvm.yml             # Alertas de JVM y contenedores
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/        # Prometheus + Loki auto-configurados
│   │   └── dashboards/         # Provisioning automático de dashboards
│   └── dashboards/
│       └── overview.json       # Dashboard principal de microservicios
├── loki/
│   └── loki-config.yml         # Config de Loki (retención 30 días)
├── promtail/
│   └── promtail-config.yml     # Recolección de logs Docker → Loki
└── alertmanager/
    └── alertmanager.yml        # Rutas y receptores de alertas
```

## Queries LogQL útiles en Grafana → Explore → Loki

```logql
# Todos los errores de todos los servicios
{container=~"gateway|payment-service|usermanagement-service"} |= "ERROR"

# Logs del gateway con latencia
{service="gateway"} | json | duration > 1s

# Errores de pago en los últimos 15 minutos
{service="payment"} |= "ERROR" | json | line_format "{{.message}}"

# Conteo de errores por servicio
sum by (service) (count_over_time({container=~".+"} |= "ERROR" [5m]))
```

## Queries PromQL útiles en Grafana → Explore → Prometheus

```promql
# Request rate por servicio
sum(rate(http_server_requests_seconds_count[1m])) by (service)

# p99 de latencia
histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (service, le))

# Tasa de errores
100 * sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (service)
      / sum(rate(http_server_requests_seconds_count[5m])) by (service)

# Heap JVM
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```