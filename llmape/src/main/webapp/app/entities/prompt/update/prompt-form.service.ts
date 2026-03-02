import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IPrompt, NewPrompt } from '../prompt.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPrompt for edit and NewPromptFormGroupInput for create.
 */
type PromptFormGroupInput = IPrompt | PartialWithRequiredKeyOf<NewPrompt>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IPrompt | NewPrompt> = Omit<T, 'timestamp'> & {
	timestamp?: string | null;
};

type PromptFormRawValue = FormValueOf<IPrompt>;

type NewPromptFormRawValue = FormValueOf<NewPrompt>;

type PromptFormDefaults = Pick<NewPrompt, 'id' | 'isRejected' | 'isFromPublicPage' | 'timestamp'>;

type PromptFormGroupContent = {
	id: FormControl<PromptFormRawValue['id'] | NewPrompt['id']>;
	promptText: FormControl<PromptFormRawValue['promptText']>;
	category: FormControl<PromptFormRawValue['category']>;
	isRejected: FormControl<PromptFormRawValue['isRejected']>;
	isFromPublicPage: FormControl<PromptFormRawValue['isFromPublicPage']>;
	timestamp: FormControl<PromptFormRawValue['timestamp']>;
	sessionId: FormControl<PromptFormRawValue['sessionId']>;
	generationModelId: FormControl<PromptFormRawValue['generationModelId']>;
};

export type PromptFormGroup = FormGroup<PromptFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PromptFormService {
	createPromptFormGroup(prompt: PromptFormGroupInput = { id: null }): PromptFormGroup {
		const promptRawValue = this.convertPromptToPromptRawValue({
			...this.getFormDefaults(),
			...prompt,
		});
		return new FormGroup<PromptFormGroupContent>({
			id: new FormControl(
				{ value: promptRawValue.id, disabled: true },
				{
					nonNullable: true,
					validators: [Validators.required],
				},
			),
			promptText: new FormControl(promptRawValue.promptText, {
				validators: [Validators.required, Validators.maxLength(16320)],
			}),
			category: new FormControl(promptRawValue.category),
			isRejected: new FormControl(promptRawValue.isRejected, {
				validators: [Validators.required],
			}),
			isFromPublicPage: new FormControl(promptRawValue.isFromPublicPage, {
				validators: [Validators.required],
			}),
			timestamp: new FormControl(promptRawValue.timestamp, {
				validators: [Validators.required],
			}),
			sessionId: new FormControl(promptRawValue.sessionId, {
				validators: [Validators.required],
			}),
			generationModelId: new FormControl(promptRawValue.generationModelId),
		});
	}

	getPrompt(form: PromptFormGroup): IPrompt | NewPrompt {
		return this.convertPromptRawValueToPrompt(form.getRawValue() as PromptFormRawValue | NewPromptFormRawValue);
	}

	resetForm(form: PromptFormGroup, prompt: PromptFormGroupInput): void {
		const promptRawValue = this.convertPromptToPromptRawValue({ ...this.getFormDefaults(), ...prompt });
		form.reset(
			{
				...promptRawValue,
				id: { value: promptRawValue.id, disabled: true },
			} as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
		);
	}

	private getFormDefaults(): PromptFormDefaults {
		const currentTime = dayjs();

		return {
			id: null,
			isRejected: false,
			isFromPublicPage: false,
			timestamp: currentTime,
		};
	}

	private convertPromptRawValueToPrompt(rawPrompt: PromptFormRawValue | NewPromptFormRawValue): IPrompt | NewPrompt {
		return {
			...rawPrompt,
			timestamp: dayjs(rawPrompt.timestamp, DATE_TIME_FORMAT),
		};
	}

	private convertPromptToPromptRawValue(
		prompt: IPrompt | (Partial<NewPrompt> & PromptFormDefaults),
	): PromptFormRawValue | PartialWithRequiredKeyOf<NewPromptFormRawValue> {
		return {
			...prompt,
			timestamp: prompt.timestamp ? prompt.timestamp.format(DATE_TIME_FORMAT) : undefined,
		};
	}
}
