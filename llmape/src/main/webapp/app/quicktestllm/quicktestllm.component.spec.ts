import { ComponentFixture, TestBed } from '@angular/core/testing';

import QuicktestllmComponent from './quicktestllm.component';
import { AccountService } from 'app/core/auth/account.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

describe('QuicktestllmComponent', () => {
	let component: QuicktestllmComponent;
	let fixture: ComponentFixture<QuicktestllmComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [QuicktestllmComponent],
			providers: [AccountService, provideHttpClient(), provideHttpClientTesting()],
		})
			.overrideTemplate(QuicktestllmComponent, '')
			.compileComponents();

		fixture = TestBed.createComponent(QuicktestllmComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
