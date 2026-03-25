import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, LoginResponse } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login successfully', () => {
    const mockResponse: LoginResponse = {
      token: 'test-jwt-token',
      userId: 'USER0001',
      userType: 'U'
    };

    service.login('USER0001', 'PASSWORD').subscribe(response => {
      expect(response.token).toBe('test-jwt-token');
      expect(response.userId).toBe('USER0001');
      expect(response.userType).toBe('U');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ userId: 'USER0001', password: 'PASSWORD' });
    req.flush(mockResponse);
  });

  it('should store token on login', () => {
    const mockResponse: LoginResponse = {
      token: 'test-jwt-token',
      userId: 'USER0001',
      userType: 'U'
    };

    service.login('USER0001', 'PASSWORD').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    req.flush(mockResponse);

    expect(service.getToken()).toBe('test-jwt-token');
    expect(service.getUserId()).toBe('USER0001');
    expect(service.getUserType()).toBe('U');
  });

  it('should clear token on logout', () => {
    localStorage.setItem('jwt_token', 'test-token');
    localStorage.setItem('user_id', 'USER0001');
    localStorage.setItem('user_type', 'U');

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.getUserId()).toBeNull();
    expect(service.getUserType()).toBeNull();
  });

  it('should return hasToken false when no token', () => {
    expect(service.hasToken()).toBeFalse();
  });

  it('should return hasToken true when token exists', () => {
    localStorage.setItem('jwt_token', 'test-token');
    expect(service.hasToken()).toBeTrue();
  });

  it('should return isAdmin true for admin user', () => {
    localStorage.setItem('user_type', 'A');
    expect(service.isAdmin()).toBeTrue();
  });

  it('should return isAdmin false for regular user', () => {
    localStorage.setItem('user_type', 'U');
    expect(service.isAdmin()).toBeFalse();
  });

  it('should emit isAuthenticated$ true on login', () => {
    const mockResponse: LoginResponse = {
      token: 'test-jwt-token',
      userId: 'USER0001',
      userType: 'U'
    };

    let isAuth = false;
    service.isAuthenticated$.subscribe(val => isAuth = val);

    service.login('USER0001', 'PASSWORD').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    req.flush(mockResponse);

    expect(isAuth).toBeTrue();
  });

  it('should emit isAuthenticated$ false on logout', () => {
    localStorage.setItem('jwt_token', 'test-token');

    let isAuth = true;
    service.isAuthenticated$.subscribe(val => isAuth = val);

    service.logout();
    expect(isAuth).toBeFalse();
  });
});
