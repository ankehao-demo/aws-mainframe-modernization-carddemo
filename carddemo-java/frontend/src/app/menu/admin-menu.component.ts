import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../services/auth.service';

export interface AdminMenuItem {
  id: number;
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-admin-menu',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatToolbarModule
  ],
  template: `
    <mat-toolbar color="warn">
      <span>CardDemo - Admin Menu</span>
      <span class="spacer"></span>
      <span class="user-info">Admin: {{ userId }}</span>
      <button mat-icon-button (click)="logout()" aria-label="Logout">
        <mat-icon>logout</mat-icon>
      </button>
    </mat-toolbar>

    <div class="menu-container">
      <mat-card class="menu-card">
        <mat-card-header>
          <mat-card-title>Admin Menu</mat-card-title>
          <mat-card-subtitle>Administrative functions</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <mat-nav-list>
            <a mat-list-item *ngFor="let item of adminMenuItems" (click)="navigate(item)">
              <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
              <span matListItemTitle>{{ item.id }}. {{ item.label }}</span>
            </a>
          </mat-nav-list>
        </mat-card-content>
      </mat-card>

      <mat-card class="menu-card">
        <mat-card-header>
          <mat-card-title>User Functions</mat-card-title>
          <mat-card-subtitle>Standard user operations</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <mat-nav-list>
            <a mat-list-item (click)="goToUserMenu()">
              <mat-icon matListItemIcon>menu</mat-icon>
              <span matListItemTitle>Access User Menu</span>
            </a>
          </mat-nav-list>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .spacer { flex: 1 1 auto; }
    .user-info { margin-right: 16px; font-size: 14px; }
    .menu-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px;
      gap: 16px;
    }
    .menu-card { width: 600px; }
  `]
})
export class AdminMenuComponent {
  userId: string;

  adminMenuItems: AdminMenuItem[] = [
    { id: 1, label: 'User List', icon: 'people', route: '/admin/users' },
    { id: 2, label: 'User Add', icon: 'person_add', route: '/admin/users/add' },
    { id: 3, label: 'User Update', icon: 'manage_accounts', route: '/admin/users/update' },
    { id: 4, label: 'User Delete', icon: 'person_remove', route: '/admin/users/delete' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.userId = this.authService.getUserId() || '';
  }

  navigate(item: AdminMenuItem): void {
    this.router.navigate([item.route]);
  }

  goToUserMenu(): void {
    this.router.navigate(['/menu']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
