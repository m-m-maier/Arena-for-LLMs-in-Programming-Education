import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { PromptDetailComponent } from './prompt-detail.component';

describe('Prompt Management Detail Component', () => {
	let comp: PromptDetailComponent;
	let fixture: ComponentFixture<PromptDetailComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [PromptDetailComponent],
			providers: [
				provideRouter(
					[
						{
							path: '**',
							loadComponent: () => import('./prompt-detail.component').then(m => m.PromptDetailComponent),
							resolve: { prompt: () => of({ id: 3853 }) },
						},
					],
					withComponentInputBinding(),
				),
			],
		})
			.overrideTemplate(PromptDetailComponent, '')
			.compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(PromptDetailComponent);
		comp = fixture.componentInstance;
	});

	describe('OnInit', () => {
		it('Should load prompt on init', async () => {
			const harness = await RouterTestingHarness.create();
			const instance = await harness.navigateByUrl('/', PromptDetailComponent);

			// THEN
			expect(instance.prompt()).toEqual(expect.objectContaining({ id: 3853 }));
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
