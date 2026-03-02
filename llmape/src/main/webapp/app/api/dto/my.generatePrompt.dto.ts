import { Category } from 'app/entities/enumerations/category.model';

export interface IGeneratePromptDTO {
	modelId: number;
	category: Category;
}
