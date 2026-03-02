import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IBattle } from '../battle.model';
import { BattleService } from '../service/battle.service';

const battleResolve = (route: ActivatedRouteSnapshot): Observable<null | IBattle> => {
	const id = route.params.id;
	if (id) {
		return inject(BattleService)
			.find(id)
			.pipe(
				mergeMap((battle: HttpResponse<IBattle>) => {
					if (battle.body) {
						return of(battle.body);
					}
					inject(Router).navigate(['404']);
					return EMPTY;
				}),
			);
	}
	return of(null);
};

export default battleResolve;
