import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IBattle } from '../battle.model';

@Component({
	selector: 'jhi-battle-detail',
	templateUrl: './battle-detail.component.html',
	imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class BattleDetailComponent {
	battle = input<IBattle | null>(null);

	previousState(): void {
		window.history.back();
	}
}
