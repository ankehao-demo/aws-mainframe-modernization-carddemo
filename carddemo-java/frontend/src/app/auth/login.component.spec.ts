import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        RouterTestingModule.withRoutes([
          { path: 'menu', component: LoginComponent },
          { path: 'admin-menu', component: LoginComponent }
        ]),
        BrowserAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a login form with userId and password', () => {
    expect(component.loginForm.get('userId')).toBeTruthy();
    expect(component.loginForm.get('password')).toBeTruthy();
  });

  it('should require userId', () => {
    component.loginForm.get('userId')?.setValue('');
    expect(component.loginForm.get('userId')?.hasError('required')).toBeTrue();
  });

  it('should require password', () => {
    component.loginForm.get('password')?.setValue('');
    expect(component.loginForm.get('password')?.hasError('required')).toBeTrue();
  });

  it('should not submit if form is invalid', () => {
    component.loginForm.get('userId')?.setValue('');
    component.loginForm.get('password')?.setValue('');
    component.onSubmit();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should call authService.login on valid submit', () => {
    authServiceSpy.login.and.returnValue(of({
      token: 'test-token',
      userId: 'USER0001',
      userType: 'U'
    }));

    component.loginForm.get('userId')?.setValue('USER0001');
    component.loginForm.get('password')?.setValue('PASSWORD');
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith('USER0001', 'PASSWORD');
  });

  it('should navigate to menu on successful user login', () => {
    const navigateSpy = spyOn(router, 'navigate');
    authServiceSpy.login.and.returnValue(of({
      token: 'test-token',
      userId: 'USER0001',
      userType: 'U'
    }));

    component.loginForm.get('userId')?.setValue('USER0001');
    component.loginForm.get('password')?.setValue('PASSWORD');
    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/menu']);
  });

  it('should navigate to admin-menu on successful admin login', () => {
    const navigateSpy = spyOn(router, 'navigate');
    authServiceSpy.login.and.returnValue(of({
      token: 'admin-token',
      userId: 'ADMIN001',
      userType: 'A'
    }));

    component.loginForm.get('userId')?.setValue('ADMIN001');
    component.loginForm.get('password')?.setValue('PASSWORD');
    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/admin-menu']);
  });

  it('should display error message on failed login', () => {
    authServiceSpy.login.and.returnValue(throwError(() => ({
      error: { error: 'Wrong Password. Try again ...' }
    })));

    component.loginForm.get('userId')?.setValue('USER0001');
    component.loginForm.get('password')?.setValue('WRONG');
    component.onSubmit();

    expect(component.errorMessage).toBe('Wrong Password. Try again ...');
  });

  it('should display generic error on network failure', () => {
    authServiceSpy.login.and.returnValue(throwError(() => ({
      error: null
    })));

    component.loginForm.get('userId')?.setValue('USER0001');
    component.loginForm.get('password')?.setValue('PASSWORD');
    component.onSubmit();

    expect(component.errorMessage).toBe('Login failed. Please try again.');
  });

  it('should set isLoading during login', () => {
    authServiceSpy.login.and.returnValue(of({
      token: 'test-token',
      userId: 'USER0001',
      userType: 'U'
    }));

    component.loginForm.get('userId')?.setValue('USER0001');
    component.loginForm.get('password')?.setValue('PASSWORD');

    expect(component.isLoading).toBeFalse();
    component.onSubmit();
    expect(component.isLoading).toBeFalse(); // After completion
  });
});
