import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IPrompt } from 'app/entities/prompt/prompt.model';
import { PromptService } from 'app/entities/prompt/service/prompt.service';
import { IModel } from 'app/entities/model/model.model';
import { ModelService } from 'app/entities/model/service/model.service';
import { IBattle } from '../battle.model';
import { BattleService } from '../service/battle.service';
import { BattleFormService } from './battle-form.service';

import { BattleUpdateComponent } from './battle-update.component';

describe('Battle Management Update Component', () => {
	let comp: BattleUpdateComponent;
	let fixture: ComponentFixture<BattleUpdateComponent>;
	let activatedRoute: ActivatedRoute;
	let battleFormService: BattleFormService;
	let battleService: BattleService;
	let promptService: PromptService;
	let modelService: ModelService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [BattleUpdateComponent],
			providers: [
				provideHttpClient(),
				FormBuilder,
				{
					provide: ActivatedRoute,
					useValue: {
						params: from([{}]),
					},
				},
			],
		})
			.overrideTemplate(BattleUpdateComponent, '')
			.compileComponents();

		fixture = TestBed.createComponent(BattleUpdateComponent);
		activatedRoute = TestBed.inject(ActivatedRoute);
		battleFormService = TestBed.inject(BattleFormService);
		battleService = TestBed.inject(BattleService);
		promptService = TestBed.inject(PromptService);
		modelService = TestBed.inject(ModelService);

		comp = fixture.componentInstance;
	});

	describe('ngOnInit', () => {
		it('Should call prompt query and add missing value', () => {
			const battle: IBattle = { id: 6189 };
			const prompt: IPrompt = { id: 3853 };
			battle.prompt = prompt;

			const promptCollection: IPrompt[] = [{ id: 3853 }];
			jest.spyOn(promptService, 'query').mockReturnValue(of(new HttpResponse({ body: promptCollection })));
			const expectedCollection: IPrompt[] = [prompt, ...promptCollection];
			jest.spyOn(promptService, 'addPromptToCollectionIfMissing').mockReturnValue(expectedCollection);

			activatedRoute.data = of({ battle });
			comp.ngOnInit();

			expect(promptService.query).toHaveBeenCalled();
			expect(promptService.addPromptToCollectionIfMissing).toHaveBeenCalledWith(promptCollection, prompt);
			expect(comp.promptsCollection).toEqual(expectedCollection);
		});

		it('Should call Model query and add missing value', () => {
			const battle: IBattle = { id: 6189 };
			const model1: IModel = { id: 2694 };
			battle.model1 = model1;
			const model2: IModel = { id: 2694 };
			battle.model2 = model2;
			const winnerModel: IModel = { id: 2694 };
			battle.winnerModel = winnerModel;

			const modelCollection: IModel[] = [{ id: 2694 }];
			jest.spyOn(modelService, 'query').mockReturnValue(of(new HttpResponse({ body: modelCollection })));
			const additionalModels = [model1, model2, winnerModel];
			const expectedCollection: IModel[] = [...additionalModels, ...modelCollection];
			jest.spyOn(modelService, 'addModelToCollectionIfMissing').mockReturnValue(expectedCollection);

			activatedRoute.data = of({ battle });
			comp.ngOnInit();

			expect(modelService.query).toHaveBeenCalled();
			expect(modelService.addModelToCollectionIfMissing).toHaveBeenCalledWith(
				modelCollection,
				...additionalModels.map(expect.objectContaining),
			);
			expect(comp.modelsSharedCollection).toEqual(expectedCollection);
		});

		it('Should update editForm', () => {
			const battle: IBattle = { id: 6189 };
			const prompt: IPrompt = { id: 3853 };
			battle.prompt = prompt;
			const model1: IModel = { id: 2694 };
			battle.model1 = model1;
			const model2: IModel = { id: 2694 };
			battle.model2 = model2;
			const winnerModel: IModel = { id: 2694 };
			battle.winnerModel = winnerModel;

			activatedRoute.data = of({ battle });
			comp.ngOnInit();

			expect(comp.promptsCollection).toContainEqual(prompt);
			expect(comp.modelsSharedCollection).toContainEqual(model1);
			expect(comp.modelsSharedCollection).toContainEqual(model2);
			expect(comp.modelsSharedCollection).toContainEqual(winnerModel);
			expect(comp.battle).toEqual(battle);
		});
	});

	describe('save', () => {
		it('Should call update service on save for existing entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IBattle>>();
			const battle = { id: 22957 };
			jest.spyOn(battleFormService, 'getBattle').mockReturnValue(battle);
			jest.spyOn(battleService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ battle });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: battle }));
			saveSubject.complete();

			// THEN
			expect(battleFormService.getBattle).toHaveBeenCalled();
			expect(comp.previousState).toHaveBeenCalled();
			expect(battleService.update).toHaveBeenCalledWith(expect.objectContaining(battle));
			expect(comp.isSaving).toEqual(false);
		});

		it('Should call create service on save for new entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IBattle>>();
			const battle = { id: 22957 };
			jest.spyOn(battleFormService, 'getBattle').mockReturnValue({ id: null });
			jest.spyOn(battleService, 'create').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ battle: null });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: battle }));
			saveSubject.complete();

			// THEN
			expect(battleFormService.getBattle).toHaveBeenCalled();
			expect(battleService.create).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).toHaveBeenCalled();
		});

		it('Should set isSaving to false on error', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<IBattle>>();
			const battle = { id: 22957 };
			jest.spyOn(battleService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ battle });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.error('This is an error!');

			// THEN
			expect(battleService.update).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).not.toHaveBeenCalled();
		});
	});

	describe('Compare relationships', () => {
		describe('comparePrompt', () => {
			it('Should forward to promptService', () => {
				const entity = { id: 3853 };
				const entity2 = { id: 29985 };
				jest.spyOn(promptService, 'comparePrompt');
				comp.comparePrompt(entity, entity2);
				expect(promptService.comparePrompt).toHaveBeenCalledWith(entity, entity2);
			});
		});

		describe('compareModel', () => {
			it('Should forward to modelService', () => {
				const entity = { id: 2694 };
				const entity2 = { id: 873 };
				jest.spyOn(modelService, 'compareModel');
				comp.compareModel(entity, entity2);
				expect(modelService.compareModel).toHaveBeenCalledWith(entity, entity2);
			});
		});
	});
});
