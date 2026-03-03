import 'zone.js';

import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { App } from './app/app';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/interceptors/auth-interceptor';
import { debugInterceptor } from './app/core/interceptors/debug-interceptor';

bootstrapApplication(App, {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withFetch(),
      // authInterceptor should run before debugInterceptor so the logger sees the final request
      withInterceptors([authInterceptor, debugInterceptor]),
    ),
  ],
}).catch((err) => console.error(err));
