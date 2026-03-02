import { TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { IPrompt } from '../prompt.model';
import { PromptService } from '../service/prompt.service';

import promptResolve from './prompt-routing-resolve.service';

describe('Prompt routing resolve service', () => {
	let mockRouter: Router;
	let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
	let service: PromptService;
	let resultPrompt: IPrompt | null | undefined;

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
		service = TestBed.inject(PromptService);
		resultPrompt = undefined;
	});

	describe('resolve', () => {
		it('should return IPrompt returned by find', () => {
			// GIVEN
			service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
			mockActivatedRouteSnapshot.params = { id: 123 };

			// WHEN
			TestBed.runInInjectionContext(() => {
				promptResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultPrompt = result;
					},
				});
			});

			// THEN
			expect(service.find).toHaveBeenCalledWith(123);
			expect(resultPrompt).toEqual({ id: 123 });
		});

		it('should return null if id is not provided', () => {
			// GIVEN
			service.find = jest.fn();
			mockActivatedRouteSnapshot.params = {};

			// WHEN
			TestBed.runInInjectionContext(() => {
				promptResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultPrompt = result;
					},
				});
			});

			// THEN
			expect(service.find).not.toHaveBeenCalled();
			expect(resultPrompt).toEqual(null);
		});

		it('should route to 404 page if data not found in server', () => {
			// GIVEN
			jest.spyOn(service, 'find').mockReturnValue(of(new HttpResponse<IPrompt>({ body: null })));
			mockActivatedRouteSnapshot.params = { id: 123 };

			// WHEN
			TestBed.runInInjectionContext(() => {
				promptResolve(mockActivatedRouteSnapshot).subscribe({
					next(result) {
						resultPrompt = result;
					},
				});
			});

			// THEN
			expect(service.find).toHaveBeenCalledWith(123);
			expect(resultPrompt).toEqual(undefined);
			expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
		});
	});
});
