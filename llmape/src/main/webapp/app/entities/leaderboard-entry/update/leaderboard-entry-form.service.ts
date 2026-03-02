import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ILeaderboardEntry, NewLeaderboardEntry } from '../leaderboard-entry.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ILeaderboardEntry for edit and NewLeaderboardEntryFormGroupInput for create.
 */
type LeaderboardEntryFormGroupInput = ILeaderboardEntry | PartialWithRequiredKeyOf<NewLeaderboardEntry>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ILeaderboardEntry | NewLeaderboardEntry> = Omit<T, 'timestamp'> & {
	timestamp?: string | null;
};

type LeaderboardEntryFormRawValue = FormValueOf<ILeaderboardEntry>;

type NewLeaderboardEntryFormRawValue = FormValueOf<NewLeaderboardEntry>;

type LeaderboardEntryFormDefaults = Pick<NewLeaderboardEntry, 'id' | 'timestamp'>;

type LeaderboardEntryFormGroupContent = {
	id: FormControl<LeaderboardEntryFormRawValue['id'] | NewLeaderboardEntry['id']>;
	entryJson: FormControl<LeaderboardEntryFormRawValue['entryJson']>;
	category: FormControl<LeaderboardEntryFormRawValue['category']>;
	timestamp: FormControl<LeaderboardEntryFormRawValue['timestamp']>;
};

export type LeaderboardEntryFormGroup = FormGroup<LeaderboardEntryFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class LeaderboardEntryFormService {
	createLeaderboardEntryFormGroup(leaderboardEntry: LeaderboardEntryFormGroupInput = { id: null }): LeaderboardEntryFormGroup {
		const leaderboardEntryRawValue = this.convertLeaderboardEntryToLeaderboardEntryRawValue({
			...this.getFormDefaults(),
			...leaderboardEntry,
		});
		return new FormGroup<LeaderboardEntryFormGroupContent>({
			id: new FormControl(
				{ value: leaderboardEntryRawValue.id, disabled: true },
				{
					nonNullable: true,
					validators: [Validators.required],
				},
			),
			entryJson: new FormControl(leaderboardEntryRawValue.entryJson, {
				validators: [Validators.required, Validators.maxLength(8192)],
			}),
			category: new FormControl(leaderboardEntryRawValue.category),
			timestamp: new FormControl(leaderboardEntryRawValue.timestamp, {
				validators: [Validators.required],
			}),
		});
	}

	getLeaderboardEntry(form: LeaderboardEntryFormGroup): ILeaderboardEntry | NewLeaderboardEntry {
		return this.convertLeaderboardEntryRawValueToLeaderboardEntry(
			form.getRawValue() as LeaderboardEntryFormRawValue | NewLeaderboardEntryFormRawValue,
		);
	}

	resetForm(form: LeaderboardEntryFormGroup, leaderboardEntry: LeaderboardEntryFormGroupInput): void {
		const leaderboardEntryRawValue = this.convertLeaderboardEntryToLeaderboardEntryRawValue({
			...this.getFormDefaults(),
			...leaderboardEntry,
		});
		form.reset(
			{
				...leaderboardEntryRawValue,
				id: { value: leaderboardEntryRawValue.id, disabled: true },
			} as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
		);
	}

	private getFormDefaults(): LeaderboardEntryFormDefaults {
		const currentTime = dayjs();

		return {
			id: null,
			timestamp: currentTime,
		};
	}

	private convertLeaderboardEntryRawValueToLeaderboardEntry(
		rawLeaderboardEntry: LeaderboardEntryFormRawValue | NewLeaderboardEntryFormRawValue,
	): ILeaderboardEntry | NewLeaderboardEntry {
		return {
			...rawLeaderboardEntry,
			timestamp: dayjs(rawLeaderboardEntry.timestamp, DATE_TIME_FORMAT),
		};
	}

	private convertLeaderboardEntryToLeaderboardEntryRawValue(
		leaderboardEntry: ILeaderboardEntry | (Partial<NewLeaderboardEntry> & LeaderboardEntryFormDefaults),
	): LeaderboardEntryFormRawValue | PartialWithRequiredKeyOf<NewLeaderboardEntryFormRawValue> {
		return {
			...leaderboardEntry,
			timestamp: leaderboardEntry.timestamp ? leaderboardEntry.timestamp.format(DATE_TIME_FORMAT) : undefined,
		};
	}
}
