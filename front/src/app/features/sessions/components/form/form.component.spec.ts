import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { expect } from '@jest/globals';
import { of } from 'rxjs';
import { SessionService } from 'src/app/services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { TeacherService } from '../../../../services/teacher.service';
import { Session } from '../../interfaces/session.interface';
import { Teacher } from '../../../../interfaces/teacher.interface';

import { FormComponent } from './form.component';

// Mock component for navigation testing
@Component({
  template: '<h1>Sessions Page</h1>'
})
class MockSessionsComponent {}

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  let router: Router;

  const mockTeachers: Teacher[] = [
    {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      createdAt: new Date(),
      updatedAt: new Date()
    },
    {
      id: 2,
      firstName: 'Jane',
      lastName: 'Smith',
      createdAt: new Date(),
      updatedAt: new Date()
    }
  ];

  const mockSession: Session = {
    id: 1,
    name: 'Existing Session',
    description: 'Existing session description',
    date: new Date('2023-12-01'),
    teacher_id: 1,
    users: [1, 2],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const createMockSessionService = (isAdmin: boolean) => ({
    sessionInformation: {
      admin: isAdmin,
      id: 1,
      username: 'test@test.com',
      firstName: 'Test',
      lastName: 'User'
    }
  });

  const mockSessionApiService = {
    create: jest.fn().mockReturnValue(of(mockSession)),
    update: jest.fn().mockReturnValue(of(mockSession)),
    detail: jest.fn().mockReturnValue(of(mockSession))
  };

  const mockTeacherService = {
    all: jest.fn().mockReturnValue(of(mockTeachers))
  };

  const mockMatSnackBar = {
    open: jest.fn()
  };

  const createMockActivatedRoute = (sessionId?: string) => ({
    snapshot: {
      paramMap: {
        get: jest.fn().mockReturnValue(sessionId || null)
      }
    }
  });

  const configureTestBed = async (isAdmin: boolean = true, isUpdateMode: boolean = false, sessionId?: string) => {
    // Reset all mocks
    jest.clearAllMocks();
    mockSessionApiService.create.mockReturnValue(of(mockSession));
    mockSessionApiService.update.mockReturnValue(of(mockSession));
    mockSessionApiService.detail.mockReturnValue(of(mockSession));
    mockTeacherService.all.mockReturnValue(of(mockTeachers));

    // Mock router.url for update detection
    const mockRouter = {
      url: isUpdateMode ? '/sessions/update/1' : '/sessions/create',
      navigate: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'sessions', component: MockSessionsComponent }
        ]),
        HttpClientModule,
        CommonModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        ReactiveFormsModule,
        MatSnackBarModule,
        MatSelectModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: SessionService, useValue: createMockSessionService(isAdmin) },
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: TeacherService, useValue: mockTeacherService },
        { provide: MatSnackBar, useValue: mockMatSnackBar },
        { provide: ActivatedRoute, useValue: createMockActivatedRoute(sessionId) },
        { provide: Router, useValue: mockRouter }
      ],
      declarations: [FormComponent, MockSessionsComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  };

  it('should create', async () => {
    await configureTestBed();
    expect(component).toBeTruthy();
  });

  it('should redirect non-admin users to sessions page', async () => {
    await configureTestBed(false); // non-admin user
    
    expect(router.navigate).toHaveBeenCalledWith(['/sessions']);
  });

  it('should initialize form for create mode', async () => {
    await configureTestBed(true, false); // admin, create mode
    
    expect(component.onUpdate).toBeFalsy();
    expect(component.sessionForm).toBeDefined();
    expect(mockTeacherService.all).toHaveBeenCalled();
  });

  it('should initialize form for update mode', async () => {
    await configureTestBed(true, true, '1'); // admin, update mode, session id 1
    
    expect(component.onUpdate).toBeTruthy();
    expect(mockSessionApiService.detail).toHaveBeenCalledWith('1');
  });

  // ==================== REQUIRED FIELDS VALIDATION TESTS ====================

  describe('Required Fields Validation', () => {
    beforeEach(async () => {
      await configureTestBed(true, false); // admin, create mode
    });

    it('should have invalid form when name field is empty', () => {
      component.sessionForm!.controls['name'].setValue('');
      component.sessionForm!.controls['date'].setValue('2023-12-01');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue('Test description');
      
      expect(component.sessionForm!.valid).toBeFalsy();
      expect(component.sessionForm!.controls['name'].errors?.['required']).toBeTruthy();
    });

    it('should have invalid form when date field is empty', () => {
      component.sessionForm!.controls['name'].setValue('Test Session');
      component.sessionForm!.controls['date'].setValue('');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue('Test description');
      
      expect(component.sessionForm!.valid).toBeFalsy();
      expect(component.sessionForm!.controls['date'].errors?.['required']).toBeTruthy();
    });

    it('should have invalid form when teacher_id field is empty', () => {
      component.sessionForm!.controls['name'].setValue('Test Session');
      component.sessionForm!.controls['date'].setValue('2023-12-01');
      component.sessionForm!.controls['teacher_id'].setValue('');
      component.sessionForm!.controls['description'].setValue('Test description');
      
      expect(component.sessionForm!.valid).toBeFalsy();
      expect(component.sessionForm!.controls['teacher_id'].errors?.['required']).toBeTruthy();
    });

    it('should have invalid form when description field is empty', () => {
      component.sessionForm!.controls['name'].setValue('Test Session');
      component.sessionForm!.controls['date'].setValue('2023-12-01');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue('');
      
      expect(component.sessionForm!.valid).toBeFalsy();
      expect(component.sessionForm!.controls['description'].errors?.['required']).toBeTruthy();
    });

    it('should have valid form when all required fields are filled', () => {
      component.sessionForm!.controls['name'].setValue('Test Session');
      component.sessionForm!.controls['date'].setValue('2023-12-01');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue('Test description');
      
      expect(component.sessionForm!.valid).toBeTruthy();
    });
  });

  // ==================== SUBMIT BUTTON STATE TESTS ====================

  describe('Submit Button State', () => {
    beforeEach(async () => {
      await configureTestBed(true, false); // admin, create mode
    });

    it('should disable Save button when form is invalid', async () => {
      component.sessionForm!.controls['name'].setValue('');
      component.sessionForm!.controls['date'].setValue('');
      component.sessionForm!.controls['teacher_id'].setValue('');
      component.sessionForm!.controls['description'].setValue('');
      fixture.detectChanges();

      const saveButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(saveButton.nativeElement.disabled).toBeTruthy();
    });

    it('should enable Save button when form is valid', async () => {
      component.sessionForm!.controls['name'].setValue('Test Session');
      component.sessionForm!.controls['date'].setValue('2023-12-01');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue('Test description');
      fixture.detectChanges();

      const saveButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(saveButton.nativeElement.disabled).toBeFalsy();
    });

    it('should disable Save button when only some required fields are filled', async () => {
      component.sessionForm!.controls['name'].setValue('Test Session');
      component.sessionForm!.controls['date'].setValue('');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue('');
      fixture.detectChanges();

      const saveButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(saveButton.nativeElement.disabled).toBeTruthy();
    });
  });

  // ==================== DESCRIPTION VALIDATION TESTS ====================

  describe('Description Field Validation', () => {
    beforeEach(async () => {
      await configureTestBed(true, false); // admin, create mode
    });

    it('should validate description with numeric value (Validators.max limitation)', () => {
      // Note: Validators.max(2000) compares numeric values, not string length
      // This is likely a bug in the original code - should be Validators.maxLength(2000)
      // But we test what's actually implemented
      
      component.sessionForm!.controls['description'].setValue('1999');
      expect(component.sessionForm!.controls['description'].errors?.['max']).toBeFalsy();
      
      component.sessionForm!.controls['description'].setValue('2001');
      expect(component.sessionForm!.controls['description'].errors?.['max']).toBeTruthy();
    });

    it('should accept text description (Validators.max does not apply to text)', () => {
      const longTextDescription = 'A'.repeat(3000); // Long text
      
      component.sessionForm!.controls['name'].setValue('Test Session');
      component.sessionForm!.controls['date'].setValue('2023-12-01');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue(longTextDescription);
      
      // Validators.max doesn't validate text length, so this should be valid
      expect(component.sessionForm!.controls['description'].errors?.['max']).toBeFalsy();
      expect(component.sessionForm!.valid).toBeTruthy();
    });
  });

  // ==================== FORM SUBMISSION TESTS ====================

  describe('Form Submission', () => {
    it('should create session when form is submitted in create mode', async () => {
      await configureTestBed(true, false); // admin, create mode
      
      const sessionData = {
        name: 'New Session',
        date: '2023-12-01',
        teacher_id: 1,
        description: 'New session description'
      };
      
      component.sessionForm!.controls['name'].setValue(sessionData.name);
      component.sessionForm!.controls['date'].setValue(sessionData.date);
      component.sessionForm!.controls['teacher_id'].setValue(sessionData.teacher_id);
      component.sessionForm!.controls['description'].setValue(sessionData.description);
      
      component.submit();
      
      expect(mockSessionApiService.create).toHaveBeenCalledWith(sessionData);
      expect(mockMatSnackBar.open).toHaveBeenCalledWith('Session created !', 'Close', { duration: 3000 });
      expect(router.navigate).toHaveBeenCalledWith(['sessions']);
    });

    it('should update session when form is submitted in update mode', async () => {
      await configureTestBed(true, true, '1'); // admin, update mode, session id 1
      
      const sessionData = {
        name: 'Updated Session',
        date: '2023-12-15',
        teacher_id: 2,
        description: 'Updated session description'
      };
      
      component.sessionForm!.controls['name'].setValue(sessionData.name);
      component.sessionForm!.controls['date'].setValue(sessionData.date);
      component.sessionForm!.controls['teacher_id'].setValue(sessionData.teacher_id);
      component.sessionForm!.controls['description'].setValue(sessionData.description);
      
      component.submit();
      
      expect(mockSessionApiService.update).toHaveBeenCalledWith('1', sessionData);
      expect(mockMatSnackBar.open).toHaveBeenCalledWith('Session updated !', 'Close', { duration: 3000 });
      expect(router.navigate).toHaveBeenCalledWith(['sessions']);
    });

    // ==================== EDGE CASE: UNDEFINED FORM TEST ====================
    
    it('should handle submit when sessionForm is undefined', async () => {
      await configureTestBed(true, false); // admin, create mode
      
      // Manually set sessionForm to undefined to test the optional chaining behavior
      component.sessionForm = undefined;
      
      // This should not throw an error due to optional chaining (?.) in line 50
      expect(() => component.submit()).not.toThrow();
      
      // When sessionForm is undefined, sessionForm?.value returns undefined
      // So the API service should be called with undefined as Session
      expect(mockSessionApiService.create).toHaveBeenCalledWith(undefined);
    });
  });

  // ==================== INTEGRATION SCENARIOS ====================

  describe('Integration Scenarios', () => {
    it('should handle complete create workflow', async () => {
      await configureTestBed(true, false); // admin, create mode
      
      // Initially form should be invalid and button disabled
      expect(component.sessionForm!.valid).toBeFalsy();
      
      const saveButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(saveButton.nativeElement.disabled).toBeTruthy();
      
      // Fill all required fields
      component.sessionForm!.controls['name'].setValue('Complete Test Session');
      component.sessionForm!.controls['date'].setValue('2023-12-01');
      component.sessionForm!.controls['teacher_id'].setValue(1);
      component.sessionForm!.controls['description'].setValue('Complete test description');
      fixture.detectChanges();
      
      // Form should now be valid and button enabled
      expect(component.sessionForm!.valid).toBeTruthy();
      expect(saveButton.nativeElement.disabled).toBeFalsy();
      
      // Submit the form
      saveButton.nativeElement.click();
      
      expect(mockSessionApiService.create).toHaveBeenCalled();
    });

    it('should handle complete update workflow', async () => {
      await configureTestBed(true, true, '1'); // admin, update mode, session id 1
      
      // Form should be pre-filled with existing data
      expect(component.sessionForm!.controls['name'].value).toBe(mockSession.name);
      expect(component.sessionForm!.valid).toBeTruthy();
      
      const saveButton = fixture.debugElement.query(By.css('button[type="submit"]'));
      expect(saveButton.nativeElement.disabled).toBeFalsy();
      
      // Modify some fields
      component.sessionForm!.controls['name'].setValue('Modified Session Name');
      component.sessionForm!.controls['description'].setValue('Modified description');
      
      // Submit the form
      component.submit();
      
      expect(mockSessionApiService.update).toHaveBeenCalledWith('1', expect.objectContaining({
        name: 'Modified Session Name',
        description: 'Modified description'
      }));
    });
  });

  // ==================== UI DISPLAY TESTS ====================

  describe('UI Display', () => {
    it('should display Create session title in create mode', async () => {
      await configureTestBed(true, false); // admin, create mode
      
      expect(fixture.nativeElement.textContent).toContain('Create session');
      expect(fixture.nativeElement.textContent).not.toContain('Update session');
    });

    it('should display Update session title in update mode', async () => {
      await configureTestBed(true, true, '1'); // admin, update mode
      
      expect(fixture.nativeElement.textContent).toContain('Update session');
      expect(fixture.nativeElement.textContent).not.toContain('Create session');
    });

    it('should display teachers in select dropdown', async () => {
      await configureTestBed(true, false); // admin, create mode
      
      expect(mockTeacherService.all).toHaveBeenCalled();
      expect(component.teachers$).toBeDefined();
      
      component.teachers$.subscribe(teachers => {
        expect(teachers).toEqual(mockTeachers);
        expect(teachers.length).toBe(2);
      });
    });
  });
});
