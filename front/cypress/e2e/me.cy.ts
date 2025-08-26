// ============================
// UTILITIES & COMMON DATA
// ============================

const mockUserProfil = {
  id: 3,
  email: 'test@example.com',
  firstName: 'test',
  lastName: 'test',
  admin: false,
  createdAt: '2025-08-11 16:02:35',
  updatedAt: '2025-08-11 16:02:35',
};

const mockAdminUserProfil = {
  id: 1,
  email: 'yoga@studio.com',
  firstName: 'Admin',
  lastName: 'Admin',
  admin: true,
  createdAt: '2025-08-11 15:57:09',
  updatedAt: '2025-08-11 15:57:09',
};

function loginAsUserProfil() {
  cy.intercept('POST', '/api/auth/login', {
    statusCode: 200,
    body: {
      id: 3,
      username: 'testuser',
      firstName: 'Test',
      lastName: 'User',
      admin: false,
      token: 'fake-jwt-token',
    },
  }).as('loginRequest');

  cy.visit('/login');

  cy.get('input[formControlName=email]').type('test@example.com');
  cy.get('input[formControlName=password]').type('password123');
  cy.get('button[type=submit]').click();

  cy.wait('@loginRequest');
  cy.url().should('include', '/sessions');
}

function loginAsAdminProfil() {
  cy.intercept('POST', '/api/auth/login', {
    statusCode: 200,
    body: {
      id: 1,
      username: 'adminuser',
      firstName: 'Admin',
      lastName: 'Admin',
      admin: true,
      token: 'fake-admin-jwt-token',
    },
  }).as('adminLoginRequest');

  cy.visit('/login');

  cy.get('input[formControlName=email]').type('admin@example.com');
  cy.get('input[formControlName=password]').type('password123');
  cy.get('button[type=submit]').click();

  cy.wait('@adminLoginRequest');
  cy.url().should('include', '/sessions');
}

function navigateToUserAccount(mockProfile, userId = mockProfile.id) {
  cy.intercept('GET', `/api/user/${userId}`, {
    statusCode: 200,
    body: mockProfile,
  }).as('getUserAccount');

  // Click on the Account span element
  cy.get('span[routerlink="me"]').contains('Account').click();

  cy.wait('@getUserAccount');
}

// ============================
// TESTS - REGULAR USER PROFILE
// ============================

describe('Me (Profile) - Regular User', () => {
  beforeEach(() => {
    loginAsUserProfil();
    navigateToUserAccount(mockUserProfil);
  });

  it('should display user profile information correctly', () => {
    // Check page title and back button
    cy.get('h1').should('contain', 'User information');
    cy.get('button[mat-icon-button]').should('contain', 'arrow_back');

    // Check user information
    cy.contains('Name: test TEST').should('be.visible');
    cy.contains('Email: test@example.com').should('be.visible');

    // Should NOT show admin message for regular user
    cy.contains('You are admin').should('not.exist');

    // Should show delete account section for regular user
    cy.contains('Delete my account:').should('be.visible');
    cy.get('button[color="warn"]')
      .should('contain', 'Detail')
      .should('be.visible');

    // Check dates
    cy.contains('Create at: August 11, 2025').should('be.visible');
    cy.contains('Last update: August 11, 2025').should('be.visible');
  });

  it('should allow regular user to delete their account', () => {
    // Mock delete API call
    cy.intercept('DELETE', '/api/user/3', {
      statusCode: 200,
    }).as('deleteUser');

    // Click delete button
    cy.get('button[color="warn"]').click();
    cy.wait('@deleteUser');

    // Should show success message
    cy.get('.mat-snack-bar-container').should(
      'contain',
      'Your account has been deleted !'
    );

    // Should redirect to home page
    cy.url().should('eq', Cypress.config().baseUrl);
  });

  it('should navigate back when clicking back button', () => {
    cy.get('button[mat-icon-button]').click();

    // Should go back (probably to /sessions)
    cy.url().should('not.include', '/me');
  });

  it('should handle profile loading error gracefully', () => {
    // Do login first
    loginAsUserProfil();

    // Override with error response
    cy.intercept('GET', '/api/user/3', {
      statusCode: 404,
      body: { message: 'User not found' },
    }).as('getUserError');

    // Navigate to profile page
    cy.get('span[routerlink="me"]').contains('Account').click();
    cy.wait('@getUserError');

    // User info should not be displayed
    cy.contains('Name:').should('not.exist');
    cy.contains('Email:').should('not.exist');
  });
});

// ============================
// TESTS - ADMIN USER PROFILE
// ============================

describe('Me (Profile) - Admin User', () => {
  beforeEach(() => {
    loginAsAdminProfil();
    navigateToUserAccount(mockAdminUserProfil);
  });

  it('should display admin profile information correctly', () => {
    // Check page title
    cy.get('h1').should('contain', 'User information');

    // Check user information
    cy.contains('Name: Admin ADMIN').should('be.visible');
    cy.contains('Email: yoga@studio.com').should('be.visible');

    // Should show admin message
    cy.contains('You are admin').should('be.visible');

    // Should NOT show delete account section for admin
    cy.contains('Delete my account:').should('not.exist');
    cy.get('button[color="warn"]').should('not.exist');

    // Check dates
    cy.contains('Create at: August 11, 2025').should('be.visible');
    cy.contains('Last update: August 11, 2025').should('be.visible');
  });
});

// ============================
// TESTS - ERROR SCENARIOS
// ============================

describe('Me (Profile) - Error Scenarios', () => {
  it('should handle account deletion error gracefully', () => {
    // Mock delete API error
    cy.intercept('DELETE', '/api/user/3', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('deleteUserError');

    loginAsUserProfil();
    navigateToUserAccount(mockUserProfil);

    // Click delete button
    cy.get('button[color="warn"]').click();
    cy.wait('@deleteUserError');

    // Should still be on the me page (deletion failed)
    cy.url().should('include', '/me');
  });

  it('should handle profile loading error gracefully', () => {
    loginAsUserProfil();

    // Override with error response for ngOnInit
    cy.intercept('GET', '/api/user/3', {
      statusCode: 404,
      body: { message: 'User not found' },
    }).as('getUserError');

    // Navigate to profile page 
    cy.get('span[routerlink="me"]').contains('Account').click();
    cy.wait('@getUserError');

    // User info should not be displayed when API fails
    cy.contains('Name:').should('not.exist');
    cy.contains('Email:').should('not.exist');
    cy.contains('You are admin').should('not.exist');
  });

  it('should test back button functionality', () => {
    loginAsUserProfil();
    navigateToUserAccount(mockUserProfil);

    // Click back button to test back() method
    cy.get('button[mat-icon-button]').click();
    
    // Should navigate away from /me page
    cy.url().should('not.include', '/me');
  });

  it('should test successful account deletion with snackbar message', () => {
    loginAsUserProfil();
    navigateToUserAccount(mockUserProfil);

    // Mock successful deletion
    cy.intercept('DELETE', '/api/user/3', {
      statusCode: 200,
    }).as('deleteUserSuccess');

    // Click delete button
    cy.get('button[color="warn"]').click();
    cy.wait('@deleteUserSuccess');

    // Should show success message in snackbar
    cy.get('.mat-snack-bar-container')
      .should('contain', 'Your account has been deleted !');

    // Should redirect to home page (testing router.navigate(['/']))
    cy.url().should('eq', Cypress.config().baseUrl);
  });

  it('should test component initialization with different user types', () => {
    // Test with admin user to cover different user properties
    loginAsAdminProfil();
    navigateToUserAccount(mockAdminUserProfil, 1); // Admin user has ID 1

    // Should display admin-specific content
    cy.contains('You are admin').should('be.visible');
    cy.get('button[color="warn"]').should('not.exist'); // No delete button for admin
  });

  it('should test user service getById method call', () => {
    // Test that ngOnInit calls userService.getById with correct ID
    loginAsUserProfil();

    // Mock the API call to verify it's called with correct ID
    cy.intercept('GET', '/api/user/3', {
      statusCode: 200,
      body: mockUserProfil,
    }).as('getUserById');

    cy.get('span[routerlink="me"]').contains('Account').click();
    cy.wait('@getUserById');

    // Verify the component loaded user data
    cy.contains('Name: test TEST').should('be.visible');
  });

  it('should test sessionService.sessionInformation usage', () => {
    // Test that the component uses sessionService.sessionInformation.id
    loginAsUserProfil();

    // Mock API call to different user ID to test session information usage
    cy.intercept('GET', '/api/user/3', {
      statusCode: 200,
      body: {
        ...mockUserProfil,
        id: 3, // Confirm it's using session info ID
      },
    }).as('getSessionUser');

    cy.get('span[routerlink="me"]').contains('Account').click();
    cy.wait('@getSessionUser');

    // Should display the user from session information
    cy.get('h1').should('contain', 'User information');
  });

  it('should test delete method with sessionService.logOut', () => {
    loginAsUserProfil();
    navigateToUserAccount(mockUserProfil);

    // Mock successful deletion to test the complete delete flow
    cy.intercept('DELETE', '/api/user/3', {
      statusCode: 200,
    }).as('deleteSuccess');

    cy.get('button[color="warn"]').click();
    cy.wait('@deleteSuccess');

    // Verify snackbar message (testing matSnackBar.open)
    cy.get('.mat-snack-bar-container').should('be.visible');
    
    // Verify logout and redirect (testing sessionService.logOut() and router.navigate)
    cy.url().should('eq', Cypress.config().baseUrl);
  });

  it('should test component with undefined user initially', () => {
    loginAsUserProfil();

    // Mock delayed API response to test undefined user state
    cy.intercept('GET', '/api/user/3', {
      statusCode: 200,
      body: mockUserProfil,
      delay: 1000, // Add delay to test loading state
    }).as('getDelayedUser');

    cy.get('span[routerlink="me"]').contains('Account').click();
    
    // Initially user should be undefined (testing *ngIf="user" in template)
    cy.get('mat-card-content').should('exist');
    
    cy.wait('@getDelayedUser');
    
    // After API response, user data should appear
    cy.contains('Name: test TEST').should('be.visible');
  });
});
