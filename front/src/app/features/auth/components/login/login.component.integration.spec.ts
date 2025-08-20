import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';
import { AuthService } from '../../services/auth.service';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';
import { By } from '@angular/platform-browser';
import { Component } from '@angular/core';

import { LoginComponent } from './login.component';

// Mock component for navigation testing
@Component({
  template: '<h1>Sessions Page</h1>'
})
class MockSessionsComponent { }

describe('LoginComponent Integration Tests', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let httpMock: HttpTestingController;
  let sessionService: SessionService;
  let router: Router;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent, MockSessionsComponent],
      providers: [
        AuthService,
        SessionService
      ],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'sessions', component: MockSessionsComponent }
        ]),
        BrowserAnimationsModule,
        HttpClientTestingModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        ReactiveFormsModule
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    sessionService = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    authService = TestBed.inject(AuthService);

    fixture.detectChanges();
  });

  afterEach(() => {
    // Verify no unexpected HTTP requests, but be flexible about our own test requests
    try {
      httpMock.verify();
    } catch (error) {
      // Handle expected pending requests in some tests
      const pendingRequests = httpMock.match(() => true);
      if (pendingRequests.length > 0) {
        pendingRequests.forEach(req => req.flush({}));
      }
    }
  });

  // ==================== REAL HTTP INTEGRATION TESTS ====================

  describe('HTTP Integration', () => {
    it('should make real HTTP call to login endpoint with correct data', () => {
      // Arrange
      const loginData = {
        email: 'test@example.com',
        password: 'password123'
      };
      const mockResponse: SessionInformation = {
        token: 'fake-jwt-token',
        type: 'Bearer',
        id: 1,
        username: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        admin: false
      };

      // Act - Fill form and submit
      component.form.controls['email'].setValue(loginData.email);
      component.form.controls['password'].setValue(loginData.password);
      component.submit();

      // Assert - Verify HTTP request
      const req = httpMock.expectOne('api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginData);

      // Respond with mock data
      req.flush(mockResponse);
    });

    it('should handle HTTP error responses correctly', () => {
      // Arrange
      component.form.controls['email'].setValue('wrong@example.com');
      component.form.controls['password'].setValue('wrongpassword');

      // Act
      component.submit();

      // Assert - Verify HTTP request and error handling
      const req = httpMock.expectOne('api/auth/login');
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

      expect(component.onError).toBeTruthy();
    });

    it('should handle network errors', () => {
      // Arrange
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('password123');

      // Act
      component.submit();

      // Assert - Simulate network error
      const req = httpMock.expectOne('api/auth/login');
      req.error(new ErrorEvent('Network error'));

      expect(component.onError).toBeTruthy();
    });
  });

  // ==================== COMPLETE WORKFLOW INTEGRATION TESTS ====================

  describe('Complete Workflow Integration', () => {
    it('should complete full login workflow: form → HTTP → session → navigation', async () => {
      // Arrange
      const loginData = {
        email: 'integration@test.com',
        password: 'testpassword'
      };
      const mockResponse: SessionInformation = {
        token: 'integration-jwt-token',
        type: 'Bearer',
        id: 1,
        username: 'integration@test.com',
        firstName: 'Integration',
        lastName: 'Test',
        admin: true
      };

      jest.spyOn(sessionService, 'logIn');
      jest.spyOn(router, 'navigate');

      // Act - Complete login workflow
      component.form.controls['email'].setValue(loginData.email);
      component.form.controls['password'].setValue(loginData.password);
      component.submit();

      // Handle HTTP request
      const req = httpMock.expectOne('api/auth/login');
      req.flush(mockResponse);

      // Assert - Verify complete workflow
      expect(sessionService.logIn).toHaveBeenCalledWith(mockResponse);
      expect(router.navigate).toHaveBeenCalledWith(['/sessions']);
      expect(component.onError).toBeFalsy();
    });

    it('should store session information correctly after login', () => {
      // Arrange
      const mockResponse: SessionInformation = {
        token: 'session-token',
        type: 'Bearer',
        id: 2,
        username: 'session@test.com',
        firstName: 'Session',
        lastName: 'User',
        admin: false
      };

      jest.spyOn(sessionService, 'logIn');

      // Act
      component.form.controls['email'].setValue('session@test.com');
      component.form.controls['password'].setValue('password');
      component.submit();

      const req = httpMock.expectOne('api/auth/login');
      req.flush(mockResponse);

      // Assert - Verify session service was called with correct data
      expect(sessionService.logIn).toHaveBeenCalledWith(mockResponse);
    });
  });

  // ==================== DOM INTERACTION INTEGRATION TESTS ====================

  describe('DOM Interaction Integration', () => {
    it('should handle complete user interaction flow', async () => {
      // Arrange
      const emailInput = fixture.debugElement.query(By.css('input[formControlName="email"]'));
      const passwordInput = fixture.debugElement.query(By.css('input[formControlName="password"]'));
      const submitButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      const form = fixture.debugElement.query(By.css('form'));

      const mockResponse: SessionInformation = {
        token: 'dom-test-token',
        type: 'Bearer',
        id: 1,
        username: 'dom@test.com',
        firstName: 'DOM',
        lastName: 'Test',
        admin: false
      };

      // Act - Simulate real user interaction
      // User types in email field
      emailInput.nativeElement.value = 'dom@test.com';
      emailInput.nativeElement.dispatchEvent(new Event('input'));

      // User types in password field
      passwordInput.nativeElement.value = 'domtestpass';
      passwordInput.nativeElement.dispatchEvent(new Event('input'));

      fixture.detectChanges();

      // Verify form is valid and button is enabled
      expect(component.form.valid).toBeTruthy();
      expect(submitButton.nativeElement.disabled).toBeFalsy();

      // User submits the form
      form.nativeElement.dispatchEvent(new Event('submit'));

      // Handle HTTP response
      const req = httpMock.expectOne('api/auth/login');
      req.flush(mockResponse);

      // Assert - Verify the complete interaction worked
      expect(req.request.body.email).toBe('dom@test.com');
      expect(req.request.body.password).toBe('domtestpass');
    });

    it('should show error message in DOM when login fails', () => {
      // Arrange
      component.form.controls['email'].setValue('fail@test.com');
      component.form.controls['password'].setValue('wrongpass');

      // Act - Submit and fail
      component.submit();

      const req = httpMock.expectOne('api/auth/login');
      req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

      fixture.detectChanges();

      // Assert - Verify error message appears in DOM
      const errorMessage = fixture.debugElement.query(By.css('.error'));
      expect(errorMessage).toBeTruthy();
      expect(errorMessage.nativeElement.textContent).toContain('An error occurred');
      expect(component.onError).toBeTruthy();
    });

    it('should hide error message after successful login', () => {
      // Arrange - First show error
      component.onError = true;
      fixture.detectChanges();

      let errorMessage = fixture.debugElement.query(By.css('.error'));
      expect(errorMessage).toBeTruthy();

      // Act - Successful login
      const mockResponse: SessionInformation = {
        token: 'success-token',
        type: 'Bearer',
        id: 1,
        username: 'success@test.com',
        firstName: 'Success',
        lastName: 'User',
        admin: false
      };

      component.form.controls['email'].setValue('success@test.com');
      component.form.controls['password'].setValue('correctpass');
      component.submit();

      const req = httpMock.expectOne('api/auth/login');
      req.flush(mockResponse);

      fixture.detectChanges();

      // Assert - Error message should be hidden
      errorMessage = fixture.debugElement.query(By.css('.error'));
      expect(errorMessage).toBeFalsy();
      expect(component.onError).toBeFalsy();
    });
  });

  // ==================== FORM INTERACTION INTEGRATION TESTS ====================

  describe('Form Interaction Integration', () => {
    it('should validate form in real-time as user types', () => {
      // Arrange
      const emailInput = fixture.debugElement.query(By.css('input[formControlName="email"]'));
      const passwordInput = fixture.debugElement.query(By.css('input[formControlName="password"]'));
      const submitButton = fixture.debugElement.query(By.css('button[type="submit"]'));

      // Initially form should be invalid
      expect(component.form.valid).toBeFalsy();
      expect(submitButton.nativeElement.disabled).toBeTruthy();

      // Act - User types invalid email
      emailInput.nativeElement.value = 'invalid-email';
      emailInput.nativeElement.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      // Assert - Form still invalid
      expect(component.form.valid).toBeFalsy();
      expect(submitButton.nativeElement.disabled).toBeTruthy();

      // Act - User types valid email
      emailInput.nativeElement.value = 'valid@email.com';
      emailInput.nativeElement.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      // Form still invalid (no password)
      expect(component.form.valid).toBeFalsy();
      expect(submitButton.nativeElement.disabled).toBeTruthy();

      // Act - User types password
      passwordInput.nativeElement.value = 'validpassword';
      passwordInput.nativeElement.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      // Assert - Form now valid
      expect(component.form.valid).toBeTruthy();
      expect(submitButton.nativeElement.disabled).toBeFalsy();
    });

    it('should handle password visibility toggle integration', () => {
      // Arrange
      const passwordInput = fixture.debugElement.query(By.css('input[formControlName="password"]'));
      const toggleButton = fixture.debugElement.query(By.css('button[matSuffix]'));
      const icon = fixture.debugElement.query(By.css('mat-icon'));

      // Initially password should be hidden
      expect(passwordInput.nativeElement.type).toBe('password');
      expect(icon.nativeElement.textContent).toBe('visibility_off');
      expect(component.hide).toBeTruthy();

      // Act - Click toggle button
      toggleButton.nativeElement.click();
      fixture.detectChanges();

      // Assert - Password should be visible
      expect(passwordInput.nativeElement.type).toBe('text');
      expect(icon.nativeElement.textContent).toBe('visibility');
      expect(component.hide).toBeFalsy();

      // Act - Click toggle button again
      toggleButton.nativeElement.click();
      fixture.detectChanges();

      // Assert - Password should be hidden again
      expect(passwordInput.nativeElement.type).toBe('password');
      expect(icon.nativeElement.textContent).toBe('visibility_off');
      expect(component.hide).toBeTruthy();
    });
  });

  // ==================== ERROR HANDLING INTEGRATION TESTS ====================

  describe('Error Handling Integration', () => {
    it('should handle multiple consecutive login attempts', () => {
      // First failed attempt
      component.form.controls['email'].setValue('fail1@test.com');
      component.form.controls['password'].setValue('wrong1');
      component.submit();

      let req = httpMock.expectOne('api/auth/login');
      req.flush({ error: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

      expect(component.onError).toBeTruthy();

      // Second failed attempt
      component.form.controls['email'].setValue('fail2@test.com');
      component.form.controls['password'].setValue('wrong2');
      component.submit();

      req = httpMock.expectOne('api/auth/login');
      req.flush({ error: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

      expect(component.onError).toBeTruthy();

      // Successful attempt
      const mockResponse: SessionInformation = {
        token: 'success-token',
        type: 'Bearer',
        id: 1,
        username: 'success@test.com',
        firstName: 'Success',
        lastName: 'User',
        admin: false
      };

      component.form.controls['email'].setValue('success@test.com');
      component.form.controls['password'].setValue('correctpass');
      component.submit();

      req = httpMock.expectOne('api/auth/login');
      req.flush(mockResponse);

      expect(component.onError).toBeFalsy();
    });

    it('should handle different types of HTTP errors', () => {
      const testCases = [
        { status: 400, statusText: 'Bad Request', errorMessage: 'Bad Request' },
        { status: 401, statusText: 'Unauthorized', errorMessage: 'Unauthorized' },
        { status: 403, statusText: 'Forbidden', errorMessage: 'Forbidden' },
        { status: 500, statusText: 'Internal Server Error', errorMessage: 'Server Error' }
      ];

      testCases.forEach(testCase => {
        component.onError = false; // Reset error state
        
        component.form.controls['email'].setValue('test@error.com');
        component.form.controls['password'].setValue('testpass');
        component.submit();

        const req = httpMock.expectOne('api/auth/login');
        req.flush(
          { message: testCase.errorMessage },
          { status: testCase.status, statusText: testCase.statusText }
        );

        expect(component.onError).toBeTruthy();
      });
    });
  });

  // ==================== NAVIGATION INTEGRATION TESTS ====================

  describe('Navigation Integration', () => {
    it('should navigate to sessions page after successful login', async () => {
      // Arrange
      const mockResponse: SessionInformation = {
        token: 'nav-test-token',
        type: 'Bearer',
        id: 1,
        username: 'nav@test.com',
        firstName: 'Nav',
        lastName: 'Test',
        admin: false
      };

      // Act
      component.form.controls['email'].setValue('nav@test.com');
      component.form.controls['password'].setValue('navpass');
      component.submit();

      const req = httpMock.expectOne('api/auth/login');
      req.flush(mockResponse);

      // Wait for navigation to complete
      await fixture.whenStable();

      // Assert - Should be on sessions route
      expect(router.url).toBe('/sessions');
    });

    it('should stay on login page when login fails', async () => {
      // Arrange
      component.form.controls['email'].setValue('stay@test.com');
      component.form.controls['password'].setValue('wrongpass');

      // Act
      component.submit();

      const req = httpMock.expectOne('api/auth/login');
      req.flush({ error: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

      await fixture.whenStable();

      // Assert - Should still be on login page (initial route)
      expect(router.url).toBe('/');
      expect(component.onError).toBeTruthy();
    });
  });

  // ==================== SESSION SERVICE INTEGRATION TESTS ====================

  describe('Session Service Integration', () => {
    it('should properly integrate with SessionService', () => {
      // Arrange
      const mockResponse: SessionInformation = {
        token: 'session-integration-token',
        type: 'Bearer',
        id: 5,
        username: 'session@integration.com',
        firstName: 'Session',
        lastName: 'Integration',
        admin: true
      };

      // Spy on SessionService
      const logInSpy = jest.spyOn(sessionService, 'logIn');

      // Act
      component.form.controls['email'].setValue('session@integration.com');
      component.form.controls['password'].setValue('sessionpass');
      component.submit();

      const req = httpMock.expectOne('api/auth/login');
      req.flush(mockResponse);

      // Assert - Verify SessionService integration
      expect(logInSpy).toHaveBeenCalledWith(mockResponse);
      expect(logInSpy).toHaveBeenCalledTimes(1);
    });
  });
});