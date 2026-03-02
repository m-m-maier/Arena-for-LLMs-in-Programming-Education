import { Injectable, NgZone, inject } from '@angular/core';
import { HttpClient, HttpResponse, HttpStatusCode } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { NewPrompt } from 'app/entities/prompt/prompt.model';
import { IAIResponseDTO } from '../dto/my.airesponse.dto';
import { AIStreamTokenDTO } from '../dto/my.aiStreamToken.dto';
import { IGeneratePromptDTO } from '../dto/my.generatePrompt.dto';
import { IGeneratePromptResponseDTO } from '../dto/my.generatePromptResponse.dto';

@Injectable({ providedIn: 'root' })
export class MyPromptService {
	protected readonly http = inject(HttpClient);
	protected readonly applicationConfigService = inject(ApplicationConfigService);
	private readonly zone = inject(NgZone);

	submitPrompt(prompt: NewPrompt): Observable<HttpResponse<IAIResponseDTO>> {
		const resourceUrl = this.applicationConfigService.getEndpointFor('api/myprompt/submitPrompt');
		return this.http.post<IAIResponseDTO>(resourceUrl, prompt, { observe: 'response' });
	}

	submitQuickTestPrompt(prompt: NewPrompt): Observable<HttpResponse<number>> {
		const resourceUrl = this.applicationConfigService.getEndpointFor('api/myprompt/submitQuickTestPrompt');
		return this.http.post<number>(resourceUrl, prompt, { observe: 'response' });
	}

	streamAIResponses(battleId: number): Observable<AIStreamTokenDTO> {
		return new Observable<AIStreamTokenDTO>(observer => {
			const resourceUrl = this.applicationConfigService.getEndpointFor('api/myprompt/streamAIResponses');
			const url = `${resourceUrl}?battleId=${battleId}`;
			const eventSource = new EventSource(url);

			let receivedCompletes = 0;

			eventSource.onmessage = event => {
				const token = JSON.parse(event.data) as AIStreamTokenDTO;

				if (token.complete) {
					receivedCompletes += 1;
					if (receivedCompletes > 1) {
						this.zone.run(() => {
							// needed for that the view knows that there were changes
							observer.complete();
						});
						eventSource.close();
					}
				} else {
					this.zone.run(() => {
						// needed for that the view knows that there were changes
						observer.next(token);
					});
				}
			};

			eventSource.onerror = error => {
				this.zone.run(() => {
					// needed for that the view knows that there were changes
					observer.error(error);
				});

				eventSource.close();
			};

			return () => {
				eventSource.close();
			};
		});
	}

	streamManyAIResponses(promptId: number, modelIds: number[]): Observable<AIStreamTokenDTO> {
		const resourceUrl = this.applicationConfigService.getEndpointFor('api/myprompt/streamManyAIResponses');
		const url = `${resourceUrl}?promptId=${promptId}` + modelIds.map(id => `&selectedModelIds=${id}`).join('');

		return new Observable<AIStreamTokenDTO>(observer => {
			let receivedCompletes = 0;
			const abortController = new AbortController();

			this.fetchSSE(
				url,

				(tokenObj: AIStreamTokenDTO) => {
					this.zone.run(() => {
						if (tokenObj.complete) {
							receivedCompletes++;
							if (receivedCompletes >= modelIds.length) {
								observer.complete();
							}
						} else {
							observer.next(tokenObj);
						}
					});
				},

				error => {
					this.zone.run(() => observer.error(error));
				},
				abortController.signal,
			);

			return () => {
				abortController.abort();
			};
		});
	}

	generatePromptAndVote(generatePromptDTO: IGeneratePromptDTO): Observable<HttpResponse<IGeneratePromptResponseDTO>> {
		const resourceUrl = this.applicationConfigService.getEndpointFor('api/myprompt/generatePromptAndVote');
		return this.http.post<IGeneratePromptResponseDTO>(resourceUrl, generatePromptDTO, { observe: 'response' });
	}

	async fetchSSE(url: string, onMessage: (data: any) => void, onError: (err: any) => void, signal: AbortSignal): Promise<void> {
		try {
			let authToken = localStorage.getItem('jhi-authenticationToken') ?? sessionStorage.getItem('jhi-authenticationToken');
			if (authToken) {
				authToken = JSON.parse(authToken);
			}
			const response = await fetch(url, {
				method: 'GET',
				headers: {
					Authorization: `Bearer ${authToken}`,
				},
				signal,
			});

			if ((response.status as HttpStatusCode) === HttpStatusCode.TooManyRequests)
				throw new Error('Too many requests, rate limit exceeded.');
			if (!response.body) throw new Error('ReadableStream not supported');

			const reader = response.body.getReader();
			const decoder = new TextDecoder();
			let buffer = '';

			// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
			while (true) {
				const { done, value } = await reader.read();
				if (done) break;

				buffer += decoder.decode(value, { stream: true });

				// Process complete SSE events (delimited by blank line)
				let eventEnd = buffer.indexOf('\n\n');
				while (eventEnd !== -1) {
					const rawEvent = buffer.slice(0, eventEnd).trim();
					buffer = buffer.slice(eventEnd + 2);

					const eventData = this.parseSSEEvent(rawEvent);
					if (eventData !== null) {
						onMessage(eventData);
					}

					eventEnd = buffer.indexOf('\n\n');
				}
			}

			if (buffer.trim()) {
				const eventData = this.parseSSEEvent(buffer.trim());
				if (eventData !== null) {
					onMessage(eventData);
				}
			}
		} catch (err) {
			if (err instanceof DOMException && err.name === 'AbortError') {
				// Intentionally ignoring AbortError
			} else if (err instanceof Error) {
				onError(err);
			} else {
				onError(new Error('Unknown error during SSE fetch'));
			}
		}
	}

	parseSSEEvent(rawEvent: string): any {
		const lines = rawEvent.split(/\r?\n/);
		const dataLines: string[] = [];

		for (const line of lines) {
			if (line.startsWith('data:')) {
				dataLines.push(line.slice(5).trim());
			}
		}

		if (dataLines.length > 0) {
			const dataStr = dataLines.join('\n');
			try {
				return JSON.parse(dataStr);
			} catch (e) {
				console.error('SSE parse error: invalid JSON', dataStr, e);
				return null;
			}
		}

		return null;
	}
}
