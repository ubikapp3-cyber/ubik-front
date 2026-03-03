import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class ForgotService {
  private baseUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  requestReset(email: string): Observable<string> {
    return this.http.post(
      `${this.baseUrl}/reset-password-request?email=${encodeURIComponent(email)}`,
      null,
      { responseType: 'text' }
    );
  }

  resetPassword(token: string, newPassword: string): Observable<string> {
    return this.http.post(
      `${this.baseUrl}/reset-password`,
      { token, newPassword },
      { responseType: 'text' }
    );
  }
}
