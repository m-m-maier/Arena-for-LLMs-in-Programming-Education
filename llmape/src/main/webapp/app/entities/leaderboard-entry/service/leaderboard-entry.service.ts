import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ILeaderboardEntry, NewLeaderboardEntry } from '../leaderboard-entry.model';

export type PartialUpdateLeaderboardEntry = Partial<ILeaderboardEntry> & Pick<ILeaderboardEntry, 'id'>;

type RestOf<T extends ILeaderboardEntry | NewLeaderboardEntry> = Omit<T, 'timestamp'> & {
	timestamp?: string | null;
};

export type RestLeaderboardEntry = RestOf<ILeaderboardEntry>;

export type NewRestLeaderboardEntry = RestOf<NewLeaderboardEntry>;

export type PartialUpdateRestLeaderboardEntry = RestOf<PartialUpdateLeaderboardEntry>;

export type EntityResponseType = HttpResponse<ILeaderboardEntry>;
export type EntityArrayResponseType = HttpResponse<ILeaderboardEntry[]>;

@Injectable({ providedIn: 'root' })
export class LeaderboardEntryService {
	protected readonly http = inject(HttpClient);
	protected readonly applicationConfigService = inject(ApplicationConfigService);

	protected resourceUrl = this.applicationConfigService.getEndpointFor('api/leaderboard-entries');

	create(leaderboardEntry: NewLeaderboardEntry): Observable<EntityResponseType> {
		const copy = this.convertDateFromClient(leaderboardEntry);
		return this.http
			.post<RestLeaderboardEntry>(this.resourceUrl, copy, { observe: 'response' })
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	update(leaderboardEntry: ILeaderboardEntry): Observable<EntityResponseType> {
		const copy = this.convertDateFromClient(leaderboardEntry);
		return this.http
			.put<RestLeaderboardEntry>(`${this.resourceUrl}/${this.getLeaderboardEntryIdentifier(leaderboardEntry)}`, copy, {
				observe: 'response',
			})
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	partialUpdate(leaderboardEntry: PartialUpdateLeaderboardEntry): Observable<EntityResponseType> {
		const copy = this.convertDateFromClient(leaderboardEntry);
		return this.http
			.patch<RestLeaderboardEntry>(`${this.resourceUrl}/${this.getLeaderboardEntryIdentifier(leaderboardEntry)}`, copy, {
				observe: 'response',
			})
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	find(id: number): Observable<EntityResponseType> {
		return this.http
			.get<RestLeaderboardEntry>(`${this.resourceUrl}/${id}`, { observe: 'response' })
			.pipe(map(res => this.convertResponseFromServer(res)));
	}

	query(req?: any): Observable<EntityArrayResponseType> {
		const options = createRequestOption(req);
		return this.http
			.get<RestLeaderboardEntry[]>(this.resourceUrl, { params: options, observe: 'response' })
			.pipe(map(res => this.convertResponseArrayFromServer(res)));
	}

	delete(id: number): Observable<HttpResponse<{}>> {
		return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
	}

	getLeaderboardEntryIdentifier(leaderboardEntry: Pick<ILeaderboardEntry, 'id'>): number {
		return leaderboardEntry.id;
	}

	compareLeaderboardEntry(o1: Pick<ILeaderboardEntry, 'id'> | null, o2: Pick<ILeaderboardEntry, 'id'> | null): boolean {
		return o1 && o2 ? this.getLeaderboardEntryIdentifier(o1) === this.getLeaderboardEntryIdentifier(o2) : o1 === o2;
	}

	addLeaderboardEntryToCollectionIfMissing<Type extends Pick<ILeaderboardEntry, 'id'>>(
		leaderboardEntryCollection: Type[],
		...leaderboardEntriesToCheck: (Type | null | undefined)[]
	): Type[] {
		const leaderboardEntries: Type[] = leaderboardEntriesToCheck.filter(isPresent);
		if (leaderboardEntries.length > 0) {
			const leaderboardEntryCollectionIdentifiers = leaderboardEntryCollection.map(leaderboardEntryItem =>
				this.getLeaderboardEntryIdentifier(leaderboardEntryItem),
			);
			const leaderboardEntriesToAdd = leaderboardEntries.filter(leaderboardEntryItem => {
				const leaderboardEntryIdentifier = this.getLeaderboardEntryIdentifier(leaderboardEntryItem);
				if (leaderboardEntryCollectionIdentifiers.includes(leaderboardEntryIdentifier)) {
					return false;
				}
				leaderboardEntryCollectionIdentifiers.push(leaderboardEntryIdentifier);
				return true;
			});
			return [...leaderboardEntriesToAdd, ...leaderboardEntryCollection];
		}
		return leaderboardEntryCollection;
	}

	protected convertDateFromClient<T extends ILeaderboardEntry | NewLeaderboardEntry | PartialUpdateLeaderboardEntry>(
		leaderboardEntry: T,
	): RestOf<T> {
		return {
			...leaderboardEntry,
			timestamp: leaderboardEntry.timestamp?.toJSON() ?? null,
		};
	}

	protected convertDateFromServer(restLeaderboardEntry: RestLeaderboardEntry): ILeaderboardEntry {
		return {
			...restLeaderboardEntry,
			timestamp: restLeaderboardEntry.timestamp ? dayjs(restLeaderboardEntry.timestamp) : undefined,
		};
	}

	protected convertResponseFromServer(res: HttpResponse<RestLeaderboardEntry>): HttpResponse<ILeaderboardEntry> {
		return res.clone({
			body: res.body ? this.convertDateFromServer(res.body) : null,
		});
	}

	protected convertResponseArrayFromServer(res: HttpResponse<RestLeaderboardEntry[]>): HttpResponse<ILeaderboardEntry[]> {
		return res.clone({
			body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
		});
	}
}
