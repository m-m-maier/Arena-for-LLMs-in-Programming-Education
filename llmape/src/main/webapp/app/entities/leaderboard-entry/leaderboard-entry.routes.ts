import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import LeaderboardEntryResolve from './route/leaderboard-entry-routing-resolve.service';

const leaderboardEntryRoute: Routes = [
	{
		path: '',
		loadComponent: () => import('./list/leaderboard-entry.component').then(m => m.LeaderboardEntryComponent),
		data: {
			defaultSort: `id,${ASC}`,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/view',
		loadComponent: () => import('./detail/leaderboard-entry-detail.component').then(m => m.LeaderboardEntryDetailComponent),
		resolve: {
			leaderboardEntry: LeaderboardEntryResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: 'new',
		loadComponent: () => import('./update/leaderboard-entry-update.component').then(m => m.LeaderboardEntryUpdateComponent),
		resolve: {
			leaderboardEntry: LeaderboardEntryResolve,
		},
		canActivate: [UserRouteAccessService],
	},
	{
		path: ':id/edit',
		loadComponent: () => import('./update/leaderboard-entry-update.component').then(m => m.LeaderboardEntryUpdateComponent),
		resolve: {
			leaderboardEntry: LeaderboardEntryResolve,
		},
		canActivate: [UserRouteAccessService],
	},
];

export default leaderboardEntryRoute;
