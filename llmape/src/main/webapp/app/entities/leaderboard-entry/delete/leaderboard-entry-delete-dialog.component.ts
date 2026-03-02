import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ILeaderboardEntry } from '../leaderboard-entry.model';
import { LeaderboardEntryService } from '../service/leaderboard-entry.service';

@Component({
	templateUrl: './leaderboard-entry-delete-dialog.component.html',
	imports: [SharedModule, FormsModule],
})
export class LeaderboardEntryDeleteDialogComponent {
	leaderboardEntry?: ILeaderboardEntry;

	protected leaderboardEntryService = inject(LeaderboardEntryService);
	protected activeModal = inject(NgbActiveModal);

	cancel(): void {
		this.activeModal.dismiss();
	}

	confirmDelete(id: number): void {
		this.leaderboardEntryService.delete(id).subscribe(() => {
			this.activeModal.close(ITEM_DELETED_EVENT);
		});
	}
}
