import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../prompt.test-samples';

import { PromptFormService } from './prompt-form.service';

describe('Prompt Form Service', () => {
	let service: PromptFormService;

	beforeEach(() => {
		TestBed.configureTestingModule({});
		service = TestBed.inject(PromptFormService);
	});

	describe('Service methods', () => {
		describe('createPromptFormGroup', () => {
			it('should create a new form with FormControl', () => {
				const formGroup = service.createPromptFormGroup();

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						promptText: expect.any(Object),
						category: expect.any(Object),
						isRejected: expect.any(Object),
						isFromPublicPage: expect.any(Object),
						timestamp: expect.any(Object),
						sessionId: expect.any(Object),
						generationModelId: expect.any(Object),
					}),
				);
			});

			it('passing IPrompt should create a new form with FormGroup', () => {
				const formGroup = service.createPromptFormGroup(sampleWithRequiredData);

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						promptText: expect.any(Object),
						category: expect.any(Object),
						isRejected: expect.any(Object),
						isFromPublicPage: expect.any(Object),
						timestamp: expect.any(Object),
						sessionId: expect.any(Object),
						generationModelId: expect.any(Object),
					}),
				);
			});
		});

		describe('getPrompt', () => {
			it('should return NewPrompt for default Prompt initial value', () => {
				const formGroup = service.createPromptFormGroup(sampleWithNewData);

				const prompt = service.getPrompt(formGroup) as any;

				expect(prompt).toMatchObject(sampleWithNewData);
			});

			it('should return NewPrompt for empty Prompt initial value', () => {
				const formGroup = service.createPromptFormGroup();

				const prompt = service.getPrompt(formGroup) as any;

				expect(prompt).toMatchObject({});
			});

			it('should return IPrompt', () => {
				const formGroup = service.createPromptFormGroup(sampleWithRequiredData);

				const prompt = service.getPrompt(formGroup) as any;

				expect(prompt).toMatchObject(sampleWithRequiredData);
			});
		});

		describe('resetForm', () => {
			it('passing IPrompt should not enable id FormControl', () => {
				const formGroup = service.createPromptFormGroup();
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, sampleWithRequiredData);

				expect(formGroup.controls.id.disabled).toBe(true);
			});

			it('passing NewPrompt should disable id FormControl', () => {
				const formGroup = service.createPromptFormGroup(sampleWithRequiredData);
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, { id: null });

				expect(formGroup.controls.id.disabled).toBe(true);
			});
		});
	});
});
