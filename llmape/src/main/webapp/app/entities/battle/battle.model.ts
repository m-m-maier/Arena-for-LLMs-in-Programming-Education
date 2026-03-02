import dayjs from 'dayjs/esm';
import { IPrompt } from 'app/entities/prompt/prompt.model';
import { IModel } from 'app/entities/model/model.model';

export interface IBattle {
	id: number;
	model1Answer?: string | null;
	model2Answer?: string | null;
	voteTimestamp?: dayjs.Dayjs | null;
	prompt?: IPrompt | null;
	model1?: IModel | null;
	model2?: IModel | null;
	winnerModel?: IModel | null;
}

export type NewBattle = Omit<IBattle, 'id'> & { id: null };
