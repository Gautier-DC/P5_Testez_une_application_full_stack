import { HttpClientModule } from '@angular/common/http';
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
import { of, throwError } from 'rxjs';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';
import { By } from '@angular/platform-browser';

import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: AuthService;
  let sessionService: SessionService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            login: jest.fn()
          }
        },
        {
          provide: SessionService,
          useValue: {
            logIn: jest.fn()
          }
        }
      ],
      imports: [
        RouterTestingModule,
        BrowserAnimationsModule,
        HttpClientModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        ReactiveFormsModule]
    })
      .compileComponents();
      
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    sessionService = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should inject all required dependencies', () => {
    expect(component).toBeTruthy();
    expect(fixture).toBeTruthy();
    expect(authService).toBeTruthy();
    expect(sessionService).toBeTruthy();
    expect(router).toBeTruthy();
  });

  it('should initialize form with empty values and validators', () => {
    expect(component.form).toBeTruthy();
    expect(component.form.get('email')?.value).toBe('');
    expect(component.form.get('password')?.value).toBe('');
    
    // Verify validators are set
    expect(component.form.get('email')?.hasError('required')).toBeTruthy();
    expect(component.form.get('password')?.hasError('required')).toBeTruthy();
  });

  it('should initialize with correct default values', () => {
    expect(component.hide).toBeTruthy();
    expect(component.onError).toBeFalsy();
  });

  // ==================== FORM VALIDATION TESTS ====================

  describe('Form Validation', () => {
    it('should have invalid form when email is empty', () => {
      component.form.controls['email'].setValue('');
      component.form.controls['password'].setValue('validPassword123');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['email'].errors?.['required']).toBeTruthy();
    });

    it('should have invalid form when email is invalid format', () => {
      component.form.controls['email'].setValue('invalid-email');
      component.form.controls['password'].setValue('validPassword123');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['email'].errors?.['email']).toBeTruthy();
    });

    it('should have invalid form when password is empty', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['password'].errors?.['required']).toBeTruthy();
    });

    it('should have valid form when both fields are filled correctly', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('validPassword123');
      
      expect(component.form.valid).toBeTruthy();
    });

    it('should disable submit button when form is invalid', () => {
      component.form.controls['email'].setValue('');
      component.form.controls['password'].setValue('');
      fixture.detectChanges();

      const submitButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(submitButton.nativeElement.disabled).toBeTruthy();
    });

    it('should enable submit button when form is valid', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('validPassword123');
      fixture.detectChanges();

      const submitButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(submitButton.nativeElement.disabled).toBeFalsy();
    });
  });

  // ==================== LOGIN SUCCESS TESTS ====================

  describe('Login Success', () => {
    it('should call authService.login with correct credentials', () => {
      const loginRequest = {
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

      jest.spyOn(authService, 'login').mockReturnValue(of(mockResponse));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue(loginRequest.email);
      component.form.controls['password'].setValue(loginRequest.password);
      
      component.submit();

      expect(authService.login).toHaveBeenCalledWith(loginRequest);
      expect(sessionService.logIn).toHaveBeenCalledWith(mockResponse);
      expect(router.navigate).toHaveBeenCalledWith(['/sessions']);
    });

    it('should navigate to sessions after successful login', () => {
      const mockResponse: SessionInformation = {
        token: 'fake-jwt-token',
        type: 'Bearer',
        id: 1,
        username: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        admin: false
      };

      jest.spyOn(authService, 'login').mockReturnValue(of(mockResponse));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('password123');
      
      component.submit();

      expect(router.navigate).toHaveBeenCalledWith(['/sessions']);
    });
  });

  // ==================== LOGIN ERROR TESTS ====================

  describe('Login Error Handling', () => {
    it('should set onError to true when login fails with wrong credentials', () => {
      jest.spyOn(authService, 'login').mockReturnValue(throwError(() => ({ error: 'Unauthorized' })));

      component.form.controls['email'].setValue('wrong@example.com');
      component.form.controls['password'].setValue('wrongpassword');
      
      component.submit();

      expect(component.onError).toBeTruthy();
    });

    it('should display error message when login fails', () => {
      jest.spyOn(authService, 'login').mockReturnValue(throwError(() => ({ error: 'Unauthorized' })));

      component.form.controls['email'].setValue('wrong@example.com');
      component.form.controls['password'].setValue('wrongpassword');
      
      component.submit();
      fixture.detectChanges();

      const errorMessage = fixture.debugElement.query(By.css('.error'));
      expect(errorMessage).toBeTruthy();
      expect(errorMessage.nativeElement.textContent).toContain('An error occurred');
    });

    it('should not display error message initially', () => {
      const errorMessage = fixture.debugElement.query(By.css('.error'));
      expect(errorMessage).toBeFalsy();
    });

    it('should reset onError to false on new login attempt', () => {
      // First, simulate a failed login
      component.onError = true;
      fixture.detectChanges();

      // Then simulate a successful login
      const mockResponse: SessionInformation = {
        token: 'fake-jwt-token',
        type: 'Bearer',
        id: 1,
        username: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        admin: false
      };

      jest.spyOn(authService, 'login').mockReturnValue(of(mockResponse));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('password123');
      
      component.submit();

      expect(component.onError).toBeFalsy();
    });
  });

  // ==================== PASSWORD VISIBILITY TESTS ====================

  describe('Password Visibility', () => {
    it('should toggle password visibility', () => {
      expect(component.hide).toBeTruthy();
      
      const toggleButton = fixture.debugElement.query(By.css('button[matSuffix]'));
      toggleButton.nativeElement.click();
      
      expect(component.hide).toBeFalsy();
    });

    it('should display correct icon based on visibility state', () => {
      fixture.detectChanges();
      let icon = fixture.debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.textContent).toBe('visibility_off');

      component.hide = false;
      fixture.detectChanges();
      icon = fixture.debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.textContent).toBe('visibility');
    });

    it('should change input type based on visibility state', () => {
      fixture.detectChanges();
      let passwordInput = fixture.debugElement.query(By.css('input[formControlName="password"]'));
      expect(passwordInput.nativeElement.type).toBe('password');

      component.hide = false;
      fixture.detectChanges();
      passwordInput = fixture.debugElement.query(By.css('input[formControlName="password"]'));
      expect(passwordInput.nativeElement.type).toBe('text');
    });
  });

  // ==================== INTEGRATION TESTS ====================

  describe('Form Integration', () => {
    it('should call submit when form is submitted with valid data', () => {
      jest.spyOn(component, 'submit');
      
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['password'].setValue('password123');
      fixture.detectChanges();

      const form = fixture.debugElement.query(By.css('form'));
      form.nativeElement.dispatchEvent(new Event('submit'));

      expect(component.submit).toHaveBeenCalled();
    });
  });
});
