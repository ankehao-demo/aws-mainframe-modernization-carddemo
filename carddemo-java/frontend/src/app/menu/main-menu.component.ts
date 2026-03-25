import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../services/auth.service';

export interface MenuItem {
  id: number;
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-main-menu',
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
    <mat-toolbar color="primary">
      <span>CardDemo - Main Menu</span>
      <span class="spacer"></span>
      <span class="user-info">User: {{ userId }}</span>
      <button mat-icon-button (click)="logout()" aria-label="Logout">
        <mat-icon>logout</mat-icon>
      </button>
    </mat-toolbar>

    <div class="menu-container">
      <mat-card class="menu-card">
        <mat-card-header>
          <mat-card-title>User Menu</mat-card-title>
          <mat-card-subtitle>Select an option</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <mat-nav-list>
            <a mat-list-item *ngFor="let item of menuItems" (click)="navigate(item)">
              <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
              <span matListItemTitle>{{ item.id }}. {{ item.label }}</span>
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
      justify-content: center;
      padding: 24px;
    }
    .menu-card { width: 600px; }
  `]
})
export class MainMenuComponent {
  userId: string;

  menuItems: MenuItem[] = [
    { id: 1, label: 'Account View', icon: 'account_balance', route: '/accounts/view' },
    { id: 2, label: 'Account Update', icon: 'edit', route: '/accounts/update' },
    { id: 3, label: 'Card List', icon: 'credit_card', route: '/cards/list' },
    { id: 4, label: 'Card View', icon: 'visibility', route: '/cards/view' },
    { id: 5, label: 'Card Update', icon: 'credit_score', route: '/cards/update' },
    { id: 6, label: 'Transaction List', icon: 'list', route: '/transactions/list' },
    { id: 7, label: 'Transaction View', icon: 'receipt', route: '/transactions/view' },
    { id: 8, label: 'Transaction Add', icon: 'add_circle', route: '/transactions/add' },
    { id: 9, label: 'Transaction Reports', icon: 'assessment', route: '/reports' },
    { id: 10, label: 'Bill Payment', icon: 'payment', route: '/payments' },
    { id: 11, label: 'Pending Authorization View', icon: 'pending_actions', route: '/authorizations' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.userId = this.authService.getUserId() || '';
  }

  navigate(item: MenuItem): void {
    this.router.navigate([item.route]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
