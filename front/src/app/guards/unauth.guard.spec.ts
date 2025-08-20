import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { expect } from '@jest/globals';

import { UnauthGuard } from './unauth.guard';
import { SessionService } from '../services/session.service';

describe('UnauthGuard', () => {
  let guard: UnauthGuard;
  let mockSessionService: any;
  let mockRouter: any;

  beforeEach(() => {
    mockSessionService = {
      isLogged: false
    };

    mockRouter = {
      navigate: jest.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        UnauthGuard,
        { provide: SessionService, useValue: mockSessionService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    guard = TestBed.inject(UnauthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  // ==================== BRANCH COVERAGE TESTS ====================

  describe('canActivate method', () => {
    it('should return false and navigate to rentals when user is logged', () => {
      // Arrange
      mockSessionService.isLogged = true;

      // Act  
      const result = guard.canActivate();

      // Assert
      expect(result).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);
    });

    it('should return true when user is not logged', () => {
      // Arrange
      mockSessionService.isLogged = false;

      // Act
      const result = guard.canActivate();

      // Assert
      expect(result).toBeTruthy();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });

  // ==================== INTEGRATION TESTS ====================

  describe('Integration scenarios', () => {
    it('should handle multiple consecutive calls with different logged states', () => {
      // First call: logged (should redirect)
      mockSessionService.isLogged = true;
      let result1 = guard.canActivate();
      expect(result1).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);

      // Reset mock
      mockRouter.navigate.mockClear();

      // Second call: not logged (should allow)
      mockSessionService.isLogged = false;
      let result2 = guard.canActivate();
      expect(result2).toBeTruthy();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should always check current session state', () => {
      // Multiple calls with logged state
      mockSessionService.isLogged = true;
      
      guard.canActivate();
      guard.canActivate();
      guard.canActivate();

      expect(mockRouter.navigate).toHaveBeenCalledTimes(3);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);
    });

    it('should prevent logged users from accessing unauthenticated routes', () => {
      mockSessionService.isLogged = true;

      const result = guard.canActivate();

      expect(result).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);
    });
  });

  // ==================== EDGE CASES ====================

  describe('Edge cases', () => {
    it('should handle truthy values as logged (redirect to rentals)', () => {
      // Test with different truthy values
      const truthyValues = [true, 1, 'logged', {}, []];
      
      truthyValues.forEach(value => {
        mockSessionService.isLogged = value;
        mockRouter.navigate.mockClear();
        
        const result = guard.canActivate();
        expect(result).toBeFalsy();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);
      });
    });

    it('should handle falsy values as not logged (allow access)', () => {
      // Test with different falsy values
      const falsyValues = [false, 0, '', null, undefined];
      
      falsyValues.forEach(value => {
        mockSessionService.isLogged = value;
        mockRouter.navigate.mockClear();
        
        const result = guard.canActivate();
        expect(result).toBeTruthy();
        expect(mockRouter.navigate).not.toHaveBeenCalled();
      });
    });

    it('should handle session state changes during navigation', () => {
      // Start not logged
      mockSessionService.isLogged = false;
      let result1 = guard.canActivate();
      expect(result1).toBeTruthy();

      // Change to logged
      mockSessionService.isLogged = true;
      let result2 = guard.canActivate();
      expect(result2).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);

      // Back to not logged
      mockRouter.navigate.mockClear();
      mockSessionService.isLogged = false;
      let result3 = guard.canActivate();
      expect(result3).toBeTruthy();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });

  // ==================== SPECIFIC UNAUTH GUARD BEHAVIOR ====================

  describe('UnauthGuard specific behavior', () => {
    it('should allow access to login/register pages when not authenticated', () => {
      mockSessionService.isLogged = false;

      const result = guard.canActivate();

      expect(result).toBeTruthy();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should prevent already authenticated users from accessing login/register', () => {
      mockSessionService.isLogged = true;

      const result = guard.canActivate();

      expect(result).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);
    });

    it('should redirect to rentals (not login) when user is already authenticated', () => {
      mockSessionService.isLogged = true;

      guard.canActivate();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['rentals']);
      expect(mockRouter.navigate).not.toHaveBeenCalledWith(['login']);
    });
  });
});