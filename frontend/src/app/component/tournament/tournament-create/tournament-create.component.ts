import {Component} from '@angular/core';
import {NgForm, NgModel} from "@angular/forms";
import {TournamentCreateDto} from "../../../dto/tournament";
import {TournamentService} from "../../../service/tournament.service";
import {formatIsoDate} from "../../../util/date-helper";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../service/error-formatter.service";
import {HorseSelection} from "../../../dto/horse";
import {map, Observable} from "rxjs";
import {HorseService} from "../../../service/horse.service";
import {Location} from "@angular/common";

@Component({
  selector: 'app-tournament-create',
  templateUrl: './tournament-create.component.html',
  styleUrls: ['./tournament-create.component.scss']
})
export class TournamentCreateComponent {
  tournament: TournamentCreateDto = {
    name: "",
    startDate: new Date(), // dummy
    endDate: new Date(), //dummy
    participants: [], // dummy
  };
  participants: (HorseSelection | null)[] = new Array(8);
  dummyHorseSelectionModel: unknown; // Just needed for the autocomplete
  startDateSet = false;
  endDateSet = false;

  get startDate(): string | null {
    return this.startDateSet
      ? formatIsoDate(this.tournament.startDate)
      : null;
  }

  set startDate(value: string | null) {
    if (!value) {
      this.startDateSet = false;
    } else {
      this.tournament.startDate = new Date(value);
    }
  }

  get endDate(): string | null {
    return this.endDateSet
      ? formatIsoDate(this.tournament.endDate)
      : null;
  }

  set endDate(value: string | null) {
    if (!value) {
      this.endDateSet = false;
    } else {
      this.tournament.endDate = new Date(value);
    }
  }


  public constructor(
    private service: TournamentService,
    private horseService: HorseService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private location: Location,
  ) {
  }

  submit(form: NgForm) {
    console.log(form.valid, this.tournament);
    if (form.invalid)
      return;
    const participants= <HorseSelection[]>this.participants
      .filter(x => x != null);
    if (participants.length != 8) {
      this.notification.error("A tournament must have exactly 8 participants", "Not Enough Participants");
      return;
    }
    this.tournament.participants = participants;
    this.service.create(this.tournament)
      .subscribe({
        next: data => {
          this.notification.success(`Tournament ${this.tournament.name} created`, "Tournament created successfully");
          this.location.back();
        },
        error: error => {
          console.error(error.message, error);
          this.notification.error(this.errorFormatter.format(error), "Could Not Create Tournament", {
            enableHtml: true,
            timeOut: 10000,
          });
        }
      });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatHorse(participant: HorseSelection | null): string {
    return !participant
      ? ""
      : `${participant.name} (${participant.dateOfBirth.toLocaleDateString()})`
  }

  horseSuggestions = (input: string): Observable<HorseSelection[]> =>
    this.horseService.search({name: input, limit: 5})
      .pipe(map(horses => horses.map(h => ({
        id: h.id,
        name: h.name,
        dateOfBirth: h.dateOfBirth,
      }))));

  public addHorse(horse: HorseSelection | null) {
    if (!horse)
      return;
    // This should happen late, when the ngModelChange hook has completed,
    // so that changing dummyHorseSelectionModel works
    setTimeout(() => {
      const participants = this.participants;
      for (let i = 0; i < 8; i++) {
        if (participants[i]?.id === horse.id) {
          this.notification.error(`${horse.name} is already in participant list`, "Duplicate Participant");
          this.dummyHorseSelectionModel = null;
          return;
        }
        if (participants[i] == null) {
          participants[i] = horse;
          this.dummyHorseSelectionModel = null;
          return;
        }
      }
      // If the above has not returned, we could not add the horse because all 8 slots are full
      this.notification.error("All 8 slots are full", "Could Not Add Participant");
    });
  }

  public removeHorse(index: number) {
    this.participants.splice(index, 1);
    this.participants.push(null);
  }
}
