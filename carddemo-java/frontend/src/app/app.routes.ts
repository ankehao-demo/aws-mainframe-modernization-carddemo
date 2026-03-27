import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
import { MainMenuComponent } from './menu/main-menu.component';
import { AdminMenuComponent } from './menu/admin-menu.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'menu', component: MainMenuComponent, canActivate: [AuthGuard] },
  { path: 'admin-menu', component: AdminMenuComponent, canActivate: [AdminGuard] },
  { path: '**', redirectTo: '/login' }
];
