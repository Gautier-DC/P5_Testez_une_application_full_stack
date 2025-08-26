describe('Register spec', () => {
  beforeEach(() => {
    cy.visit('/register')
  })

  it('should display register form', () => {
    cy.contains('Register')
    cy.get('input[formControlName=firstName]').should('be.visible')
    cy.get('input[formControlName=lastName]').should('be.visible')
    cy.get('input[formControlName=email]').should('be.visible')
    cy.get('input[formControlName=password]').should('be.visible')
    cy.get('button[type=submit]').should('be.visible').and('be.disabled')
  })

  it('should enable submit button when form is valid', () => {
    cy.get('input[formControlName=firstName]').type('John')
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('input[formControlName=email]').type('john.doe@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    cy.get('button[type=submit]').should('not.be.disabled')
  })

  it('should register successfully with valid credentials', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 200,
      body: {
        message: 'User registered successfully!'
      }
    }).as('registerRequest')

    cy.get('input[formControlName=firstName]').type('John')
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('input[formControlName=email]').type('john.doe@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    cy.get('button[type=submit]').click()

    cy.wait('@registerRequest')
    cy.url().should('include', '/login')
  })

  it('should display error when registration fails', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 400,
      body: {
        message: 'Error: Email is already taken!'
      }
    }).as('registerError')

    cy.get('input[formControlName=firstName]').type('John')
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('input[formControlName=email]').type('existing@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    cy.get('button[type=submit]').click()

    cy.wait('@registerError')
    cy.get('.error').should('be.visible').and('contain', 'An error occurred')
  })

  it('should validate required fields', () => {
    // Submit button should be disabled with empty form
    cy.get('button[type=submit]').should('be.disabled')

    // Fill only first name
    cy.get('input[formControlName=firstName]').type('John')
    cy.get('button[type=submit]').should('be.disabled')

    // Fill first name and last name
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('button[type=submit]').should('be.disabled')

    // Fill first name, last name and email
    cy.get('input[formControlName=email]').type('john.doe@example.com')
    cy.get('button[type=submit]').should('be.disabled')

    // Fill all required fields
    cy.get('input[formControlName=password]').type('password123')
    cy.get('button[type=submit]').should('not.be.disabled')
  })

  it('should validate email format', () => {
    cy.get('input[formControlName=firstName]').type('John')
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('input[formControlName=password]').type('password123')

    // Invalid email format
    cy.get('input[formControlName=email]').type('invalid-email')
    cy.get('button[type=submit]').should('be.disabled')

    // Valid email format
    cy.get('input[formControlName=email]').clear().type('john.doe@example.com')
    cy.get('button[type=submit]').should('not.be.disabled')
  })

  it('should handle server error gracefully', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 500,
      body: {
        message: 'Internal server error'
      }
    }).as('serverError')

    cy.get('input[formControlName=firstName]').type('John')
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('input[formControlName=email]').type('john.doe@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    cy.get('button[type=submit]').click()

    cy.wait('@serverError')
    cy.get('.error').should('be.visible')
  })

  it('should clear form after successful registration', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 200,
      body: {
        message: 'User registered successfully!'
      }
    }).as('registerSuccess')

    cy.get('input[formControlName=firstName]').type('John')
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('input[formControlName=email]').type('john.doe@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    cy.get('button[type=submit]').click()

    cy.wait('@registerSuccess')
    cy.url().should('include', '/login')
  })

  it('should navigate to login page when clicking login link', () => {
    cy.get('span[routerlink="login"]').should('exist').click()
    cy.url().should('include', '/login')
    cy.contains('Login').should('be.visible')
  })
})