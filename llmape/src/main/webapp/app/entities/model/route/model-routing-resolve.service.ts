import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IModel } from '../model.model';
import { ModelService } from '../service/model.service';

const modelResolve = (route: ActivatedRouteSnapshot): Observable<null | IModel> => {
	const id = route.params.id;
	if (id) {
		return inject(ModelService)
			.find(id)
			.pipe(
				mergeMap((model: HttpResponse<IModel>) => {
					if (model.body) {
						return of(model.body);
					}
					inject(Router).navigate(['404']);
					return EMPTY;
				}),
			);
	}
	return of(null);
};

export default modelResolve;
