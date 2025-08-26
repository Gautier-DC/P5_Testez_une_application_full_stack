describe('Login spec', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('should display login form', () => {
    cy.contains('Login')
    cy.get('input[formControlName=email]').should('be.visible')
    cy.get('input[formControlName=password]').should('be.visible')
    cy.get('button[type=submit]').should('be.visible').and('be.disabled')
  })

  it('should enable submit button when form is valid', () => {
    cy.get('input[formControlName=email]').type('user@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    cy.get('button[type=submit]').should('not.be.disabled')
  })

  it('should login successfully with valid credentials', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'userName',
        firstName: 'firstName',
        lastName: 'lastName',
        admin: true
      },
    }).as('loginRequest')

    cy.intercept('GET', '/api/session', []).as('session')

    cy.get('input[formControlName=email]').type('yoga@studio.com')
    cy.get('input[formControlName=password]').type('test!1234')
    cy.get('button[type=submit]').click()

    cy.wait('@loginRequest')
    cy.wait('@session')
    cy.url().should('include', '/sessions')
  })

  it('should display error when login fails', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: {
        message: 'Invalid credentials'
      }
    }).as('loginError')

    cy.get('input[formControlName=email]').type('wrong@example.com')
    cy.get('input[formControlName=password]').type('wrongpassword')
    cy.get('button[type=submit]').click()

    cy.wait('@loginError')
    cy.get('.error').should('be.visible').and('contain', 'An error occurred')
  })

  it('should validate required fields', () => {
    // Submit button should be disabled with empty form
    cy.get('button[type=submit]').should('be.disabled')

    // Fill only email
    cy.get('input[formControlName=email]').type('user@example.com')
    cy.get('button[type=submit]').should('be.disabled')

    // Fill both email and password
    cy.get('input[formControlName=password]').type('password123')
    cy.get('button[type=submit]').should('not.be.disabled')
  })

  it('should validate email format', () => {
    cy.get('input[formControlName=password]').type('password123')

    // Invalid email format
    cy.get('input[formControlName=email]').type('invalid-email')
    cy.get('button[type=submit]').should('be.disabled')

    // Valid email format
    cy.get('input[formControlName=email]').clear().type('user@example.com')
    cy.get('button[type=submit]').should('not.be.disabled')
  })

  it('should toggle password visibility', () => {
    cy.get('input[formControlName=password]').should('have.attr', 'type', 'password')
    
    // Click the visibility toggle button
    cy.get('button[matSuffix]').click()
    cy.get('input[formControlName=password]').should('have.attr', 'type', 'text')
    
    // Click again to hide
    cy.get('button[matSuffix]').click()
    cy.get('input[formControlName=password]').should('have.attr', 'type', 'password')
  })

  it('should show correct visibility icon', () => {
    // Initially should show visibility_off icon (password hidden)
    cy.get('mat-icon').should('contain', 'visibility_off')
    
    // After clicking, should show visibility icon (password visible)
    cy.get('button[matSuffix]').click()
    cy.get('mat-icon').should('contain', 'visibility')
    
    // After clicking again, should show visibility_off icon
    cy.get('button[matSuffix]').click()
    cy.get('mat-icon').should('contain', 'visibility_off')
  })

  it('should handle server error gracefully', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 500,
      body: {
        message: 'Internal server error'
      }
    }).as('serverError')

    cy.get('input[formControlName=email]').type('user@example.com')
    cy.get('input[formControlName=password]').type('password123')
    cy.get('button[type=submit]').click()

    cy.wait('@serverError')
    cy.get('.error').should('be.visible')
  })

  it('should navigate to register page when clicking register link', () => {
    cy.get('span[routerlink="register"]').should('exist').click()
    cy.url().should('include', '/register')
    cy.contains('Register').should('be.visible')
  })

  it('should maintain form state when navigating away and back', () => {
    cy.get('input[formControlName=email]').type('user@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    // Navigate away
    cy.visit('/sessions')
    
    // Navigate back
    cy.visit('/login')
    
    // Form should be cleared
    cy.get('input[formControlName=email]').should('have.value', '')
    cy.get('input[formControlName=password]').should('have.value', '')
  })
});