import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ILeaderboardEntry } from '../leaderboard-entry.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../leaderboard-entry.test-samples';

import { LeaderboardEntryService, RestLeaderboardEntry } from './leaderboard-entry.service';

const requireRestSample: RestLeaderboardEntry = {
	...sampleWithRequiredData,
	timestamp: sampleWithRequiredData.timestamp?.toJSON(),
};

describe('LeaderboardEntry Service', () => {
	let service: LeaderboardEntryService;
	let httpMock: HttpTestingController;
	let expectedResult: ILeaderboardEntry | ILeaderboardEntry[] | boolean | null;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [provideHttpClient(), provideHttpClientTesting()],
		});
		expectedResult = null;
		service = TestBed.inject(LeaderboardEntryService);
		httpMock = TestBed.inject(HttpTestingController);
	});

	describe('Service methods', () => {
		it('should find an element', () => {
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.find(123).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'GET' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should create a LeaderboardEntry', () => {
			const leaderboardEntry = { ...sampleWithNewData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.create(leaderboardEntry).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'POST' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should update a LeaderboardEntry', () => {
			const leaderboardEntry = { ...sampleWithRequiredData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.update(leaderboardEntry).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'PUT' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should partial update a LeaderboardEntry', () => {
			const patchObject = { ...sampleWithPartialData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'PATCH' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should return a list of LeaderboardEntry', () => {
			const returnedFromService = { ...requireRestSample };

			const expected = { ...sampleWithRequiredData };

			service.query().subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'GET' });
			req.flush([returnedFromService]);
			httpMock.verify();
			expect(expectedResult).toMatchObject([expected]);
		});

		it('should delete a LeaderboardEntry', () => {
			const expected = true;

			service.delete(123).subscribe(resp => (expectedResult = resp.ok));

			const req = httpMock.expectOne({ method: 'DELETE' });
			req.flush({ status: 200 });
			expect(expectedResult).toBe(expected);
		});

		describe('addLeaderboardEntryToCollectionIfMissing', () => {
			it('should add a LeaderboardEntry to an empty array', () => {
				const leaderboardEntry: ILeaderboardEntry = sampleWithRequiredData;
				expectedResult = service.addLeaderboardEntryToCollectionIfMissing([], leaderboardEntry);
				expect(expectedResult).toHaveLength(1);
				expect(expectedResult).toContain(leaderboardEntry);
			});

			it('should not add a LeaderboardEntry to an array that contains it', () => {
				const leaderboardEntry: ILeaderboardEntry = sampleWithRequiredData;
				const leaderboardEntryCollection: ILeaderboardEntry[] = [
					{
						...leaderboardEntry,
					},
					sampleWithPartialData,
				];
				expectedResult = service.addLeaderboardEntryToCollectionIfMissing(leaderboardEntryCollection, leaderboardEntry);
				expect(expectedResult).toHaveLength(2);
			});

			it("should add a LeaderboardEntry to an array that doesn't contain it", () => {
				const leaderboardEntry: ILeaderboardEntry = sampleWithRequiredData;
				const leaderboardEntryCollection: ILeaderboardEntry[] = [sampleWithPartialData];
				expectedResult = service.addLeaderboardEntryToCollectionIfMissing(leaderboardEntryCollection, leaderboardEntry);
				expect(expectedResult).toHaveLength(2);
				expect(expectedResult).toContain(leaderboardEntry);
			});

			it('should add only unique LeaderboardEntry to an array', () => {
				const leaderboardEntryArray: ILeaderboardEntry[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
				const leaderboardEntryCollection: ILeaderboardEntry[] = [sampleWithRequiredData];
				expectedResult = service.addLeaderboardEntryToCollectionIfMissing(leaderboardEntryCollection, ...leaderboardEntryArray);
				expect(expectedResult).toHaveLength(3);
			});

			it('should accept varargs', () => {
				const leaderboardEntry: ILeaderboardEntry = sampleWithRequiredData;
				const leaderboardEntry2: ILeaderboardEntry = sampleWithPartialData;
				expectedResult = service.addLeaderboardEntryToCollectionIfMissing([], leaderboardEntry, leaderboardEntry2);
				expect(expectedResult).toHaveLength(2);
				expect(expectedResult).toContain(leaderboardEntry);
				expect(expectedResult).toContain(leaderboardEntry2);
			});

			it('should accept null and undefined values', () => {
				const leaderboardEntry: ILeaderboardEntry = sampleWithRequiredData;
				expectedResult = service.addLeaderboardEntryToCollectionIfMissing([], null, leaderboardEntry, undefined);
				expect(expectedResult).toHaveLength(1);
				expect(expectedResult).toContain(leaderboardEntry);
			});

			it('should return initial array if no LeaderboardEntry is added', () => {
				const leaderboardEntryCollection: ILeaderboardEntry[] = [sampleWithRequiredData];
				expectedResult = service.addLeaderboardEntryToCollectionIfMissing(leaderboardEntryCollection, undefined, null);
				expect(expectedResult).toEqual(leaderboardEntryCollection);
			});
		});

		describe('compareLeaderboardEntry', () => {
			it('Should return true if both entities are null', () => {
				const entity1 = null;
				const entity2 = null;

				const compareResult = service.compareLeaderboardEntry(entity1, entity2);

				expect(compareResult).toEqual(true);
			});

			it('Should return false if one entity is null', () => {
				const entity1 = { id: 18203 };
				const entity2 = null;

				const compareResult1 = service.compareLeaderboardEntry(entity1, entity2);
				const compareResult2 = service.compareLeaderboardEntry(entity2, entity1);

				expect(compareResult1).toEqual(false);
				expect(compareResult2).toEqual(false);
			});

			it('Should return false if primaryKey differs', () => {
				const entity1 = { id: 18203 };
				const entity2 = { id: 15470 };

				const compareResult1 = service.compareLeaderboardEntry(entity1, entity2);
				const compareResult2 = service.compareLeaderboardEntry(entity2, entity1);

				expect(compareResult1).toEqual(false);
				expect(compareResult2).toEqual(false);
			});

			it('Should return false if primaryKey matches', () => {
				const entity1 = { id: 18203 };
				const entity2 = { id: 18203 };

				const compareResult1 = service.compareLeaderboardEntry(entity1, entity2);
				const compareResult2 = service.compareLeaderboardEntry(entity2, entity1);

				expect(compareResult1).toEqual(true);
				expect(compareResult2).toEqual(true);
			});
		});
	});

	afterEach(() => {
		httpMock.verify();
	});
});
