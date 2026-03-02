import { ComponentFixture, TestBed } from '@angular/core/testing';

import GeneratePromptsAndVotesComponent from './generatePromptsAndVotes.component';
import { AccountService } from 'app/core/auth/account.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

describe('GeneratePromptsAndVotesComponent', () => {
	let component: GeneratePromptsAndVotesComponent;
	let fixture: ComponentFixture<GeneratePromptsAndVotesComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [GeneratePromptsAndVotesComponent],
			providers: [AccountService, provideHttpClient(), provideHttpClientTesting()],
		})
			.overrideTemplate(GeneratePromptsAndVotesComponent, '')
			.compileComponents();

		fixture = TestBed.createComponent(GeneratePromptsAndVotesComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
