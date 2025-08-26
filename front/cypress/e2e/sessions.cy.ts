/// <reference types="cypress" />

describe('Sessions spec', () => {
  beforeEach(() => {
    // Login as regular user
    cy.visit('/login')
    
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'testuser',
        firstName: 'Test',
        lastName: 'User',
        admin: false
      },
    }).as('loginRequest')

    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: [
        {
          id: 1,
          name: 'Morning Yoga',
          description: 'Start your day with relaxing yoga',
          date: '2024-12-25T10:00:00.000Z',
          teacher_id: 1,
          users: [2, 3],
          createdAt: '2024-01-01T00:00:00.000Z',
          updatedAt: '2024-01-01T00:00:00.000Z'
        },
        {
          id: 2,
          name: 'Evening Meditation',
          description: 'End your day with peaceful meditation',
          date: '2024-12-26T18:00:00.000Z',
          teacher_id: 2,
          users: [1, 4],
          createdAt: '2024-01-01T00:00:00.000Z',
          updatedAt: '2024-01-01T00:00:00.000Z'
        }
      ]
    }).as('getSessions')

    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('password123')
    cy.get('button[type=submit]').click()

    cy.wait('@loginRequest')
    cy.wait('@getSessions')
  })

  it('should display sessions list', () => {
    cy.url().should('include', '/sessions')
    
    // Check page title
    cy.contains('Rentals available')
    
    // Check sessions are displayed
    cy.get('mat-card.item').should('have.length', 2)
    
    // Check first session details
    cy.get('mat-card.item').first().within(() => {
      cy.get('mat-card-title').should('contain', 'Morning Yoga')
      cy.get('mat-card-subtitle').should('contain', 'December 25, 2024')
      cy.get('mat-card-content p').should('contain', 'Start your day with relaxing yoga')
      cy.get('button').contains('Detail').should('be.visible')
    })
    
    // Check second session details
    cy.get('mat-card.item').eq(1).within(() => {
      cy.get('mat-card-title').should('contain', 'Evening Meditation')
      cy.get('mat-card-subtitle').should('contain', 'December 26, 2024')
      cy.get('mat-card-content p').should('contain', 'End your day with peaceful meditation')
      cy.get('button').contains('Detail').should('be.visible')
    })
  })

  it('should not display create button for non-admin user', () => {
    cy.get('button').contains('Create').should('not.exist')
  })

  it('should not display edit buttons for non-admin user', () => {
    cy.get('button').contains('Edit').should('not.exist')
  })

  it('should navigate to session detail when clicking Detail button', () => {
    cy.intercept('GET', '/api/session/1', {
      statusCode: 200,
      body: {
        id: 1,
        name: 'Morning Yoga',
        description: 'Start your day with relaxing yoga sessions. Perfect for beginners and advanced practitioners.',
        date: '2024-12-25T10:00:00.000Z',
        teacher_id: 1,
        users: [2, 3],
        createdAt: '2024-01-01T00:00:00.000Z',
        updatedAt: '2024-01-01T00:00:00.000Z'
      }
    }).as('getSessionDetail')

    cy.intercept('GET', '/api/teacher/1', {
      statusCode: 200,
      body: {
        id: 1,
        firstName: 'John',
        lastName: 'Doe',
        createdAt: '2024-01-01T00:00:00.000Z',
        updatedAt: '2024-01-01T00:00:00.000Z'
      }
    }).as('getTeacher')

    cy.get('mat-card.item').first().within(() => {
      cy.get('button').contains('Detail').click()
    })

    cy.wait('@getSessionDetail')
    cy.wait('@getTeacher')
    cy.url().should('include', '/sessions/detail/1')
  })

})

describe('Sessions spec - Edge cases', () => {
  it('should handle empty sessions list', () => {
    // Login first
    cy.visit('/login')
    
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'testuser',
        firstName: 'Test',
        lastName: 'User',
        admin: false
      },
    }).as('loginRequest')

    // Setup empty sessions intercept
    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: []
    }).as('getEmptySessions')

    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('password123')
    cy.get('button[type=submit]').click()

    cy.wait('@loginRequest')
    cy.wait('@getEmptySessions')

    cy.contains('Rentals available')
    cy.get('mat-card.item').should('not.exist')
  })

  it('should handle sessions loading error', () => {
    // Login first
    cy.visit('/login')
    
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'testuser',
        firstName: 'Test',
        lastName: 'User',
        admin: false
      },
    }).as('loginRequest')

    // Setup error intercept
    cy.intercept('GET', '/api/session', {
      statusCode: 500,
      body: {
        message: 'Internal server error'
      }
    }).as('getSessionsError')

    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('password123')
    cy.get('button[type=submit]').click()

    cy.wait('@loginRequest')
    cy.wait('@getSessionsError')

    // Should still show the page structure even with error
    cy.contains('Rentals available')
  })
})

describe('Sessions spec - Admin user', () => {
  beforeEach(() => {
    // Login as admin user
    cy.visit('/login')
    
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'adminuser',
        firstName: 'Admin',
        lastName: 'User',
        admin: true
      },
    }).as('adminLoginRequest')

    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: [
        {
          id: 1,
          name: 'Morning Yoga',
          description: 'Start your day with relaxing yoga',
          date: '2024-12-25T10:00:00.000Z',
          teacher_id: 1,
          users: [2, 3],
          createdAt: '2024-01-01T00:00:00.000Z',
          updatedAt: '2024-01-01T00:00:00.000Z'
        }
      ]
    }).as('getAdminSessions')

    cy.get('input[formControlName=email]').type('admin@example.com')
    cy.get('input[formControlName=password]').type('password123')
    cy.get('button[type=submit]').click()

    cy.wait('@adminLoginRequest')
    cy.wait('@getAdminSessions')
  })

  it('should display create button for admin user', () => {
    cy.get('button[routerlink="create"]').should('be.visible')
    cy.get('button[routerlink="create"]').should('contain', 'Create')
  })

  it('should display edit buttons for admin user', () => {
    cy.get('mat-card.item').within(() => {
      cy.get('button').contains('Edit').should('be.visible')
    })
  })

  it('should navigate to create form when clicking Create button', () => {
    cy.get('button').contains('Create').click()
    cy.url().should('include', '/sessions/create')
  })

  it('should navigate to edit form when clicking Edit button', () => {
    cy.get('mat-card.item').within(() => {
      cy.get('button').contains('Edit').click()
    })
    cy.url().should('include', '/sessions/update/1')
  })
})