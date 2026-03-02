import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../leaderboard-entry.test-samples';

import { LeaderboardEntryFormService } from './leaderboard-entry-form.service';

describe('LeaderboardEntry Form Service', () => {
	let service: LeaderboardEntryFormService;

	beforeEach(() => {
		TestBed.configureTestingModule({});
		service = TestBed.inject(LeaderboardEntryFormService);
	});

	describe('Service methods', () => {
		describe('createLeaderboardEntryFormGroup', () => {
			it('should create a new form with FormControl', () => {
				const formGroup = service.createLeaderboardEntryFormGroup();

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						entryJson: expect.any(Object),
						category: expect.any(Object),
						timestamp: expect.any(Object),
					}),
				);
			});

			it('passing ILeaderboardEntry should create a new form with FormGroup', () => {
				const formGroup = service.createLeaderboardEntryFormGroup(sampleWithRequiredData);

				expect(formGroup.controls).toEqual(
					expect.objectContaining({
						id: expect.any(Object),
						entryJson: expect.any(Object),
						category: expect.any(Object),
						timestamp: expect.any(Object),
					}),
				);
			});
		});

		describe('getLeaderboardEntry', () => {
			it('should return NewLeaderboardEntry for default LeaderboardEntry initial value', () => {
				const formGroup = service.createLeaderboardEntryFormGroup(sampleWithNewData);

				const leaderboardEntry = service.getLeaderboardEntry(formGroup) as any;

				expect(leaderboardEntry).toMatchObject(sampleWithNewData);
			});

			it('should return NewLeaderboardEntry for empty LeaderboardEntry initial value', () => {
				const formGroup = service.createLeaderboardEntryFormGroup();

				const leaderboardEntry = service.getLeaderboardEntry(formGroup) as any;

				expect(leaderboardEntry).toMatchObject({});
			});

			it('should return ILeaderboardEntry', () => {
				const formGroup = service.createLeaderboardEntryFormGroup(sampleWithRequiredData);

				const leaderboardEntry = service.getLeaderboardEntry(formGroup) as any;

				expect(leaderboardEntry).toMatchObject(sampleWithRequiredData);
			});
		});

		describe('resetForm', () => {
			it('passing ILeaderboardEntry should not enable id FormControl', () => {
				const formGroup = service.createLeaderboardEntryFormGroup();
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, sampleWithRequiredData);

				expect(formGroup.controls.id.disabled).toBe(true);
			});

			it('passing NewLeaderboardEntry should disable id FormControl', () => {
				const formGroup = service.createLeaderboardEntryFormGroup(sampleWithRequiredData);
				expect(formGroup.controls.id.disabled).toBe(true);

				service.resetForm(formGroup, { id: null });

				expect(formGroup.controls.id.disabled).toBe(true);
			});
		});
	});
});
