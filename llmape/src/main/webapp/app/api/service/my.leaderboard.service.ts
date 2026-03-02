import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Category } from '../../entities/enumerations/category.model';
import { ILeaderboardDTO } from '../dto/my.leaderboard.dto';

@Injectable({ providedIn: 'root' })
export class MyLeaderboradService {
	protected readonly http = inject(HttpClient);
	protected readonly applicationConfigService = inject(ApplicationConfigService);

	protected resourceUrl = this.applicationConfigService.getEndpointFor('api/myleaderboard/getLeaderboard');

	getLeaderboardByCategory(category: Category | null): Observable<HttpResponse<ILeaderboardDTO>> {
		let params = new HttpParams();
		if (category !== null) {
			params = params.set('category', category);
		}
		return this.http.get<ILeaderboardDTO>(this.resourceUrl, { observe: 'response', params });
	}
}
