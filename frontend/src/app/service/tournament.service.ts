import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {map, Observable, tap} from 'rxjs';
import {formatIsoDate} from '../util/date-helper';
import {
  TournamentCreateDto, TournamentDetailDto, TournamentDetailParticipantDto,
  TournamentListDto,
  TournamentSearchParams,
  TournamentStandingsDto, TournamentStandingsTreeDto, TournamentUpdateDto
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

  /**
   * Create a new tournament in the system.
   *
   * @param tournament the data for the tournament that should be created
   * @return an Observable for the created tournament
   */
  public create(tournament: TournamentCreateDto): Observable<TournamentDetailDto> {
    return this.http.post<TournamentDetailDto>(
      baseUri,
      tournament
    );
  }

  public updateTournamentStandings(tournamentUpdateInfo: TournamentUpdateDto): Observable<TournamentDetailDto> {
    return this.http.put<TournamentDetailDto>(
      `${baseUri}/standings/${tournamentUpdateInfo.id}`,
      tournamentUpdateInfo
    );
  }

  public getTournamentDetailsById(id: number): Observable<TournamentDetailDto> {
    return this.http.get<TournamentDetailDto>(`${baseUri}/standings/${id}`)
  }

  public search(searchParams: TournamentSearchParams): Observable<TournamentListDto[]> {
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
