import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IModel } from '../model.model';
import { ModelService } from '../service/model.service';
import { ModelFormGroup, ModelFormService } from './model-form.service';

@Component({
	selector: 'jhi-model-update',
	templateUrl: './model-update.component.html',
	imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ModelUpdateComponent implements OnInit {
	isSaving = false;
	model: IModel | null = null;

	protected modelService = inject(ModelService);
	protected modelFormService = inject(ModelFormService);
	protected activatedRoute = inject(ActivatedRoute);

	// eslint-disable-next-line @typescript-eslint/member-ordering
	editForm: ModelFormGroup = this.modelFormService.createModelFormGroup();

	ngOnInit(): void {
		this.activatedRoute.data.subscribe(({ model }) => {
			this.model = model;
			if (model) {
				this.updateForm(model);
			}
		});
	}

	previousState(): void {
		window.history.back();
	}

	save(): void {
		this.isSaving = true;
		const model = this.modelFormService.getModel(this.editForm);
		if (model.id !== null) {
			this.subscribeToSaveResponse(this.modelService.update(model));
		} else {
			this.subscribeToSaveResponse(this.modelService.create(model));
		}
	}

	protected subscribeToSaveResponse(result: Observable<HttpResponse<IModel>>): void {
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

	protected updateForm(model: IModel): void {
		this.model = model;
		this.modelFormService.resetForm(this.editForm, model);
	}
}
