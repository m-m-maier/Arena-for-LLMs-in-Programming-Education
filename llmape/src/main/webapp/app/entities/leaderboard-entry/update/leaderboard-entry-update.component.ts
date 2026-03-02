import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { Category } from 'app/entities/enumerations/category.model';
import { ILeaderboardEntry } from '../leaderboard-entry.model';
import { LeaderboardEntryService } from '../service/leaderboard-entry.service';
import { LeaderboardEntryFormGroup, LeaderboardEntryFormService } from './leaderboard-entry-form.service';

@Component({
	selector: 'jhi-leaderboard-entry-update',
	templateUrl: './leaderboard-entry-update.component.html',
	imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class LeaderboardEntryUpdateComponent implements OnInit {
	isSaving = false;
	leaderboardEntry: ILeaderboardEntry | null = null;
	categoryValues = Object.keys(Category);

	protected leaderboardEntryService = inject(LeaderboardEntryService);
	protected leaderboardEntryFormService = inject(LeaderboardEntryFormService);
	protected activatedRoute = inject(ActivatedRoute);

	// eslint-disable-next-line @typescript-eslint/member-ordering
	editForm: LeaderboardEntryFormGroup = this.leaderboardEntryFormService.createLeaderboardEntryFormGroup();

	ngOnInit(): void {
		this.activatedRoute.data.subscribe(({ leaderboardEntry }) => {
			this.leaderboardEntry = leaderboardEntry;
			if (leaderboardEntry) {
				this.updateForm(leaderboardEntry);
			}
		});
	}

	previousState(): void {
		window.history.back();
	}

	save(): void {
		this.isSaving = true;
		const leaderboardEntry = this.leaderboardEntryFormService.getLeaderboardEntry(this.editForm);
		if (leaderboardEntry.id !== null) {
			this.subscribeToSaveResponse(this.leaderboardEntryService.update(leaderboardEntry));
		} else {
			this.subscribeToSaveResponse(this.leaderboardEntryService.create(leaderboardEntry));
		}
	}

	protected subscribeToSaveResponse(result: Observable<HttpResponse<ILeaderboardEntry>>): void {
		result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
			next: () => this.onSaveSuccess(),
			error: () => this.onSaveError(),
		});
	}

	protected onSaveSuccess(): void {
		this.previousState();
	}

	protected onSaveError(): void {
		// Api for inheritance.
	}

	protected onSaveFinalize(): void {
		this.isSaving = false;
	}

	protected updateForm(leaderboardEntry: ILeaderboardEntry): void {
		this.leaderboardEntry = leaderboardEntry;
		this.leaderboardEntryFormService.resetForm(this.editForm, leaderboardEntry);
	}
}
