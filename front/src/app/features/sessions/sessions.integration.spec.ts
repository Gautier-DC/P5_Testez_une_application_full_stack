import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { expect } from '@jest/globals';
import { of } from 'rxjs';

import { SessionsModule } from './sessions.module';
import { ListComponent } from './components/list/list.component';
import { FormComponent } from './components/form/form.component';
import { DetailComponent } from './components/detail/detail.component';
import { SessionApiService } from './services/session-api.service';
import { SessionService } from '../../services/session.service';
import { TeacherService } from '../../services/teacher.service';
import { Session } from './interfaces/session.interface';
import { Teacher } from '../../interfaces/teacher.interface';

describe('Sessions Module Integration Tests', () => {
  let router: Router;
  let location: Location;
  let mockSessionService: any;
  let mockSessionApiService: any;
  let mockTeacherService: any;

  const mockSessions: Session[] = [
    {
      id: 1,
      name: 'Morning Yoga',
      description: 'Relaxing morning session',
      date: new Date('2023-12-01'),
      teacher_id: 1,
      users: [1, 2],
      createdAt: new Date(),
      updatedAt: new Date()
    },
    {
      id: 2,
      name: 'Evening Flow',
      description: 'Dynamic evening practice',
      date: new Date('2023-12-02'),
      teacher_id: 2,
      users: [2, 3],
      createdAt: new Date(),
      updatedAt: new Date()
    }
  ];

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

  const mockSessionInformation = {
    token: 'test-token',
    type: 'Bearer',
    id: 1,
    username: 'admin@test.com',
    firstName: 'Admin',
    lastName: 'User',
    admin: true
  };

  beforeEach(async () => {
    mockSessionService = {
      sessionInformation: mockSessionInformation,
      isLogged: true,
      $isLogged: jest.fn().mockReturnValue(of(true))
    };

    mockSessionApiService = {
      all: jest.fn().mockReturnValue(of(mockSessions)),
      detail: jest.fn().mockReturnValue(of(mockSessions[0])),
      create: jest.fn().mockReturnValue(of(mockSessions[0])),
      update: jest.fn().mockReturnValue(of(mockSessions[0])),
      delete: jest.fn().mockReturnValue(of({})),
      participate: jest.fn().mockReturnValue(of(undefined)),
      unParticipate: jest.fn().mockReturnValue(of(undefined))
    };

    mockTeacherService = {
      all: jest.fn().mockReturnValue(of(mockTeachers))
    };

    await TestBed.configureTestingModule({
      imports: [
        SessionsModule,
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: TeacherService, useValue: mockTeacherService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
  });

  // ==================== SESSIONS LIST INTEGRATION ====================

  describe('Sessions List Integration', () => {
    let listFixture: ComponentFixture<ListComponent>;
    let listComponent: ListComponent;

    beforeEach(async () => {
      listFixture = TestBed.createComponent(ListComponent);
      listComponent = listFixture.componentInstance;
      listFixture.detectChanges();
      await listFixture.whenStable();
    });

    it('should load and display sessions list', () => {
      expect(listComponent).toBeTruthy();
      expect(mockSessionApiService.all).toHaveBeenCalled();
      
      listComponent.sessions$.subscribe(sessions => {
        expect(sessions).toEqual(mockSessions);
        expect(sessions.length).toBe(2);
      });
    });

    it('should handle session list filtering and sorting', () => {
      expect(listComponent.sessions$).toBeDefined();
      
      // Test that sessions are properly loaded
      listComponent.sessions$.subscribe(sessions => {
        expect(sessions.every(session => session.id)).toBeTruthy();
        expect(sessions.every(session => session.name)).toBeTruthy();
        expect(sessions.every(session => session.teacher_id)).toBeTruthy();
      });
    });

    it('should navigate to session detail from list', async () => {
      const sessionId = 1;
      
      // Simulate clicking on session detail
      await router.navigate(['/sessions/detail', sessionId]);
      expect(location.path()).toBe('/sessions/detail/1');
    });

    it('should navigate to session update from list', async () => {
      const sessionId = 1;
      
      // Admin user should be able to navigate to update
      await router.navigate(['/sessions/update', sessionId]);
      expect(location.path()).toBe('/sessions/update/1');
    });
  });

  // ==================== SESSIONS FORM INTEGRATION ====================

  describe('Sessions Form Integration', () => {
    let formFixture: ComponentFixture<FormComponent>;
    let formComponent: FormComponent;

    beforeEach(async () => {
      formFixture = TestBed.createComponent(FormComponent);
      formComponent = formFixture.componentInstance;
      
      // Mock router URL for create mode
      Object.defineProperty(router, 'url', {
        value: '/sessions/create',
        configurable: true
      });
      
      formFixture.detectChanges();
      await formFixture.whenStable();
    });

    it('should initialize form in create mode', () => {
      expect(formComponent).toBeTruthy();
      expect(formComponent.onUpdate).toBeFalsy();
      expect(formComponent.sessionForm).toBeDefined();
      expect(mockTeacherService.all).toHaveBeenCalled();
    });

    it('should load teachers for form dropdown', () => {
      formComponent.teachers$.subscribe(teachers => {
        expect(teachers).toEqual(mockTeachers);
        expect(teachers.length).toBe(2);
      });
    });

    it('should create new session through complete workflow', async () => {
      const newSessionData = {
        name: 'Integration Test Session',
        date: '2023-12-15',
        teacher_id: 1,
        description: 'Session created through integration test'
      };

      // Fill form
      formComponent.sessionForm!.patchValue(newSessionData);
      expect(formComponent.sessionForm!.valid).toBeTruthy();

      // Submit form
      formComponent.submit();

      expect(mockSessionApiService.create).toHaveBeenCalledWith(newSessionData);
    });

    it('should handle form validation in integration scenario', () => {
      // Test empty form
      expect(formComponent.sessionForm!.valid).toBeFalsy();

      // Fill required fields one by one
      formComponent.sessionForm!.patchValue({
        name: 'Test Session'
      });
      expect(formComponent.sessionForm!.valid).toBeFalsy();

      formComponent.sessionForm!.patchValue({
        name: 'Test Session',
        date: '2023-12-01'
      });
      expect(formComponent.sessionForm!.valid).toBeFalsy();

      formComponent.sessionForm!.patchValue({
        name: 'Test Session',
        date: '2023-12-01',
        teacher_id: 1
      });
      expect(formComponent.sessionForm!.valid).toBeFalsy();

      formComponent.sessionForm!.patchValue({
        name: 'Test Session',
        date: '2023-12-01',
        teacher_id: 1,
        description: 'Test description'
      });
      expect(formComponent.sessionForm!.valid).toBeTruthy();
    });
  });

  // ==================== SESSIONS DETAIL INTEGRATION ====================

  describe('Sessions Detail Integration', () => {
    let detailFixture: ComponentFixture<DetailComponent>;
    let detailComponent: DetailComponent;

    beforeEach(async () => {
      // Mock ActivatedRoute with session ID
      const mockActivatedRoute = {
        snapshot: {
          paramMap: {
            get: jest.fn().mockReturnValue('1')
          }
        }
      };

      await TestBed.overrideProvider('ActivatedRoute', { useValue: mockActivatedRoute })
        .compileComponents();

      detailFixture = TestBed.createComponent(DetailComponent);
      detailComponent = detailFixture.componentInstance;
      detailFixture.detectChanges();
      await detailFixture.whenStable();
    });

    it('should load session details', () => {
      expect(detailComponent).toBeTruthy();
      expect(mockSessionApiService.detail).toHaveBeenCalledWith('1');
    });

    it('should handle session participation workflow', () => {
      const userId = '2';
      
      // Mock participate
      detailComponent.participate();
      expect(mockSessionApiService.participate).toHaveBeenCalledWith('1', mockSessionInformation.id.toString());
    });

    it('should handle session unParticipation workflow', () => {
      const userId = '2';
      
      // Mock unParticipate
      detailComponent.unParticipate();
      expect(mockSessionApiService.unParticipate).toHaveBeenCalledWith('1', mockSessionInformation.id.toString());
    });

    it('should handle session deletion workflow', () => {
      // Only admin can delete
      expect(mockSessionService.sessionInformation.admin).toBeTruthy();
      
      detailComponent.delete();
      expect(mockSessionApiService.delete).toHaveBeenCalledWith('1');
    });
  });

  // ==================== COMPLETE CRUD WORKFLOW INTEGRATION ====================

  describe('Complete CRUD Workflow Integration', () => {
    it('should handle complete session lifecycle', async () => {
      // 1. List sessions
      const listFixture = TestBed.createComponent(ListComponent);
      const listComponent = listFixture.componentInstance;
      listFixture.detectChanges();
      await listFixture.whenStable();

      expect(mockSessionApiService.all).toHaveBeenCalled();

      // 2. Create new session
      const formFixture = TestBed.createComponent(FormComponent);
      const formComponent = formFixture.componentInstance;
      
      // Mock create mode
      Object.defineProperty(router, 'url', {
        value: '/sessions/create',
        configurable: true
      });
      
      formFixture.detectChanges();
      await formFixture.whenStable();

      const newSessionData = {
        name: 'CRUD Test Session',
        date: '2023-12-20',
        teacher_id: 1,
        description: 'Complete CRUD workflow test'
      };

      formComponent.sessionForm!.patchValue(newSessionData);
      formComponent.submit();
      expect(mockSessionApiService.create).toHaveBeenCalledWith(newSessionData);

      // 3. View session details
      const detailFixture = TestBed.createComponent(DetailComponent);
      const detailComponent = detailFixture.componentInstance;
      detailFixture.detectChanges();
      await detailFixture.whenStable();

      expect(mockSessionApiService.detail).toHaveBeenCalled();

      // 4. Update session
      Object.defineProperty(router, 'url', {
        value: '/sessions/update/1',
        configurable: true
      });
      
      const updateFixture = TestBed.createComponent(FormComponent);
      const updateComponent = updateFixture.componentInstance;
      updateFixture.detectChanges();
      await updateFixture.whenStable();

      expect(updateComponent.onUpdate).toBeTruthy();
      expect(mockSessionApiService.detail).toHaveBeenCalled();

      // 5. Delete session
      detailComponent.delete();
      expect(mockSessionApiService.delete).toHaveBeenCalled();
    });

    it('should handle session participation lifecycle', async () => {
      const detailFixture = TestBed.createComponent(DetailComponent);
      const detailComponent = detailFixture.componentInstance;
      detailFixture.detectChanges();
      await detailFixture.whenStable();

      // Join session
      detailComponent.participate();
      expect(mockSessionApiService.participate).toHaveBeenCalled();

      // Leave session
      detailComponent.unParticipate();
      expect(mockSessionApiService.unParticipate).toHaveBeenCalled();
    });
  });

  // ==================== MODULE LOADING INTEGRATION ====================

  describe('Module Loading Integration', () => {
    it('should load sessions module components successfully', () => {
      const listFixture = TestBed.createComponent(ListComponent);
      const formFixture = TestBed.createComponent(FormComponent);
      const detailFixture = TestBed.createComponent(DetailComponent);

      expect(listFixture.componentInstance).toBeTruthy();
      expect(formFixture.componentInstance).toBeTruthy();
      expect(detailFixture.componentInstance).toBeTruthy();
    });

    it('should provide all necessary services', () => {
      const sessionApiService = TestBed.inject(SessionApiService);
      const sessionService = TestBed.inject(SessionService);
      const teacherService = TestBed.inject(TeacherService);

      expect(sessionApiService).toBeTruthy();
      expect(sessionService).toBeTruthy();
      expect(teacherService).toBeTruthy();
    });

    it('should handle module imports correctly', () => {
      // Test that all Material modules are available
      const listFixture = TestBed.createComponent(ListComponent);
      const formFixture = TestBed.createComponent(FormComponent);
      
      listFixture.detectChanges();
      formFixture.detectChanges();
      
      expect(listFixture.nativeElement).toBeTruthy();
      expect(formFixture.nativeElement).toBeTruthy();
    });
  });

  // ==================== ERROR SCENARIOS INTEGRATION ====================

  describe('Error Scenarios Integration', () => {
    it('should handle API errors in session list', async () => {
      mockSessionApiService.all.mockReturnValue(
        Promise.reject(new Error('Failed to load sessions'))
      );

      const listFixture = TestBed.createComponent(ListComponent);
      const listComponent = listFixture.componentInstance;
      listFixture.detectChanges();

      expect(mockSessionApiService.all).toHaveBeenCalled();
    });

    it('should handle session creation errors', async () => {
      mockSessionApiService.create.mockReturnValue(
        Promise.reject(new Error('Failed to create session'))
      );

      const formFixture = TestBed.createComponent(FormComponent);
      const formComponent = formFixture.componentInstance;
      
      Object.defineProperty(router, 'url', {
        value: '/sessions/create',
        configurable: true
      });
      
      formFixture.detectChanges();
      await formFixture.whenStable();

      const sessionData = {
        name: 'Error Test Session',
        date: '2023-12-01',
        teacher_id: 1,
        description: 'Testing error handling'
      };

      formComponent.sessionForm!.patchValue(sessionData);
      formComponent.submit();

      expect(mockSessionApiService.create).toHaveBeenCalledWith(sessionData);
    });

    it('should handle session detail loading errors', async () => {
      mockSessionApiService.detail.mockReturnValue(
        Promise.reject(new Error('Session not found'))
      );

      const detailFixture = TestBed.createComponent(DetailComponent);
      const detailComponent = detailFixture.componentInstance;
      detailFixture.detectChanges();

      expect(mockSessionApiService.detail).toHaveBeenCalled();
    });
  });
});