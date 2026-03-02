import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, TemplateRef, ViewChild, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { first, takeUntil } from 'rxjs/operators';
import { ViewportScroller } from '@angular/common';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { Category } from '../entities/enumerations/category.model';
import { CategoryLabels } from 'app/entities/enumerations/categoryLabel';
import { FormsModule } from '@angular/forms';
import { MyPromptService } from 'app/api/service/my.prompt.service';
import { IVoteDTO, VoteOption } from '../api/dto/my.vote.dto';
import { NewPrompt } from 'app/entities/prompt/prompt.model';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import dayjs from 'dayjs/esm';
import { MyVoteService } from 'app/api/service/my.vote.service';
import { ILeaderboardDTO } from 'app/api/dto/my.leaderboard.dto';
import { MyLeaderboradService } from 'app/api/service/my.leaderboard.service';
import { MAX_LLM_INPUT_OUTPUT_CHARS } from '../config/constants';
import { HttpStatusCode } from '@angular/common/http';
import { EventManager } from 'app/core/util/event-manager.service';
import { AlertError } from 'app/shared/alert/alert-error.model';
import { SessionService } from 'app/core/session/session.service';

@Component({
	selector: 'jhi-home',
	templateUrl: './home.component.html',
	styleUrl: './home.component.scss',
	imports: [SharedModule, RouterModule, FormsModule],
})
export default class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
	VoteOption = VoteOption;
	account = signal<Account | null>(null);

	promptRejected = signal(false);
	promptMaximumLengthReached = signal(false);

	categories = Object.values(Category);
	categoryLabels = CategoryLabels;
	selectedPromptCategory: Category = Category.HINT_GENERATION;
	selectedLeaderboardCategory: Category | null = null;

	prompt = '';
	promptMaxLength: number = MAX_LLM_INPUT_OUTPUT_CHARS;
	modelAResponse = '';
	modelBResponse = '';
	modelAIdentity = '';
	modelBIdentity = '';
	rejectionReason = '';
	battleId?: number | null;
	waitingForResponseA = false;
	waitingForResponseB = false;
	voteButtonsDisabled = true;
	votingSectionId = 'VotingSection';

	leaderboard: ILeaderboardDTO = { rows: [], timestamp: null };

	@ViewChild('autoOpenModal') autoOpenModal!: TemplateRef<any>;
	@ViewChild('promptTextarea') promptTextarea!: ElementRef<HTMLTextAreaElement>;

	protected modalService = inject(NgbModal);
	private readonly destroy$ = new Subject<void>();

	private readonly accountService: AccountService = inject(AccountService);
	private readonly myPromptService: MyPromptService = inject(MyPromptService);
	private readonly myVoteService: MyVoteService = inject(MyVoteService);
	private readonly myLeaderboardService: MyLeaderboradService = inject(MyLeaderboradService);
	private readonly eventManager: EventManager = inject(EventManager);
	private readonly sessionService: SessionService = inject(SessionService);
	private readonly viewPortScroller: ViewportScroller = inject(ViewportScroller);

	ngOnInit(): void {
		this.accountService
			.getAuthenticationState()
			.pipe(takeUntil(this.destroy$))
			.subscribe(account => this.account.set(account));

		this.loadLeaderboard(this.selectedLeaderboardCategory);
	}

	ngAfterViewInit(): void {
		if (!this.sessionService.isSessionIdAlreadySet()) {
			this.modalService.open(this.autoOpenModal, {
				backdrop: 'static',
				keyboard: false,
			});
			this.sessionService.getOrCreateSessionId();
		}
	}

	loadLeaderboard(category: Category | null): void {
		this.myLeaderboardService
			.getLeaderboardByCategory(category)
			.pipe(first())
			.subscribe({
				next: response => {
					this.leaderboard = { rows: [], timestamp: null };
					if (response.body) {
						this.leaderboard.rows = response.body.rows;
						this.leaderboard.timestamp = dayjs(response.body.timestamp);
					}
					this.leaderboard.rows.sort((a, b) => b.score - a.score);
				},
			});
	}

	get totalVotes(): number {
		return this.leaderboard.rows.reduce((sum, entry) => sum + entry.numberOfVotes, 0) / 2;
	}

	get numberOfTies(): number {
		return this.leaderboard.rows.reduce((sum, entry) => sum + entry.numberOfTies, 0) / 2;
	}

	onLeaderboardCategoryChange(): void {
		this.loadLeaderboard(this.selectedLeaderboardCategory);
	}

	submitPrompt(): void {
		this.modelAIdentity = '';
		this.modelBIdentity = '';
		this.voteButtonsDisabled = true;

		const currentTime = dayjs();

		const newPrompt: NewPrompt = {
			id: null,
			promptText: this.prompt,
			category: this.selectedPromptCategory,
			isRejected: false,
			isFromPublicPage: true,
			timestamp: currentTime,
			sessionId: this.sessionService.getOrCreateSessionId(),
		};

		this.waitingForResponseA = true;
		this.waitingForResponseB = true;
		this.promptRejected.set(false);

		this.myPromptService
			.submitPrompt(newPrompt)
			.pipe(first())
			.subscribe({
				next: response => {
					if (response.body?.rejected) {
						this.promptIsRejected(response.body.rejectionReason ?? '');
					} else {
						this.battleId = response.body?.battleId ?? null;
						this.streamAIResponses();
					}
				},
				error: err => {
					if (err.status === HttpStatusCode.TooManyRequests) {
						this.resetPromptVariables();
					} else {
						this.promptIsRejected();
					}
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

	promptIsRejected(rejectionReason?: string): void {
		this.rejectionReason = rejectionReason ?? '';
		this.promptRejected.set(true);
		this.resetPromptVariables();
	}

	resetPromptVariables(): void {
		this.waitingForResponseA = false;
		this.waitingForResponseB = false;
		this.modelAResponse = '';
		this.modelBResponse = '';
		this.modelAIdentity = '';
		this.modelBIdentity = '';
		this.battleId = null;
	}

	streamAIResponses(): void {
		this.modelAResponse = '';
		this.modelBResponse = '';

		this.myPromptService.streamAIResponses(this.battleId!).subscribe({
			next: token => {
				if (token.source === 1) {
					this.waitingForResponseA = false;
					this.modelAResponse += token.token;
				} else {
					this.waitingForResponseB = false;
					this.modelBResponse += token.token;
				}
			},
			error: err => {
				this.eventManager.broadcast({
					name: 'llmapeApp.error',
					content: {
						message: 'Error while streaming AI responses.',
					} as AlertError,
				});
				console.error('Error while streaming AI responses.', err);
				this.resetPromptVariables();
			},
			complete: () => {
				this.voteButtonsDisabled = false;
			},
		});
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

	vote(voteOption: VoteOption): void {
		this.voteButtonsDisabled = true;

		if (!this.battleId) {
			return;
		}

		const vote: IVoteDTO = {
			battleId: this.battleId,
			voteOption,
		};

		this.myVoteService
			.submitVote(vote)
			.pipe(first())
			.subscribe({
				next: response => {
					this.modelAIdentity = response.body?.modelAIdentity ?? '';
					this.modelBIdentity = response.body?.modelBIdentity ?? '';
					this.selectedLeaderboardCategory = this.selectedPromptCategory;
					this.loadLeaderboard(this.selectedLeaderboardCategory);
				},
			});

		this.viewPortScroller.scrollToAnchor(this.votingSectionId);
	}

	getFormattedLeaderboardTimestamp(): string {
		return this.leaderboard.timestamp ? this.leaderboard.timestamp.format('DD.MM.YYYY HH:mm') : 'N/A';
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}
}
