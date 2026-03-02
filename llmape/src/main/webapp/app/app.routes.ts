import { Routes } from '@angular/router';

import { Authority } from 'app/config/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
	{
		path: '',
		loadComponent: () => import('./home/home.component'),
		title: 'Welcome to LLM-APE',
	},
	{
		path: '',
		loadComponent: () => import('./layouts/navbar/navbar.component'),
		outlet: 'navbar',
	},
	{
		path: 'quicktestllm',
		data: {
			authorities: [Authority.ADMIN, Authority.USER],
		},
		canActivate: [UserRouteAccessService],
		loadComponent: () => import('./quicktestllm/quicktestllm.component'),
		title: 'Quickly test LLMs',
	},
	{
		path: 'generatePromptsAndVotes',
		data: {
			authorities: [Authority.ADMIN],
		},
		canActivate: [UserRouteAccessService],
		loadComponent: () => import('./generatePromptsAndVotes/generatePromptsAndVotes.component'),
		title: 'Generate prompts and votes',
	},
	{
		path: 'admin',
		data: {
			authorities: [Authority.ADMIN],
		},
		canActivate: [UserRouteAccessService],
		loadChildren: () => import('./admin/admin.routes'),
	},
	{
		path: 'account',
		loadChildren: () => import('./account/account.route'),
	},
	{
		path: 'login',
		loadComponent: () => import('./login/login.component'),
		title: 'login.title',
	},
	{
		path: '',
		data: {
			authorities: [Authority.ADMIN],
		},
		canActivate: [UserRouteAccessService],
		loadChildren: () => import(`./entities/entity.routes`),
	},
	...errorRoute,
];

export default routes;
