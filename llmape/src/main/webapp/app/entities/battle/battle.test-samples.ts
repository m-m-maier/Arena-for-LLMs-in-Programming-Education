import dayjs from 'dayjs/esm';

import { IBattle, NewBattle } from './battle.model';

export const sampleWithRequiredData: IBattle = {
	id: 4186,
};

export const sampleWithPartialData: IBattle = {
	id: 21562,
	voteTimestamp: dayjs('2025-03-31T17:20'),
};

export const sampleWithFullData: IBattle = {
	id: 3858,
	model1Answer: 'supposing tame provided',
	model2Answer: 'meanwhile drat sleet',
	voteTimestamp: dayjs('2025-04-01T09:48'),
};

export const sampleWithNewData: NewBattle = {
	id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
