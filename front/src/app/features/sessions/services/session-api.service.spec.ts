import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';

import { SessionApiService } from './session-api.service';
import { Session } from '../interfaces/session.interface';

describe('SessionApiService', () => {
  let service: SessionApiService;
  let httpMock: HttpTestingController;

  const mockSession: Session = {
    id: 1,
    name: 'Test Session',
    description: 'Test Description',
    date: new Date(),
    teacher_id: 1,
    users: [1, 2],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SessionApiService]
    });
    service = TestBed.inject(SessionApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ==================== FUNCTION COVERAGE TESTS ====================

  describe('all method', () => {
    it('should fetch all sessions', () => {
      const mockSessions: Session[] = [mockSession, { ...mockSession, id: 2 }];

      service.all().subscribe(sessions => {
        expect(sessions).toEqual(mockSessions);
      });

      const req = httpMock.expectOne('api/session');
      expect(req.request.method).toBe('GET');
      req.flush(mockSessions);
    });
  });

  describe('detail method', () => {
    it('should fetch session detail by id', () => {
      const sessionId = '1';

      service.detail(sessionId).subscribe(session => {
        expect(session).toEqual(mockSession);
      });

      const req = httpMock.expectOne('api/session/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockSession);
    });
  });

  describe('delete method', () => {
    it('should delete session by id', () => {
      const sessionId = '1';

      service.delete(sessionId).subscribe(response => {
        expect(response).toEqual({});
      });

      const req = httpMock.expectOne('api/session/1');
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('create method', () => {
    it('should create a new session', () => {
      service.create(mockSession).subscribe(session => {
        expect(session).toEqual(mockSession);
      });

      const req = httpMock.expectOne('api/session');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockSession);
      req.flush(mockSession);
    });
  });

  describe('update method', () => {
    it('should update a session', () => {
      const sessionId = '1';
      const updatedSession = { ...mockSession, name: 'Updated Session' };

      service.update(sessionId, updatedSession).subscribe(session => {
        expect(session).toEqual(updatedSession);
      });

      const req = httpMock.expectOne('api/session/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updatedSession);
      req.flush(updatedSession);
    });
  });

  describe('participate method', () => {
    it('should add user to session', () => {
      const sessionId = '1';
      const userId = '2';

      service.participate(sessionId, userId).subscribe(response => {
        expect(response).toBeUndefined();
      });

      const req = httpMock.expectOne('api/session/1/participate/2');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeNull();
      req.flush(null);
    });
  });

  describe('unParticipate method', () => {
    it('should remove user from session', () => {
      const sessionId = '1';
      const userId = '2';

      service.unParticipate(sessionId, userId).subscribe(response => {
        expect(response).toBeUndefined();
      });

      const req = httpMock.expectOne('api/session/1/participate/2');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ==================== INTEGRATION TESTS ====================

  describe('Integration scenarios', () => {
    it('should handle multiple concurrent requests', () => {
      const sessions: Session[] = [mockSession];
      const sessionDetail = mockSession;

      service.all().subscribe(result => expect(result).toEqual(sessions));
      service.detail('1').subscribe(result => expect(result).toEqual(sessionDetail));

      const allReq = httpMock.expectOne('api/session');
      const detailReq = httpMock.expectOne('api/session/1');
      
      expect(allReq.request.method).toBe('GET');
      expect(detailReq.request.method).toBe('GET');
      
      allReq.flush(sessions);
      detailReq.flush(sessionDetail);
    });

    it('should handle CRUD operations sequence', () => {
      const newSession = { ...mockSession, id: undefined };
      const createdSession = { ...mockSession, id: 3 };
      const updatedSession = { ...createdSession, name: 'Updated' };

      // Create
      service.create(newSession as Session).subscribe(result => {
        expect(result).toEqual(createdSession);
      });
      
      const createReq = httpMock.expectOne('api/session');
      expect(createReq.request.method).toBe('POST');
      createReq.flush(createdSession);

      // Update
      service.update('3', updatedSession).subscribe(result => {
        expect(result).toEqual(updatedSession);
      });
      
      const updateReq = httpMock.expectOne('api/session/3');
      expect(updateReq.request.method).toBe('PUT');
      updateReq.flush(updatedSession);

      // Delete
      service.delete('3').subscribe(result => {
        expect(result).toEqual({});
      });
      
      const deleteReq = httpMock.expectOne('api/session/3');
      expect(deleteReq.request.method).toBe('DELETE');
      deleteReq.flush({});
    });
  });

  // ==================== EDGE CASES ====================

  describe('Edge cases', () => {
    it('should handle different session id formats', () => {
      const testIds = ['1', '123', 'abc123', '999999'];
      
      testIds.forEach(id => {
        service.detail(id).subscribe();
        const req = httpMock.expectOne(`api/session/${id}`);
        expect(req.request.method).toBe('GET');
        req.flush(mockSession);
      });
    });

    it('should handle empty session data', () => {
      const emptySessions: Session[] = [];
      
      service.all().subscribe(sessions => {
        expect(sessions).toEqual(emptySessions);
      });
      
      const req = httpMock.expectOne('api/session');
      req.flush(emptySessions);
    });

    it('should handle participation with different user id formats', () => {
      const userIds = ['1', '999', 'user123'];
      
      userIds.forEach(userId => {
        service.participate('1', userId).subscribe();
        const req = httpMock.expectOne(`api/session/1/participate/${userId}`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toBeNull();
        req.flush(null);
      });
    });
  });
});
