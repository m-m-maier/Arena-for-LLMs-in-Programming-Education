import { Routes } from '@angular/router';

const routes: Routes = [
	{
		path: 'authority',
		data: { pageTitle: 'Authorities' },
		loadChildren: () => import('./admin/authority/authority.routes'),
	},
	{
		path: 'model',
		data: { pageTitle: 'Models' },
		loadChildren: () => import('./model/model.routes'),
	},
	{
		path: 'battle',
		data: { pageTitle: 'Battles' },
		loadChildren: () => import('./battle/battle.routes'),
	},
	{
		path: 'prompt',
		data: { pageTitle: 'Prompts' },
		loadChildren: () => import('./prompt/prompt.routes'),
	},
	{
		path: 'leaderboard-entry',
		data: { pageTitle: 'LeaderboardEntries' },
		loadChildren: () => import('./leaderboard-entry/leaderboard-entry.routes'),
	},
	/* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
