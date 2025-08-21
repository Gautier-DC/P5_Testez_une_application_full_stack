import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
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
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';

import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: AuthService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            register: jest.fn()
          }
        }
      ],
      imports: [
        BrowserAnimationsModule,
        HttpClientModule,
        RouterTestingModule,
        ReactiveFormsModule,  
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatButtonModule
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
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
    expect(router).toBeTruthy();
  });

  it('should initialize form with empty values and validators', () => {
    expect(component.form).toBeTruthy();
    expect(component.form.get('email')?.value).toBe('');
    expect(component.form.get('firstName')?.value).toBe('');
    expect(component.form.get('lastName')?.value).toBe('');
    expect(component.form.get('password')?.value).toBe('');
    
    // Verify validators are set
    expect(component.form.get('email')?.hasError('required')).toBeTruthy();
    expect(component.form.get('firstName')?.hasError('required')).toBeTruthy();
    expect(component.form.get('lastName')?.hasError('required')).toBeTruthy();
    expect(component.form.get('password')?.hasError('required')).toBeTruthy();
  });

  it('should initialize with correct default values', () => {
    expect(component.onError).toBeFalsy();
  });

  // ==================== FORM VALIDATION TESTS ====================

  describe('Form Validation', () => {
    it('should have invalid form when email is empty', () => {
      component.form.controls['email'].setValue('');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['email'].errors?.['required']).toBeTruthy();
    });

    it('should have invalid form when email is invalid format', () => {
      component.form.controls['email'].setValue('invalid-email');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['email'].errors?.['email']).toBeTruthy();
    });

    it('should have invalid form when firstName is empty', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['firstName'].errors?.['required']).toBeTruthy();
    });

    it('should have invalid form when lastName is empty', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('');
      component.form.controls['password'].setValue('password123');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['lastName'].errors?.['required']).toBeTruthy();
    });

    it('should have invalid form when password is empty', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('');
      
      expect(component.form.valid).toBeFalsy();
      expect(component.form.controls['password'].errors?.['required']).toBeTruthy();
    });

    it('should have valid form when all fields are filled correctly', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      expect(component.form.valid).toBeTruthy();
    });

    it('should disable submit button when form is invalid', () => {
      component.form.controls['email'].setValue('');
      component.form.controls['firstName'].setValue('');
      component.form.controls['lastName'].setValue('');
      component.form.controls['password'].setValue('');
      fixture.detectChanges();

      const submitButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(submitButton.nativeElement.disabled).toBeTruthy();
    });

    it('should enable submit button when form is valid', () => {
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      fixture.detectChanges();

      const submitButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(submitButton.nativeElement.disabled).toBeFalsy();
    });
  });

  // ==================== FIELD LENGTH VALIDATION TESTS ====================

  describe('Field Length Validation', () => {
    it('should validate firstName min length', () => {
      component.form.controls['firstName'].setValue('Jo'); // Less than 3 chars
      expect(component.form.controls['firstName'].errors?.['minlength']).toBeTruthy();
    });

    it('should validate firstName max length', () => {
      component.form.controls['firstName'].setValue('A'.repeat(21)); // More than 20 chars
      expect(component.form.controls['firstName'].errors?.['maxlength']).toBeTruthy();
    });

    it('should validate lastName min length', () => {
      component.form.controls['lastName'].setValue('Do'); // Less than 3 chars
      expect(component.form.controls['lastName'].errors?.['minlength']).toBeTruthy();
    });

    it('should validate lastName max length', () => {
      component.form.controls['lastName'].setValue('A'.repeat(21)); // More than 20 chars
      expect(component.form.controls['lastName'].errors?.['maxlength']).toBeTruthy();
    });

    it('should validate password min length', () => {
      component.form.controls['password'].setValue('12'); // Less than 3 chars
      expect(component.form.controls['password'].errors?.['minlength']).toBeTruthy();
    });

    it('should validate password max length', () => {
      component.form.controls['password'].setValue('A'.repeat(41)); // More than 40 chars
      expect(component.form.controls['password'].errors?.['maxlength']).toBeTruthy();
    });

    it('should accept valid field lengths', () => {
      component.form.controls['firstName'].setValue('John'); // Between 3 and 20
      component.form.controls['lastName'].setValue('Doe'); // Between 3 and 20
      component.form.controls['password'].setValue('password123'); // Between 3 and 40
      
      expect(component.form.controls['firstName'].errors?.['minlength']).toBeFalsy();
      expect(component.form.controls['firstName'].errors?.['maxlength']).toBeFalsy();
      expect(component.form.controls['lastName'].errors?.['minlength']).toBeFalsy();
      expect(component.form.controls['lastName'].errors?.['maxlength']).toBeFalsy();
      expect(component.form.controls['password'].errors?.['minlength']).toBeFalsy();
      expect(component.form.controls['password'].errors?.['maxlength']).toBeFalsy();
    });
  });

  // ==================== REGISTRATION SUCCESS TESTS ====================

  describe('Registration Success', () => {
    it('should call authService.register with correct data', () => {
      const registerRequest = {
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      };

      jest.spyOn(authService, 'register').mockReturnValue(of(undefined));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue(registerRequest.email);
      component.form.controls['firstName'].setValue(registerRequest.firstName);
      component.form.controls['lastName'].setValue(registerRequest.lastName);
      component.form.controls['password'].setValue(registerRequest.password);
      
      component.submit();

      expect(authService.register).toHaveBeenCalledWith(registerRequest);
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should navigate to login after successful registration', () => {
      jest.spyOn(authService, 'register').mockReturnValue(of(undefined));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      component.submit();

      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should not show error when registration is successful', () => {
      jest.spyOn(authService, 'register').mockReturnValue(of(undefined));

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      component.submit();

      expect(component.onError).toBeFalsy();
    });
  });

  // ==================== REGISTRATION ERROR TESTS ====================

  describe('Registration Error Handling', () => {
    it('should set onError to true when registration fails', () => {
      jest.spyOn(authService, 'register').mockReturnValue(throwError(() => ({ error: 'Registration failed' })));

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      component.submit();

      expect(component.onError).toBeTruthy();
    });

    it('should display error message when registration fails', () => {
      jest.spyOn(authService, 'register').mockReturnValue(throwError(() => ({ error: 'Registration failed' })));

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
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

    it('should not navigate to login when registration fails', () => {
      jest.spyOn(authService, 'register').mockReturnValue(throwError(() => ({ error: 'Registration failed' })));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      component.submit();

      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  // ==================== INTEGRATION SCENARIOS ====================

  describe('Integration Scenarios', () => {
    it('should handle multiple registration attempts', () => {
      // First failed attempt
      jest.spyOn(authService, 'register').mockReturnValue(throwError(() => ({ error: 'Email already exists' })));

      component.form.controls['email'].setValue('existing@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      component.submit();
      expect(component.onError).toBeTruthy();

      // Second successful attempt
      jest.spyOn(authService, 'register').mockReturnValue(of(undefined));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue('new@example.com');
      component.submit();

      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should call submit when form is submitted with valid data', () => {
      jest.spyOn(component, 'submit');
      
      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      fixture.detectChanges();

      const form = fixture.debugElement.query(By.css('form'));
      form.nativeElement.dispatchEvent(new Event('submit'));

      expect(component.submit).toHaveBeenCalled();
    });
  });

  // ==================== ERROR HANDLING EDGE CASES ====================

  describe('Error Handling Edge Cases', () => {
    it('should handle service exceptions gracefully', () => {
      jest.spyOn(authService, 'register').mockReturnValue(throwError(() => new Error('Network error')));

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      expect(() => component.submit()).not.toThrow();
      expect(component.onError).toBeTruthy();
    });

    it('should reset error state on successful registration after failed attempt', () => {
      // First, set error state
      component.onError = true;

      // Then successful registration
      jest.spyOn(authService, 'register').mockReturnValue(of(undefined));
      jest.spyOn(router, 'navigate');

      component.form.controls['email'].setValue('test@example.com');
      component.form.controls['firstName'].setValue('John');
      component.form.controls['lastName'].setValue('Doe');
      component.form.controls['password'].setValue('password123');
      
      component.submit();

      expect(component.onError).toBeFalsy(); // onError should be reset on new attempt
    });
  });
});
