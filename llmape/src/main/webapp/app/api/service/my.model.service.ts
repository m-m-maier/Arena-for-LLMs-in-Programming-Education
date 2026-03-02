import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IModelDTO } from '../dto/my.model.dto';

@Injectable({ providedIn: 'root' })
export class MyModelService {
	protected readonly http = inject(HttpClient);
	protected readonly applicationConfigService = inject(ApplicationConfigService);

	protected resourceUrl = this.applicationConfigService.getEndpointFor('api/mymodel/getAllActiveModels');

	getAllActiveModels(): Observable<HttpResponse<IModelDTO[]>> {
		return this.http.get<IModelDTO[]>(this.resourceUrl, { observe: 'response' });
	}
}
