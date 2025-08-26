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

  // ============================
  // VALIDATION TESTS
  // ============================

  it('should show validation error for firstName too short', () => {
    cy.get('input[formControlName=firstName]').type('ab') // < 3 chars
    cy.get('input[formControlName=lastName]').type('ValidName')
    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('validpass')
    
    // Submit button should be disabled due to validation error
    cy.get('button[type=submit]').should('be.disabled')
  })

  it('should show validation error for firstName too long', () => {
    cy.get('input[formControlName=firstName]').type('a'.repeat(21)) // > 20 chars
    cy.get('input[formControlName=lastName]').type('ValidName')
    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('validpass')
    
    // Submit button should be disabled due to validation error
    cy.get('button[type=submit]').should('be.disabled')
  })

  it('should show validation error for lastName too short', () => {
    cy.get('input[formControlName=firstName]').type('ValidName')
    cy.get('input[formControlName=lastName]').type('ab') // < 3 chars
    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('validpass')
    
    // Submit button should be disabled due to validation error
    cy.get('button[type=submit]').should('be.disabled')
  })

  it('should show validation error for lastName too long', () => {
    cy.get('input[formControlName=firstName]').type('ValidName')
    cy.get('input[formControlName=lastName]').type('b'.repeat(21)) // > 20 chars
    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('validpass')
    
    // Submit button should be disabled due to validation error
    cy.get('button[type=submit]').should('be.disabled')
  })

  it('should show validation error for password too short', () => {
    cy.get('input[formControlName=firstName]').type('ValidName')
    cy.get('input[formControlName=lastName]').type('ValidName')
    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('ab') // < 3 chars
    
    // Submit button should be disabled due to validation error
    cy.get('button[type=submit]').should('be.disabled')
  })

  it('should show validation error for password too long', () => {
    cy.get('input[formControlName=firstName]').type('ValidName')
    cy.get('input[formControlName=lastName]').type('ValidName')
    cy.get('input[formControlName=email]').type('test@example.com')
    cy.get('input[formControlName=password]').type('p'.repeat(41)) // > 40 chars
    
    // Submit button should be disabled due to validation error
    cy.get('button[type=submit]').should('be.disabled')
  })

  it('should show validation error for invalid email format', () => {
    cy.get('input[formControlName=firstName]').type('ValidName')
    cy.get('input[formControlName=lastName]').type('ValidName')
    cy.get('input[formControlName=email]').type('invalid-email') // invalid format
    cy.get('input[formControlName=password]').type('validpass')
    
    // Submit button should be disabled due to validation error
    cy.get('button[type=submit]').should('be.disabled')
  })

  it('should reset error state when retrying after server error', () => {
    // First attempt with server error
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 400,
      body: { message: 'Email already exists' }
    }).as('registerError')

    cy.get('input[formControlName=firstName]').type('John')
    cy.get('input[formControlName=lastName]').type('Doe')
    cy.get('input[formControlName=email]').type('existing@example.com')
    cy.get('input[formControlName=password]').type('password123')
    
    cy.get('button[type=submit]').click()
    cy.wait('@registerError')
    cy.get('.error').should('be.visible')

    // Second attempt with success (should reset error state)
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 200,
      body: { message: 'Success' }
    }).as('registerSuccess')

    cy.get('input[formControlName=email]').clear().type('new@example.com')
    cy.get('button[type=submit]').click()
    cy.wait('@registerSuccess')
    
    // Error should be cleared (onError = false)
    cy.url().should('include', '/login')
  })

  it('should test all validation combinations', () => {
    // Test valid form with minimum length values
    cy.get('input[formControlName=firstName]').type('abc') // exactly 3 chars
    cy.get('input[formControlName=lastName]').type('def') // exactly 3 chars
    cy.get('input[formControlName=email]').type('a@b.com') // valid minimal email
    cy.get('input[formControlName=password]').type('123') // exactly 3 chars
    
    cy.get('button[type=submit]').should('not.be.disabled')

    // Test valid form with maximum length values
    cy.get('input[formControlName=firstName]').clear().type('a'.repeat(20)) // exactly 20 chars
    cy.get('input[formControlName=lastName]').clear().type('b'.repeat(20)) // exactly 20 chars
    cy.get('input[formControlName=password]').clear().type('p'.repeat(40)) // exactly 40 chars
    
    cy.get('button[type=submit]').should('not.be.disabled')
  })

  // ============================
  // NAVIGATION TESTS
  // ============================

  it('should navigate to login page when clicking login link', () => {
    cy.get('span[routerlink="login"]').should('exist').click()
    cy.url().should('include', '/login')
    cy.contains('Login').should('be.visible')
  })
})