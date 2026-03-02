import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPrompt } from '../prompt.model';
import { PromptService } from '../service/prompt.service';

@Component({
	templateUrl: './prompt-delete-dialog.component.html',
	imports: [SharedModule, FormsModule],
})
export class PromptDeleteDialogComponent {
	prompt?: IPrompt;

	protected promptService = inject(PromptService);
	protected activeModal = inject(NgbActiveModal);

	cancel(): void {
		this.activeModal.dismiss();
	}

	confirmDelete(id: number): void {
		this.promptService.delete(id).subscribe(() => {
			this.activeModal.close(ITEM_DELETED_EVENT);
		});
	}
}
