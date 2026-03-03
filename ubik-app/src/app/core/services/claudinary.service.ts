import { Injectable } from '@angular/core';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

type CloudinaryUploadResponse = {
  secure_url: string;
  public_id: string;
};

@Injectable({ providedIn: 'root' })
export class CloudinaryService {
  private httpNoInterceptors: HttpClient;

  private cloudName = environment.cloudinary.cloudName;
  private uploadPreset = environment.cloudinary.uploadPreset;
  private baseFolder = environment.cloudinary.folder;

  constructor(handler: HttpBackend) {
    // ✅ este cliente no pasa por interceptores
    this.httpNoInterceptors = new HttpClient(handler);
  }

  uploadFile(file: File, folderSuffix: string): Observable<string> {
    // ✅ endpoint correcto según tipo
    const isImage = file.type.startsWith('image/');
    const resource = isImage ? 'image' : 'raw'; // PDFs/docs => raw

    const url = `https://api.cloudinary.com/v1_1/${this.cloudName}/${resource}/upload`;

    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.uploadPreset);
    formData.append('folder', `${this.baseFolder}/${folderSuffix}`);

    // Debug rápido (puedes quitarlo luego)
    // for (const [k, v] of formData.entries()) console.log('CLD', k, v);

    console.log('cloudName', this.cloudName);
    console.log('uploadPreset', this.uploadPreset);
    for (const [k, v] of formData.entries()) console.log('CLD', k, v);

    return this.httpNoInterceptors
      
      .post<CloudinaryUploadResponse>(url, formData)
      .pipe(map((res) => res.secure_url));
  }
}
