import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of, retry} from 'rxjs';
import {Horse} from 'src/app/dto/horse';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {Breed} from "../../../dto/breed";
import {BreedService} from "../../../service/breed.service";
import {ErrorFormatterService} from '../../../service/error-formatter.service'


export enum HorseCreateEditMode {
  create,
  edit,
  info
}

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {
  inputsDisabled: Boolean = false; // Inputs will be enabled for all views except the details view.
  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    sex: Sex.female,
    dateOfBirth: new Date(),
    height: 0,
    weight: 0,
  };
  horseID: number = 42;

  private heightSet: boolean = false;
  private weightSet: boolean = false;
  private dateOfBirthSet: boolean = false;

  constructor(
    private service: HorseService,
    private breedService: BreedService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormater: ErrorFormatterService,
  ) {
  }

  get height(): number | null {
    return this.heightSet
      ? this.horse.height
      : null;
  }

  set height(value: number) {
    this.heightSet = true;
    this.horse.height = value;
  }

  get weight(): number | null {
    return this.weightSet
      ? this.horse.weight
      : null;
  }

  set weight(value: number) {
    this.weightSet = true;
    this.horse.weight = value;
  }

  get dateOfBirth(): Date | null {
    return this.dateOfBirthSet
      ? this.horse.dateOfBirth
      : null;
  }

  set dateOfBirth(value: Date) {
    this.dateOfBirthSet = true;
    this.horse.dateOfBirth = value;
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Edit Horse: ' + this.horse.name;
      case HorseCreateEditMode.info:
        return 'Details about Horse: ' + this.horse.name;
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Edit';
      case HorseCreateEditMode.info:
        return 'Change to Edit View'
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }


  get sex(): string {
    switch (this.horse.sex) {
      case Sex.male: return 'Male';
      case Sex.female: return 'Female';
      default: return '';
    }
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'edited';
        // Method won't be called in details view
      default:
        return '?';
    }
  }

  ngOnInit(): void {
    this.route.data.subscribe(extractedMode => {
      this.mode = extractedMode.mode;
      if (this.mode == HorseCreateEditMode.info){
        this.inputsDisabled = true;
      }
      else {
        this.inputsDisabled = false;
      }
    });
    if (this.mode == HorseCreateEditMode.edit || this.mode == HorseCreateEditMode.info){
      this.route.url.subscribe(url =>{
        this.service.getById(Number(url[1])).subscribe({
          next: data => {
            this.horse = data;
          this.heightSet = true;
          this.weightSet = true;
          this.dateOfBirthSet = true;
          this.horseID = Number(url[1]);
          },
          error: error => {
            console.error('Error fetching horses', error);
            this.displayErrorMessageOnScreen(error)
          }
        });
      })
    }
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatBreedName(breed: Breed | null): string {
    return breed?.name ?? '';
  }

  breedSuggestions = (input: string) => (input === '')
    ? of([])
    :  this.breedService.breedsByName(input, 5);

  public onSubmit(form: NgForm): void {
    if (this.mode == HorseCreateEditMode.info){ // When in details mode, the submit button will instead directly lead to the edit page.
      this.router.navigate(['/horses/edit/'+this.horseID]); // In the details mode we want to directly change to the edit mode
    }
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(this.horse);
          break;
        case HorseCreateEditMode.edit:
          observable = this.service.put(this.horse);
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error editing horse', error);
          this.displayErrorMessageOnScreen(error)
        }
      });
    }
  }

  public onDelete(): void {
    if (this.mode == HorseCreateEditMode.edit || this.mode == HorseCreateEditMode.info) {
      let observable: Observable<Horse> = this.service.deleteById(this.horseID);
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully deleted.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error deleting horse', error);
          this.displayErrorMessageOnScreen(error)
        }
      });
    }
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
