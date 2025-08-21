import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { AppModule } from './app.module';
import { AppRoutingModule } from './app-routing.module';
import { AuthModule } from './features/auth/auth.module';
import { SessionsModule } from './features/sessions/sessions.module';

describe('Modules Integration', () => {
  
  describe('AppModule', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [AppModule]
      }).compileComponents();
    });

    it('should create app module', () => {
      const appModule = TestBed.inject(AppModule);
      expect(appModule).toBeTruthy();
    });

    it('should import app routing module', () => {
      const appRoutingModule = TestBed.inject(AppRoutingModule);
      expect(appRoutingModule).toBeTruthy();
    });
  });

  describe('AuthModule', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [AuthModule]
      }).compileComponents();
    });

    it('should create auth module', () => {
      const authModule = TestBed.inject(AuthModule);
      expect(authModule).toBeTruthy();
    });
  });

  describe('SessionsModule', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [SessionsModule]
      }).compileComponents();
    });

    it('should create sessions module', () => {
      const sessionsModule = TestBed.inject(SessionsModule);
      expect(sessionsModule).toBeTruthy();
    });
  });
});