import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IBattle } from '../battle.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../battle.test-samples';

import { BattleService, RestBattle } from './battle.service';

const requireRestSample: RestBattle = {
	...sampleWithRequiredData,
	voteTimestamp: sampleWithRequiredData.voteTimestamp?.toJSON(),
};

describe('Battle Service', () => {
	let service: BattleService;
	let httpMock: HttpTestingController;
	let expectedResult: IBattle | IBattle[] | boolean | null;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [provideHttpClient(), provideHttpClientTesting()],
		});
		expectedResult = null;
		service = TestBed.inject(BattleService);
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

		it('should create a Battle', () => {
			const battle = { ...sampleWithNewData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.create(battle).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'POST' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should update a Battle', () => {
			const battle = { ...sampleWithRequiredData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.update(battle).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'PUT' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should partial update a Battle', () => {
			const patchObject = { ...sampleWithPartialData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'PATCH' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should return a list of Battle', () => {
			const returnedFromService = { ...requireRestSample };

			const expected = { ...sampleWithRequiredData };

			service.query().subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'GET' });
			req.flush([returnedFromService]);
			httpMock.verify();
			expect(expectedResult).toMatchObject([expected]);
		});

		it('should delete a Battle', () => {
			const expected = true;

			service.delete(123).subscribe(resp => (expectedResult = resp.ok));

			const req = httpMock.expectOne({ method: 'DELETE' });
			req.flush({ status: 200 });
			expect(expectedResult).toBe(expected);
		});

		describe('addBattleToCollectionIfMissing', () => {
			it('should add a Battle to an empty array', () => {
				const battle: IBattle = sampleWithRequiredData;
				expectedResult = service.addBattleToCollectionIfMissing([], battle);
				expect(expectedResult).toHaveLength(1);
				expect(expectedResult).toContain(battle);
			});

			it('should not add a Battle to an array that contains it', () => {
				const battle: IBattle = sampleWithRequiredData;
				const battleCollection: IBattle[] = [
					{
						...battle,
					},
					sampleWithPartialData,
				];
				expectedResult = service.addBattleToCollectionIfMissing(battleCollection, battle);
				expect(expectedResult).toHaveLength(2);
			});

			it("should add a Battle to an array that doesn't contain it", () => {
				const battle: IBattle = sampleWithRequiredData;
				const battleCollection: IBattle[] = [sampleWithPartialData];
				expectedResult = service.addBattleToCollectionIfMissing(battleCollection, battle);
				expect(expectedResult).toHaveLength(2);
				expect(expectedResult).toContain(battle);
			});

			it('should add only unique Battle to an array', () => {
				const battleArray: IBattle[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
				const battleCollection: IBattle[] = [sampleWithRequiredData];
				expectedResult = service.addBattleToCollectionIfMissing(battleCollection, ...battleArray);
				expect(expectedResult).toHaveLength(3);
			});

			it('should accept varargs', () => {
				const battle: IBattle = sampleWithRequiredData;
				const battle2: IBattle = sampleWithPartialData;
				expectedResult = service.addBattleToCollectionIfMissing([], battle, battle2);
				expect(expectedResult).toHaveLength(2);
				expect(expectedResult).toContain(battle);
				expect(expectedResult).toContain(battle2);
			});

			it('should accept null and undefined values', () => {
				const battle: IBattle = sampleWithRequiredData;
				expectedResult = service.addBattleToCollectionIfMissing([], null, battle, undefined);
				expect(expectedResult).toHaveLength(1);
				expect(expectedResult).toContain(battle);
			});

			it('should return initial array if no Battle is added', () => {
				const battleCollection: IBattle[] = [sampleWithRequiredData];
				expectedResult = service.addBattleToCollectionIfMissing(battleCollection, undefined, null);
				expect(expectedResult).toEqual(battleCollection);
			});
		});

		describe('compareBattle', () => {
			it('Should return true if both entities are null', () => {
				const entity1 = null;
				const entity2 = null;

				const compareResult = service.compareBattle(entity1, entity2);

				expect(compareResult).toEqual(true);
			});

			it('Should return false if one entity is null', () => {
				const entity1 = { id: 22957 };
				const entity2 = null;

				const compareResult1 = service.compareBattle(entity1, entity2);
				const compareResult2 = service.compareBattle(entity2, entity1);

				expect(compareResult1).toEqual(false);
				expect(compareResult2).toEqual(false);
			});

			it('Should return false if primaryKey differs', () => {
				const entity1 = { id: 22957 };
				const entity2 = { id: 6189 };

				const compareResult1 = service.compareBattle(entity1, entity2);
				const compareResult2 = service.compareBattle(entity2, entity1);

				expect(compareResult1).toEqual(false);
				expect(compareResult2).toEqual(false);
			});

			it('Should return false if primaryKey matches', () => {
				const entity1 = { id: 22957 };
				const entity2 = { id: 22957 };

				const compareResult1 = service.compareBattle(entity1, entity2);
				const compareResult2 = service.compareBattle(entity2, entity1);

				expect(compareResult1).toEqual(true);
				expect(compareResult2).toEqual(true);
			});
		});
	});

	afterEach(() => {
		httpMock.verify();
	});
});
