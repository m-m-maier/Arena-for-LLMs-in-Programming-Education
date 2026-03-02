import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { Category } from 'app/entities/enumerations/category.model';
import { IPrompt } from '../prompt.model';
import { PromptService } from '../service/prompt.service';
import { PromptFormGroup, PromptFormService } from './prompt-form.service';

@Component({
	selector: 'jhi-prompt-update',
	templateUrl: './prompt-update.component.html',
	imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PromptUpdateComponent implements OnInit {
	isSaving = false;
	prompt: IPrompt | null = null;
	categoryValues = Object.keys(Category);

	protected promptService = inject(PromptService);
	protected promptFormService = inject(PromptFormService);
	protected activatedRoute = inject(ActivatedRoute);

	// eslint-disable-next-line @typescript-eslint/member-ordering
	editForm: PromptFormGroup = this.promptFormService.createPromptFormGroup();

	ngOnInit(): void {
		this.activatedRoute.data.subscribe(({ prompt }) => {
			this.prompt = prompt;
			if (prompt) {
				this.updateForm(prompt);
			}
		});
	}

	previousState(): void {
		window.history.back();
	}

	save(): void {
		this.isSaving = true;
		const prompt = this.promptFormService.getPrompt(this.editForm);
		if (prompt.id !== null) {
			this.subscribeToSaveResponse(this.promptService.update(prompt));
		} else {
			this.subscribeToSaveResponse(this.promptService.create(prompt));
		}
	}

	protected subscribeToSaveResponse(result: Observable<HttpResponse<IPrompt>>): void {
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

	protected updateForm(prompt: IPrompt): void {
		this.prompt = prompt;
		this.promptFormService.resetForm(this.editForm, prompt);
	}
}
