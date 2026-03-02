import dayjs from 'dayjs/esm';

export interface ILeaderboardRowDTO {
	id: number;
	modelName: string;
	score: number;
	numberOfVotes: number;
	numberOfTies: number;
	organization: string;
	license: string;
}

export interface ILeaderboardDTO {
	rows: ILeaderboardRowDTO[];
	timestamp: dayjs.Dayjs | null;
}
