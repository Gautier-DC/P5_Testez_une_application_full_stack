import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { expect } from '@jest/globals';

import { AuthGuard } from './auth.guard';
import { SessionService } from '../services/session.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
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
        AuthGuard,
        { provide: SessionService, useValue: mockSessionService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    guard = TestBed.inject(AuthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  // ==================== BRANCH COVERAGE TESTS ====================

  describe('canActivate method', () => {
    it('should return false and navigate to login when user is not logged', () => {
      // Arrange
      mockSessionService.isLogged = false;

      // Act  
      const result = guard.canActivate();

      // Assert
      expect(result).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['login']);
    });

    it('should return true when user is logged', () => {
      // Arrange
      mockSessionService.isLogged = true;

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
      // First call: not logged
      mockSessionService.isLogged = false;
      let result1 = guard.canActivate();
      expect(result1).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['login']);

      // Reset mock
      mockRouter.navigate.mockClear();

      // Second call: logged
      mockSessionService.isLogged = true;
      let result2 = guard.canActivate();
      expect(result2).toBeTruthy();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should always check current session state', () => {
      // Multiple calls with not logged state
      mockSessionService.isLogged = false;
      
      guard.canActivate();
      guard.canActivate();
      guard.canActivate();

      expect(mockRouter.navigate).toHaveBeenCalledTimes(3);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['login']);
    });
  });

  // ==================== EDGE CASES ====================

  describe('Edge cases', () => {
    it('should handle undefined/null isLogged state as not logged', () => {
      // Test with undefined
      mockSessionService.isLogged = undefined;
      let result1 = guard.canActivate();
      expect(result1).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['login']);

      mockRouter.navigate.mockClear();

      // Test with null
      mockSessionService.isLogged = null;
      let result2 = guard.canActivate();
      expect(result2).toBeFalsy();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['login']);
    });

    it('should handle truthy values as logged', () => {
      // Test with different truthy values
      const truthyValues = [true, 1, 'logged', {}, []];
      
      truthyValues.forEach(value => {
        mockSessionService.isLogged = value;
        mockRouter.navigate.mockClear();
        
        const result = guard.canActivate();
        expect(result).toBeTruthy();
        expect(mockRouter.navigate).not.toHaveBeenCalled();
      });
    });

    it('should handle falsy values as not logged', () => {
      // Test with different falsy values
      const falsyValues = [false, 0, '', null, undefined];
      
      falsyValues.forEach(value => {
        mockSessionService.isLogged = value;
        mockRouter.navigate.mockClear();
        
        const result = guard.canActivate();
        expect(result).toBeFalsy();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['login']);
      });
    });
  });
});