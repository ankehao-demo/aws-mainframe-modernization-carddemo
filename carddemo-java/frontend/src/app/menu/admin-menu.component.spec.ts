import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { AdminMenuComponent } from './admin-menu.component';
import { AuthService } from '../services/auth.service';

describe('AdminMenuComponent', () => {
  let component: AdminMenuComponent;
  let fixture: ComponentFixture<AdminMenuComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserId', 'logout']);
    authServiceSpy.getUserId.and.returnValue('ADMIN001');

    await TestBed.configureTestingModule({
      imports: [
        AdminMenuComponent,
        RouterTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminMenuComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display admin user id', () => {
    expect(component.userId).toBe('ADMIN001');
  });

  it('should have 4 admin menu items', () => {
    expect(component.adminMenuItems.length).toBe(4);
  });

  it('should have User List as first admin menu item', () => {
    expect(component.adminMenuItems[0].label).toBe('User List');
  });

  it('should navigate on admin menu item click', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.navigate(component.adminMenuItems[0]);
    expect(navigateSpy).toHaveBeenCalledWith(['/admin/users']);
  });

  it('should navigate to user menu', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.goToUserMenu();
    expect(navigateSpy).toHaveBeenCalledWith(['/menu']);
  });

  it('should logout and redirect to login', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.logout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should default userId to empty string when getUserId returns null', () => {
    authServiceSpy.getUserId.and.returnValue(null);
    const newFixture = TestBed.createComponent(AdminMenuComponent);
    const newComponent = newFixture.componentInstance;
    expect(newComponent.userId).toBe('');
  });
});
