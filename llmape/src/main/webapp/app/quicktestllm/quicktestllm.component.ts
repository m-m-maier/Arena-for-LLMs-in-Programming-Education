import { Component, ElementRef, inject, OnDestroy, OnInit, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MAX_LLM_INPUT_OUTPUT_CHARS } from 'app/config/constants';
import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import SharedModule from 'app/shared/shared.module';
import dayjs from 'dayjs/esm';
import { Subject } from 'rxjs';
import { IModelDTO } from '../api/dto/my.model.dto';
import { MyModelService } from 'app/api/service/my.model.service';
import { NewPrompt } from 'app/entities/prompt/prompt.model';
import { MyPromptService } from 'app/api/service/my.prompt.service';
import { first, takeUntil } from 'rxjs/operators';
import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager } from 'app/core/util/event-manager.service';
import { SessionService } from 'app/core/session/session.service';

type ModelSelection = {
	model: IModelDTO;
	selected: boolean;
};

type Response = {
	id: number;
	text: string;
};

@Component({
	selector: 'jhi-quicktestllm',
	imports: [SharedModule, RouterModule, FormsModule],
	templateUrl: './quicktestllm.component.html',
	styleUrl: './quicktestllm.component.scss',
})
export default class QuicktestllmComponent implements OnInit, OnDestroy {
	account = signal<Account | null>(null);

	promptMaximumLengthReached = signal(false);

	prompt = '';
	promptMaxLength: number = MAX_LLM_INPUT_OUTPUT_CHARS;
	waitingForResponse = false;

	modelSelections: ModelSelection[] = [];
	responses: Response[] = [];

	@ViewChild('promptTextarea') promptTextarea!: ElementRef<HTMLTextAreaElement>;

	private readonly destroy$ = new Subject<void>();

	private readonly accountService: AccountService = inject(AccountService);
	private readonly myModelService: MyModelService = inject(MyModelService);
	private readonly myPromptService: MyPromptService = inject(MyPromptService);
	private readonly eventManager: EventManager = inject(EventManager);
	private readonly sessionService: SessionService = inject(SessionService);

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
						response.body.forEach(model => {
							this.modelSelections.push({ model: { id: model.id, modelName: model.modelName }, selected: true });
						});
					}
				},
			});
	}

	submitPrompt(): void {
		const currentTime = dayjs();

		const newPrompt: NewPrompt = {
			id: null,
			promptText: this.prompt,
			category: null,
			isRejected: false,
			isFromPublicPage: false,
			timestamp: currentTime,
			sessionId: this.sessionService.getOrCreateSessionId(),
		};

		this.waitingForResponse = true;

		this.myPromptService
			.submitQuickTestPrompt(newPrompt)
			.pipe(first())
			.subscribe({
				next: response => {
					if (response.body) {
						this.streamAIResponses(response.body);
					} else {
						this.waitingForResponse = false;
					}
				},
				error: err => {
					this.waitingForResponse = false;
				},
			});

		this.prompt = '';
		this.promptMaximumLengthReached.set(false);

		// Delay to let Angular clear the textarea first
		setTimeout(() => {
			const textArea = this.promptTextarea.nativeElement;
			this.autoResize(textArea);
		});
	}

	streamAIResponses(promptId: number): void {
		this.responses = [];

		for (const modelSelection of this.modelSelections) {
			if (modelSelection.selected) {
				this.responses.push({
					id: modelSelection.model.id,
					text: '',
				});
			}
		}

		const selectedModelIds = this.modelSelections.filter(ms => ms.selected).map(ms => ms.model.id);

		this.myPromptService.streamManyAIResponses(promptId, selectedModelIds).subscribe({
			next: token => {
				this.waitingForResponse = false;

				const response = this.responses.find(r => r.id === token.source);
				if (response) {
					response.text += token.token;
				}
			},
			error: err => {
				this.waitingForResponse = false;
				this.eventManager.broadcast({
					name: 'llmapeApp.error',
					content: {
						message: 'Error while streaming AI responses.',
					} as AlertError,
				});
				console.error('Error while streaming AI responses.', err);
			},
			complete() {
				// nothing to do
			},
		});
	}

	getModelNameById(modelId: number): string {
		return this.modelSelections.find(m => m.model.id === modelId)?.model.modelName ?? '';
	}

	autoResize(textArea: HTMLTextAreaElement): void {
		textArea.style.height = 'auto';
		const lineHeight = parseFloat(getComputedStyle(textArea).lineHeight || '16');
		const lines = Math.round(textArea.scrollHeight / lineHeight);

		if (lines > 2) {
			textArea.style.height = `${textArea.scrollHeight}px`;
		}
	}

	onTextAreaInput(textArea: HTMLTextAreaElement): void {
		this.autoResize(textArea);

		this.promptMaximumLengthReached.set(false);

		if (this.prompt.length === this.promptMaxLength) {
			this.promptMaximumLengthReached.set(true);
		}
	}

	allUnselected(): boolean {
		return this.modelSelections.every(ms => !ms.selected);
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}
}
