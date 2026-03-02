import {
	entityConfirmDeleteButtonSelector,
	entityCreateButtonSelector,
	entityCreateCancelButtonSelector,
	entityCreateSaveButtonSelector,
	entityDeleteButtonSelector,
	entityDetailsBackButtonSelector,
	entityDetailsButtonSelector,
	entityEditButtonSelector,
	entityTableSelector,
} from '../../support/entity';

describe('LeaderboardEntry e2e test', () => {
	const leaderboardEntryPageUrl = '/leaderboard-entry';
	const leaderboardEntryPageUrlPattern = new RegExp('/leaderboard-entry(\\?.*)?$');
	const username = Cypress.env('E2E_USERNAME') ?? 'user';
	const password = Cypress.env('E2E_PASSWORD') ?? 'user';
	const leaderboardEntrySample = { entryJson: 'millet fray the', timestamp: '2025-06-09T19:53:38.865Z' };

	let leaderboardEntry;

	beforeEach(() => {
		cy.login(username, password);
	});

	beforeEach(() => {
		cy.intercept('GET', '/api/leaderboard-entries+(?*|)').as('entitiesRequest');
		cy.intercept('POST', '/api/leaderboard-entries').as('postEntityRequest');
		cy.intercept('DELETE', '/api/leaderboard-entries/*').as('deleteEntityRequest');
	});

	afterEach(() => {
		if (leaderboardEntry) {
			cy.authenticatedRequest({
				method: 'DELETE',
				url: `/api/leaderboard-entries/${leaderboardEntry.id}`,
			}).then(() => {
				leaderboardEntry = undefined;
			});
		}
	});

	it('LeaderboardEntries menu should load LeaderboardEntries page', () => {
		cy.visit('/');
		cy.clickOnEntityMenuItem('leaderboard-entry');
		cy.wait('@entitiesRequest').then(({ response }) => {
			if (response?.body.length === 0) {
				cy.get(entityTableSelector).should('not.exist');
			} else {
				cy.get(entityTableSelector).should('exist');
			}
		});
		cy.getEntityHeading('LeaderboardEntry').should('exist');
		cy.url().should('match', leaderboardEntryPageUrlPattern);
	});

	describe('LeaderboardEntry page', () => {
		describe('create button click', () => {
			beforeEach(() => {
				cy.visit(leaderboardEntryPageUrl);
				cy.wait('@entitiesRequest');
			});

			it('should load create LeaderboardEntry page', () => {
				cy.get(entityCreateButtonSelector).click();
				cy.url().should('match', new RegExp('/leaderboard-entry/new$'));
				cy.getEntityCreateUpdateHeading('LeaderboardEntry');
				cy.get(entityCreateSaveButtonSelector).should('exist');
				cy.get(entityCreateCancelButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', leaderboardEntryPageUrlPattern);
			});
		});

		describe('with existing value', () => {
			beforeEach(() => {
				cy.authenticatedRequest({
					method: 'POST',
					url: '/api/leaderboard-entries',
					body: leaderboardEntrySample,
				}).then(({ body }) => {
					leaderboardEntry = body;

					cy.intercept(
						{
							method: 'GET',
							url: '/api/leaderboard-entries+(?*|)',
							times: 1,
						},
						{
							statusCode: 200,
							body: [leaderboardEntry],
						},
					).as('entitiesRequestInternal');
				});

				cy.visit(leaderboardEntryPageUrl);

				cy.wait('@entitiesRequestInternal');
			});

			it('detail button click should load details LeaderboardEntry page', () => {
				cy.get(entityDetailsButtonSelector).first().click();
				cy.getEntityDetailsHeading('leaderboardEntry');
				cy.get(entityDetailsBackButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', leaderboardEntryPageUrlPattern);
			});

			it('edit button click should load edit LeaderboardEntry page and go back', () => {
				cy.get(entityEditButtonSelector).first().click();
				cy.getEntityCreateUpdateHeading('LeaderboardEntry');
				cy.get(entityCreateSaveButtonSelector).should('exist');
				cy.get(entityCreateCancelButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', leaderboardEntryPageUrlPattern);
			});

			it('edit button click should load edit LeaderboardEntry page and save', () => {
				cy.get(entityEditButtonSelector).first().click();
				cy.getEntityCreateUpdateHeading('LeaderboardEntry');
				cy.get(entityCreateSaveButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', leaderboardEntryPageUrlPattern);
			});

			it('last delete button click should delete instance of LeaderboardEntry', () => {
				cy.get(entityDeleteButtonSelector).last().click();
				cy.getEntityDeleteDialogHeading('leaderboardEntry').should('exist');
				cy.get(entityConfirmDeleteButtonSelector).click();
				cy.wait('@deleteEntityRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(204);
				});
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', leaderboardEntryPageUrlPattern);

				leaderboardEntry = undefined;
			});
		});
	});

	describe('new LeaderboardEntry page', () => {
		beforeEach(() => {
			cy.visit(`${leaderboardEntryPageUrl}`);
			cy.get(entityCreateButtonSelector).click();
			cy.getEntityCreateUpdateHeading('LeaderboardEntry');
		});

		it('should create an instance of LeaderboardEntry', () => {
			cy.get(`[data-cy="entryJson"]`).type('meanwhile near mortally');
			cy.get(`[data-cy="entryJson"]`).should('have.value', 'meanwhile near mortally');

			cy.get(`[data-cy="category"]`).select('CODE_ASSESSMENT');

			cy.get(`[data-cy="timestamp"]`).type('2025-06-10T12:57');
			cy.get(`[data-cy="timestamp"]`).blur();
			cy.get(`[data-cy="timestamp"]`).should('have.value', '2025-06-10T12:57');

			cy.get(entityCreateSaveButtonSelector).click();

			cy.wait('@postEntityRequest').then(({ response }) => {
				expect(response?.statusCode).to.equal(201);
				leaderboardEntry = response.body;
			});
			cy.wait('@entitiesRequest').then(({ response }) => {
				expect(response?.statusCode).to.equal(200);
			});
			cy.url().should('match', leaderboardEntryPageUrlPattern);
		});
	});
});
