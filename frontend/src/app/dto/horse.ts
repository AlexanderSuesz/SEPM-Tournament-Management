import {Sex} from './sex';
import {Breed} from "./breed";

export interface Horse {
  id?: number;
  name: string;
  sex: Sex;
  dateOfBirth: Date;
  height: number;
  weight: number;
  breed?: Breed;
}

export interface HorseListDto {
  id: number,
  name: string,
  sex: Sex,
  dateOfBirth: Date;
  breed: Breed;
}


export interface HorseSearch {
  name?: string;
  sex?: Sex;
  bornEarliest?: Date;
  bornLastest?: Date;
  breedName?: string;
  limit?: number;
}

export interface HorseSelection {
    id: number;
    name: string;
    dateOfBirth: Date;
}
