import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth-interceptor';

describe('AuthInterceptor', () => {
  let interceptor: authInterceptor;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [authInterceptor],
    });
    interceptor = TestBed.inject(authInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });
});
