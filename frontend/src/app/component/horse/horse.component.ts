import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse, HorseListDto} from '../../dto/horse';
import {HorseSearch} from '../../dto/horse';
import {debounceTime, map, Observable, of, Subject} from 'rxjs';
import {BreedService} from "../../service/breed.service";
import {ErrorFormatterService} from '../../service/error-formatter.service'

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {
  search = false;
  horses: HorseListDto[] = [];
  searchParams: HorseSearch = {};
  searchBornEarliest: string | null = null;
  searchBornLatest: string | null = null;
  horseForDeletion: Horse | undefined;
  searchChangedObservable = new Subject<void>();

  constructor(
    private service: HorseService,
    private breedService: BreedService,
    private notification: ToastrService,
    private errorFormater: ErrorFormatterService,
) { }

  ngOnInit(): void {
    this.reloadHorses();
    this.searchChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.reloadHorses()});
  }

  reloadHorses() {
    if (this.searchBornEarliest == null || this.searchBornEarliest === "") {
      delete this.searchParams.bornEarliest;
    } else {
      this.searchParams.bornEarliest = new Date(this.searchBornEarliest);
    }
    if (this.searchBornLatest == null || this.searchBornLatest === "") {
      delete this.searchParams.bornLastest;
    } else {
      this.searchParams.bornLastest = new Date(this.searchBornLatest);
    }
    this.service.search(this.searchParams)
      .subscribe({
        next: data => {
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.displayErrorMessageOnScreen(error)
        }
      });
  }
  searchChanged(): void {
    this.searchChangedObservable.next();
  }

  breedSuggestions = (input: string): Observable<string[]> =>
    this.breedService.breedsByName(input, 5)
      .pipe(map(bs =>
        bs.map(b => b.name)));

  formatBreedName = (name: string) => name; // It is already the breed name, we just have to give a function to the component

  public onDelete(horseId: number): void {
    let horse: Horse;
    // retrieves the horse with the given id to display the name of the deleted horse in a notification. 
    this.service.getById(horseId).subscribe({
      next: data => {
        horse = data;
      },
      error: error => {
        console.error('Error fetching horses', error);
        this.displayErrorMessageOnScreen(error)
      }
    });
    let observable: Observable<Horse> = this.service.deleteById(horseId);
    observable.subscribe({
      next: data => {
        this.notification.success(`Horse ${horse.name} successfully deleted.`);
        this.reloadHorses();
      },
      error: error => {
        console.error('Error deleting horse', error);
        this.displayErrorMessageOnScreen(error)
      }
    });
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
