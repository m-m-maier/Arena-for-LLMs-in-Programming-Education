import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IModel } from '../model.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../model.test-samples';

import { ModelService } from './model.service';

const requireRestSample: IModel = {
	...sampleWithRequiredData,
};

describe('Model Service', () => {
	let service: ModelService;
	let httpMock: HttpTestingController;
	let expectedResult: IModel | IModel[] | boolean | null;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [provideHttpClient(), provideHttpClientTesting()],
		});
		expectedResult = null;
		service = TestBed.inject(ModelService);
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

		it('should create a Model', () => {
			const model = { ...sampleWithNewData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.create(model).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'POST' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should update a Model', () => {
			const model = { ...sampleWithRequiredData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.update(model).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'PUT' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should partial update a Model', () => {
			const patchObject = { ...sampleWithPartialData };
			const returnedFromService = { ...requireRestSample };
			const expected = { ...sampleWithRequiredData };

			service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'PATCH' });
			req.flush(returnedFromService);
			expect(expectedResult).toMatchObject(expected);
		});

		it('should return a list of Model', () => {
			const returnedFromService = { ...requireRestSample };

			const expected = { ...sampleWithRequiredData };

			service.query().subscribe(resp => (expectedResult = resp.body));

			const req = httpMock.expectOne({ method: 'GET' });
			req.flush([returnedFromService]);
			httpMock.verify();
			expect(expectedResult).toMatchObject([expected]);
		});

		it('should delete a Model', () => {
			const expected = true;

			service.delete(123).subscribe(resp => (expectedResult = resp.ok));

			const req = httpMock.expectOne({ method: 'DELETE' });
			req.flush({ status: 200 });
			expect(expectedResult).toBe(expected);
		});

		describe('addModelToCollectionIfMissing', () => {
			it('should add a Model to an empty array', () => {
				const model: IModel = sampleWithRequiredData;
				expectedResult = service.addModelToCollectionIfMissing([], model);
				expect(expectedResult).toHaveLength(1);
				expect(expectedResult).toContain(model);
			});

			it('should not add a Model to an array that contains it', () => {
				const model: IModel = sampleWithRequiredData;
				const modelCollection: IModel[] = [
					{
						...model,
					},
					sampleWithPartialData,
				];
				expectedResult = service.addModelToCollectionIfMissing(modelCollection, model);
				expect(expectedResult).toHaveLength(2);
			});

			it("should add a Model to an array that doesn't contain it", () => {
				const model: IModel = sampleWithRequiredData;
				const modelCollection: IModel[] = [sampleWithPartialData];
				expectedResult = service.addModelToCollectionIfMissing(modelCollection, model);
				expect(expectedResult).toHaveLength(2);
				expect(expectedResult).toContain(model);
			});

			it('should add only unique Model to an array', () => {
				const modelArray: IModel[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
				const modelCollection: IModel[] = [sampleWithRequiredData];
				expectedResult = service.addModelToCollectionIfMissing(modelCollection, ...modelArray);
				expect(expectedResult).toHaveLength(3);
			});

			it('should accept varargs', () => {
				const model: IModel = sampleWithRequiredData;
				const model2: IModel = sampleWithPartialData;
				expectedResult = service.addModelToCollectionIfMissing([], model, model2);
				expect(expectedResult).toHaveLength(2);
				expect(expectedResult).toContain(model);
				expect(expectedResult).toContain(model2);
			});

			it('should accept null and undefined values', () => {
				const model: IModel = sampleWithRequiredData;
				expectedResult = service.addModelToCollectionIfMissing([], null, model, undefined);
				expect(expectedResult).toHaveLength(1);
				expect(expectedResult).toContain(model);
			});

			it('should return initial array if no Model is added', () => {
				const modelCollection: IModel[] = [sampleWithRequiredData];
				expectedResult = service.addModelToCollectionIfMissing(modelCollection, undefined, null);
				expect(expectedResult).toEqual(modelCollection);
			});
		});

		describe('compareModel', () => {
			it('Should return true if both entities are null', () => {
				const entity1 = null;
				const entity2 = null;

				const compareResult = service.compareModel(entity1, entity2);

				expect(compareResult).toEqual(true);
			});

			it('Should return false if one entity is null', () => {
				const entity1 = { id: 2694 };
				const entity2 = null;

				const compareResult1 = service.compareModel(entity1, entity2);
				const compareResult2 = service.compareModel(entity2, entity1);

				expect(compareResult1).toEqual(false);
				expect(compareResult2).toEqual(false);
			});

			it('Should return false if primaryKey differs', () => {
				const entity1 = { id: 2694 };
				const entity2 = { id: 873 };

				const compareResult1 = service.compareModel(entity1, entity2);
				const compareResult2 = service.compareModel(entity2, entity1);

				expect(compareResult1).toEqual(false);
				expect(compareResult2).toEqual(false);
			});

			it('Should return false if primaryKey matches', () => {
				const entity1 = { id: 2694 };
				const entity2 = { id: 2694 };

				const compareResult1 = service.compareModel(entity1, entity2);
				const compareResult2 = service.compareModel(entity2, entity1);

				expect(compareResult1).toEqual(true);
				expect(compareResult2).toEqual(true);
			});
		});
	});

	afterEach(() => {
		httpMock.verify();
	});
});
