import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { of } from 'rxjs';
import { SessionService } from 'src/app/services/session.service';
import { UserService } from 'src/app/services/user.service';
import { User } from 'src/app/interfaces/user.interface';
import { MatSnackBar } from '@angular/material/snack-bar';
import { expect } from '@jest/globals';

import { MeComponent } from './me.component';

describe('MeComponent Integration Tests', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  let compiled: HTMLElement;
  
  // Jest mocks for integration tests
  let mockSessionService: {
    sessionInformation: any;
    logOut: jest.MockedFunction<() => void>;
  };
  let mockUserService: {
    getById: jest.MockedFunction<(id: string) => any>;
    delete: jest.MockedFunction<(id: string) => any>;
  };
  let mockRouter: {
    navigate: jest.MockedFunction<(commands: any[]) => Promise<boolean>>;
  };
  let mockMatSnackBar: {
    open: jest.MockedFunction<(message: string, action?: string, config?: any) => any>;
  };

  // Test data
  const mockAdminUser: User = {
    id: 1,
    email: 'admin@yoga.com',
    firstName: 'Admin',
    lastName: 'SuperUser',
    admin: true,
    password: 'password123',
    createdAt: new Date('2023-01-15'),
    updatedAt: new Date('2023-12-01')
  };

  const mockRegularUser: User = {
    id: 2,
    email: 'user@yoga.com',
    firstName: 'John',
    lastName: 'Doe',
    admin: false,
    password: 'password123',
    createdAt: new Date('2023-02-10'),
    updatedAt: new Date('2023-11-20')
  };

  const mockSessionInfo = {
    token: 'mock-token',
    type: 'Bearer',
    id: 1,
    username: 'testuser',
    firstName: 'John',
    lastName: 'Doe',
    admin: false
  };

  beforeEach(async () => {
    // Create Jest mocks
    mockSessionService = {
      sessionInformation: mockSessionInfo,
      logOut: jest.fn()
    };
    
    mockUserService = {
      getById: jest.fn(),
      delete: jest.fn()
    };
    
    mockRouter = {
      navigate: jest.fn()
    };
    
    mockMatSnackBar = {
      open: jest.fn()
    };

    // Default mock behaviors
    mockUserService.getById.mockReturnValue(of(mockRegularUser));
    mockUserService.delete.mockReturnValue(of({}));
    mockRouter.navigate.mockResolvedValue(true);

    await TestBed.configureTestingModule({
      declarations: [MeComponent],
      imports: [
        MatSnackBarModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatButtonModule // ✅ Important for mat-button testing
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter },
        { provide: MatSnackBar, useValue: mockMatSnackBar }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
    compiled = fixture.nativeElement as HTMLElement;
  });

  describe('Initial rendering', () => {
    it('should create component and render basic structure', () => {
      expect(component).toBeTruthy();
      
      // Trigger change detection to render the template
      fixture.detectChanges();
      
      // Check if mat-card is rendered
      const matCard = compiled.querySelector('mat-card');
      expect(matCard).toBeTruthy();
    });

    it('should display title "User information"', () => {
      fixture.detectChanges();
      
      const title = compiled.querySelector('h1');
      expect(title?.textContent?.trim()).toBe('User information');
    });

    it('should display back button with arrow icon', () => {
      fixture.detectChanges();
      
      const backButton = compiled.querySelector('button[mat-icon-button]');
      expect(backButton).toBeTruthy();
      
      const arrowIcon = compiled.querySelector('mat-icon');
      expect(arrowIcon?.textContent?.trim()).toBe('arrow_back');
    });
  });

  describe('User data display', () => {
    beforeEach(() => {
      // Ensure user data is loaded
      fixture.detectChanges(); // Triggers ngOnInit
    });

    it('should display user name with uppercase pipe', () => {
      const nameElement = compiled.querySelector('p');
      expect(nameElement?.textContent).toContain('Name: John DOE'); // lastName should be uppercase
    });

    it('should display user email', () => {
      const paragraphs = compiled.querySelectorAll('p');
      const emailParagraph = Array.from(paragraphs).find(p => 
        p.textContent?.includes('Email:')
      );
      expect(emailParagraph?.textContent).toContain('Email: user@yoga.com');
    });

    it('should display creation date with date pipe', () => {
      const paragraphs = compiled.querySelectorAll('p');
      const createDateParagraph = Array.from(paragraphs).find(p => 
        p.textContent?.includes('Create at:')
      );
      expect(createDateParagraph?.textContent).toContain('Create at:');
      expect(createDateParagraph?.textContent).toContain('February'); // Should format date
    });

    it('should display last update date with date pipe', () => {
      const paragraphs = compiled.querySelectorAll('p');
      const updateDateParagraph = Array.from(paragraphs).find(p => 
        p.textContent?.includes('Last update:')
      );
      expect(updateDateParagraph?.textContent).toContain('Last update:');
      expect(updateDateParagraph?.textContent).toContain('November'); // Should format date
    });
  });

  describe('Conditional rendering based on user role', () => {
    it('should show admin message when user is admin', () => {
      // Arrange - Set up admin user
      mockUserService.getById.mockReturnValue(of(mockAdminUser));
      
      // Act - Trigger component initialization
      fixture.detectChanges();
      
      // Assert - Admin message should be visible
      const adminMessage = compiled.querySelector('p.my2');
      expect(adminMessage?.textContent?.trim()).toBe('You are admin');
    });

    it('should hide admin message when user is not admin', () => {
      // Regular user is already set up in beforeEach
      fixture.detectChanges();
      
      const adminMessage = compiled.querySelector('p.my2');
      expect(adminMessage).toBeFalsy(); // Should not exist for regular users
    });

    it('should show delete button for non-admin users', () => {
      fixture.detectChanges();
      
      const deleteButton = compiled.querySelector('button[color="warn"]');
      expect(deleteButton).toBeTruthy();
      expect(deleteButton?.textContent).toContain('Detail'); // Button text
    });

    it('should hide delete button for admin users', () => {
      // Arrange - Set up admin user
      mockUserService.getById.mockReturnValue(of(mockAdminUser));
      
      // Act
      fixture.detectChanges();
      
      // Assert - Delete button should not exist
      const deleteButton = compiled.querySelector('button[color="warn"]');
      expect(deleteButton).toBeFalsy();
    });
  });

  describe('User interactions', () => {
    beforeEach(() => {
      fixture.detectChanges(); // Initialize component
    });

    it('should call back() when back button is clicked', () => {
      // Arrange - Spy on window.history.back
      const historySpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});
      
      // Act - Click the back button
      const backButton = compiled.querySelector('button[mat-icon-button]') as HTMLButtonElement;
      backButton.click();
      
      // Assert
      expect(historySpy).toHaveBeenCalled();
      
      // Cleanup
      historySpy.mockRestore();
    });

    it('should call delete() when delete button is clicked', () => {
      // Arrange - Spy on component delete method
      const deleteSpy = jest.spyOn(component, 'delete');
      
      // Act - Click the delete button
      const deleteButton = compiled.querySelector('button[color="warn"]') as HTMLButtonElement;
      deleteButton.click();
      
      // Assert
      expect(deleteSpy).toHaveBeenCalled();
    });

    it('should trigger service calls when delete button is clicked', () => {
      // Act - Click delete button
      const deleteButton = compiled.querySelector('button[color="warn"]') as HTMLButtonElement;
      deleteButton.click();
      
      // Assert - Verify service method calls
      expect(mockUserService.delete).toHaveBeenCalledWith('1');
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Your account has been deleted !', 
        'Close', 
        { duration: 3000 }
      );
      expect(mockSessionService.logOut).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });
  });

  describe('Loading states', () => {
    it('should not display user content when user is undefined', () => {
      // Arrange - Prevent ngOnInit from loading data
      mockUserService.getById.mockReturnValue(of(undefined as any));
      
      // Act - Trigger component initialization
      fixture.detectChanges(); // This calls ngOnInit
      
      // Manually set user to undefined to simulate loading state
      component.user = undefined;
      fixture.detectChanges(); // Re-render with undefined user
      
      // Assert - User content should not be visible
      const userContent = compiled.querySelector('div[fxLayout="column"]');
      expect(userContent).toBeFalsy();
    });

    it('should display user content when user is loaded', () => {
      // User is loaded in beforeEach via ngOnInit mock
      fixture.detectChanges();
      
      const userContent = compiled.querySelector('div[fxLayout="column"]');
      expect(userContent).toBeTruthy();
    });

    it('should not display user content initially before ngOnInit', () => {
      // Don't call fixture.detectChanges() yet
      // Component exists but ngOnInit hasn't run
      
      const userContent = compiled.querySelector('div[fxLayout="column"]');
      expect(userContent).toBeFalsy();
    });
  });

  describe('DebugElement approach (Alternative testing method)', () => {
    it('should find elements using DebugElement and CSS selectors', () => {
      fixture.detectChanges();
      
      // Using DebugElement for more precise element selection
      const backButtonDebug = fixture.debugElement.query(By.css('button[mat-icon-button]'));
      expect(backButtonDebug).toBeTruthy();
      
      const titleDebug = fixture.debugElement.query(By.css('h1'));
      expect(titleDebug.nativeElement.textContent.trim()).toBe('User information');
    });

    it('should trigger click events using DebugElement', () => {
      fixture.detectChanges();
      
      const deleteSpy = jest.spyOn(component, 'delete');
      
      // Find delete button using DebugElement
      const deleteButtonDebug = fixture.debugElement.query(By.css('button[color="warn"]'));
      
      // Trigger click event
      deleteButtonDebug.triggerEventHandler('click', null);
      
      expect(deleteSpy).toHaveBeenCalled();
    });
  });
});