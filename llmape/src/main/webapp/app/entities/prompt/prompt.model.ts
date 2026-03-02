import dayjs from 'dayjs/esm';
import { Category } from 'app/entities/enumerations/category.model';

export interface IPrompt {
	id: number;
	promptText?: string | null;
	category?: keyof typeof Category | null;
	isRejected?: boolean | null;
	isFromPublicPage?: boolean | null;
	timestamp?: dayjs.Dayjs | null;
	sessionId?: string | null;
	generationModelId?: number | null;
}

export type NewPrompt = Omit<IPrompt, 'id'> & { id: null };
