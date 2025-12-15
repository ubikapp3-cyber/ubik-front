# Guía de Construcción de Docker para Microservicios

## Problemas Resueltos

### 1. Versión de Spring Boot Incorrecta
**Problema**: El `pom.xml` padre referenciaba Spring Boot versión `3.5.3`, la cual **no existe** en Maven Central.

**Solución**: Actualizado a Spring Boot `3.2.5` (versión estable y disponible).

### 2. Versión de Spring Cloud Incompatible
**Problema**: Spring Cloud versión `2025.0.0` no es compatible con Spring Boot 3.2.5.

**Solución**: Actualizado a Spring Cloud `2023.0.1` para compatibilidad.

### 3. Inconsistencia en Versiones de Java
**Problema**: El módulo `motelManagement` usaba Java 21, mientras que `gateway` y `userManagement` usaban Java 17.

**Solución**: Unificado todo a Java 17, consistente con el `pom.xml` padre.

## Construcción de Imágenes Docker

### Gateway
```bash
cd /ruta/al/proyecto/microservicios/microreactivo
docker build -f gateway/Dockerfile -t ubik/gateway:latest .
```

### User Management
```bash
cd /ruta/al/proyecto/microservicios/microreactivo
docker build -f userManagement/Dockerfile -t ubik/user-management:latest .
```

### Motel Management
```bash
cd /ruta/al/proyecto/microservicios/microreactivo
# Nota: El directorio se llama "motelManegement" (typo en el nombre original)
docker build -f motelManegement/Dockerfile -t ubik/motel-management:latest .
```

## Nota Importante sobre Certificados SSL en Docker

Si encuentras errores como:
```
PKIX path building failed: unable to find valid certification path to requested target
```

Esto NO es un error en los Dockerfiles, sino un problema de configuración de certificados SSL en el entorno Docker. 

### Soluciones Alternativas:

#### Opción 1: Usar `--network=host`
```bash
docker build --network=host -f gateway/Dockerfile -t ubik/gateway:latest .
```

#### Opción 2: Construir fuera de Docker primero
```bash
# Compilar localmente primero
mvn clean package -DskipTests

# Luego usar un Dockerfile simplificado que solo copia el JAR
```

#### Opción 3: Configurar proxy o certificados corporativos
Si estás detrás de un proxy corporativo, necesitas configurar:
- Variables de entorno HTTP_PROXY, HTTPS_PROXY
- Certificados corporativos en el sistema

## Verificación

Para verificar que los Dockerfiles son correctos, compila localmente:

```bash
cd /ruta/al/proyecto/microservicios/microreactivo
mvn clean package -DskipTests
```

Si la compilación local funciona pero Docker falla con errores SSL, confirma que es un problema de entorno, no de código.

## Estructura de los Dockerfiles

Todos los Dockerfiles siguen una estructura de multi-etapa:

1. **Etapa BUILD**: Usa `maven:3.9.6-eclipse-temurin-17-alpine` para compilar
2. **Etapa RUNTIME**: Usa `eclipse-temurin:17-jre-alpine` para ejecutar

Esto asegura imágenes finales ligeras con solo el JRE necesario.
