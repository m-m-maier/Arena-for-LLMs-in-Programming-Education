import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import SharedModule from 'app/shared/shared.module';
import { Subject } from 'rxjs';
import { IModelDTO } from '../api/dto/my.model.dto';
import { MyModelService } from 'app/api/service/my.model.service';
import { MyPromptService } from 'app/api/service/my.prompt.service';
import { finalize, first, takeUntil } from 'rxjs/operators';
import { CategoryLabels } from 'app/entities/enumerations/categoryLabel';
import { Category } from 'app/entities/enumerations/category.model';
import { IGeneratePromptResponseDTO } from 'app/api/dto/my.generatePromptResponse.dto';
import { IGeneratePromptDTO } from 'app/api/dto/my.generatePrompt.dto';

type GeneratePromptRequest = {
	waitingForResponse: boolean;
	modelName: string;
	response?: IGeneratePromptResponseDTO;
};

@Component({
	selector: 'jhi-generate-prompts-and-votes',
	imports: [SharedModule, RouterModule, FormsModule],
	templateUrl: './generatePromptsAndVotes.component.html',
	styleUrl: './generatePromptsAndVotes.component.scss',
})
export default class GeneratePromptsAndVotesComponent implements OnInit, OnDestroy {
	account = signal<Account | null>(null);

	categories = Object.values(Category);
	categoryLabels = CategoryLabels;
	selectedCategory: Category = Category.HINT_GENERATION;
	selectedAmount = 1;

	waitingForResponse = false;

	models: IModelDTO[] = [];
	selectedModel: IModelDTO | null = null;
	generatePromptRequests: GeneratePromptRequest[] = [];
	startingError = '';
	responseError = '';

	private readonly destroy$ = new Subject<void>();

	private readonly accountService: AccountService = inject(AccountService);
	private readonly myModelService: MyModelService = inject(MyModelService);
	private readonly myPromptService: MyPromptService = inject(MyPromptService);

	ngOnInit(): void {
		this.accountService
			.getAuthenticationState()
			.pipe(takeUntil(this.destroy$))
			.subscribe(account => this.account.set(account));

		this.myModelService
			.getAllActiveModels()
			.pipe(first())
			.subscribe({
				next: response => {
					if (response.body) {
						this.models = response.body;
						if (this.models.length < 3) {
							this.startingError = this.resolveError('error.notenoughmodels');
						}
					}
				},
				error: err => {
					this.responseError = this.resolveError(err.error.message);
				},
			});
	}

	submit(): void {
		this.responseError = '';
		this.waitingForResponse = true;
		this.generatePromptRequests = [];

		// because requests take long, store values such that user cannot change them in dropdown during requests
		const submittedModel = this.selectedModel;
		const submittedCategory = this.selectedCategory;
		const generateAmount = this.selectedModel === null ? this.selectedAmount * this.models.length : this.selectedAmount;

		const finished = 0;

		this.doGeneratePromptsAndVotes(finished, generateAmount, submittedCategory, submittedModel);
	}

	doGeneratePromptsAndVotes(finished: number, generateAmount: number, submittedCategory: Category, submittedModel: IModelDTO | null): void {
		const generatePromptDTO: IGeneratePromptDTO = {
			modelId: submittedModel === null ? this.models[finished % this.models.length].id : submittedModel.id,
			category: submittedCategory,
		};

		const generatePromptRequest: GeneratePromptRequest = {
			waitingForResponse: true,
			modelName: (submittedModel === null ? this.models[finished % this.models.length].modelName : submittedModel.modelName) ?? '',
		};
		this.generatePromptRequests.push(generatePromptRequest);

		this.myPromptService
			.generatePromptAndVote(generatePromptDTO)
			.pipe(
				first(),
				finalize(() => {
					finished++;
					if (finished === generateAmount) {
						this.waitingForResponse = false;
					} else {
						this.doGeneratePromptsAndVotes(finished, generateAmount, submittedCategory, submittedModel);
					}
				}),
			)
			.subscribe({
				next(response) {
					if (response.body) {
						generatePromptRequest.response = { wasSuccessful: response.body.wasSuccessful, detailedMessage: response.body.detailedMessage };
					}
					generatePromptRequest.waitingForResponse = false;
				},
				error: err => {
					this.responseError = this.resolveError(err.error.message);
					generatePromptRequest.waitingForResponse = false;
				},
			});
	}

	resolveError(errorMessage: string): string {
		if (errorMessage === 'error.notenoughmodels') {
			return 'At least 3 active models in database needed to generate prompt and vote.';
		} else if (errorMessage === 'error.idnotfound') {
			return 'Model id not found in database.';
		}

		return 'Unknown error occured';
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}
}
