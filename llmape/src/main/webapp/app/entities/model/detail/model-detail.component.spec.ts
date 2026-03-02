import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ModelDetailComponent } from './model-detail.component';

describe('Model Management Detail Component', () => {
	let comp: ModelDetailComponent;
	let fixture: ComponentFixture<ModelDetailComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [ModelDetailComponent],
			providers: [
				provideRouter(
					[
						{
							path: '**',
							loadComponent: () => import('./model-detail.component').then(m => m.ModelDetailComponent),
							resolve: { model: () => of({ id: 2694 }) },
						},
					],
					withComponentInputBinding(),
				),
			],
		})
			.overrideTemplate(ModelDetailComponent, '')
			.compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(ModelDetailComponent);
		comp = fixture.componentInstance;
	});

	describe('OnInit', () => {
		it('Should load model on init', async () => {
			const harness = await RouterTestingHarness.create();
			const instance = await harness.navigateByUrl('/', ModelDetailComponent);

			// THEN
			expect(instance.model()).toEqual(expect.objectContaining({ id: 2694 }));
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
