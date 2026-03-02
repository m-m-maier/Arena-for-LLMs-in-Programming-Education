export interface IModel {
	id: number;
	modelName?: string | null;
	organization?: string | null;
	provider?: string | null;
	apiKey?: string | null;
	baseUrl?: string | null;
	license?: string | null;
	active?: boolean | null;
}

export type NewModel = Omit<IModel, 'id'> & { id: null };
