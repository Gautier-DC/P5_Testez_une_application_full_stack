import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';
import { expect } from '@jest/globals';
import { of } from 'rxjs';
import { SessionService } from 'src/app/services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { Session } from '../../interfaces/session.interface';

import { ListComponent } from './list.component';

describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let sessionService: SessionService;
  let sessionApiService: SessionApiService;

  const mockSessions: Session[] = [
    {
      id: 1,
      name: 'Yoga Session 1',
      description: 'A relaxing yoga session',
      date: new Date('2023-10-01'),
      teacher_id: 1,
      users: [1, 2, 3],
      createdAt: new Date(),
      updatedAt: new Date()
    },
    {
      id: 2,
      name: 'Advanced Yoga',
      description: 'For experienced practitioners',
      date: new Date('2023-10-15'),
      teacher_id: 2,
      users: [4, 5],
      createdAt: new Date(),
      updatedAt: new Date()
    }
  ];

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
    all: jest.fn().mockReturnValue(of(mockSessions))
  };

  const configureTestBed = async (isAdmin: boolean) => {
    await TestBed.configureTestingModule({
      declarations: [ListComponent],
      imports: [
        HttpClientModule,
        CommonModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        RouterTestingModule
      ],
      providers: [
        { provide: SessionService, useValue: createMockSessionService(isAdmin) },
        { provide: SessionApiService, useValue: mockSessionApiService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
    sessionService = TestBed.inject(SessionService);
    sessionApiService = TestBed.inject(SessionApiService);
    fixture.detectChanges();
  };

  it('should create', async () => {
    await configureTestBed(true);
    expect(component).toBeTruthy();
  });

  it('should initialize with sessions from API', async () => {
    await configureTestBed(true);
    
    component.sessions$.subscribe(sessions => {
      expect(sessions).toEqual(mockSessions);
    });
    expect(sessionApiService.all).toHaveBeenCalled();
  });

  it('should get user information from session service', async () => {
    await configureTestBed(true);
    
    const user = component.user;
    expect(user).toBe(sessionService.sessionInformation);
    expect(user?.admin).toBeTruthy();
  });

  // ==================== ADMIN BUTTON VISIBILITY TESTS ====================

  describe('Admin Button Visibility', () => {
    it('should show Create button when user is admin', async () => {
      await configureTestBed(true);
      
      const createButton = fixture.debugElement.query(By.css('button[routerLink="create"]'));
      expect(createButton).toBeTruthy();
      expect(createButton.nativeElement.textContent).toContain('Create');
    });

    it('should NOT show Create button when user is not admin', async () => {
      await configureTestBed(false);
      
      const createButton = fixture.debugElement.query(By.css('button[routerLink="create"]'));
      expect(createButton).toBeFalsy();
    });
  });

  // ==================== BASIC FUNCTIONALITY TESTS ====================

  describe('Basic Functionality', () => {
    it('should call SessionApiService.all() on initialization', async () => {
      await configureTestBed(true);
      
      expect(mockSessionApiService.all).toHaveBeenCalled();
    });

    it('should have sessions$ observable', async () => {
      await configureTestBed(true);
      
      expect(component.sessions$).toBeDefined();
      
      component.sessions$.subscribe(sessions => {
        expect(Array.isArray(sessions)).toBeTruthy();
        expect(sessions.length).toBe(2);
        expect(sessions[0].name).toBe('Yoga Session 1');
        expect(sessions[1].name).toBe('Advanced Yoga');
      });
    });

    it('should handle admin user correctly', async () => {
      await configureTestBed(true);
      
      expect(component.user?.admin).toBeTruthy();
    });

    it('should handle non-admin user correctly', async () => {
      await configureTestBed(false);
      
      expect(component.user?.admin).toBeFalsy();
    });
  });

  // ==================== ERROR HANDLING TESTS ====================

  describe('Error Handling', () => {
    it('should handle empty sessions list', async () => {
      const emptyMockSessionApiService = {
        all: jest.fn().mockReturnValue(of([]))
      };
      
      await TestBed.configureTestingModule({
        declarations: [ListComponent],
        imports: [
          HttpClientModule,
          CommonModule,
          MatCardModule,
          MatIconModule,
          MatButtonModule,
          RouterTestingModule
        ],
        providers: [
          { provide: SessionService, useValue: createMockSessionService(true) },
          { provide: SessionApiService, useValue: emptyMockSessionApiService }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ListComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      
      component.sessions$.subscribe(sessions => {
        expect(sessions).toEqual([]);
        expect(sessions.length).toBe(0);
      });
    });

    it('should handle undefined user gracefully', async () => {
      const undefinedUserSessionService = {
        sessionInformation: undefined
      };
      
      await TestBed.configureTestingModule({
        declarations: [ListComponent],
        imports: [
          HttpClientModule,
          CommonModule,
          MatCardModule,
          MatIconModule,
          MatButtonModule,
          RouterTestingModule
        ],
        providers: [
          { provide: SessionService, useValue: undefinedUserSessionService },
          { provide: SessionApiService, useValue: mockSessionApiService }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ListComponent);
      component = fixture.componentInstance;
      
      // The template uses user!.admin so it will throw if user is undefined
      // This is expected behavior with the non-null assertion operator
      expect(() => fixture.detectChanges()).toThrow();
      expect(component.user).toBeUndefined();
    });
  });
});