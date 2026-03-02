import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ILeaderboardEntry } from '../leaderboard-entry.model';

@Component({
	selector: 'jhi-leaderboard-entry-detail',
	templateUrl: './leaderboard-entry-detail.component.html',
	imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class LeaderboardEntryDetailComponent {
	leaderboardEntry = input<ILeaderboardEntry | null>(null);

	previousState(): void {
		window.history.back();
	}
}
