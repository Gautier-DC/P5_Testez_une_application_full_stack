import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';
import { SessionService } from './session.service';
import { SessionInformation } from '../interfaces/sessionInformation.interface';

describe('SessionService', () => {
  let service: SessionService;
  
  // Mock data - similar to React test fixtures
  const mockUser: SessionInformation = {
    token: 'mock-jwt-token-user',
    type: 'Bearer',
    id: 1,
    username: 'testuser',
    firstName: 'John',
    lastName: 'Doe',
    admin: false
  };

  const mockAdmin: SessionInformation = {
    token: 'mock-jwt-token-admin',
    type: 'Bearer',
    id: 2,
    username: 'admin',
    firstName: 'Admin',
    lastName: 'User',
    admin: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SessionService);
  });

  describe('Initial state', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should initialize with logged out state', () => {
      expect(service.isLogged).toBe(false);
      expect(service.sessionInformation).toBeUndefined();
    });

    it('should emit false for initial $isLogged observable', (done) => {
      service.$isLogged().subscribe(isLogged => {
        expect(isLogged).toBe(false);
        done();
      });
    });
  });

  describe('logIn', () => {
    it('should set user information when logging in', () => {
      service.logIn(mockUser);
      
      expect(service.isLogged).toBe(true);
      expect(service.sessionInformation).toEqual(mockUser);
    });

    it('should emit true through $isLogged observable after login', (done) => {
      // Skip initial emission (false) and test the login emission
      let emissionCount = 0;
      
      service.$isLogged().subscribe(isLogged => {
        emissionCount++;
        if (emissionCount === 2) { // Second emission after login
          expect(isLogged).toBe(true);
          done();
        }
      });
      
      service.logIn(mockUser);
    });

    it('should handle admin user login correctly', () => {
      service.logIn(mockAdmin);
      
      expect(service.isLogged).toBe(true);
      expect(service.sessionInformation?.admin).toBe(true);
    });

    it('should overwrite previous session when logging in with different user', () => {
      // First login
      service.logIn(mockUser);
      expect(service.sessionInformation?.username).toBe('testuser');
      
      // Second login with different user
      service.logIn(mockAdmin);
      expect(service.sessionInformation?.username).toBe('admin');
      expect(service.sessionInformation?.admin).toBe(true);
    });
  });

  describe('logOut', () => {
    beforeEach(() => {
      // Setup: login first
      service.logIn(mockUser);
    });

    it('should clear user information when logging out', () => {
      service.logOut();
      
      expect(service.isLogged).toBe(false);
      expect(service.sessionInformation).toBeUndefined();
    });

    it('should emit false through $isLogged observable after logout', (done) => {
      // We subscribe AFTER login (which happened in beforeEach)
      // So we should immediately get 'true', then 'false' after logout
      let emissionCount = 0;
      
      service.$isLogged().subscribe(isLogged => {
        emissionCount++;
        if (emissionCount === 1) {
          // First emission should be true (current state after login)
          expect(isLogged).toBe(true);
        } else if (emissionCount === 2) {
          // Second emission should be false (after logout)
          expect(isLogged).toBe(false);
          done();
        }
      });
      
      service.logOut();
    });

    it('should handle logout when already logged out', () => {
      service.logOut(); // First logout
      service.logOut(); // Second logout
      
      expect(service.isLogged).toBe(false);
      expect(service.sessionInformation).toBeUndefined();
    });
  });

  describe('$isLogged Observable', () => {
    it('should emit current state to new subscribers', (done) => {
      // Login first
      service.logIn(mockUser);
      
      // New subscriber should immediately get the current state
      service.$isLogged().subscribe(isLogged => {
        expect(isLogged).toBe(true);
        done();
      });
    });

    it('should notify multiple subscribers', () => {
      const subscriber1 = jest.fn();
      const subscriber2 = jest.fn();
      
      service.$isLogged().subscribe(subscriber1);
      service.$isLogged().subscribe(subscriber2);
      
      service.logIn(mockUser);
      
      // Both subscribers should receive initial false + login true
      expect(subscriber1).toHaveBeenCalledTimes(2);
      expect(subscriber2).toHaveBeenCalledTimes(2);
      expect(subscriber1).toHaveBeenLastCalledWith(true);
      expect(subscriber2).toHaveBeenLastCalledWith(true);
    });

    it('should complete login/logout cycle correctly', () => {
      const emissions: boolean[] = [];
      
      service.$isLogged().subscribe(isLogged => {
        emissions.push(isLogged);
      });
      
      service.logIn(mockUser);
      service.logOut();
      
      expect(emissions).toEqual([false, true, false]);
    });
  });

  describe('Edge cases', () => {
    it('should handle undefined user in logIn gracefully', () => {
      service.logIn(undefined as any);
      
      expect(service.isLogged).toBe(true);
      expect(service.sessionInformation).toBeUndefined();
    });

    it('should handle partial user data', () => {
      const partialUser = { 
        token: 'partial-token',
        type: 'Bearer',
        id: 1, 
        username: 'partial' 
      } as SessionInformation;
      
      service.logIn(partialUser);
      
      expect(service.isLogged).toBe(true);
      expect(service.sessionInformation).toEqual(partialUser);
    });
  });
});