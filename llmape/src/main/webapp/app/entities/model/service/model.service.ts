import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IModel, NewModel } from '../model.model';

export type PartialUpdateModel = Partial<IModel> & Pick<IModel, 'id'>;

export type EntityResponseType = HttpResponse<IModel>;
export type EntityArrayResponseType = HttpResponse<IModel[]>;

@Injectable({ providedIn: 'root' })
export class ModelService {
	protected readonly http = inject(HttpClient);
	protected readonly applicationConfigService = inject(ApplicationConfigService);

	protected resourceUrl = this.applicationConfigService.getEndpointFor('api/models');

	create(model: NewModel): Observable<EntityResponseType> {
		return this.http.post<IModel>(this.resourceUrl, model, { observe: 'response' });
	}

	update(model: IModel): Observable<EntityResponseType> {
		return this.http.put<IModel>(`${this.resourceUrl}/${this.getModelIdentifier(model)}`, model, { observe: 'response' });
	}

	partialUpdate(model: PartialUpdateModel): Observable<EntityResponseType> {
		return this.http.patch<IModel>(`${this.resourceUrl}/${this.getModelIdentifier(model)}`, model, { observe: 'response' });
	}

	find(id: number): Observable<EntityResponseType> {
		return this.http.get<IModel>(`${this.resourceUrl}/${id}`, { observe: 'response' });
	}

	query(req?: any): Observable<EntityArrayResponseType> {
		const options = createRequestOption(req);
		return this.http.get<IModel[]>(this.resourceUrl, { params: options, observe: 'response' });
	}

	delete(id: number): Observable<HttpResponse<{}>> {
		return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
	}

	getModelIdentifier(model: Pick<IModel, 'id'>): number {
		return model.id;
	}

	compareModel(o1: Pick<IModel, 'id'> | null, o2: Pick<IModel, 'id'> | null): boolean {
		return o1 && o2 ? this.getModelIdentifier(o1) === this.getModelIdentifier(o2) : o1 === o2;
	}

	addModelToCollectionIfMissing<Type extends Pick<IModel, 'id'>>(
		modelCollection: Type[],
		...modelsToCheck: (Type | null | undefined)[]
	): Type[] {
		const models: Type[] = modelsToCheck.filter(isPresent);
		if (models.length > 0) {
			const modelCollectionIdentifiers = modelCollection.map(modelItem => this.getModelIdentifier(modelItem));
			const modelsToAdd = models.filter(modelItem => {
				const modelIdentifier = this.getModelIdentifier(modelItem);
				if (modelCollectionIdentifiers.includes(modelIdentifier)) {
					return false;
				}
				modelCollectionIdentifiers.push(modelIdentifier);
				return true;
			});
			return [...modelsToAdd, ...modelCollection];
		}
		return modelCollection;
	}
}
