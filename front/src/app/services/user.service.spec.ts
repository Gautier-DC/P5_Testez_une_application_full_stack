import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { UserService } from './user.service';
import { User } from '../interfaces/user.interface';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // check that there are no outstanding requests
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(httpMock).toBeTruthy();
  });

  describe('getById', () => {
    it('should return user by id', () => {
      const mockUser: User = {
        id: 1,
        email: 'john.doe@test.com',
        firstName: 'John',
        lastName: 'Doe',
        admin: false,
        password: 'password123',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      service.getById('1').subscribe((user) => {
        expect(user).toEqual(mockUser);
      });

      const req = httpMock.expectOne('api/user/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockUser);
    });

    it('should handle error when user not found', () => {
      service.getById('999').subscribe({
        next: () => fail('should have failed with 404'),
        error: (error) => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpMock.expectOne('api/user/999');
      req.flush('User not found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle invalid id format', () => {
      service.getById('invalid-id').subscribe({
        error: (error) => expect(error.status).toBe(400),
      });

      const req = httpMock.expectOne('api/user/invalid-id');
      req.flush('Invalid ID format', {
        status: 400,
        statusText: 'Bad Request',
      });
    });
  });

  describe('delete', () => {
    it('should delete user by id', () => {
      service.delete('1').subscribe((response) => {
        expect(response).toBeNull(); // Assuming the API returns null on success
      });

      const req = httpMock.expectOne('api/user/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle error when deletion fails', () => {
      service.delete('999').subscribe({
        next: () => fail('should have failed with 404'),
        error: (error) => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpMock.expectOne('api/user/999');
      req.flush('User not found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle invalid id format on delete', () => {
      service.getById('invalid-id').subscribe({
        error: (error) => expect(error.status).toBe(400),
      });

      const req = httpMock.expectOne('api/user/invalid-id');
      req.flush('Invalid ID format', {
        status: 400,
        statusText: 'Bad Request',
      });
    });
  });
});
