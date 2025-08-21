import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';
import { Component } from '@angular/core';
import { expect } from '@jest/globals';
import { of } from 'rxjs';
import { SessionService } from '../../../../services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { TeacherService } from '../../../../services/teacher.service';
import { Session } from '../../interfaces/session.interface';
import { Teacher } from '../../../../interfaces/teacher.interface';

import { DetailComponent } from './detail.component';

// Mock component for navigation testing
@Component({
  template: '<h1>Sessions Page</h1>'
})
class MockSessionsComponent {}

describe('DetailComponent', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;

  const mockSession: Session = {
    id: 1,
    name: 'Test Session',
    description: 'Test session description',
    date: new Date('2023-12-01'),
    teacher_id: 1,
    users: [2, 3], // User 1 (admin) not participating
    createdAt: new Date('2023-11-01'),
    updatedAt: new Date('2023-11-15')
  };

  const mockTeacher: Teacher = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const createMockSessionService = (isAdmin: boolean, userId: number) => ({
    sessionInformation: {
      admin: isAdmin,
      id: userId,
      username: 'test@test.com',
      firstName: 'Test',
      lastName: 'User'
    }
  });

  const mockSessionApiService = {
    detail: jest.fn().mockReturnValue(of(mockSession)),
    delete: jest.fn().mockReturnValue(of({})),
    participate: jest.fn().mockReturnValue(of({})),
    unParticipate: jest.fn().mockReturnValue(of({}))
  };

  const mockTeacherService = {
    detail: jest.fn().mockReturnValue(of(mockTeacher))
  };

  const mockMatSnackBar = {
    open: jest.fn()
  };

  const mockActivatedRoute = {
    snapshot: {
      paramMap: {
        get: jest.fn().mockReturnValue('1')
      }
    }
  };

  const configureTestBed = async (isAdmin: boolean, userId: number = 1) => {
    // Reset all mocks before each test configuration
    jest.clearAllMocks();
    mockSessionApiService.detail.mockReturnValue(of(mockSession));
    mockSessionApiService.delete.mockReturnValue(of({}));
    mockTeacherService.detail.mockReturnValue(of(mockTeacher));

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'sessions', component: MockSessionsComponent }
        ]),
        HttpClientModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        CommonModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule
      ],
      declarations: [DetailComponent, MockSessionsComponent],
      providers: [
        { provide: SessionService, useValue: createMockSessionService(isAdmin, userId) },
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: TeacherService, useValue: mockTeacherService },
        { provide: MatSnackBar, useValue: mockMatSnackBar },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  };

  it('should create', async () => {
    await configureTestBed(true);
    expect(component).toBeTruthy();
  });

  it('should initialize with correct session and user data', async () => {
    await configureTestBed(true, 1);
    
    expect(component.sessionId).toBe('1');
    expect(component.userId).toBe('1');
    expect(component.isAdmin).toBeTruthy();
  });

  it('should fetch session and teacher data on init', async () => {
    await configureTestBed(true);
    
    expect(mockSessionApiService.detail).toHaveBeenCalledWith('1');
    expect(mockTeacherService.detail).toHaveBeenCalledWith('1');
    expect(component.session).toEqual(mockSession);
    expect(component.teacher).toEqual(mockTeacher);
  });

  // ==================== ADMIN DELETE BUTTON TESTS ====================

  describe('Admin Delete Button Visibility and Functionality', () => {
    it('should show Delete button when user is admin', async () => {
      await configureTestBed(true);
      
      const deleteButtons = fixture.debugElement.queryAll(By.css('button'));
      const deleteButton = deleteButtons.find(btn => 
        btn.nativeElement.textContent.includes('Delete')
      );
      
      expect(deleteButton).toBeTruthy();
      expect(deleteButton!.nativeElement.textContent).toContain('Delete');
    });

    it('should NOT show Delete button when user is not admin', async () => {
      await configureTestBed(false);
      
      const deleteButtons = fixture.debugElement.queryAll(By.css('button'));
      const deleteButton = deleteButtons.find(btn => 
        btn.nativeElement.textContent.includes('Delete')
      );
      
      expect(deleteButton).toBeFalsy();
    });

    it('should call delete method when Delete button is clicked', async () => {
      await configureTestBed(true);
      jest.spyOn(component, 'delete');
      
      const deleteButtons = fixture.debugElement.queryAll(By.css('button'));
      const deleteButton = deleteButtons.find(btn => 
        btn.nativeElement.textContent.includes('Delete')
      );
      
      deleteButton!.nativeElement.click();
      
      expect(component.delete).toHaveBeenCalled();
    });

    it('should execute complete delete workflow', async () => {
      await configureTestBed(true);
      const router = TestBed.inject(Router);
      jest.spyOn(router, 'navigate');
      
      component.delete();
      
      expect(mockSessionApiService.delete).toHaveBeenCalledWith('1');
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Session deleted !', 
        'Close', 
        { duration: 3000 }
      );
      expect(router.navigate).toHaveBeenCalledWith(['sessions']);
    });
  });

  // ==================== PARTICIPATION TESTS ====================

  describe('Participation Functionality', () => {
    it('should show Participate button for non-admin user not participating', async () => {
      await configureTestBed(false, 5); // User 5 not in mockSession.users
      
      const buttons = fixture.debugElement.queryAll(By.css('button'));
      const participateButton = buttons.find(btn => 
        btn.nativeElement.textContent.includes('Participate') && 
        !btn.nativeElement.textContent.includes('Do not participate')
      );
      
      expect(participateButton).toBeTruthy();
    });

    it('should show Do not participate button for non-admin user already participating', async () => {
      await configureTestBed(false, 2); // User 2 is in mockSession.users
      
      const buttons = fixture.debugElement.queryAll(By.css('button'));
      const unparticipateButton = buttons.find(btn => 
        btn.nativeElement.textContent.includes('Do not participate')
      );
      
      expect(unparticipateButton).toBeTruthy();
    });

    it('should execute participate workflow', async () => {
      await configureTestBed(false, 5);
      
      component.participate();
      
      expect(mockSessionApiService.participate).toHaveBeenCalledWith('1', '5');
      expect(mockSessionApiService.detail).toHaveBeenCalled(); // fetchSession called
    });

    it('should execute unParticipate workflow', async () => {
      await configureTestBed(false, 2);
      
      component.unParticipate();
      
      expect(mockSessionApiService.unParticipate).toHaveBeenCalledWith('1', '2');
      expect(mockSessionApiService.detail).toHaveBeenCalled(); // fetchSession called
    });
  });

  // ==================== NAVIGATION AND UI TESTS ====================

  describe('Navigation and UI', () => {
    it('should call window.history.back() when back button is clicked', async () => {
      await configureTestBed(true);
      jest.spyOn(window.history, 'back');
      
      component.back();
      
      expect(window.history.back).toHaveBeenCalled();
    });

    it('should display session information correctly', async () => {
      await configureTestBed(true);
      
      expect(fixture.nativeElement.textContent).toContain(mockSession.name); // titlecase pipe, not uppercase
      expect(fixture.nativeElement.textContent).toContain(mockSession.description);
      expect(fixture.nativeElement.textContent).toContain(mockTeacher.firstName);
      expect(fixture.nativeElement.textContent).toContain(mockTeacher.lastName.toUpperCase());
    });

    it('should display correct number of attendees', async () => {
      await configureTestBed(true);
      
      expect(fixture.nativeElement.textContent).toContain(`${mockSession.users.length} attendees`);
    });
  });

  // ==================== INTEGRATION SCENARIOS ====================

  describe('Integration Scenarios', () => {
    it('should handle admin user complete workflow', async () => {
      await configureTestBed(true);
      
      // Admin should see delete button
      const buttons = fixture.debugElement.queryAll(By.css('button'));
      const deleteButton = buttons.find(btn => 
        btn.nativeElement.textContent.includes('Delete')
      );
      expect(deleteButton).toBeTruthy();
      
      // Admin should not see participation buttons
      const participateButton = buttons.find(btn => 
        btn.nativeElement.textContent.includes('Participate')
      );
      expect(participateButton).toBeFalsy();
    });

    it('should handle non-admin user complete workflow', async () => {
      await configureTestBed(false, 5);
      
      // Non-admin should not see delete button
      const buttons = fixture.debugElement.queryAll(By.css('button'));
      const deleteButton = buttons.find(btn => 
        btn.nativeElement.textContent.includes('Delete')
      );
      expect(deleteButton).toBeFalsy();
      
      // Non-admin should see participate button (user 5 not participating)
      const participateButton = buttons.find(btn => 
        btn.nativeElement.textContent.includes('Participate') &&
        !btn.nativeElement.textContent.includes('Do not participate')
      );
      expect(participateButton).toBeTruthy();
    });
  });

  // ==================== BASIC FUNCTIONALITY TESTS ====================

  describe('Basic Functionality', () => {
    it('should handle admin user correctly', async () => {
      await configureTestBed(true);
      
      expect(component.isAdmin).toBeTruthy();
    });

    it('should handle non-admin user correctly', async () => {
      await configureTestBed(false);
      
      expect(component.isAdmin).toBeFalsy();
    });

    it('should set participation status correctly for participating user', async () => {
      await configureTestBed(false, 2); // User 2 is in mockSession.users
      
      expect(component.isParticipate).toBeTruthy();
    });

    it('should set participation status correctly for non-participating user', async () => {
      await configureTestBed(false, 5); // User 5 is not in mockSession.users
      
      expect(component.isParticipate).toBeFalsy();
    });
  });
});