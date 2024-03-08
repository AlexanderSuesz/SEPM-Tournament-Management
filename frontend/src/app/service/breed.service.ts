import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Breed} from "../dto/breed";
import {Observable} from "rxjs";

const baseUri = environment.backendUrl + "/breeds";

@Injectable({
  providedIn: 'root'
})
export class BreedService {


  constructor(
    private http: HttpClient
  ) {
  }

  public breedsByName(name: string, limit: number | undefined): Observable<Breed[]> {
    let params = new HttpParams();
    params = params.append("name", name);
    if (limit != null) {
      params = params.append("limit", limit);
    }
    return this.http.get<Breed[]>(baseUri, { params });
  }
}
