import { TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { ILeaderboardEntry } from '../leaderboard-entry.model';
import { LeaderboardEntryService } from '../service/leaderboard-entry.service';

import leaderboardEntryResolve from './leaderboard-entry-routing-resolve.service';

describe('LeaderboardEntry routing resolve service', () => {
	let mockRouter: Router;
	let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
	let service: LeaderboardEntryService;
	let resultLeaderboardEntry: ILeaderboardEntry | null | undefined;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [
				provideHttpClient(),
				{
					provide: ActivatedRoute,
					useValue: {
						snapshot: {
							paramMap: convertToParamMap({}),
						},
					},
				},
			],
		});
		mockRouter = TestBed.inject(Router);
		jest.spyOn(mockRouter, 'navigate').mockImplementation(() => Promise.resolve(true));
		mockActivatedRouteSnapshot = TestBed.inject(ActivatedRoute).snapshot;
		service = TestBed.inject(LeaderboardEntryService);
		resultLeaderboardEntry = undefined;
	});

	describe('resolve', () => {
		it('should return ILeaderboardEntry returned by find', () => {
			// GIVEN
			service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
			mockActivatedRouteSnapshot.params = { id: 123 };

			// WHEN
			TestBed.runInInjectionContext(() => {
				leaderboardEntryResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultLeaderboardEntry = result;
					},
				});
			});

			// THEN
			expect(service.find).toHaveBeenCalledWith(123);
			expect(resultLeaderboardEntry).toEqual({ id: 123 });
		});

		it('should return null if id is not provided', () => {
			// GIVEN
			service.find = jest.fn();
			mockActivatedRouteSnapshot.params = {};

			// WHEN
			TestBed.runInInjectionContext(() => {
				leaderboardEntryResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultLeaderboardEntry = result;
					},
				});
			});

			// THEN
			expect(service.find).not.toHaveBeenCalled();
			expect(resultLeaderboardEntry).toEqual(null);
		});

		it('should route to 404 page if data not found in server', () => {
			// GIVEN
			jest.spyOn(service, 'find').mockReturnValue(of(new HttpResponse<ILeaderboardEntry>({ body: null })));
			mockActivatedRouteSnapshot.params = { id: 123 };

			// WHEN
			TestBed.runInInjectionContext(() => {
				leaderboardEntryResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultLeaderboardEntry = result;
					},
				});
			});

			// THEN
			expect(service.find).toHaveBeenCalledWith(123);
			expect(resultLeaderboardEntry).toEqual(undefined);
			expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
		});
	});
});
