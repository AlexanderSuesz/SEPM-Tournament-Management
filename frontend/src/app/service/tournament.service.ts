import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {map, Observable, tap, throwError} from 'rxjs';
import {formatIsoDate} from '../util/date-helper';
import {
  TournamentCreateDto, TournamentDetailDto, TournamentDetailParticipantDto,
  TournamentListDto,
  TournamentSearchParams,
  TournamentStandingsDto, TournamentStandingsTreeDto
} from "../dto/tournament";


const baseUri = environment.backendUrl + '/tournaments';

@Injectable({
  providedIn: 'root'
})
export class TournamentService {
  constructor(
    private http: HttpClient
  ) {
  }


  public create(tournament: TournamentCreateDto): Observable<TournamentDetailDto> {
    // TODO this is not implemented yet!
    return throwError(() => ({message: "Not implemented yet"}));
  }

  search(searchParams: TournamentSearchParams): Observable<TournamentListDto[]> {
    if (searchParams.name === '') {
      delete searchParams.name;
    }
    let params = new HttpParams();
    if (searchParams.name) {
      params = params.append('name', searchParams.name);
    }
    if (searchParams.earliestTournamentDay) {
      params = params.append('earliestTournamentDay', formatIsoDate(searchParams.earliestTournamentDay));
    }
    if (searchParams.latestTournamentDay) {
      params = params.append('latestTournamentDay', formatIsoDate(searchParams.latestTournamentDay));
    }
    if (searchParams.limit) {
      params = params.append('limit', searchParams.limit);
    }
    return this.http.get<TournamentListDto[]>(baseUri, { params })
      .pipe(tap(tournaments => tournaments.map(h => {
        h.startDate = new Date(h.startDate); // Parse date string
        h.endDate = new Date(h.endDate); // Parse date string
      })));
  }

}
