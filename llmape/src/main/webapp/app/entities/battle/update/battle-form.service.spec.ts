import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../battle.test-samples';

import { BattleFormService } from './battle-form.service';

describe('Battle Form Service', () => {
	let service: BattleFormService;

	beforeEach(() => {
		TestBed.configureTestingModule({});
		service = TestBed.inject(BattleFormService);
	});

	describe('Service methods', () => {
		describe('createBattleFormGroup', () => {
			it('should create a new form with FormControl', () => {
				const formGroup = service.createBattleFormGroup();

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						model1Answer: expect.any(Object),
						model2Answer: expect.any(Object),
						voteTimestamp: expect.any(Object),
						prompt: expect.any(Object),
						model1: expect.any(Object),
						model2: expect.any(Object),
						winnerModel: expect.any(Object),
					}),
				);
			});

			it('passing IBattle should create a new form with FormGroup', () => {
				const formGroup = service.createBattleFormGroup(sampleWithRequiredData);

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						model1Answer: expect.any(Object),
						model2Answer: expect.any(Object),
						voteTimestamp: expect.any(Object),
						prompt: expect.any(Object),
						model1: expect.any(Object),
						model2: expect.any(Object),
						winnerModel: expect.any(Object),
					}),
				);
			});
		});

		describe('getBattle', () => {
			it('should return NewBattle for default Battle initial value', () => {
				const formGroup = service.createBattleFormGroup(sampleWithNewData);

				const battle = service.getBattle(formGroup) as any;

				expect(battle).toMatchObject(sampleWithNewData);
			});

			it('should return NewBattle for empty Battle initial value', () => {
				const formGroup = service.createBattleFormGroup();

				const battle = service.getBattle(formGroup) as any;

				expect(battle).toMatchObject({});
			});

			it('should return IBattle', () => {
				const formGroup = service.createBattleFormGroup(sampleWithRequiredData);

				const battle = service.getBattle(formGroup) as any;

				expect(battle).toMatchObject(sampleWithRequiredData);
			});
		});

		describe('resetForm', () => {
			it('passing IBattle should not enable id FormControl', () => {
				const formGroup = service.createBattleFormGroup();
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, sampleWithRequiredData);

				expect(formGroup.controls.id.disabled).toBe(true);
			});

			it('passing NewBattle should disable id FormControl', () => {
				const formGroup = service.createBattleFormGroup(sampleWithRequiredData);
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, { id: null });

				expect(formGroup.controls.id.disabled).toBe(true);
			});
		});
	});
});
