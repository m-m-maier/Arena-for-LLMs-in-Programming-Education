import dayjs from 'dayjs/esm';

import { ILeaderboardEntry, NewLeaderboardEntry } from './leaderboard-entry.model';

export const sampleWithRequiredData: ILeaderboardEntry = {
	id: 7899,
	entryJson: 'dearly yuck offensively',
	timestamp: dayjs('2025-06-10T13:38'),
};

export const sampleWithPartialData: ILeaderboardEntry = {
	id: 2754,
	entryJson: 'ew reprove prestigious',
	timestamp: dayjs('2025-06-09T18:50'),
};

export const sampleWithFullData: ILeaderboardEntry = {
	id: 13264,
	entryJson: 'fearless across alliance',
	category: 'CODE_ASSESSMENT',
	timestamp: dayjs('2025-06-10T09:42'),
};

export const sampleWithNewData: NewLeaderboardEntry = {
	entryJson: 'accessorise legging',
	timestamp: dayjs('2025-06-10T02:30'),
	id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
