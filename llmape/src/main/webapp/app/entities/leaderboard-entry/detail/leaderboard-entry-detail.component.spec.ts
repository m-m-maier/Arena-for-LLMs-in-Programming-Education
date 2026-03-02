import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { LeaderboardEntryDetailComponent } from './leaderboard-entry-detail.component';

describe('LeaderboardEntry Management Detail Component', () => {
	let comp: LeaderboardEntryDetailComponent;
	let fixture: ComponentFixture<LeaderboardEntryDetailComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [LeaderboardEntryDetailComponent],
			providers: [
				provideRouter(
					[
						{
							path: '**',
							loadComponent: () => import('./leaderboard-entry-detail.component').then(m => m.LeaderboardEntryDetailComponent),
							resolve: { leaderboardEntry: () => of({ id: 18203 }) },
						},
					],
					withComponentInputBinding(),
				),
			],
		})
			.overrideTemplate(LeaderboardEntryDetailComponent, '')
			.compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(LeaderboardEntryDetailComponent);
		comp = fixture.componentInstance;
	});

	describe('OnInit', () => {
		it('Should load leaderboardEntry on init', async () => {
			const harness = await RouterTestingHarness.create();
			const instance = await harness.navigateByUrl('/', LeaderboardEntryDetailComponent);

			// THEN
			expect(instance.leaderboardEntry()).toEqual(expect.objectContaining({ id: 18203 }));
		});
	});

	describe('PreviousState', () => {
		it('Should navigate to previous state', () => {
			jest.spyOn(window.history, 'back');
			comp.previousState();
			expect(window.history.back).toHaveBeenCalled();
		});
	});
});
