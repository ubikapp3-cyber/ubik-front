# ![Logo Ubik](./public/assets/logo/favicon.png) UBIK

Frontend web de Ubik – aplicación orientada a dispositivos móviles, construida con Angular, diseñada como plataforma formativa para el SENA.

Ubik es una plataforma web progresiva (PWA) que apoya la gestión hotelera y mejora la experiencia de los usuarios finales, permitiendo la 
administración de establecimientos tipo motel y ofreciendo a los clientes una forma simple de encontrar habitaciones disponibles a buenos precios.

--- 

## 🧠 ¿Qué es Ubik?

Ubik es una solución orientada a la gestión de moteles y a la interacción entre:

  **✔ Dueños de establecimientos — pueden gestionar sus habitaciones y clientes**<br>
  **✔ Clientes — pueden encontrar moteles y reservar habitaciones**

Se trata de un proyecto formativo desarrollado como parte de la capacitación del Servicio Nacional de Aprendizaje (SENA) en Colombia.

🔎 Enfoque principal del repositorio es el frontend web adaptado a mobile, con futuro soporte para mapas y búsqueda de lugares cercanos mediante geolocalización del usuario.
  
### 🚀 Características principales
  
- ✨ Autenticación y roles de usuario

- Registro e inicio de sesión.

### Selección de tipo de rol:

  🧑‍💼 Dueño de establecimiento
  
  👤 Cliente (anónimo)
  
### 📍 Geolocalización

  Muestra ubicación del usuario en tiempo real en un mapa.
  
  Próximamente: listado y visualización de establecimientos cercanos.

## 📍 Diseño Mobile-First

  UI pensada para dispositivos pequeños.
  
  Arquitectura modular con Angular.

## ⚠ Para versiones iniciales:

  Funcionalidades de búsqueda aún no están disponibles.
  
  Vista principal (Home) con elementos básicos para prototipado.

## 📦 Tecnologías utilizadas

  La aplicación fue desarrollada principalmente con:
  
  Angular	Framework frontend principal
  TypeScript	Lógica de aplicación
  HTML / Tailwind	Estructura y estilos
  Mapa y Geolocalización	Integración con APIs de mapa para mostrar ubicación
  
  (Esta información está deducida del contenido del repositorio, que contiene archivos de Angular como angular.json, TypeScript y configuraciones típicas de este framework).

## 🗂 Estructura del proyecto

  La estructura principal del repositorio es la siguiente:
    ``` code
      /
      ├── public/               # Archivos estáticos
      ├── src/                  # Código fuente Angular
      ├── angular.json          # Configuración Angular
      ├── package.json          # Dependencias y scripts
      ├── tsconfig.json         # Configuración TypeScript
      └── ...otros archivos de configuración
    ``` 

  Puedes abrir el proyecto con tu editor preferido y ejecutar el frontend localmente con los scripts típicos de Angular.
  
## 🛠️ Cómo ejecutar el proyecto
  
  1. Clonar el repositorio
  
    ``` bash
    
      git clone https://github.com/Juankos0714/Ubik-App.git
    ```
  
  2. Instalar dependencias
     
    ``` bash
    
      npm install
    ```
  
  3. Ejecutar la app en modo desarrollo
     
    ``` bash
    
      ng serve
    ```
  
  4. Abrir en el navegador
  
    ``` code
    
      http://localhost:4200
    ```
### 📌 Estado actual y roadmap

  **❗ Funcionalidades actuales**
  
  Registro e inicio de sesión
  
  Selección de tipo de usuario
  
  Vista básica de mapa con ubicación del usuario

### 🚧 Funcionalidades en desarrollo

  Búsquedas en Explore y Home
  
  Mostrar moteles cercanos con mapa
  
  Integración con backend (si aplica)
  
  Reservas desde la UI

