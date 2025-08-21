import { HttpClientModule } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';
import { of } from 'rxjs';

import { AppComponent } from './app.component';
import { AuthService } from './features/auth/services/auth.service';
import { SessionService } from './services/session.service';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: any;
  let mockAuthService: any;
  let mockSessionService: any;
  let mockRouter: any;

  beforeEach(async () => {
    mockAuthService = {
      // Add any auth service methods if needed
    };

    mockSessionService = {
      $isLogged: jest.fn(),
      logOut: jest.fn()
    };

    mockRouter = {
      navigate: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientModule,
        MatToolbarModule
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: SessionService, useValue: mockSessionService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  // ==================== FUNCTION COVERAGE TESTS ====================

  describe('$isLogged method', () => {
    it('should return observable from sessionService', () => {
      const mockObservable = of(true);
      mockSessionService.$isLogged.mockReturnValue(mockObservable);

      const result = component.$isLogged();

      expect(result).toBe(mockObservable);
      expect(mockSessionService.$isLogged).toHaveBeenCalled();
    });

    it('should handle different logged states', () => {
      // Test logged state
      mockSessionService.$isLogged.mockReturnValue(of(true));
      component.$isLogged().subscribe(isLogged => {
        expect(isLogged).toBeTruthy();
      });

      // Test not logged state
      mockSessionService.$isLogged.mockReturnValue(of(false));
      component.$isLogged().subscribe(isLogged => {
        expect(isLogged).toBeFalsy();
      });

      expect(mockSessionService.$isLogged).toHaveBeenCalledTimes(2);
    });
  });

  describe('logout method', () => {
    it('should call sessionService.logOut and navigate to home', () => {
      component.logout();

      expect(mockSessionService.logOut).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });

    it('should perform complete logout sequence', () => {
      // Ensure clean state
      expect(mockSessionService.logOut).not.toHaveBeenCalled();
      expect(mockRouter.navigate).not.toHaveBeenCalled();

      // Perform logout
      component.logout();

      // Verify both actions occurred
      expect(mockSessionService.logOut).toHaveBeenCalledTimes(1);
      expect(mockRouter.navigate).toHaveBeenCalledTimes(1);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });

    it('should handle multiple logout calls', () => {
      component.logout();
      component.logout();
      component.logout();

      expect(mockSessionService.logOut).toHaveBeenCalledTimes(3);
      expect(mockRouter.navigate).toHaveBeenCalledTimes(3);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });
  });

  // ==================== INTEGRATION TESTS ====================

  describe('Integration scenarios', () => {
    it('should handle login state check and logout in sequence', () => {
      // Check login state
      mockSessionService.$isLogged.mockReturnValue(of(true));
      component.$isLogged().subscribe(isLogged => {
        expect(isLogged).toBeTruthy();
      });

      // Perform logout
      component.logout();

      expect(mockSessionService.$isLogged).toHaveBeenCalled();
      expect(mockSessionService.logOut).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });

    it('should work with different authentication states', () => {
      const authStates = [true, false, true, false];
      
      authStates.forEach(state => {
        mockSessionService.$isLogged.mockReturnValue(of(state));
        component.$isLogged().subscribe(isLogged => {
          expect(isLogged).toBe(state);
        });
      });

      expect(mockSessionService.$isLogged).toHaveBeenCalledTimes(authStates.length);
    });
  });
});
