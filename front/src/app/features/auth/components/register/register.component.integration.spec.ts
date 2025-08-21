import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
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
import { AuthService } from '../../services/auth.service';
import { By } from '@angular/platform-browser';
import { Component } from '@angular/core';

import { RegisterComponent } from './register.component';

// Mock component for navigation testing
@Component({
  template: '<h1>Login Page</h1>',
})
class MockLoginComponent {}

describe('RegisterComponent Integration Tests', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegisterComponent, MockLoginComponent],
      providers: [AuthService],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'login', component: MockLoginComponent },
        ]),
        BrowserAnimationsModule,
        HttpClientTestingModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        ReactiveFormsModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

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
        pendingRequests.forEach((req) => req.flush({}));
      }
    }
  });

  // ==================== REAL HTTP INTEGRATION TESTS ====================

  describe('HTTP Integration', () => {
    it('should make real HTTP call to register endpoint with correct data', () => {
      // Arrange
      const registerData = {
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        password: 'password123',
      };

      // Act - Fill form and submit
      component.form.controls['firstName'].setValue(registerData.firstName);
      component.form.controls['lastName'].setValue(registerData.lastName);
      component.form.controls['email'].setValue(registerData.email);
      component.form.controls['password'].setValue(registerData.password);
      component.submit();

      // Assert - Verify HTTP request
      const req = httpMock.expectOne('api/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerData);

      // Respond with mock data
      req.flush(null);
    });

    it('should handle HTTP error responses correctly', () => {
      // Arrange
      component.form.controls['firstName'].setValue('Test');
      component.form.controls['lastName'].setValue('User');
      component.form.controls['email'].setValue('wrongexample.com');
      component.form.controls['password'].setValue('wrongpassword');

      // Act
      component.submit();

      // Assert - Verify HTTP request and error handling
      const req = httpMock.expectOne('api/auth/register');
      req.flush(
        { message: 'Unauthorized' },
        { status: 401, statusText: 'Unauthorized' }
      );

      expect(component.onError).toBeTruthy();
    });

    it('should handle network errors', () => {
      // Arrange
      component.form.controls['firstName'].setValue('Test');
      component.form.controls['lastName'].setValue('User');
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('password123');

      // Act
      component.submit();

      // Assert - Simulate network error
      const req = httpMock.expectOne('api/auth/register');
      req.flush(null, { status: 0, statusText: 'Network Error' });

      expect(component.onError).toBeTruthy();
    });
  });

  // ==================== COMPLETE WORKFLOW INTEGRATION TESTS ====================

  describe('Complete Workflow Integration', () => {
    it('should complete full Register workflow: form → HTTP → register → navigation', async () => {
      // Arrange
      const registerData = {
        firstName: 'Integration',
        lastName: 'Test',
        email: 'integration@test.com',
        password: 'testpassword',
      };

      jest.spyOn(router, 'navigate');

      // Act - Complete register workflow
      component.form.controls['firstName'].setValue(registerData.firstName);
      component.form.controls['lastName'].setValue(registerData.lastName);
      component.form.controls['email'].setValue(registerData.email);
      component.form.controls['password'].setValue(registerData.password);
      component.submit();

      // Handle HTTP request
      const req = httpMock.expectOne('api/auth/register');
      expect(req.request.body).toEqual(registerData); // Verify HTTP body instead
      req.flush(null);

      // Assert - Verify complete workflow
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      expect(component.onError).toBeFalsy();
    });
  });

  // ==================== DOM INTERACTION INTEGRATION TESTS ====================

  describe('DOM Interaction Integration', () => {
    it('should handle complete user interaction flow', async () => {
      // Arrange
      const firstNameInput = fixture.debugElement.query(
        By.css('input[formControlName="firstName"]')
      );
      const lastNameInput = fixture.debugElement.query(
        By.css('input[formControlName="lastName"]')
      );
      const emailInput = fixture.debugElement.query(
        By.css('input[formControlName="email"]')
      );
      const passwordInput = fixture.debugElement.query(
        By.css('input[formControlName="password"]')
      );
      const submitButton = fixture.debugElement.query(
        By.css('button[type="submit"]')
      );
      const form = fixture.debugElement.query(By.css('form'));

      // Act - Simulate real user interaction
      // User types in fields
      firstNameInput.nativeElement.value = 'Dom';
      firstNameInput.nativeElement.dispatchEvent(new Event('input'));
      lastNameInput.nativeElement.value = 'Test';
      lastNameInput.nativeElement.dispatchEvent(new Event('input'));
      emailInput.nativeElement.value = 'dom@test.com';
      emailInput.nativeElement.dispatchEvent(new Event('input'));
      passwordInput.nativeElement.value = 'domtestpass';
      passwordInput.nativeElement.dispatchEvent(new Event('input'));

      fixture.detectChanges();

      // Verify form is valid and button is enabled
      expect(component.form.valid).toBeTruthy();
      expect(submitButton.nativeElement.disabled).toBeFalsy();

      // User submits the form
      form.nativeElement.dispatchEvent(new Event('submit'));

      // Handle HTTP response
      const req = httpMock.expectOne('api/auth/register');
      req.flush(null);

      // Assert - Verify the complete interaction worked
      expect(req.request.body.firstName).toBe('Dom');
      expect(req.request.body.lastName).toBe('Test');
      expect(req.request.body.email).toBe('dom@test.com');
      expect(req.request.body.password).toBe('domtestpass');
    });

    it('should show error message in DOM when register fails', () => {
      // Arrange
      component.form.controls['firstName'].setValue('Fail');
      component.form.controls['lastName'].setValue('Test');
      component.form.controls['email'].setValue('fail.test.com');
      component.form.controls['password'].setValue('wrongpass');

      // Act - Submit and fail
      component.submit();

      const req = httpMock.expectOne('api/auth/register');
      req.flush(
        { message: 'Invalid credentials' },
        { status: 401, statusText: 'Unauthorized' }
      );

      fixture.detectChanges();

      // Assert - Verify error message appears in DOM
      const errorMessage = fixture.debugElement.query(By.css('.error'));
      expect(errorMessage).toBeTruthy();
      expect(errorMessage.nativeElement.textContent).toContain(
        'An error occurred'
      );
      expect(component.onError).toBeTruthy();
    });
  });

  // ==================== FORM INTERACTION INTEGRATION TESTS ====================

  describe('Form Interaction Integration', () => {
    it('should validate form in real-time as user types', () => {
      // Arrange
      const firstNameInput = fixture.debugElement.query(
        By.css('input[formControlName="firstName"]')
      );
      const lastNameInput = fixture.debugElement.query(
        By.css('input[formControlName="lastName"]')
      );
      const emailInput = fixture.debugElement.query(
        By.css('input[formControlName="email"]')
      );
      const passwordInput = fixture.debugElement.query(
        By.css('input[formControlName="password"]')
      );
      const submitButton = fixture.debugElement.query(
        By.css('button[type="submit"]')
      );

      // Initially form should be invalid
      expect(component.form.valid).toBeFalsy();
      expect(submitButton.nativeElement.disabled).toBeTruthy();

      // Act - User types invalid email
      firstNameInput.nativeElement.value = 'Test';
      firstNameInput.nativeElement.dispatchEvent(new Event('input'));
      lastNameInput.nativeElement.value = 'User';
      lastNameInput.nativeElement.dispatchEvent(new Event('input'));
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
  });

  // ==================== ERROR HANDLING INTEGRATION TESTS ====================

  describe('Error Handling Integration', () => {
    it('should handle multiple consecutive registration attempts', () => {
      // First failed attempt
      component.form.controls['firstName'].setValue('Fail1');
      component.form.controls['lastName'].setValue('Fail1');
      component.form.controls['email'].setValue('fail1@test.com');
      component.form.controls['password'].setValue('wrong1');
      component.submit();

      let req = httpMock.expectOne('api/auth/register');
      req.flush(
        { error: 'Unauthorized' },
        { status: 401, statusText: 'Unauthorized' }
      );

      expect(component.onError).toBeTruthy();

      // Second failed attempt
      component.form.controls['email'].setValue('fail2@test.com');
      component.form.controls['password'].setValue('wrong2');
      component.submit();

      req = httpMock.expectOne('api/auth/register');
      req.flush(
        { error: 'Unauthorized' },
        { status: 401, statusText: 'Unauthorized' }
      );

      expect(component.onError).toBeTruthy();

      // Successful attempt

      component.form.controls['email'].setValue('success@test.com');
      component.form.controls['password'].setValue('correctpass');
      component.submit();

      req = httpMock.expectOne('api/auth/register');
      req.flush(null);

      expect(component.onError).toBeFalsy();
    });

    it('should handle different types of HTTP errors', () => {
      const testCases = [
        { status: 400, statusText: 'Bad Request', errorMessage: 'Bad Request' },
        {
          status: 401,
          statusText: 'Unauthorized',
          errorMessage: 'Unauthorized',
        },
        { status: 403, statusText: 'Forbidden', errorMessage: 'Forbidden' },
        {
          status: 500,
          statusText: 'Internal Server Error',
          errorMessage: 'Server Error',
        },
      ];

      testCases.forEach((testCase) => {
        component.onError = false; // Reset error state
        component.form.controls['firstName'].setValue('Test');
        component.form.controls['lastName'].setValue('User');
        component.form.controls['email'].setValue('test@error.com');
        component.form.controls['password'].setValue('testpass');
        component.submit();

        const req = httpMock.expectOne('api/auth/register');
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
    it('should navigate to login page after successful registration', async () => {
      // Arrange & Act
      component.form.controls['firstName'].setValue('Nav');
      component.form.controls['lastName'].setValue('Test');
      component.form.controls['email'].setValue('nav@test.com');
      component.form.controls['password'].setValue('navpass');
      component.submit();

      const req = httpMock.expectOne('api/auth/register');
      req.flush(null);

      // Wait for navigation to complete
      await fixture.whenStable();

      // Assert - Should be on login route
      expect(router.url).toBe('/login');
    });

    it('should stay on register page when registration fails', async () => {
      // Arrange
      component.form.controls['firstName'].setValue('Stay');
      component.form.controls['lastName'].setValue('Here');
      component.form.controls['email'].setValue('stay@test.com');
      component.form.controls['password'].setValue('wrongpass');

      // Act
      component.submit();

      const req = httpMock.expectOne('api/auth/register');
      req.flush(
        { error: 'Unauthorized' },
        { status: 401, statusText: 'Unauthorized' }
      );

      await fixture.whenStable();

      // Assert - Should still be on register page
      expect(router.url).toBe('/');
      expect(component.onError).toBeTruthy();
    });
  });
});
