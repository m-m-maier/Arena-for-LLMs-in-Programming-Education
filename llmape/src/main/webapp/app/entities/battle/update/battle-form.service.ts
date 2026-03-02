import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IBattle, NewBattle } from '../battle.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IBattle for edit and NewBattleFormGroupInput for create.
 */
type BattleFormGroupInput = IBattle | PartialWithRequiredKeyOf<NewBattle>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IBattle | NewBattle> = Omit<T, 'voteTimestamp'> & {
	voteTimestamp?: string | null;
};

type BattleFormRawValue = FormValueOf<IBattle>;

type NewBattleFormRawValue = FormValueOf<NewBattle>;

type BattleFormDefaults = Pick<NewBattle, 'id' | 'voteTimestamp'>;

type BattleFormGroupContent = {
	id: FormControl<BattleFormRawValue['id'] | NewBattle['id']>;
	model1Answer: FormControl<BattleFormRawValue['model1Answer']>;
	model2Answer: FormControl<BattleFormRawValue['model2Answer']>;
	voteTimestamp: FormControl<BattleFormRawValue['voteTimestamp']>;
	prompt: FormControl<BattleFormRawValue['prompt']>;
	model1: FormControl<BattleFormRawValue['model1']>;
	model2: FormControl<BattleFormRawValue['model2']>;
	winnerModel: FormControl<BattleFormRawValue['winnerModel']>;
};

export type BattleFormGroup = FormGroup<BattleFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BattleFormService {
	createBattleFormGroup(battle: BattleFormGroupInput = { id: null }): BattleFormGroup {
		const battleRawValue = this.convertBattleToBattleRawValue({
			...this.getFormDefaults(),
			...battle,
		});
		return new FormGroup<BattleFormGroupContent>({
			id: new FormControl(
				{ value: battleRawValue.id, disabled: true },
				{
					nonNullable: true,
					validators: [Validators.required],
				},
			),
			model1Answer: new FormControl(battleRawValue.model1Answer, {
				validators: [Validators.maxLength(16320)],
			}),
			model2Answer: new FormControl(battleRawValue.model2Answer, {
				validators: [Validators.maxLength(16320)],
			}),
			voteTimestamp: new FormControl(battleRawValue.voteTimestamp),
			prompt: new FormControl(battleRawValue.prompt),
			model1: new FormControl(battleRawValue.model1),
			model2: new FormControl(battleRawValue.model2),
			winnerModel: new FormControl(battleRawValue.winnerModel),
		});
	}

	getBattle(form: BattleFormGroup): IBattle | NewBattle {
		return this.convertBattleRawValueToBattle(form.getRawValue() as BattleFormRawValue | NewBattleFormRawValue);
	}

	resetForm(form: BattleFormGroup, battle: BattleFormGroupInput): void {
		const battleRawValue = this.convertBattleToBattleRawValue({ ...this.getFormDefaults(), ...battle });
		form.reset(
			{
				...battleRawValue,
				id: { value: battleRawValue.id, disabled: true },
			} as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
		);
	}

	private getFormDefaults(): BattleFormDefaults {
		const currentTime = dayjs();

		return {
			id: null,
			voteTimestamp: currentTime,
		};
	}

	private convertBattleRawValueToBattle(rawBattle: BattleFormRawValue | NewBattleFormRawValue): IBattle | NewBattle {
		return {
			...rawBattle,
			voteTimestamp: dayjs(rawBattle.voteTimestamp, DATE_TIME_FORMAT),
		};
	}

	private convertBattleToBattleRawValue(
		battle: IBattle | (Partial<NewBattle> & BattleFormDefaults),
	): BattleFormRawValue | PartialWithRequiredKeyOf<NewBattleFormRawValue> {
		return {
			...battle,
			voteTimestamp: battle.voteTimestamp ? battle.voteTimestamp.format(DATE_TIME_FORMAT) : undefined,
		};
	}
}
