import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import PromptResolve from './route/prompt-routing-resolve.service';

const promptRoute: Routes = [
	{
		path: '',
		loadComponent: () => import('./list/prompt.component').then(m => m.PromptComponent),
		data: {
			defaultSort: `id,${ASC}`,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/view',
		loadComponent: () => import('./detail/prompt-detail.component').then(m => m.PromptDetailComponent),
		resolve: {
			prompt: PromptResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: 'new',
		loadComponent: () => import('./update/prompt-update.component').then(m => m.PromptUpdateComponent),
		resolve: {
			prompt: PromptResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/edit',
		loadComponent: () => import('./update/prompt-update.component').then(m => m.PromptUpdateComponent),
		resolve: {
			prompt: PromptResolve,
		},
		canActivate: [UserRouteAccessService],
	},
];

export default promptRoute;
