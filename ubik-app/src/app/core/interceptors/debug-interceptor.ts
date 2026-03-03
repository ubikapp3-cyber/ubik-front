import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export const debugInterceptor: HttpInterceptorFn = (req, next) => {
  // Only log in non-production to avoid leaking sensitive info in prod logs
  if (environment.production) return next(req);

  try {
    console.groupCollapsed(`[HTTP][REQ] ${req.method} ${req.urlWithParams}`);
    console.log('Request url:', req.urlWithParams);
    console.log('Request method:', req.method);
    console.log('Request headers:', req.headers);
    // Try to log body safely (FormData, Blobs, etc. may not serialize)
    try {
      console.log('Request body:', req.body);
    } catch (e) {
      console.log('Request body: <unserializable>');
    }
    console.groupEnd();
  } catch (e) {
    console.warn('Failed to log request', e);
  }

  return next(req).pipe(
    tap({
      next: (event) => {
        if (event instanceof HttpResponse) {
          try {
            console.groupCollapsed(
              `[HTTP][RES] ${req.method} ${req.urlWithParams} -> ${event.status}`,
            );
            console.log('Status:', event.status);
            console.log('Response headers:', event.headers);
            try {
              console.log('Response body:', event.body);
            } catch (e) {
              console.log('Response body: <unserializable>');
            }
            console.groupEnd();
          } catch (e) {
            console.warn('Failed to log response', e);
          }
        }
      },
      error: (err) => {
        try {
          console.groupCollapsed(`[HTTP][ERR] ${req.method} ${req.urlWithParams}`);
          console.error(err);
          console.groupEnd();
        } catch (e) {
          console.warn('Failed to log error', e);
        }
      },
    }),
  );
};
