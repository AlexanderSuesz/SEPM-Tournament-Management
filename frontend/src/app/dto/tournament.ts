import {HorseSelection} from "./horse";

export interface TournamentSearchParams {
  name?: string;
  startDate?: Date;
  endDate?: Date;
}

export interface TournamentListDto {
  id: number;
  name: string;
  startDate: Date;
  endDate: Date;
}

export interface TournamentCreateDto {
  name: string;
  startDate: Date;
  endDate: Date;
  participants: HorseSelection[];
}

export interface TournamentDetailDto {
  id: number;
  name: string;
  startDate: Date;
  endDate: Date;
  participants: TournamentDetailParticipantDto[];
}

export interface TournamentDetailParticipantDto {
  horseId: number;
  name: string;
  dateOfBirth: Date;
  entryNumber?: number;
  roundReached?: number;
}

export interface TournamentStandingsTreeDto {
  thisParticipant: TournamentDetailParticipantDto | null;
  branches?: TournamentStandingsTreeDto[];
}


export interface TournamentStandingsDto {
  id: number;
  name: string;
  participants: TournamentDetailParticipantDto[];
  tree: TournamentStandingsTreeDto;
}
