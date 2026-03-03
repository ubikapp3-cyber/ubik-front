import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { LoginService } from '../../../core/services/login.service';
import { AuthService } from '../../../core/middleware/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  let mockLoginService: jasmine.SpyObj<LoginService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    mockLoginService = jasmine.createSpyObj<LoginService>('LoginService', [
      'login',
      'getProfile',
      'validateForm',
    ]);

    mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);

    mockAuthService = jasmine.createSpyObj<AuthService>('AuthService', [
      // not used directly by component methods, but included for completeness
      'setToken',
      'setUser',
      'logout',
      'isLogged',
      'token',
    ]);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter },
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('form updates', () => {
    it('should update username field', () => {
      component.updateField('username', 'test@example.com');
      expect(component.formData().username).toBe('test@example.com');
    });

    it('should update password field', () => {
      component.updateField('password', 'password123');
      expect(component.formData().password).toBe('password123');
    });

    it('should update rememberMe flag', () => {
      const event = { target: { checked: true } } as unknown as Event;
      component.onRememberMeChange(event);
      expect(component.rememberMe()).toBeTrue();
    });
  });

  describe('form submission', () => {
    it('should return early if username or password missing', () => {
      // username empty by default
      component.updateField('password', 'password123');

      component.onFormSubmit();

      expect(component.isSubmitting()).toBeFalse();
      expect(mockLoginService.login).not.toHaveBeenCalled();
      expect(mockLoginService.getProfile).not.toHaveBeenCalled();
    });

    it('should set isSubmitting true and call login with rememberMe', () => {
      mockLoginService.login.and.returnValue(of('"test-token"'));
      mockLoginService.getProfile.and.returnValue(of({}));

      component.rememberMe.set(true);
      component.updateField('username', 'testuser');
      component.updateField('password', 'password123');

      component.onFormSubmit();

      expect(component.isSubmitting()).toBeTrue();
      expect(mockLoginService.login).toHaveBeenCalledWith(
        { username: 'testuser', password: 'password123' },
        true
      );
    });

    it('should call getProfile after login success and navigate to "/"', fakeAsync(() => {
      mockLoginService.login.and.returnValue(of('"test-token"'));
      mockLoginService.getProfile.and.returnValue(of({ id: 1, name: 'User' }));
      mockRouter.navigate.and.returnValue(Promise.resolve(true));

      component.updateField('username', 'testuser');
      component.updateField('password', 'password123');

      component.onFormSubmit();
      tick(); // flush nested subscribe chain

      expect(mockLoginService.getProfile).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
      expect(component.isSubmitting()).toBeFalse();
    }));

    it('should stop submitting if login fails', fakeAsync(() => {
      mockLoginService.login.and.returnValue(throwError(() => ({ message: 'Login failed' })));

      component.updateField('username', 'testuser');
      component.updateField('password', 'password123');

      component.onFormSubmit();
      tick();

      expect(mockLoginService.getProfile).not.toHaveBeenCalled();
      expect(component.isSubmitting()).toBeFalse();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    }));

    it('should stop submitting if getProfile fails', fakeAsync(() => {
      mockLoginService.login.and.returnValue(of('"test-token"'));
      mockLoginService.getProfile.and.returnValue(
        throwError(() => ({ message: 'Profile failed' }))
      );

      component.updateField('username', 'testuser');
      component.updateField('password', 'password123');

      component.onFormSubmit();
      tick();

      expect(mockLoginService.getProfile).toHaveBeenCalled();
      expect(component.isSubmitting()).toBeFalse();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    }));
  });

  describe('navigation', () => {
    it('navigateToRegister should navigate to "/" (as implemented)', () => {
      mockRouter.navigate.and.returnValue(Promise.resolve(true));

      component.navigateToRegister();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });

    it('navigateToPasswordReset should navigate to "/forgot-password"', () => {
      mockRouter.navigate.and.returnValue(Promise.resolve(true));

      component.navigateToPasswordReset();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/forgot-password']);
    });
  });

  describe('error helpers', () => {
    it('should get field error', () => {
      component.errors.set([{ field: 'username', message: 'El usuario es obligatorio' }]);
      expect(component.getFieldError('username')).toBe('El usuario es obligatorio');
    });

    it('should return null for non-existent error', () => {
      component.errors.set([]);
      expect(component.getFieldError('username')).toBeNull();
    });

    it('should check if field has error', () => {
      component.errors.set([{ field: 'username', message: 'El usuario es obligatorio' }]);
      expect(component.hasFieldError('username')).toBeTrue();
      expect(component.hasFieldError('password')).toBeFalse();
    });
  });
});
