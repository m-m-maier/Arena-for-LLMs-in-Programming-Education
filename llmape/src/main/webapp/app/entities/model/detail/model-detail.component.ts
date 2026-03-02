import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IModel } from '../model.model';

@Component({
	selector: 'jhi-model-detail',
	templateUrl: './model-detail.component.html',
	imports: [SharedModule, RouterModule],
})
export class ModelDetailComponent {
	model = input<IModel | null>(null);

	previousState(): void {
		window.history.back();
	}
}
