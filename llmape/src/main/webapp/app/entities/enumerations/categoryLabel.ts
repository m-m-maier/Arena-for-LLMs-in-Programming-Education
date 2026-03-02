import { Category } from './category.model';

// added this in own file, because JHipster deletes everything from model files when jdl changes
export const CategoryLabels: Record<Category, string> = {
	[Category.HINT_GENERATION]: 'Hint Generation',
	[Category.EXERCISE_GENERATION]: 'Exercise Generation',
	[Category.CODE_ASSESSMENT]: 'Code Assessment',
};
