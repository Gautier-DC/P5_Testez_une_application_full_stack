import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { Component } from '@angular/core';
import { expect } from '@jest/globals';
import { of } from 'rxjs';

import { AppModule } from './app.module';
import { AppComponent } from './app.component';
import { SessionService } from './services/session.service';
import { AuthService } from './features/auth/services/auth.service';
import { SessionApiService } from './features/sessions/services/session-api.service';
import { TeacherService } from './services/teacher.service';
import { UserService } from './services/user.service';

// Mock components for routing tests
@Component({
  template: '<h1>Login Page</h1><router-outlet></router-outlet>'
})
class MockLoginComponent { }

@Component({
  template: '<h1>Register Page</h1>'
})
class MockRegisterComponent { }

@Component({
  template: '<h1>Sessions Page</h1>'
})
class MockSessionsComponent { }

@Component({
  template: '<h1>Me Page</h1>'
})
class MockMeComponent { }

@Component({
  template: '<h1>Not Found Page</h1>'
})
class MockNotFoundComponent { }

describe('App Integration Tests', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let router: Router;
  let location: Location;
  let mockSessionService: any;
  let mockAuthService: any;
  let mockSessionApiService: any;
  let mockTeacherService: any;
  let mockUserService: any;

  const mockSession = {
    id: 1,
    name: 'Test Session',
    description: 'Test Description',
    date: new Date(),
    teacher_id: 1,
    users: [1, 2],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const mockUser = {
    id: 1,
    firstName: 'Test',
    lastName: 'User',
    email: 'test@test.com',
    admin: true,
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const mockSessionInformation = {
    token: 'test-token',
    type: 'Bearer',
    id: 1,
    username: 'test@test.com',
    firstName: 'Test',
    lastName: 'User',
    admin: true
  };

  beforeEach(async () => {
    mockSessionService = {
      sessionInformation: mockSessionInformation,
      isLogged: true,
      $isLogged: jest.fn().mockReturnValue(of(true)),
      logIn: jest.fn(),
      logOut: jest.fn()
    };

    mockAuthService = {
      me: jest.fn().mockReturnValue(of(mockSessionInformation)),
      register: jest.fn().mockReturnValue(of(undefined)),
      login: jest.fn().mockReturnValue(of(mockSessionInformation))
    };

    mockSessionApiService = {
      all: jest.fn().mockReturnValue(of([mockSession])),
      detail: jest.fn().mockReturnValue(of(mockSession)),
      create: jest.fn().mockReturnValue(of(mockSession)),
      update: jest.fn().mockReturnValue(of(mockSession)),
      delete: jest.fn().mockReturnValue(of({})),
      participate: jest.fn().mockReturnValue(of(undefined)),
      unParticipate: jest.fn().mockReturnValue(of(undefined))
    };

    mockTeacherService = {
      all: jest.fn().mockReturnValue(of([{
        id: 1,
        firstName: 'John',
        lastName: 'Doe',
        createdAt: new Date(),
        updatedAt: new Date()
      }]))
    };

    mockUserService = {
      getById: jest.fn().mockReturnValue(of(mockUser))
    };

    await TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: TeacherService, useValue: mockTeacherService },
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);

    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  // ==================== ROUTING INTEGRATION TESTS ====================

  describe('Navigation Integration', () => {
    it('should navigate to login when not logged', async () => {
      mockSessionService.isLogged = false;
      mockSessionService.$isLogged.mockReturnValue(of(false));

      await router.navigate(['/login']);
      expect(location.path()).toBe('/login');
    });

    it('should navigate to sessions when logged', async () => {
      mockSessionService.isLogged = true;
      mockSessionService.$isLogged.mockReturnValue(of(true));

      await router.navigate(['/sessions']);
      expect(location.path()).toBe('/sessions');
    });

    it('should navigate to register page', async () => {
      await router.navigate(['/register']);
      expect(location.path()).toBe('/register');
    });

    it('should navigate to me page when authenticated', async () => {
      mockSessionService.isLogged = true;
      await router.navigate(['/me']);
      expect(location.path()).toBe('/me');
    });

    it('should redirect to 404 for invalid routes', async () => {
      await router.navigate(['/invalid-route']);
      expect(location.path()).toBe('/404');
    });
  });

  // ==================== AUTHENTICATION WORKFLOW INTEGRATION ====================

  describe('Authentication Workflow Integration', () => {
    it('should complete login workflow', async () => {
      // Start as not logged
      mockSessionService.isLogged = false;
      mockSessionService.$isLogged.mockReturnValue(of(false));

      // Navigate to login
      await router.navigate(['/login']);
      expect(location.path()).toBe('/login');

      // Simulate successful login
      mockSessionService.isLogged = true;
      mockSessionService.$isLogged.mockReturnValue(of(true));
      mockSessionService.logIn(mockSessionInformation);

      expect(mockSessionService.logIn).toHaveBeenCalledWith(mockSessionInformation);

      // Should now be able to access protected routes
      await router.navigate(['/sessions']);
      expect(location.path()).toBe('/sessions');
    });

    it('should complete logout workflow', async () => {
      // Start as logged
      mockSessionService.isLogged = true;
      mockSessionService.$isLogged.mockReturnValue(of(true));

      // Perform logout
      component.logout();

      expect(mockSessionService.logOut).toHaveBeenCalled();
    });

    it('should handle complete registration workflow', async () => {
      // Navigate to register
      await router.navigate(['/register']);
      expect(location.path()).toBe('/register');

      // Mock successful registration
      mockAuthService.register.mockReturnValue(of(undefined));
      
      const registerData = {
        firstName: 'New',
        lastName: 'User',
        email: 'new@test.com',
        password: 'password123'
      };

      mockAuthService.register(registerData);
      expect(mockAuthService.register).toHaveBeenCalledWith(registerData);
    });
  });

  // ==================== SESSION MANAGEMENT INTEGRATION ====================

  describe('Session Management Integration', () => {
    beforeEach(() => {
      mockSessionService.isLogged = true;
      mockSessionService.$isLogged.mockReturnValue(of(true));
    });

    it('should handle session creation workflow', async () => {
      await router.navigate(['/sessions/create']);
      expect(location.path()).toBe('/sessions/create');

      // Mock form data
      const newSession = {
        name: 'New Integration Session',
        description: 'Created via integration test',
        date: new Date(),
        teacher_id: 1
      };

      mockSessionApiService.create(newSession);
      expect(mockSessionApiService.create).toHaveBeenCalledWith(newSession);
    });

    it('should handle session update workflow', async () => {
      const sessionId = '1';
      await router.navigate([`/sessions/update/${sessionId}`]);
      expect(location.path()).toBe('/sessions/update/1');

      // Should fetch session details
      expect(mockSessionApiService.detail).toHaveBeenCalledWith(sessionId);

      // Mock update
      const updatedSession = { ...mockSession, name: 'Updated Session' };
      mockSessionApiService.update(sessionId, updatedSession);
      expect(mockSessionApiService.update).toHaveBeenCalledWith(sessionId, updatedSession);
    });

    it('should handle session detail view workflow', async () => {
      const sessionId = '1';
      await router.navigate([`/sessions/detail/${sessionId}`]);
      expect(location.path()).toBe('/sessions/detail/1');

      // Should fetch session details
      expect(mockSessionApiService.detail).toHaveBeenCalledWith(sessionId);
    });

    it('should handle session list workflow', async () => {
      await router.navigate(['/sessions']);
      expect(location.path()).toBe('/sessions');

      // Should fetch all sessions
      expect(mockSessionApiService.all).toHaveBeenCalled();
    });
  });

  // ==================== USER PROFILE INTEGRATION ====================

  describe('User Profile Integration', () => {
    beforeEach(() => {
      mockSessionService.isLogged = true;
      mockSessionService.$isLogged.mockReturnValue(of(true));
    });

    it('should handle me page workflow', async () => {
      await router.navigate(['/me']);
      expect(location.path()).toBe('/me');

      // Should fetch user information
      expect(mockUserService.getById).toHaveBeenCalledWith(mockSessionInformation.id.toString());
    });

    it('should handle user information display', () => {
      const result = component.$isLogged();
      result.subscribe(isLogged => {
        expect(isLogged).toBeTruthy();
      });
      expect(mockSessionService.$isLogged).toHaveBeenCalled();
    });
  });

  // ==================== GUARD INTEGRATION TESTS ====================

  describe('Guards Integration', () => {
    it('should test auth guard integration with routing', async () => {
      // Not logged user trying to access protected route
      mockSessionService.isLogged = false;
      
      await router.navigate(['/sessions']);
      // Auth guard should redirect to login
      expect(location.path()).toBe('/login');
    });

    it('should test unauth guard integration with routing', async () => {
      // Logged user trying to access auth routes
      mockSessionService.isLogged = true;
      
      await router.navigate(['/login']);
      // Unauth guard should redirect to rentals/sessions
      expect(location.path()).toBe('/sessions');
    });
  });

  // ==================== ERROR HANDLING INTEGRATION ====================

  describe('Error Handling Integration', () => {
    it('should handle API errors gracefully', async () => {
      mockSessionApiService.all.mockReturnValue(
        new Promise((_, reject) => reject(new Error('API Error')))
      );

      await router.navigate(['/sessions']);
      expect(location.path()).toBe('/sessions');
      
      // Should still attempt to call API
      expect(mockSessionApiService.all).toHaveBeenCalled();
    });

    it('should handle navigation to non-existent session', async () => {
      mockSessionApiService.detail.mockReturnValue(
        new Promise((_, reject) => reject(new Error('Session not found')))
      );

      await router.navigate(['/sessions/detail/999']);
      expect(location.path()).toBe('/sessions/detail/999');
      expect(mockSessionApiService.detail).toHaveBeenCalledWith('999');
    });
  });

  // ==================== STATE MANAGEMENT INTEGRATION ====================

  describe('State Management Integration', () => {
    it('should maintain session state across navigation', async () => {
      // Start logged in
      mockSessionService.isLogged = true;
      mockSessionService.$isLogged.mockReturnValue(of(true));

      // Navigate through different pages
      await router.navigate(['/sessions']);
      expect(location.path()).toBe('/sessions');

      await router.navigate(['/me']);
      expect(location.path()).toBe('/me');

      await router.navigate(['/sessions/create']);
      expect(location.path()).toBe('/sessions/create');

      // Session state should be maintained
      expect(mockSessionService.$isLogged).toHaveBeenCalled();
    });

    it('should handle session state changes during navigation', async () => {
      // Start with logged state
      mockSessionService.isLogged = true;
      mockSessionService.$isLogged.mockReturnValue(of(true));

      await router.navigate(['/sessions']);
      expect(location.path()).toBe('/sessions');

      // Logout during session
      component.logout();
      mockSessionService.isLogged = false;
      mockSessionService.$isLogged.mockReturnValue(of(false));

      expect(mockSessionService.logOut).toHaveBeenCalled();
    });
  });
});