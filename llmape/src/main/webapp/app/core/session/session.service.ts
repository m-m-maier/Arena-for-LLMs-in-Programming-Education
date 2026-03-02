import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { v4 as uuidv4 } from 'uuid';

@Injectable({ providedIn: 'root' })
export class SessionService {
	private readonly cookieName = 'SESSION_ID';

	constructor(private cookieService: CookieService) {}

	isSessionIdAlreadySet(): boolean {
		return this.cookieService.get(this.cookieName) ? true : false;
	}

	getOrCreateSessionId(): string {
		let sessionId = this.cookieService.get(this.cookieName);

		if (!sessionId) {
			sessionId = uuidv4();
			this.cookieService.set(this.cookieName, sessionId, {
				path: '/',
				sameSite: 'Lax',
				secure: true,
			});
		}

		return sessionId;
	}
}
