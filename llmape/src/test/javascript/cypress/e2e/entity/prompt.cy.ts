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

describe('Prompt e2e test', () => {
	const promptPageUrl = '/prompt';
	const promptPageUrlPattern = new RegExp('/prompt(\\?.*)?$');
	const username = Cypress.env('E2E_USERNAME') ?? 'user';
	const password = Cypress.env('E2E_PASSWORD') ?? 'user';
	const promptSample = {
		promptText: 'hamburger silt',
		isRejected: false,
		isFromPublicPage: true,
		timestamp: '2025-03-31T23:08:41.501Z',
		sessionId: 'old-fashioned decision',
	};

	let prompt;

	beforeEach(() => {
		cy.login(username, password);
	});

	beforeEach(() => {
		cy.intercept('GET', '/api/prompts+(?*|)').as('entitiesRequest');
		cy.intercept('POST', '/api/prompts').as('postEntityRequest');
		cy.intercept('DELETE', '/api/prompts/*').as('deleteEntityRequest');
	});

	afterEach(() => {
		if (prompt) {
			cy.authenticatedRequest({
				method: 'DELETE',
				url: `/api/prompts/${prompt.id}`,
			}).then(() => {
				prompt = undefined;
			});
		}
	});

	it('Prompts menu should load Prompts page', () => {
		cy.visit('/');
		cy.clickOnEntityMenuItem('prompt');
		cy.wait('@entitiesRequest').then(({ response }) => {
			if (response?.body.length === 0) {
				cy.get(entityTableSelector).should('not.exist');
			} else {
				cy.get(entityTableSelector).should('exist');
			}
		});
		cy.getEntityHeading('Prompt').should('exist');
		cy.url().should('match', promptPageUrlPattern);
	});

	describe('Prompt page', () => {
		describe('create button click', () => {
			beforeEach(() => {
				cy.visit(promptPageUrl);
				cy.wait('@entitiesRequest');
			});

			it('should load create Prompt page', () => {
				cy.get(entityCreateButtonSelector).click();
				cy.url().should('match', new RegExp('/prompt/new$'));
				cy.getEntityCreateUpdateHeading('Prompt');
				cy.get(entityCreateSaveButtonSelector).should('exist');
				cy.get(entityCreateCancelButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', promptPageUrlPattern);
			});
		});

		describe('with existing value', () => {
			beforeEach(() => {
				cy.authenticatedRequest({
					method: 'POST',
					url: '/api/prompts',
					body: promptSample,
				}).then(({ body }) => {
					prompt = body;

					cy.intercept(
						{
							method: 'GET',
							url: '/api/prompts+(?*|)',
							times: 1,
						},
						{
							statusCode: 200,
							body: [prompt],
						},
					).as('entitiesRequestInternal');
				});

				cy.visit(promptPageUrl);

				cy.wait('@entitiesRequestInternal');
			});

			it('detail button click should load details Prompt page', () => {
				cy.get(entityDetailsButtonSelector).first().click();
				cy.getEntityDetailsHeading('prompt');
				cy.get(entityDetailsBackButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', promptPageUrlPattern);
			});

			it('edit button click should load edit Prompt page and go back', () => {
				cy.get(entityEditButtonSelector).first().click();
				cy.getEntityCreateUpdateHeading('Prompt');
				cy.get(entityCreateSaveButtonSelector).should('exist');
				cy.get(entityCreateCancelButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', promptPageUrlPattern);
			});

			it('edit button click should load edit Prompt page and save', () => {
				cy.get(entityEditButtonSelector).first().click();
				cy.getEntityCreateUpdateHeading('Prompt');
				cy.get(entityCreateSaveButtonSelector).click();
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', promptPageUrlPattern);
			});

			it('last delete button click should delete instance of Prompt', () => {
				cy.get(entityDeleteButtonSelector).last().click();
				cy.getEntityDeleteDialogHeading('prompt').should('exist');
				cy.get(entityConfirmDeleteButtonSelector).click();
				cy.wait('@deleteEntityRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(204);
				});
				cy.wait('@entitiesRequest').then(({ response }) => {
					expect(response?.statusCode).to.equal(200);
				});
				cy.url().should('match', promptPageUrlPattern);

				prompt = undefined;
			});
		});
	});

	describe('new Prompt page', () => {
		beforeEach(() => {
			cy.visit(`${promptPageUrl}`);
			cy.get(entityCreateButtonSelector).click();
			cy.getEntityCreateUpdateHeading('Prompt');
		});

		it('should create an instance of Prompt', () => {
			cy.get(`[data-cy="promptText"]`).type('grouper wisely er');
			cy.get(`[data-cy="promptText"]`).should('have.value', 'grouper wisely er');

			cy.get(`[data-cy="category"]`).select('CODE_ASSESSMENT');

			cy.get(`[data-cy="isRejected"]`).should('not.be.checked');
			cy.get(`[data-cy="isRejected"]`).click();
			cy.get(`[data-cy="isRejected"]`).should('be.checked');

			cy.get(`[data-cy="isFromPublicPage"]`).should('not.be.checked');
			cy.get(`[data-cy="isFromPublicPage"]`).click();
			cy.get(`[data-cy="isFromPublicPage"]`).should('be.checked');

			cy.get(`[data-cy="timestamp"]`).type('2025-03-31T18:28');
			cy.get(`[data-cy="timestamp"]`).blur();
			cy.get(`[data-cy="timestamp"]`).should('have.value', '2025-03-31T18:28');

			cy.get(`[data-cy="sessionId"]`).type('misreport mmm');
			cy.get(`[data-cy="sessionId"]`).should('have.value', 'misreport mmm');

			cy.get(`[data-cy="generationModelId"]`).type('18981');
			cy.get(`[data-cy="generationModelId"]`).should('have.value', '18981');

			cy.get(entityCreateSaveButtonSelector).click();

			cy.wait('@postEntityRequest').then(({ response }) => {
				expect(response?.statusCode).to.equal(201);
				prompt = response.body;
			});
			cy.wait('@entitiesRequest').then(({ response }) => {
				expect(response?.statusCode).to.equal(200);
			});
			cy.url().should('match', promptPageUrlPattern);
		});
	});
});
