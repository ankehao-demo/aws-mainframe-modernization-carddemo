import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { MainMenuComponent } from './main-menu.component';
import { AuthService } from '../services/auth.service';

describe('MainMenuComponent', () => {
  let component: MainMenuComponent;
  let fixture: ComponentFixture<MainMenuComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserId', 'logout']);
    authServiceSpy.getUserId.and.returnValue('USER0001');

    await TestBed.configureTestingModule({
      imports: [
        MainMenuComponent,
        RouterTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MainMenuComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display user id', () => {
    expect(component.userId).toBe('USER0001');
  });

  it('should have 11 menu items', () => {
    expect(component.menuItems.length).toBe(11);
  });

  it('should have Account View as first menu item', () => {
    expect(component.menuItems[0].label).toBe('Account View');
  });

  it('should have Pending Authorization View as last menu item', () => {
    expect(component.menuItems[10].label).toBe('Pending Authorization View');
  });

  it('should navigate on menu item click', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.navigate(component.menuItems[0]);
    expect(navigateSpy).toHaveBeenCalledWith(['/accounts/view']);
  });

  it('should logout and redirect to login', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.logout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should default userId to empty string when getUserId returns null', () => {
    authServiceSpy.getUserId.and.returnValue(null);
    const newFixture = TestBed.createComponent(MainMenuComponent);
    const newComponent = newFixture.componentInstance;
    expect(newComponent.userId).toBe('');
  });
});
