import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ILeaderboardEntry } from '../leaderboard-entry.model';
import { LeaderboardEntryService } from '../service/leaderboard-entry.service';

const leaderboardEntryResolve = (route: ActivatedRouteSnapshot): Observable<null | ILeaderboardEntry> => {
	const id = route.params.id;
	if (id) {
		return inject(LeaderboardEntryService)
			.find(id)
			.pipe(
				mergeMap((leaderboardEntry: HttpResponse<ILeaderboardEntry>) => {
					if (leaderboardEntry.body) {
						return of(leaderboardEntry.body);
					}
					inject(Router).navigate(['404']);
					return EMPTY;
				}),
			);
	}
	return of(null);
};

export default leaderboardEntryResolve;
