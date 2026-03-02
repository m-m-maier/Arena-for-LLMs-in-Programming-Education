import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import BattleResolve from './route/battle-routing-resolve.service';

const battleRoute: Routes = [
	{
		path: '',
		loadComponent: () => import('./list/battle.component').then(m => m.BattleComponent),
		data: {
			defaultSort: `id,${ASC}`,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/view',
		loadComponent: () => import('./detail/battle-detail.component').then(m => m.BattleDetailComponent),
		resolve: {
			battle: BattleResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: 'new',
		loadComponent: () => import('./update/battle-update.component').then(m => m.BattleUpdateComponent),
		resolve: {
			battle: BattleResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/edit',
		loadComponent: () => import('./update/battle-update.component').then(m => m.BattleUpdateComponent),
		resolve: {
			battle: BattleResolve,
		},
		canActivate: [UserRouteAccessService],
	},
];

export default battleRoute;
