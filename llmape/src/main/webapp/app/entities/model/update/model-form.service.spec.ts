import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../model.test-samples';

import { ModelFormService } from './model-form.service';

describe('Model Form Service', () => {
	let service: ModelFormService;

	beforeEach(() => {
		TestBed.configureTestingModule({});
		service = TestBed.inject(ModelFormService);
	});

	describe('Service methods', () => {
		describe('createModelFormGroup', () => {
			it('should create a new form with FormControl', () => {
				const formGroup = service.createModelFormGroup();

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						modelName: expect.any(Object),
						organization: expect.any(Object),
						provider: expect.any(Object),
						apiKey: expect.any(Object),
						baseUrl: expect.any(Object),
						license: expect.any(Object),
						active: expect.any(Object),
					}),
				);
			});

			it('passing IModel should create a new form with FormGroup', () => {
				const formGroup = service.createModelFormGroup(sampleWithRequiredData);

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						modelName: expect.any(Object),
						organization: expect.any(Object),
						provider: expect.any(Object),
						apiKey: expect.any(Object),
						baseUrl: expect.any(Object),
						license: expect.any(Object),
						active: expect.any(Object),
					}),
				);
			});
		});

		describe('getModel', () => {
			it('should return NewModel for default Model initial value', () => {
				const formGroup = service.createModelFormGroup(sampleWithNewData);

				const model = service.getModel(formGroup) as any;

				expect(model).toMatchObject(sampleWithNewData);
			});

			it('should return NewModel for empty Model initial value', () => {
				const formGroup = service.createModelFormGroup();

				const model = service.getModel(formGroup) as any;

				expect(model).toMatchObject({});
			});

			it('should return IModel', () => {
				const formGroup = service.createModelFormGroup(sampleWithRequiredData);

				const model = service.getModel(formGroup) as any;

				expect(model).toMatchObject(sampleWithRequiredData);
			});
		});

		describe('resetForm', () => {
			it('passing IModel should not enable id FormControl', () => {
				const formGroup = service.createModelFormGroup();
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, sampleWithRequiredData);

				expect(formGroup.controls.id.disabled).toBe(true);
			});

			it('passing NewModel should disable id FormControl', () => {
				const formGroup = service.createModelFormGroup(sampleWithRequiredData);
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, { id: null });

				expect(formGroup.controls.id.disabled).toBe(true);
			});
		});
	});
});
