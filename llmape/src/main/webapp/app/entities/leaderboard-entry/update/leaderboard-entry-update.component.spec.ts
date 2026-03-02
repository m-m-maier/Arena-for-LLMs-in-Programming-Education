import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { LeaderboardEntryService } from '../service/leaderboard-entry.service';
import { ILeaderboardEntry } from '../leaderboard-entry.model';
import { LeaderboardEntryFormService } from './leaderboard-entry-form.service';

import { LeaderboardEntryUpdateComponent } from './leaderboard-entry-update.component';

describe('LeaderboardEntry Management Update Component', () => {
	let comp: LeaderboardEntryUpdateComponent;
	let fixture: ComponentFixture<LeaderboardEntryUpdateComponent>;
	let activatedRoute: ActivatedRoute;
	let leaderboardEntryFormService: LeaderboardEntryFormService;
	let leaderboardEntryService: LeaderboardEntryService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [LeaderboardEntryUpdateComponent],
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
			.overrideTemplate(LeaderboardEntryUpdateComponent, '')
			.compileComponents();

		fixture = TestBed.createComponent(LeaderboardEntryUpdateComponent);
		activatedRoute = TestBed.inject(ActivatedRoute);
		leaderboardEntryFormService = TestBed.inject(LeaderboardEntryFormService);
		leaderboardEntryService = TestBed.inject(LeaderboardEntryService);

		comp = fixture.componentInstance;
	});

	describe('ngOnInit', () => {
		it('Should update editForm', () => {
			const leaderboardEntry: ILeaderboardEntry = { id: 15470 };

			activatedRoute.data = of({ leaderboardEntry });
			comp.ngOnInit();

			expect(comp.leaderboardEntry).toEqual(leaderboardEntry);
		});
	});

	describe('save', () => {
		it('Should call update service on save for existing entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<ILeaderboardEntry>>();
			const leaderboardEntry = { id: 18203 };
			jest.spyOn(leaderboardEntryFormService, 'getLeaderboardEntry').mockReturnValue(leaderboardEntry);
			jest.spyOn(leaderboardEntryService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ leaderboardEntry });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: leaderboardEntry }));
			saveSubject.complete();

			// THEN
			expect(leaderboardEntryFormService.getLeaderboardEntry).toHaveBeenCalled();
			expect(comp.previousState).toHaveBeenCalled();
			expect(leaderboardEntryService.update).toHaveBeenCalledWith(expect.objectContaining(leaderboardEntry));
			expect(comp.isSaving).toEqual(false);
		});

		it('Should call create service on save for new entity', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<ILeaderboardEntry>>();
			const leaderboardEntry = { id: 18203 };
			jest.spyOn(leaderboardEntryFormService, 'getLeaderboardEntry').mockReturnValue({ id: null });
			jest.spyOn(leaderboardEntryService, 'create').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ leaderboardEntry: null });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.next(new HttpResponse({ body: leaderboardEntry }));
			saveSubject.complete();

			// THEN
			expect(leaderboardEntryFormService.getLeaderboardEntry).toHaveBeenCalled();
			expect(leaderboardEntryService.create).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).toHaveBeenCalled();
		});

		it('Should set isSaving to false on error', () => {
			// GIVEN
			const saveSubject = new Subject<HttpResponse<ILeaderboardEntry>>();
			const leaderboardEntry = { id: 18203 };
			jest.spyOn(leaderboardEntryService, 'update').mockReturnValue(saveSubject);
			jest.spyOn(comp, 'previousState');
			activatedRoute.data = of({ leaderboardEntry });
			comp.ngOnInit();

			// WHEN
			comp.save();
			expect(comp.isSaving).toEqual(true);
			saveSubject.error('This is an error!');

			// THEN
			expect(leaderboardEntryService.update).toHaveBeenCalled();
			expect(comp.isSaving).toEqual(false);
			expect(comp.previousState).not.toHaveBeenCalled();
		});
	});
});
