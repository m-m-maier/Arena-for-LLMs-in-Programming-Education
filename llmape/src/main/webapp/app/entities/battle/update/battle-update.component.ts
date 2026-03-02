import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPrompt } from 'app/entities/prompt/prompt.model';
import { PromptService } from 'app/entities/prompt/service/prompt.service';
import { IModel } from 'app/entities/model/model.model';
import { ModelService } from 'app/entities/model/service/model.service';
import { BattleService } from '../service/battle.service';
import { IBattle } from '../battle.model';
import { BattleFormGroup, BattleFormService } from './battle-form.service';

@Component({
	selector: 'jhi-battle-update',
	templateUrl: './battle-update.component.html',
	imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class BattleUpdateComponent implements OnInit {
	isSaving = false;
	battle: IBattle | null = null;

	promptsCollection: IPrompt[] = [];
	modelsSharedCollection: IModel[] = [];

	protected battleService = inject(BattleService);
	protected battleFormService = inject(BattleFormService);
	protected promptService = inject(PromptService);
	protected modelService = inject(ModelService);
	protected activatedRoute = inject(ActivatedRoute);

	// eslint-disable-next-line @typescript-eslint/member-ordering
	editForm: BattleFormGroup = this.battleFormService.createBattleFormGroup();

	comparePrompt = (o1: IPrompt | null, o2: IPrompt | null): boolean => this.promptService.comparePrompt(o1, o2);

	compareModel = (o1: IModel | null, o2: IModel | null): boolean => this.modelService.compareModel(o1, o2);

	ngOnInit(): void {
		this.activatedRoute.data.subscribe(({ battle }) => {
			this.battle = battle;
			if (battle) {
				this.updateForm(battle);
			}

			this.loadRelationshipsOptions();
		});
	}

	previousState(): void {
		window.history.back();
	}

	save(): void {
		this.isSaving = true;
		const battle = this.battleFormService.getBattle(this.editForm);
		if (battle.id !== null) {
			this.subscribeToSaveResponse(this.battleService.update(battle));
		} else {
			this.subscribeToSaveResponse(this.battleService.create(battle));
		}
	}

	protected subscribeToSaveResponse(result: Observable<HttpResponse<IBattle>>): void {
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

	protected updateForm(battle: IBattle): void {
		this.battle = battle;
		this.battleFormService.resetForm(this.editForm, battle);

		this.promptsCollection = this.promptService.addPromptToCollectionIfMissing<IPrompt>(this.promptsCollection, battle.prompt);
		this.modelsSharedCollection = this.modelService.addModelToCollectionIfMissing<IModel>(
			this.modelsSharedCollection,
			battle.model1,
			battle.model2,
			battle.winnerModel,
		);
	}

	protected loadRelationshipsOptions(): void {
		this.promptService
			.query({ filter: 'battle-is-null' })
			.pipe(map((res: HttpResponse<IPrompt[]>) => res.body ?? []))
			.pipe(map((prompts: IPrompt[]) => this.promptService.addPromptToCollectionIfMissing<IPrompt>(prompts, this.battle?.prompt)))
			.subscribe((prompts: IPrompt[]) => (this.promptsCollection = prompts));

		this.modelService
			.query()
			.pipe(map((res: HttpResponse<IModel[]>) => res.body ?? []))
			.pipe(
				map((models: IModel[]) =>
					this.modelService.addModelToCollectionIfMissing<IModel>(
						models,
						this.battle?.model1,
						this.battle?.model2,
						this.battle?.winnerModel,
					),
				),
			)
			.subscribe((models: IModel[]) => (this.modelsSharedCollection = models));
	}
}
