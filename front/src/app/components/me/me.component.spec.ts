import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { SessionService } from 'src/app/services/session.service';
import { UserService } from 'src/app/services/user.service';
import { User } from 'src/app/interfaces/user.interface';
import { MatSnackBar } from '@angular/material/snack-bar';
import { expect } from '@jest/globals'; // ✅ Fix pour les types Jest

import { MeComponent } from './me.component';
import { MatButtonModule } from '@angular/material/button';

describe('MeComponent', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  
  // Pure Jest mocks with proper typing
  let mockSessionService: {
    sessionInformation: any;
    logOut: jest.MockedFunction<() => void>;
  };
  let mockUserService: {
    getById: jest.MockedFunction<(id: string) => Observable<User>>;
    delete: jest.MockedFunction<(id: string) => Observable<any>>;
  };
  let mockRouter: {
    navigate: jest.MockedFunction<(commands: any[]) => Promise<boolean>>;
  };
  let mockMatSnackBar: {
    open: jest.MockedFunction<(message: string, action?: string, config?: any) => any>;
  };

  // Mock data
  const mockUser: User = {
    id: 1,
    email: 'test@test.com',
    firstName: 'John',
    lastName: 'Doe',
    admin: false,
    password: 'password123',
    createdAt: new Date('2023-01-01'),
    updatedAt: new Date('2023-06-01')
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
    // Create pure Jest mocks - no jasmine.createSpyObj
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

    // Setup default mock behaviors with Jest syntax
    mockUserService.getById.mockReturnValue(of(mockUser));
    mockUserService.delete.mockReturnValue(of({}));
    mockRouter.navigate.mockResolvedValue(true);

    await TestBed.configureTestingModule({
      declarations: [MeComponent],
      imports: [
        MatSnackBarModule,
        HttpClientTestingModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule
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
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('back', () => {
    it('should call window.history.back', () => {
      // Arrange - Mock window.history.back
      const spy = jest.spyOn(window.history, 'back').mockImplementation(() => {});
      
      // Act - Call the method
      component.back();
      
      // Assert - Verify the call
      expect(spy).toHaveBeenCalled();
      
      // Cleanup
      spy.mockRestore();
    });
  });

  describe('ngOnInit', () => {
    it('should load user data on initialization', () => {
      // Act - Call ngOnInit (or trigger with detectChanges)
      component.ngOnInit();
      
      // Assert - Verify service was called with correct ID
      expect(mockUserService.getById).toHaveBeenCalledWith('1');
      expect(component.user).toEqual(mockUser);
    });

    it('should handle session information correctly', () => {
      // Act
      component.ngOnInit();
      
      // Assert - Verify the user ID from session is used
      expect(mockUserService.getById).toHaveBeenCalledWith(
        mockSessionService.sessionInformation!.id.toString()
      );
    });
  });

  describe('delete', () => {
    beforeEach(() => {
      // Setup component with user data
      component.user = mockUser;
    });

    it('should delete user account successfully', () => {
      // Act
      component.delete();
      
      // Assert - Verify all the expected calls
      expect(mockUserService.delete).toHaveBeenCalledWith('1');
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Your account has been deleted !', 
        'Close', 
        { duration: 3000 }
      );
      expect(mockSessionService.logOut).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should use correct user ID for deletion', () => {
      // Act
      component.delete();
      
      // Assert - Verify deletion uses session information ID
      expect(mockUserService.delete).toHaveBeenCalledWith(
        mockSessionService.sessionInformation!.id.toString()
      );
    });

    it('should handle deletion workflow in correct order', () => {
      // Arrange - Create a call order tracker with Jest
      let callOrder: string[] = [];
      
      mockUserService.delete.mockImplementation(() => {
        callOrder.push('delete');
        return of({});
      });
      
      mockMatSnackBar.open.mockImplementation(() => {
        callOrder.push('snackbar');
        return {} as any;
      });
      
      mockSessionService.logOut.mockImplementation(() => {
        callOrder.push('logout');
      });
      
      mockRouter.navigate.mockImplementation(() => {
        callOrder.push('navigate');
        return Promise.resolve(true);
      });
      
      // Act
      component.delete();
      
      // Assert - Verify the order
      expect(callOrder).toEqual(['delete', 'snackbar', 'logout', 'navigate']);
    });
  });

  describe('Edge cases', () => {
    it('should handle missing session information gracefully', () => {
      // Arrange - Set session information to null
      mockSessionService.sessionInformation = null;
      
      // Act & Assert - Should throw error because component uses !
      expect(() => component.ngOnInit()).toThrow();
    });

    it('should handle user service HTTP error', () => {
      // Arrange - Make getById return an error Observable
      mockUserService.getById.mockReturnValue(
        throwError(() => new Error('User not found'))
      );
      
      // Act - ngOnInit should not crash (but Observable will error)
      expect(() => component.ngOnInit()).not.toThrow();
      
      // Note: The Observable error would be handled by error handling in a real app
      // but this component doesn't have error handling implemented
    });

    it('should call ngOnInit without crashing when user service succeeds', () => {
      // This test verifies the happy path doesn't throw
      expect(() => component.ngOnInit()).not.toThrow();
      expect(mockUserService.getById).toHaveBeenCalled();
    });
  });
});