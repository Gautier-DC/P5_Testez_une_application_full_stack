import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandler } from '@angular/common/http';
import { expect } from '@jest/globals';
import { of } from 'rxjs';

import { JwtInterceptor } from './jwt.interceptor';
import { SessionService } from '../services/session.service';

describe('JwtInterceptor', () => {
  let interceptor: JwtInterceptor;
  let mockSessionService: any;
  let mockHttpHandler: HttpHandler;
  let mockRequest: HttpRequest<any>;

  beforeEach(() => {
    mockSessionService = {
      isLogged: false,
      sessionInformation: {
        token: 'test-jwt-token-123',
        admin: true,
        id: 1,
        username: 'test@test.com',
        firstName: 'Test',
        lastName: 'User'
      }
    };

    mockHttpHandler = {
      handle: jest.fn().mockReturnValue(of({ data: 'test response' }))
    } as any;

    TestBed.configureTestingModule({
      providers: [
        JwtInterceptor,
        { provide: SessionService, useValue: mockSessionService }
      ]
    });

    interceptor = TestBed.inject(JwtInterceptor);
    mockRequest = new HttpRequest('GET', '/api/test');
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  // ==================== BRANCH COVERAGE TESTS ====================

  describe('intercept method', () => {
    it('should add Authorization header when user is logged', () => {
      // Arrange
      mockSessionService.isLogged = true;
      const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

      // Act
      const result = interceptor.intercept(mockRequest, { handle: mockHandler } as any);

      // Assert
      expect(result).toBeDefined();
      expect(mockHandler).toHaveBeenCalled();
      
      const capturedRequest = mockHandler.mock.calls[0][0];
      expect(capturedRequest.headers.get('Authorization')).toBe('Bearer test-jwt-token-123');
    });

    it('should NOT add Authorization header when user is not logged', () => {
      // Arrange
      mockSessionService.isLogged = false;
      const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

      // Act
      const result = interceptor.intercept(mockRequest, { handle: mockHandler } as any);

      // Assert
      expect(result).toBeDefined();
      expect(mockHandler).toHaveBeenCalledWith(mockRequest);
      
      const capturedRequest = mockHandler.mock.calls[0][0];
      expect(capturedRequest.headers.get('Authorization')).toBeNull();
    });

    it('should always call next.handle and return its result', () => {
      const expectedResponse = { data: 'test response', status: 200 };
      const mockHandler = jest.fn().mockReturnValue(of(expectedResponse));

      // Test with logged user
      mockSessionService.isLogged = true;
      let result1 = interceptor.intercept(mockRequest, { handle: mockHandler } as any);
      
      result1.subscribe(response => {
        expect(response).toEqual(expectedResponse);
      });
      
      expect(mockHandler).toHaveBeenCalled();

      // Reset and test with non-logged user
      mockHandler.mockClear();
      mockSessionService.isLogged = false;
      let result2 = interceptor.intercept(mockRequest, { handle: mockHandler } as any);
      
      result2.subscribe(response => {
        expect(response).toEqual(expectedResponse);
      });
      
      expect(mockHandler).toHaveBeenCalled();
    });
  });

  // ==================== INTEGRATION TESTS ====================

  describe('Integration scenarios', () => {
    it('should handle multiple requests with different authentication states', () => {
      const request1 = new HttpRequest('GET', '/api/endpoint1');
      const request2 = new HttpRequest('POST', '/api/endpoint2', { data: 'test' });
      const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

      // First request: not logged
      mockSessionService.isLogged = false;
      interceptor.intercept(request1, { handle: mockHandler } as any);

      // Second request: logged
      mockSessionService.isLogged = true;
      interceptor.intercept(request2, { handle: mockHandler } as any);

      // Assert
      expect(mockHandler).toHaveBeenCalledTimes(2);
      
      const firstRequest = mockHandler.mock.calls[0][0];
      const secondRequest = mockHandler.mock.calls[1][0];
      
      expect(firstRequest.headers.get('Authorization')).toBeNull();
      expect(secondRequest.headers.get('Authorization')).toBe('Bearer test-jwt-token-123');
    });

    it('should preserve original request properties when not logged', () => {
      const originalRequest = new HttpRequest('POST', '/api/test', { data: 'test' });
      mockSessionService.isLogged = false;
      const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

      interceptor.intercept(originalRequest, { handle: mockHandler } as any);

      const capturedRequest = mockHandler.mock.calls[0][0];
      expect(capturedRequest).toBe(originalRequest); // Should be same reference
      expect(capturedRequest.method).toBe('POST');
      expect(capturedRequest.url).toBe('/api/test');
      expect(capturedRequest.body).toEqual({ data: 'test' });
    });

    it('should preserve original request properties when logged and add auth header', () => {
      const originalRequest = new HttpRequest('PUT', '/api/update', { id: 1 });
      mockSessionService.isLogged = true;
      const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

      interceptor.intercept(originalRequest, { handle: mockHandler } as any);

      const capturedRequest = mockHandler.mock.calls[0][0];
      expect(capturedRequest).not.toBe(originalRequest); // Should be cloned
      expect(capturedRequest.method).toBe('PUT');
      expect(capturedRequest.url).toBe('/api/update');
      expect(capturedRequest.body).toEqual({ id: 1 });
      expect(capturedRequest.headers.get('Authorization')).toBe('Bearer test-jwt-token-123');
    });
  });

  // ==================== EDGE CASES ====================

  describe('Edge cases', () => {
    it('should handle different token values', () => {
      const testTokens = [
        'short-token',
        'very-long-jwt-token-with-many-characters',
        'token.with.dots',
        'token_with_underscores'
      ];

      testTokens.forEach(token => {
        mockSessionService.sessionInformation.token = token;
        mockSessionService.isLogged = true;
        const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

        interceptor.intercept(mockRequest, { handle: mockHandler } as any);

        const capturedRequest = mockHandler.mock.calls[0][0];
        expect(capturedRequest.headers.get('Authorization')).toBe(`Bearer ${token}`);
      });
    });

    it('should handle falsy isLogged values as not logged', () => {
      const falsyValues = [false, 0, '', null, undefined];
      
      falsyValues.forEach(value => {
        mockSessionService.isLogged = value;
        const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

        interceptor.intercept(mockRequest, { handle: mockHandler } as any);

        const capturedRequest = mockHandler.mock.calls[0][0];
        expect(capturedRequest.headers.get('Authorization')).toBeNull();
        expect(capturedRequest).toBe(mockRequest); // Should be original request
      });
    });

    it('should handle truthy isLogged values as logged', () => {
      const truthyValues = [true, 1, 'logged', {}, []];
      
      truthyValues.forEach(value => {
        mockSessionService.isLogged = value;
        const mockHandler = jest.fn().mockReturnValue(of({ data: 'response' }));

        interceptor.intercept(mockRequest, { handle: mockHandler } as any);

        const capturedRequest = mockHandler.mock.calls[0][0];
        expect(capturedRequest.headers.get('Authorization')).toBe('Bearer test-jwt-token-123');
      });
    });
  });

  // ==================== REQUEST MODIFICATION TESTS ====================

  describe('Request modification behavior', () => {
    it('should clone request only when adding authorization header', () => {
      const mockHandler = jest.fn().mockReturnValue(of({}));

      // When not logged, should return original request
      mockSessionService.isLogged = false;
      interceptor.intercept(mockRequest, { handle: mockHandler } as any);
      const notLoggedRequest = mockHandler.mock.calls[0][0];
      expect(notLoggedRequest).toBe(mockRequest); // Same reference

      mockHandler.mockClear();

      // When logged, should return cloned request
      mockSessionService.isLogged = true;
      interceptor.intercept(mockRequest, { handle: mockHandler } as any);
      const loggedRequest = mockHandler.mock.calls[0][0];
      expect(loggedRequest).not.toBe(mockRequest); // Different reference (cloned)
    });

    it('should not modify original request when cloning', () => {
      mockSessionService.isLogged = true;
      const mockHandler = jest.fn().mockReturnValue(of({}));

      interceptor.intercept(mockRequest, { handle: mockHandler } as any);

      // Original request should remain unchanged
      expect(mockRequest.headers.get('Authorization')).toBeNull();
    });

    it('should handle session information access correctly', () => {
      mockSessionService.isLogged = true;
      const mockHandler = jest.fn().mockReturnValue(of({}));

      // Should not throw when accessing sessionInformation.token
      expect(() => {
        interceptor.intercept(mockRequest, { handle: mockHandler } as any);
      }).not.toThrow();

      const capturedRequest = mockHandler.mock.calls[0][0];
      expect(capturedRequest.headers.get('Authorization')).toBe('Bearer test-jwt-token-123');
    });
  });
});