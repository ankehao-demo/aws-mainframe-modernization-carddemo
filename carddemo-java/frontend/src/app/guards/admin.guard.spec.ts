import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AdminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';

describe('AdminGuard', () => {
  let guard: AdminGuard;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['hasToken', 'isAdmin']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AdminGuard,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    guard = TestBed.inject(AdminGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access for admin users', () => {
    authServiceSpy.hasToken.and.returnValue(true);
    authServiceSpy.isAdmin.and.returnValue(true);
    expect(guard.canActivate()).toBeTrue();
  });

  it('should deny access and redirect to login when no token', () => {
    authServiceSpy.hasToken.and.returnValue(false);
    authServiceSpy.isAdmin.and.returnValue(false);
    expect(guard.canActivate()).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should deny access and redirect to menu for non-admin users', () => {
    authServiceSpy.hasToken.and.returnValue(true);
    authServiceSpy.isAdmin.and.returnValue(false);
    expect(guard.canActivate()).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/menu']);
  });
});
