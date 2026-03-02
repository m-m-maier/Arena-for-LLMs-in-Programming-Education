import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IPrompt } from '../prompt.model';

@Component({
	selector: 'jhi-prompt-detail',
	templateUrl: './prompt-detail.component.html',
	imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class PromptDetailComponent {
	prompt = input<IPrompt | null>(null);

	previousState(): void {
		window.history.back();
	}
}
