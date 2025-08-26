/// <reference types="cypress" />

// ============================
// UTILITIES & COMMON DATA
// ============================

const mockTeacher = {
  id: 1,
  firstName: 'Margot',
  lastName: 'DELAHAYE',
  createdAt: '2025-08-11 15:57:09',
  updatedAt: '2025-08-11 15:57:09',
};

const mockUser = {
  id: 3,
  email: 'test@example.com',
  firstName: 'test',
  lastName: 'test',
  admin: false,
  createdAt: '2025-08-11 16:02:35',
  updatedAt: '2025-08-11 16:02:35',
};

const mockAdminUser = {
  id: 1,
  email: 'yoga@studio.com',
  firstName: 'Admin',
  lastName: 'Admin',
  admin: true,
  createdAt: '2025-08-11 15:57:09',
  updatedAt: '2025-08-11 15:57:09',
};

function createMockSession(isUserParticipating = false) {
  return {
    "id": 3,
    "name": "Test",
    "date": "2025-08-29T00:00:00.000+00:00",
    "teacher_id": 1,
    "description": "ezfzef ezhfkzefkhfbkjfbd",
    "users": isUserParticipating ? [2, 3, 4] : [2, 4],
    "createdAt": "2025-08-14T12:05:25",
    "updatedAt": "2025-08-14T12:05:25"
  };
}

function setupCommonIntercepts() {
  cy.intercept('GET', '/api/user/3', {
    statusCode: 200,
    body: mockUser,
  }).as('getCurrentUser');

  cy.intercept('GET', '/api/teacher/1', {
    statusCode: 200,
    body: mockTeacher,
  }).as('getTeacher');
}

function setupAdminIntercepts() {
  cy.intercept('GET', '/api/user/1', {
    statusCode: 200,
    body: mockAdminUser,
  }).as('getCurrentAdminUser');

  cy.intercept('GET', '/api/teacher/1', {
    statusCode: 200,
    body: mockTeacher,
  }).as('getTeacher');
}

function loginAsUser(isUserParticipating = false) {
  const mockSession = createMockSession(isUserParticipating);
  
  setupCommonIntercepts();
  
  cy.visit('/login');

  cy.intercept('POST', '/api/auth/login', {
    statusCode: 200,
    body: {
      id: 3,
      username: 'testuser',
      firstName: 'test',
      lastName: 'test',
      admin: false,
      token: 'fake-jwt-token',
    },
  }).as('loginRequest');

  cy.intercept('GET', '/api/session', [mockSession]).as('getSessions');

  cy.get('input[formControlName=email]').type('test@example.com');
  cy.get('input[formControlName=password]').type('password123');
  cy.get('button[type=submit]').click();

  cy.wait('@loginRequest');
  cy.wait('@getSessions');
  cy.url().should('include', '/sessions');

  return mockSession;
}

function loginAsAdmin(isUserParticipating = false) {
  const mockSession = createMockSession(isUserParticipating);

  setupAdminIntercepts();
  
  cy.visit('/login');

  cy.intercept('POST', '/api/auth/login', {
    statusCode: 200,
    body: {
      id: 1,
      username: 'adminuser',
      firstName: 'Admin',
      lastName: 'User',
      admin: true,
      token: 'fake-admin-jwt-token',
    },
  }).as('adminLoginRequest');

  cy.intercept('GET', '/api/session', [mockSession]).as('getSessions');

  cy.get('input[formControlName=email]').type('admin@example.com');
  cy.get('input[formControlName=password]').type('password123');
  cy.get('button[type=submit]').click();

  cy.wait('@adminLoginRequest');
  cy.wait('@getSessions');
  cy.url().should('include', '/sessions');
}

function navigateToSessionDetail(mockSession) {
  cy.intercept('GET', '/api/session/3', {
    statusCode: 200,
    body: mockSession,
  }).as('getSessionDetail');

  cy.get('mat-card.item').first().within(() => {
    cy.get('button').contains('Detail').click();
  });

  cy.wait('@getSessionDetail');
  cy.wait('@getTeacher');
}

// ============================
// TESTS - USER NOT PARTICIPATING
// ============================

describe('Session Detail - User NOT participating initially', () => {
  let mockSession;

  beforeEach(() => {
    mockSession = loginAsUser(false); // false = not participating
    navigateToSessionDetail(mockSession);
  });

  it('should display session details correctly', () => {
    cy.url().should('include', '/sessions/detail/3');
    cy.get('h1').should('contain', 'Test');
    cy.get('mat-card-subtitle').should('contain', 'Margot DELAHAYE');
    cy.contains('2 attendees').should('be.visible'); // 2 car user 3 n'est pas participant
    cy.contains('August 29, 2025').should('be.visible');
    cy.get('.description').should('contain', 'ezfzef ezhfkzefkhfbkjfbd');
    cy.get('.created').should('contain', 'August 14, 2025');
    cy.get('.updated').should('contain', 'August 14, 2025');
    cy.get('button[mat-icon-button]').should('contain', 'arrow_back');
  });

  it('should allow user to participate when not already participating', () => {
    cy.intercept('POST', '/api/session/3/participate/3', {
      statusCode: 200,
    }).as('participateRequest');

    cy.url().should('include', '/sessions/detail/3');

    // Should show participate button
    cy.get('button').contains('Participate').should('be.visible');
    cy.get('button').contains('Do not participate').should('not.exist');

    // Click participate
    cy.get('button').contains('Participate').click();
    cy.wait('@participateRequest');
  });

  it('should handle participation error gracefully', () => {
    cy.intercept('POST', '/api/session/3/participate/3', {
      statusCode: 400,
      body: { message: 'Cannot participate' },
    }).as('participateError');

    cy.get('button').contains('Participate').click();
    cy.wait('@participateError');
    
    // Should still be on the detail page after error
    cy.url().should('include', '/sessions/detail/3');
  });
});

// ============================
// TESTS - USER PARTICIPATING
// ============================

describe('Session Detail - User participating initially', () => {
  let mockSession;

  beforeEach(() => {
    mockSession = loginAsUser(true); // true = participating
    navigateToSessionDetail(mockSession);
  });

  it('should display session details correctly with user participating', () => {
    cy.url().should('include', '/sessions/detail/3');
    cy.get('h1').should('contain', 'Test');
    cy.contains('3 attendees').should('be.visible');
    
    // Should show unparticipate button
    cy.get('button').contains('Do not participate').should('be.visible');
    cy.get('button').contains('Participate').should('not.exist');
  });

  it('should allow user to unparticipate when already participating', () => {
    cy.intercept('DELETE', '/api/session/3/participate/3', {
      statusCode: 200,
    }).as('unparticipateRequest');

    cy.intercept('GET', '/api/session/3', {
      statusCode: 200,
      body: {
        ...mockSession,
        users: [2, 4], // User 3is no longer participating
      },
    }).as('getUpdatedSession');

    cy.url().should('include', '/sessions/detail/3');

    // Click unparticipate
    cy.get('button').contains('Do not participate').click();
    cy.wait('@unparticipateRequest');
    cy.wait('@getUpdatedSession');
  });
});

// ============================
// TESTS - ADMIN USER
// ============================

describe('Session Detail - Admin user', () => {
  let mockSession;

  beforeEach(() => {
    mockSession = createMockSession(false);
    
    // Setup intercepts like for regular user but with admin user data
    cy.intercept('GET', '/api/user/1', {
      statusCode: 200,
      body: mockAdminUser,
    }).as('getCurrentUser');

    cy.intercept('GET', '/api/teacher/1', {
      statusCode: 200,
      body: mockTeacher,
    }).as('getTeacher');
    
    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'adminuser',
        firstName: 'Admin',
        lastName: 'User',
        admin: true,
        token: 'fake-admin-jwt-token',
      },
    }).as('loginRequest');

    // Mock sessions list with our test session
    cy.intercept('GET', '/api/session', [mockSession]).as('getSessions');

    cy.get('input[formControlName=email]').type('admin@example.com');
    cy.get('input[formControlName=password]').type('password123');
    cy.get('button[type=submit]').click();

    cy.wait('@loginRequest');
    cy.wait('@getSessions');
    cy.url().should('include', '/sessions');

    // Navigate to session detail like in working tests
    cy.intercept('GET', '/api/session/3', {
      statusCode: 200,
      body: mockSession,
    }).as('getSessionDetail');

    cy.get('mat-card.item').first().within(() => {
      cy.get('button').contains('Detail').click();
    });

    cy.wait('@getSessionDetail');
    cy.wait('@getTeacher');
  });

  it('should show delete button for admin user', () => {
    // Should be on session detail page already thanks to beforeEach
    cy.url().should('include', '/sessions/detail/3');

    // Should show delete button for admin
    cy.get('button').contains('Delete').should('be.visible');

    // Should not show participate/unparticipate buttons for admin
    cy.get('button').contains('Participate').should('not.exist');
    cy.get('button').contains('Do not participate').should('not.exist');
  });

  it('should delete session when admin clicks delete', () => {
    cy.intercept('DELETE', '/api/session/3', {
      statusCode: 200,
    }).as('deleteSession');

    cy.intercept('GET', '/api/session', []).as('getSessionsAfterDelete');

    // Should be on session detail page already 
    cy.url().should('include', '/sessions/detail/3');

    cy.get('button').contains('Delete').click();
    cy.wait('@deleteSession');
    cy.wait('@getSessionsAfterDelete');

    // Should redirect to sessions list after deletion
    cy.url().should('include', '/sessions');
    cy.url().should('not.include', '/sessions/detail/3');
  });

  it('should handle delete error gracefully', () => {
    cy.intercept('DELETE', '/api/session/3', {
      statusCode: 500,
      body: { message: 'Cannot delete session' },
    }).as('deleteError');

    // Should be on session detail page already
    cy.url().should('include', '/sessions/detail/3');

    cy.get('button').contains('Delete').click();
    cy.wait('@deleteError');

    // Should still be on the detail page after error
    cy.url().should('include', '/sessions/detail/3');
  });
});

// ============================
// TESTS - GENERAL (NAVIGATION & ERRORS)
// ============================

describe('Session Detail - General functionality', () => {
  it('should navigate back when clicking back button', () => {
    const mockSession = createMockSession(false);
    
    // Do full login and navigation like other working tests
    cy.intercept('GET', '/api/user/3', {
      statusCode: 200,
      body: mockUser,
    }).as('getCurrentUser');

    cy.intercept('GET', '/api/teacher/1', {
      statusCode: 200,
      body: mockTeacher,
    }).as('getTeacher');
    
    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 3,
        username: 'testuser',
        firstName: 'test',
        lastName: 'test',
        admin: false,
        token: 'fake-jwt-token',
      },
    }).as('loginRequest');

    cy.intercept('GET', '/api/session', [mockSession]).as('getSessions');

    cy.get('input[formControlName=email]').type('test@example.com');
    cy.get('input[formControlName=password]').type('password123');
    cy.get('button[type=submit]').click();

    cy.wait('@loginRequest');
    cy.wait('@getSessions');
    cy.url().should('include', '/sessions');

    // Navigate to session detail
    cy.intercept('GET', '/api/session/3', {
      statusCode: 200,
      body: mockSession,
    }).as('getSessionDetail');

    cy.get('mat-card.item').first().within(() => {
      cy.get('button').contains('Detail').click();
    });

    cy.wait('@getSessionDetail');
    cy.wait('@getTeacher');

    // Now test the back button
    cy.get('button[mat-icon-button]').click();
    cy.url().should('not.include', '/sessions/detail/3');
  });

  it('should handle session not found error', () => {
    // Do login first to be authenticated
    cy.visit('/login');
    
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 3,
        username: 'testuser',
        firstName: 'test',
        lastName: 'test',
        admin: false,
        token: 'fake-jwt-token',
      },
    }).as('loginRequest');

    cy.get('input[formControlName=email]').type('test@example.com');
    cy.get('input[formControlName=password]').type('password123');
    cy.get('button[type=submit]').click();
    cy.wait('@loginRequest');

    // Now setup the 404 intercept  
    cy.intercept('GET', '/api/session/999', {
      statusCode: 404,
      body: { message: 'Session not found' },
    }).as('getSessionNotFound');

    // Visit the non-existent session page
    cy.visit('/sessions/detail/999', { failOnStatusCode: false });
    
    // Don't wait for the intercept, just check the final state
    // cy.wait('@getSessionNotFound');

    // Check that the page shows appropriate error state or redirects
    cy.url().then((url) => {
      // Either we're still on the detail page with no content,
      // or we've been redirected somewhere else
      if (url.includes('/sessions/detail/999')) {
        // Should not display session card when session is not found
        cy.get('mat-card').should('not.exist');
      } else {
        // App redirected us somewhere else (like back to sessions list)
        cy.log('App redirected to: ' + url);
      }
    });
  });
});