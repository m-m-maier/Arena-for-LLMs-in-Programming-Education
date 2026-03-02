import { IModel, NewModel } from './model.model';

export const sampleWithRequiredData: IModel = {
	id: 16480,
	modelName: 'nor including yowza',
	organization: 'ack afraid mechanically',
	provider: 'whether',
	license: 'maul before since',
	active: true,
};

export const sampleWithPartialData: IModel = {
	id: 22255,
	modelName: 'uh-huh sympathetically',
	organization: 'beyond hmph',
	provider: 'indeed blaring',
	apiKey: 'availability',
	license: 'after against reboot',
	active: true,
};

export const sampleWithFullData: IModel = {
	id: 8613,
	modelName: 'hm readily',
	organization: 'gah nor',
	provider: 'clueless pull transom',
	apiKey: 'less ceramics er',
	baseUrl: 'trusty',
	license: 'geez superb authentic',
	active: true,
};

export const sampleWithNewData: NewModel = {
	modelName: 'knife courageously demob',
	organization: 'worth considering',
	provider: 'mom libel',
	license: 'gah',
	active: false,
	id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
