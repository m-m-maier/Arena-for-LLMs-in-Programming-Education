export interface IVoteDTO {
	battleId: number;
	voteOption: VoteOption;
}

export enum VoteOption {
	MODEL_A_BETTER,
	TIE,
	MODEL_B_BETTER,
}
