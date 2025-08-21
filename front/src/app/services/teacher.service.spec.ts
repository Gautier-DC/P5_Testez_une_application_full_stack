import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { TeacherService } from './teacher.service';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

describe('TeacherService', () => {
  let service: TeacherService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(TeacherService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // check that there are no outstanding requests
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(httpMock).toBeTruthy();
  });

  describe('get all teachers', () => {
    it('should return all teachers', () => {
      const mockTeachers = [
        {
          id: '1',
          firstName: 'John',
          lastName: 'Doe',
          createdAt: new Date(),
          updatedAt: new Date(),
        },
        {
          id: '2',
          firstName: 'Jane',
          lastName: 'Smith',
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      ];

      service.all().subscribe((teachers) => {
        expect(teachers).toEqual(mockTeachers);
      });

      const req = httpMock.expectOne('api/teacher');
      expect(req.request.method).toBe('GET');
      req.flush(mockTeachers);
    });

    it('should handle error when no teachers found', () => {
      service.all().subscribe({
        next: () => fail('should have failed with 404'),
        error: (error) => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpMock.expectOne('api/teacher');
      req.flush('No teachers found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle network errors gracefully', () => {
      service.all().subscribe({
        next: () => fail('should have failed with network error'),
        error: (error) => {
          expect(error.status).toBe(0);
          expect(error.statusText).toBe('Unknown Error');
        },
      });

      const req = httpMock.expectOne('api/teacher');
      req.flush(new ErrorEvent('Network error'));
    });
    
  });

  describe('get teacher details', () => {
    it('should return teacher by id', () => {
      const mockTeacher = {
        id: '1',
        firstName: 'John',
        lastName: 'Doe',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      service.detail('1').subscribe((teacher) => {
        expect(teacher).toEqual(mockTeacher);
      });

      const req = httpMock.expectOne('api/teacher/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockTeacher);
    });

    it('should handle error when teacher not found', () => {
      service.detail('999').subscribe({
        next: () => fail('should have failed with 404'),
        error: (error) => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpMock.expectOne('api/teacher/999');
      req.flush('Teacher not found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle invalid id format', () => {
      service.detail('invalid-id').subscribe({
        error: (error) => expect(error.status).toBe(400),
      });

      const req = httpMock.expectOne('api/teacher/invalid-id');
      req.flush('Invalid ID format', {
        status: 400,
        statusText: 'Bad Request',
      });
    });
  });
});
