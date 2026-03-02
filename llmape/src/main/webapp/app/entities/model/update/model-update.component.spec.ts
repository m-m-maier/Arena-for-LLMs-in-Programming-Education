import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ModelService } from '../service/model.service';
import { IModel } from '../model.model';
import { ModelFormService } from './model-form.service';

import { ModelUpdateComponent } from './model-update.component';

describe('Model Management Update Component', () => {
	let comp: ModelUpdateComponent;
	let fixture: ComponentFixture<ModelUpdateComponent>;
	let activatedRoute: ActivatedRoute;
	let modelFormService: ModelFormService;
	let modelService: ModelService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [ModelUpdateComponent],
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
			.overrideTemplate(ModelUpdateComponent, '')
			.compileComponents();

		fixture = TestBed.createComponent(ModelUpdateComponent);
		activatedRoute = TestBed.inject(ActivatedRoute);
		modelFormService = TestBed.inject(ModelFormService);
		modelService = TestBed.inject(ModelService);

		comp = fixture.componentInstance;
	});

	describe('ngOnInit', () => {
		it('Should update editForm', () => {
			const model: IModel = { id: 873 };

			activatedRoute.data = of({ model });
			comp.ngOnInit();

			expect(comp.model).toEqual(model);
		});
	});

	describe('save', () => {
		it('Should call update service on save for existing entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IModel>>();
			const model = { id: 2694 };
			jest.spyOn(modelFormService, 'getModel').mockReturnValue(model);
			jest.spyOn(modelService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ model });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: model }));
			saveSubject.complete();

			// THEN
			expect(modelFormService.getModel).toHaveBeenCalled();
			expect(comp.previousState).toHaveBeenCalled();
			expect(modelService.update).toHaveBeenCalledWith(expect.objectContaining(model));
			expect(comp.isSaving).toEqual(false);
		});

		it('Should call create service on save for new entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IModel>>();
			const model = { id: 2694 };
			jest.spyOn(modelFormService, 'getModel').mockReturnValue({ id: null });
			jest.spyOn(modelService, 'create').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ model: null });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: model }));
			saveSubject.complete();

			// THEN
			expect(modelFormService.getModel).toHaveBeenCalled();
			expect(modelService.create).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).toHaveBeenCalled();
		});

		it('Should set isSaving to false on error', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IModel>>();
			const model = { id: 2694 };
			jest.spyOn(modelService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ model });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.error('This is an error!');

			// THEN
			expect(modelService.update).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).not.toHaveBeenCalled();
		});
	});
});
