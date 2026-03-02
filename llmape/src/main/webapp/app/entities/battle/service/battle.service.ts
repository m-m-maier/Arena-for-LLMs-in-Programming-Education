import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IBattle, NewBattle } from '../battle.model';

export type PartialUpdateBattle = Partial<IBattle> & Pick<IBattle, 'id'>;

type RestOf<T extends IBattle | NewBattle> = Omit<T, 'voteTimestamp'> & {
	voteTimestamp?: string | null;
};

export type RestBattle = RestOf<IBattle>;

export type NewRestBattle = RestOf<NewBattle>;

export type PartialUpdateRestBattle = RestOf<PartialUpdateBattle>;

export type EntityResponseType = HttpResponse<IBattle>;
export type EntityArrayResponseType = HttpResponse<IBattle[]>;

@Injectable({ providedIn: 'root' })
export class BattleService {
	protected readonly http = inject(HttpClient);
	protected readonly applicationConfigService = inject(ApplicationConfigService);

	protected resourceUrl = this.applicationConfigService.getEndpointFor('api/battles');

	create(battle: NewBattle): Observable<EntityResponseType> {
		const copy = this.convertDateFromClient(battle);
		return this.http
			.post<RestBattle>(this.resourceUrl, copy, { observe: 'response' })
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	update(battle: IBattle): Observable<EntityResponseType> {
		const copy = this.convertDateFromClient(battle);
		return this.http
			.put<RestBattle>(`${this.resourceUrl}/${this.getBattleIdentifier(battle)}`, copy, { observe: 'response' })
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	partialUpdate(battle: PartialUpdateBattle): Observable<EntityResponseType> {
		const copy = this.convertDateFromClient(battle);
		return this.http
			.patch<RestBattle>(`${this.resourceUrl}/${this.getBattleIdentifier(battle)}`, copy, { observe: 'response' })
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	find(id: number): Observable<EntityResponseType> {
		return this.http
			.get<RestBattle>(`${this.resourceUrl}/${id}`, { observe: 'response' })
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	query(req?: any): Observable<EntityArrayResponseType> {
		const options = createRequestOption(req);
		return this.http
			.get<RestBattle[]>(this.resourceUrl, { params: options, observe: 'response' })
			.pipe(map(res => this.convertResponseArrayFromServer(res)));
	}

	delete(id: number): Observable<HttpResponse<{}>> {
		return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
	}

	getBattleIdentifier(battle: Pick<IBattle, 'id'>): number {
		return battle.id;
	}

	compareBattle(o1: Pick<IBattle, 'id'> | null, o2: Pick<IBattle, 'id'> | null): boolean {
		return o1 && o2 ? this.getBattleIdentifier(o1) === this.getBattleIdentifier(o2) : o1 === o2;
	}

	addBattleToCollectionIfMissing<Type extends Pick<IBattle, 'id'>>(
		battleCollection: Type[],
		...battlesToCheck: (Type | null | undefined)[]
	): Type[] {
		const battles: Type[] = battlesToCheck.filter(isPresent);
		if (battles.length > 0) {
			const battleCollectionIdentifiers = battleCollection.map(battleItem => this.getBattleIdentifier(battleItem));
			const battlesToAdd = battles.filter(battleItem => {
				const battleIdentifier = this.getBattleIdentifier(battleItem);
				if (battleCollectionIdentifiers.includes(battleIdentifier)) {
					return false;
				}
				battleCollectionIdentifiers.push(battleIdentifier);
				return true;
			});
			return [...battlesToAdd, ...battleCollection];
		}
		return battleCollection;
	}

	protected convertDateFromClient<T extends IBattle | NewBattle | PartialUpdateBattle>(battle: T): RestOf<T> {
		return {
			...battle,
			voteTimestamp: battle.voteTimestamp?.toJSON() ?? null,
		};
	}

	protected convertDateFromServer(restBattle: RestBattle): IBattle {
		return {
			...restBattle,
			voteTimestamp: restBattle.voteTimestamp ? dayjs(restBattle.voteTimestamp) : undefined,
		};
	}

	protected convertResponseFromServer(res: HttpResponse<RestBattle>): HttpResponse<IBattle> {
		return res.clone({
			body: res.body ? this.convertDateFromServer(res.body) : null,
		});
	}

	protected convertResponseArrayFromServer(res: HttpResponse<RestBattle[]>): HttpResponse<IBattle[]> {
		return res.clone({
			body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
		});
	}
}
