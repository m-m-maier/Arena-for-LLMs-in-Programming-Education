import dayjs from 'dayjs/esm';

import { IPrompt, NewPrompt } from './prompt.model';

export const sampleWithRequiredData: IPrompt = {
	id: 14908,
	promptText: 'stale provided or',
	isRejected: false,
	isFromPublicPage: true,
	timestamp: dayjs('2025-04-01T12:45'),
	sessionId: 'obsess',
};

export const sampleWithPartialData: IPrompt = {
	id: 13989,
	promptText: 'bleakly fireplace',
	category: 'CODE_ASSESSMENT',
	isRejected: false,
	isFromPublicPage: false,
	timestamp: dayjs('2025-04-01T05:39'),
	sessionId: 'wiggly',
	generationModelId: 15129,
};

export const sampleWithFullData: IPrompt = {
	id: 31408,
	promptText: 'till',
	category: 'CODE_ASSESSMENT',
	isRejected: false,
	isFromPublicPage: false,
	timestamp: dayjs('2025-04-01T07:03'),
	sessionId: 'vague questionably ick',
	generationModelId: 30714,
};

export const sampleWithNewData: NewPrompt = {
	promptText: 'ew amidst',
	isRejected: true,
	isFromPublicPage: true,
	timestamp: dayjs('2025-04-01T03:36'),
	sessionId: 'aside rudely modulo',
	id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
