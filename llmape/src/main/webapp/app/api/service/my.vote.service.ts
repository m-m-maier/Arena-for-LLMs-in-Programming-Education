import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IVoteDTO } from '../dto/my.vote.dto';
import { IModelIdentityResponseDTO } from '../dto/my.modelIdentityResponse.dto';

@Injectable({ providedIn: 'root' })
export class MyVoteService {
	protected readonly http = inject(HttpClient);
	protected readonly applicationConfigService = inject(ApplicationConfigService);

	protected resourceUrl = this.applicationConfigService.getEndpointFor('api/myvote/submitVote');

	submitVote(vote: IVoteDTO): Observable<HttpResponse<IModelIdentityResponseDTO>> {
		return this.http.post<IModelIdentityResponseDTO>(this.resourceUrl, vote, { observe: 'response' });
	}
}
