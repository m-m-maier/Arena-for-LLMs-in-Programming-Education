import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IModel } from '../model.model';
import { ModelService } from '../service/model.service';

@Component({
	templateUrl: './model-delete-dialog.component.html',
	imports: [SharedModule, FormsModule],
})
export class ModelDeleteDialogComponent {
	model?: IModel;

	protected modelService = inject(ModelService);
	protected activeModal = inject(NgbActiveModal);

	cancel(): void {
		this.activeModal.dismiss();
	}

	confirmDelete(id: number): void {
		this.modelService.delete(id).subscribe(() => {
			this.activeModal.close(ITEM_DELETED_EVENT);
		});
	}
}
