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

describe('Battle e2e test', () => {
	const battlePageUrl = '/battle';
	const battlePageUrlPattern = new RegExp('/battle(\\?.*)?$');
	const username = Cypress.env('E2E_USERNAME') ?? 'user';
	const password = Cypress.env('E2E_PASSWORD') ?? 'user';
	const battleSample = {};

	let battle;

	beforeEach(() => {
		cy.login(username, password);
	});

	beforeEach(() => {
		cy.intercept('GET', '/api/battles+(?*|)').as('entitiesRequest');
		cy.intercept('POST', '/api/battles').as('postEntityRequest');
		cy.intercept('DELETE', '/api/battles/*').as('deleteEntityRequest');
	});

	afterEach(() => {
		if (battle) {
			cy.authenticatedRequest({
				method: 'DELETE',
				url: `/api/battles/${battle.id}`,
			}).then(() => {
				battle = undefined;
			});
		}
	});

	it('Battles menu should load Battles page', () => {
		cy.visit('/');
		cy.clickOnEntityMenuItem('battle');
		cy.wait('@entitiesRequest').then(({ response }) => {
			if (response?.body.length === 0) {
				cy.get(entityTableSelector).should('not.exist');
			} else {
				cy.get(entityTableSelector).should('exist');
			}
		});
		cy.getEntityHeading('Battle').should('exist');
		cy.url().should('match', battlePageUrlPattern);
	});

	describe('Battle page', () => {
		describe('create button click', () => {
			beforeEach(() => {
				cy.visit(battlePageUrl);
				cy.wait('@entitiesRequest');
			});

			it('should load create Battle page', () => {
				cy.get(entityCreateButtonSelector).click();
				cy.url().should('match', new RegExp('/battle/new$'));
				cy.getEntityCreateUpdateHeading('Battle');
				cy.get(entityCreateSaveButtonSelector).should('exist');
				cy.get(entityCreateCancelButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', battlePageUrlPattern);
			});
		});

		describe('with existing value', () => {
			beforeEach(() => {
				cy.authenticatedRequest({
					method: 'POST',
					url: '/api/battles',
					body: battleSample,
				}).then(({ body }) => {
					battle = body;

					cy.intercept(
						{
							method: 'GET',
							url: '/api/battles+(?*|)',
							times: 1,
						},
						{
							statusCode: 200,
							body: [battle],
						},
					).as('entitiesRequestInternal');
				});

				cy.visit(battlePageUrl);

				cy.wait('@entitiesRequestInternal');
			});

			it('detail button click should load details Battle page', () => {
				cy.get(entityDetailsButtonSelector).first().click();
				cy.getEntityDetailsHeading('battle');
				cy.get(entityDetailsBackButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', battlePageUrlPattern);
			});

			it('edit button click should load edit Battle page and go back', () => {
				cy.get(entityEditButtonSelector).first().click();
				cy.getEntityCreateUpdateHeading('Battle');
				cy.get(entityCreateSaveButtonSelector).should('exist');
				cy.get(entityCreateCancelButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', battlePageUrlPattern);
			});

			it('edit button click should load edit Battle page and save', () => {
				cy.get(entityEditButtonSelector).first().click();
				cy.getEntityCreateUpdateHeading('Battle');
				cy.get(entityCreateSaveButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', battlePageUrlPattern);
			});

			it('last delete button click should delete instance of Battle', () => {
				cy.get(entityDeleteButtonSelector).last().click();
				cy.getEntityDeleteDialogHeading('battle').should('exist');
				cy.get(entityConfirmDeleteButtonSelector).click();
				cy.wait('@deleteEntityRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(204);
				});
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', battlePageUrlPattern);

				battle = undefined;
			});
		});
	});

	describe('new Battle page', () => {
		beforeEach(() => {
			cy.visit(`${battlePageUrl}`);
			cy.get(entityCreateButtonSelector).click();
			cy.getEntityCreateUpdateHeading('Battle');
		});

		it('should create an instance of Battle', () => {
			cy.get(`[data-cy="model1Answer"]`).type('although');
			cy.get(`[data-cy="model1Answer"]`).should('have.value', 'although');

			cy.get(`[data-cy="model2Answer"]`).type('ravel till waft');
			cy.get(`[data-cy="model2Answer"]`).should('have.value', 'ravel till waft');

			cy.get(`[data-cy="voteTimestamp"]`).type('2025-04-01T08:29');
			cy.get(`[data-cy="voteTimestamp"]`).blur();
			cy.get(`[data-cy="voteTimestamp"]`).should('have.value', '2025-04-01T08:29');

			cy.get(entityCreateSaveButtonSelector).click();

			cy.wait('@postEntityRequest').then(({ response }) => {
				expect(response?.statusCode).to.equal(201);
				battle = response.body;
			});
			cy.wait('@entitiesRequest').then(({ response }) => {
				expect(response?.statusCode).to.equal(200);
			});
			cy.url().should('match', battlePageUrlPattern);
		});
	});
});
