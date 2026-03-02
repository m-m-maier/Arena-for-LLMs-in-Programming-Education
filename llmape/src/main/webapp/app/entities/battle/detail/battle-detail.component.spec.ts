import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { BattleDetailComponent } from './battle-detail.component';

describe('Battle Management Detail Component', () => {
	let comp: BattleDetailComponent;
	let fixture: ComponentFixture<BattleDetailComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [BattleDetailComponent],
			providers: [
				provideRouter(
					[
						{
							path: '**',
							loadComponent: () => import('./battle-detail.component').then(m => m.BattleDetailComponent),
							resolve: { battle: () => of({ id: 22957 }) },
						},
					],
					withComponentInputBinding(),
				),
			],
		})
			.overrideTemplate(BattleDetailComponent, '')
			.compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(BattleDetailComponent);
		comp = fixture.componentInstance;
	});

	describe('OnInit', () => {
		it('Should load battle on init', async () => {
			const harness = await RouterTestingHarness.create();
			const instance = await harness.navigateByUrl('/', BattleDetailComponent);

			// THEN
			expect(instance.battle()).toEqual(expect.objectContaining({ id: 22957 }));
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
