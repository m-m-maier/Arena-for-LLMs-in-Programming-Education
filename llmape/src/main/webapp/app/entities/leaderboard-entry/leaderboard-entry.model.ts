import dayjs from 'dayjs/esm';
import { Category } from 'app/entities/enumerations/category.model';

export interface ILeaderboardEntry {
	id: number;
	entryJson?: string | null;
	category?: keyof typeof Category | null;
	timestamp?: dayjs.Dayjs | null;
}

export type NewLeaderboardEntry = Omit<ILeaderboardEntry, 'id'> & { id: null };
