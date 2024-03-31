import {Component, OnInit} from '@angular/core';
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../service/error-formatter.service";
import {TournamentService} from "../../service/tournament.service";
import {debounceTime, Subject} from "rxjs";
import {TournamentListDto, TournamentSearchParams} from "../../dto/tournament";
import {FormsModule} from "@angular/forms";
import {NgForOf} from "@angular/common";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-tournament',
  standalone: true,
  imports: [
    FormsModule,
    NgForOf,
    RouterLink
  ],
  templateUrl: './tournament.component.html',
  styleUrl: './tournament.component.scss'
})
export class TournamentComponent implements OnInit {
  tournaments: TournamentListDto[] = [];
  searchChangedObservable = new Subject<void>();
  searchParams: TournamentSearchParams = {};
  searchStartDate: string | null = null;
  searchEndDate: string | null = null;
  constructor(
    private service: TournamentService,
    private notification: ToastrService,
    private errorFormater: ErrorFormatterService,
  ) { }

  ngOnInit(): void {
    this.reloadTournaments();
    this.searchChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.reloadTournaments()});
  }

  reloadTournaments() {
    if (this.searchStartDate == null || this.searchStartDate === "") {
      delete this.searchParams.earliestTournamentDay;
    } else {
      this.searchParams.earliestTournamentDay = new Date(this.searchStartDate);
    }
    if (this.searchEndDate == null || this.searchEndDate === "") {
      delete this.searchParams.latestTournamentDay;
    } else {
      this.searchParams.latestTournamentDay = new Date(this.searchEndDate);
    }
    this.service.search(this.searchParams)
      .subscribe({
        next: data => {
          this.tournaments = data;
        },
        error: error => {
          console.error('Error fetching tournaments', error);
          this.displayErrorMessageOnScreen(error)
        }
      });
  }
  searchChanged(): void {
    this.searchChangedObservable.next();
  }

  private displayErrorMessageOnScreen(error: any): void {
    if (error.status === 0){
      this.notification.error("Couldn't load server data"); // If the backend isn't up.
    }
    else {
      this.notification.error(this.errorFormater.format(error), 'Error', {enableHtml: true});
    }
  }
}
