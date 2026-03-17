# Guía de Uso: Gestión de Imágenes de Moteles desde el Frontend

Esta guía explica el flujo de trabajo para subir y gestionar imágenes (Perfil, Portada y Galería) en el sistema Ubik.

## Flujo General

El proceso se divide en dos pasos:
1. **Subida Física:** La imagen se envía al servicio de Cloudinary a través de un endpoint general.
2. **Asociación:** La URL obtenida se asocia al motel específico según su rol (Profile, Cover o Gallery).

---

## Paso 1: Subir la Imagen a Cloudinary

Para cualquier imagen, primero debemos subirla para obtener una URL pública.

### Subir una sola imagen (Profile o Cover)
- **Endpoint:** `POST /api/images/upload`
- **Content-Type:** `multipart/form-data`
- **Query Params:** `folder=motels` (opcional, recomendado "motels")
- **Body (FormData):** `file` (el archivo binario)

**Ejemplo de respuesta:**
```json
{
  "url": "https://res.cloudinary.com/ubik/image/upload/v12345/motels/abc.jpg",
  "message": "Imagen subida exitosamente"
}
```

### Subir varias imágenes (Galería)
- **Endpoint:** `POST /api/images/upload-multiple`
- **Content-Type:** `multipart/form-data`
- **Query Params:** `folder=motels`
- **Body (FormData):** `files` (pueden ser múltiples campos con el mismo nombre)

**Ejemplo de respuesta:**
```json
{
  "urls": [
    "https://res.cloudinary.com/ubik/image/upload/v12345/motels/img1.jpg",
    "https://res.cloudinary.com/ubik/image/upload/v12345/motels/img2.jpg"
  ],
  "message": "2 imágenes subidas exitosamente"
}
```

---

## Paso 2: Asociar la URL al Motel

Una vez que tengas la URL, debes llamar al endpoint correspondiente del motel.

### Establecer Imagen de Perfil (Profile)
Sustituye la imagen de perfil actual por la nueva.
- **Endpoint:** `PUT /motels/{id}/images/profile`
- **Body:**
```json
{
  "url": "https://res.cloudinary.com/ubik/image/upload/v12345/motels/abc.jpg"
}
```

### Establecer Imagen de Portada (Cover)
Sustituye la imagen de portada actual por la nueva.
- **Endpoint:** `PUT /motels/{id}/images/cover`
- **Body:**
```json
{
  "url": "https://res.cloudinary.com/ubik/image/upload/v12345/motels/cover.jpg"
}
```

### Agregar a la Galería (Gallery) - Opción A: Dos pasos
1. Subir a `/api/images/upload`.
2. Asociar a `/motels/{id}/images/gallery`.

### Agregar a la Galería (Gallery) - Opción B: Un solo paso (Recomendado)
Este endpoint sube los archivos y los asocia automáticamente a la galería del motel.
- **Endpoint:** `POST /api/motels/{id}/images`
- **Content-Type:** `multipart/form-data`
- **Body (FormData):** `images` (uno o varios archivos)

---

## Creación/Actualización Completa de Motel con Galería

Si estás creando un motel desde cero o actualizando sus datos junto con la galería:

### Crear Motel + Galería
- **Endpoint:** `POST /api/motels/with-images`
- **Body (FormData):**
  - `motelData`: JSON con los datos del motel (similar a un `CreateMotelRequest`).
  - `images`: Archivos para la galería.

### Actualizar Motel + Reemplazar Galería
- **Endpoint:** `PUT /api/motels/{id}/with-images`
- **Body (FormData):**
  - `motelData`: JSON con los datos del motel.
  - `images`: Nuevos archivos para la galería (reemplazarán los anteriores).

---

## Resumen de Endpoints por Tipo de Imagen

| Tipo | Método | Endpoint | Body |
| :--- | :--- | :--- | :--- |
| **Perfil** | Dos pasos | `PUT /motels/{id}/images/profile` | `{ "url": "..." }` |
| **Portada**| Dos pasos | `PUT /motels/{id}/images/cover` | `{ "url": "..." }` |
| **Galería**| Directo | `POST /api/motels/{id}/images` | `FormData (images)` |
| **Galería**| Dos pasos | `POST /motels/{id}/images/gallery`| `{ "url": "..." }` |
| **Eliminar**| Directo | `DELETE /motels/{id}/images/{imageId}` | N/A |
| **Eliminar (por URL)**| Directo | `DELETE /api/motels/{id}/images` | `["url1", "url2"]` |

---

## Eliminar Imágenes

### Eliminar de la Galería del Motel
- **Endpoint:** `DELETE /motels/{id}/images/{imageId}`
- **Nota:** El `{imageId}` es el ID numérico de la entrada en la base de datos (se obtiene al consultar el motel).

### Eliminar archivo de Cloudinary (Limpieza)
Si deseas borrar el archivo físico de Cloudinary:
- **Endpoint:** `DELETE /api/images?url=https://...`

---

## Ejemplo de Implementación en Angular (Servicio)

```typescript
// motel-image.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class MotelImageService {
  private apiUrl = '/api/images';
  private motelUrl = '/motels';

  constructor(private http: HttpClient) {}

  // Ejemplo: Subir y setear perfil
  uploadAndSetProfile(motelId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(`${this.apiUrl}/upload?folder=motels`, formData).pipe(
      switchMap(response => {
        return this.http.put(`${this.motelUrl}/${motelId}/images/profile`, { url: response.url });
      })
    );
  }

  // Ejemplo: Subir varias a la galería
  uploadAndAddToGallery(motelId: number, files: File[]): Observable<any> {
    const formData = new FormData();
    files.forEach(f => formData.append('files', f));

    return this.http.post<any>(`${this.apiUrl}/upload-multiple?folder=motels`, formData).pipe(
      switchMap(response => {
        // Para la galería, como el backend actual de gallery recibe una URL a la vez, 
        // podrías necesitar iterar o tener un endpoint que acepte lista.
        // Si el backend acepta uno por uno:
        const requests = response.urls.map(url => 
          this.http.post(`${this.motelUrl}/${motelId}/images/gallery`, { url })
        );
        return forkJoin(requests);
      })
    );
  }
}
```
