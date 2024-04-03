import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TournamentDetailParticipantDto, TournamentStandingsTreeDto} from "../../../../dto/tournament";
import {of} from "rxjs";
import {formatIsoDate} from "../../../../util/date-helper";

enum TournamentBranchPosition {
  FINAL_WINNER,
  UPPER,
  LOWER,
}

@Component({
  selector: 'app-tournament-standings-branch',
  templateUrl: './tournament-standings-branch.component.html',
  styleUrls: ['./tournament-standings-branch.component.scss']
})
export class TournamentStandingsBranchComponent {

  protected readonly TournamentBranchPosition = TournamentBranchPosition;
  @Input() branchPosition = TournamentBranchPosition.FINAL_WINNER;
  @Input() treeBranch: TournamentStandingsTreeDto | undefined; // here we will receive the updated participant for this node
  @Input() allParticipants: TournamentDetailParticipantDto[] = [];

  // If this section of this round already has a winner then the participants of this section of this round can't be changed (the input field will be disabled)
  @Input() thisRoundAlreadyHasWinner: boolean = false;

  // if disableChildNodes is set true then the child nodes of this node will be disabled
  disableChildNodes: boolean = false;

  // When this node of child nodes of this node are changed then the parent will receive the tree structure of this node
  @Output() updateThisTreeBranchChangedEvent = new EventEmitter<{tree: TournamentStandingsTreeDto, pos: TournamentBranchPosition}>();

  public ngOnInit() {
    if (this.treeBranch?.thisParticipant != null) this.disableChildNodes = true;
  }


  get isUpperHalf(): boolean {
    return this.branchPosition === TournamentBranchPosition.UPPER;
  }

  get isLowerHalf(): boolean {
    return this.branchPosition === TournamentBranchPosition.LOWER;
  }

  get isFinalWinner(): boolean {
    return this.branchPosition === TournamentBranchPosition.FINAL_WINNER;
  }

  suggestions = (input: string) => {
    // The candidates are either the participants of the previous round matches in this branch
    // or, if this is the first round, all participant horses
    const allCandidates =
      this.treeBranch?.branches?.map(b => b.thisParticipant)
      ?? this.allParticipants;
    const results = allCandidates
        .filter(x => !!x)
        .map(x => <TournamentDetailParticipantDto><unknown>x)
        .filter((x) =>
            x.name.toUpperCase().match(new RegExp(`.*${input.toUpperCase()}.*`)));
    return of(results);
  };

  participantChanged() {
    if (this.treeBranch?.thisParticipant != null) {
      this.disableChildNodes = true;
      this.updateThisTreeBranchChangedEvent.emit({tree: this.treeBranch, pos: this.branchPosition})
    }
  }

  // If the left child was changed this function updates the left branch of this node
  // and sends the updated tree structure to the parent
  public updateLeftBranch(leftBranch: TournamentStandingsTreeDto, branchSelector: TournamentBranchPosition) {
    if (this.treeBranch != undefined && leftBranch != undefined && this.treeBranch?.branches != undefined) {
      let treeBranchCopy = this.treeBranch.branches.slice(); // we create a shallow copy with .slice()
      let branchLower: TournamentStandingsTreeDto | undefined = treeBranchCopy.pop(); // the first element of treeBranchCopy the right (UPPER) branch
      if (branchSelector == TournamentBranchPosition.UPPER && branchLower != undefined) {
        treeBranchCopy = [];
        treeBranchCopy.push(leftBranch);
        treeBranchCopy.push(branchLower);
        this.treeBranch.branches = treeBranchCopy;
      }
      this.updateThisTreeBranchChangedEvent.emit({tree: this.treeBranch, pos: this.branchPosition});
    }
  }

  // If the right child was changed this function updates the right branch of this node
  // and sends the updated tree structure to the parent
  public updateRightBranch(rightBranch: TournamentStandingsTreeDto, branchSelector: TournamentBranchPosition) {
    if (this.treeBranch != undefined && rightBranch != undefined && this.treeBranch?.branches != undefined) {
      let treeBranchCopy = this.treeBranch.branches.slice(); // we create a shallow copy with .slice()
      treeBranchCopy.pop(); // don't need the lower branch
      let branchUpper: TournamentStandingsTreeDto | undefined = treeBranchCopy.pop();
      if (branchSelector == TournamentBranchPosition.LOWER && branchUpper != undefined) {
        treeBranchCopy = [];
        treeBranchCopy.push(branchUpper);
        treeBranchCopy.push(rightBranch);
        this.treeBranch.branches = treeBranchCopy;
      }
      this.updateThisTreeBranchChangedEvent.emit({tree: this.treeBranch, pos: this.branchPosition});
    }
  }

  public formatParticipant(participant: TournamentDetailParticipantDto | null): string {
    return participant
        ? `${participant.name} (${formatIsoDate(participant.dateOfBirth)})`
        : "";
  }
}
