import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  userType: string;
}

export interface AuthError {
  error: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly USER_ID_KEY = 'user_id';
  private readonly USER_TYPE_KEY = 'user_type';

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(userId: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, {
      userId,
      password
    }).pipe(
      tap(response => {
        this.storeToken(response.token);
        this.storeUserId(response.userId);
        this.storeUserType(response.userType);
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    localStorage.removeItem(this.USER_TYPE_KEY);
    this.isAuthenticatedSubject.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUserId(): string | null {
    return localStorage.getItem(this.USER_ID_KEY);
  }

  getUserType(): string | null {
    return localStorage.getItem(this.USER_TYPE_KEY);
  }

  isAdmin(): boolean {
    return this.getUserType() === 'A';
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  private storeToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private storeUserId(userId: string): void {
    localStorage.setItem(this.USER_ID_KEY, userId);
  }

  private storeUserType(userType: string): void {
    localStorage.setItem(this.USER_TYPE_KEY, userType);
  }
}
