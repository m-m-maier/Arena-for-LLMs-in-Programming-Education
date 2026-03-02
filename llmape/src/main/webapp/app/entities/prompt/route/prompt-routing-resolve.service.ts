import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPrompt } from '../prompt.model';
import { PromptService } from '../service/prompt.service';

const promptResolve = (route: ActivatedRouteSnapshot): Observable<null | IPrompt> => {
	const id = route.params.id;
	if (id) {
		return inject(PromptService)
			.find(id)
			.pipe(
				mergeMap((prompt: HttpResponse<IPrompt>) => {
					if (prompt.body) {
						return of(prompt.body);
					}
					inject(Router).navigate(['404']);
					return EMPTY;
				}),
			);
	}
	return of(null);
};

export default promptResolve;
