import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { PromptService } from '../service/prompt.service';
import { IPrompt } from '../prompt.model';
import { PromptFormService } from './prompt-form.service';

import { PromptUpdateComponent } from './prompt-update.component';

describe('Prompt Management Update Component', () => {
	let comp: PromptUpdateComponent;
	let fixture: ComponentFixture<PromptUpdateComponent>;
	let activatedRoute: ActivatedRoute;
	let promptFormService: PromptFormService;
	let promptService: PromptService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [PromptUpdateComponent],
			providers: [
				provideHttpClient(),
				FormBuilder,
				{
					provide: ActivatedRoute,
					useValue: {
						params: from([{}]),
					},
				},
			],
		})
			.overrideTemplate(PromptUpdateComponent, '')
			.compileComponents();

		fixture = TestBed.createComponent(PromptUpdateComponent);
		activatedRoute = TestBed.inject(ActivatedRoute);
		promptFormService = TestBed.inject(PromptFormService);
		promptService = TestBed.inject(PromptService);

		comp = fixture.componentInstance;
	});

	describe('ngOnInit', () => {
		it('Should update editForm', () => {
			const prompt: IPrompt = { id: 29985 };

			activatedRoute.data = of({ prompt });
			comp.ngOnInit();

			expect(comp.prompt).toEqual(prompt);
		});
	});

	describe('save', () => {
		it('Should call update service on save for existing entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IPrompt>>();
			const prompt = { id: 3853 };
			jest.spyOn(promptFormService, 'getPrompt').mockReturnValue(prompt);
			jest.spyOn(promptService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ prompt });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: prompt }));
			saveSubject.complete();

			// THEN
			expect(promptFormService.getPrompt).toHaveBeenCalled();
			expect(comp.previousState).toHaveBeenCalled();
			expect(promptService.update).toHaveBeenCalledWith(expect.objectContaining(prompt));
			expect(comp.isSaving).toEqual(false);
		});

		it('Should call create service on save for new entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IPrompt>>();
			const prompt = { id: 3853 };
			jest.spyOn(promptFormService, 'getPrompt').mockReturnValue({ id: null });
			jest.spyOn(promptService, 'create').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ prompt: null });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: prompt }));
			saveSubject.complete();

			// THEN
			expect(promptFormService.getPrompt).toHaveBeenCalled();
			expect(promptService.create).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).toHaveBeenCalled();
		});

		it('Should set isSaving to false on error', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IPrompt>>();
			const prompt = { id: 3853 };
			jest.spyOn(promptService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ prompt });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.error('This is an error!');

			// THEN
			expect(promptService.update).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).not.toHaveBeenCalled();
		});
	});
});
