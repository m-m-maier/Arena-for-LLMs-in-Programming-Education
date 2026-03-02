import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ModelResolve from './route/model-routing-resolve.service';

const modelRoute: Routes = [
	{
		path: '',
		loadComponent: () => import('./list/model.component').then(m => m.ModelComponent),
		data: {
			defaultSort: `id,${ASC}`,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/view',
		loadComponent: () => import('./detail/model-detail.component').then(m => m.ModelDetailComponent),
		resolve: {
			model: ModelResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: 'new',
		loadComponent: () => import('./update/model-update.component').then(m => m.ModelUpdateComponent),
		resolve: {
			model: ModelResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/edit',
		loadComponent: () => import('./update/model-update.component').then(m => m.ModelUpdateComponent),
		resolve: {
			model: ModelResolve,
		},
		canActivate: [UserRouteAccessService],
	},
];

export default modelRoute;
