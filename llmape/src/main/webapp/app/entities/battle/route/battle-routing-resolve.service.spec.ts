import { TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { IBattle } from '../battle.model';
import { BattleService } from '../service/battle.service';

import battleResolve from './battle-routing-resolve.service';

describe('Battle routing resolve service', () => {
	let mockRouter: Router;
	let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
	let service: BattleService;
	let resultBattle: IBattle | null | undefined;

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
		service = TestBed.inject(BattleService);
		resultBattle = undefined;
	});

	describe('resolve', () => {
		it('should return IBattle returned by find', () => {
			// GIVEN
			service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
			mockActivatedRouteSnapshot.params = { id: 123 };

			// WHEN
			TestBed.runInInjectionContext(() => {
				battleResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultBattle = result;
					},
				});
			});

			// THEN
			expect(service.find).toHaveBeenCalledWith(123);
			expect(resultBattle).toEqual({ id: 123 });
		});

		it('should return null if id is not provided', () => {
			// GIVEN
			service.find = jest.fn();
			mockActivatedRouteSnapshot.params = {};

			// WHEN
			TestBed.runInInjectionContext(() => {
				battleResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultBattle = result;
					},
				});
			});

			// THEN
			expect(service.find).not.toHaveBeenCalled();
			expect(resultBattle).toEqual(null);
		});

		it('should route to 404 page if data not found in server', () => {
			// GIVEN
			jest.spyOn(service, 'find').mockReturnValue(of(new HttpResponse<IBattle>({ body: null })));
			mockActivatedRouteSnapshot.params = { id: 123 };

			// WHEN
			TestBed.runInInjectionContext(() => {
				battleResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultBattle = result;
					},
				});
			});

			// THEN
			expect(service.find).toHaveBeenCalledWith(123);
			expect(resultBattle).toEqual(undefined);
			expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
		});
	});
});
