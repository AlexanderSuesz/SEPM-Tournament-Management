import {Component, OnInit} from '@angular/core';
import {
  TournamentDetailDto,
  TournamentDetailParticipantDto,
  TournamentStandingsDto,
  TournamentStandingsTreeDto
} from "../../../dto/tournament";
import {TournamentService} from "../../../service/tournament.service";
import {ActivatedRoute} from "@angular/router";
import {NgForm} from "@angular/forms";
import {Location} from "@angular/common";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../service/error-formatter.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-tournament-standings',
  templateUrl: './tournament-standings.component.html',
  styleUrls: ['./tournament-standings.component.scss']
})
export class TournamentStandingsComponent implements OnInit {
  standings: TournamentStandingsDto = { // we fill the TournamentStandingsDto object with dummy data, so we don't get the error that it could be undefined when submitting it.
    id: -1,
    name: "",
    participants: [],
    tree: {thisParticipant: null, branches: undefined}
  };
  startDate: Date = new Date(); // placeholder for the start date of the tournament (used to build object to be submitted)
  endDate: Date = new Date();
  participantEntryNumberUpdateHelper: TournamentDetailParticipantDto[] = []; // used to temporarily save the entry numbers of participants when the tree is disassembled
  participantRoundReachedUpdateHelper: TournamentDetailParticipantDto[] = []; // used to temporarily save the round numbers of participants when the tree is disassembled

  public constructor(
    private service: TournamentService,
    private errorFormatter: ErrorFormatterService,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private location: Location,
  ) {
  }

  // Initializes tree with data received from backend and saves necessary data needed for the return-dto
  public ngOnInit() {
    this.route.url.subscribe(url =>{
      this.service.getTournamentDetailsById(Number(url[1])).subscribe({
        next: data => {
          this.startDate = data.startDate;
          this.endDate = data.endDate;
          this.standings = {
            id: data.id,
            name: data.name,
            participants: data.participants,
            tree: this.generateStandingsTree(data.participants)
          }
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.displayErrorMessageOnScreen(error)
        }
      });
    })
  }

  public submit(form: NgForm) {
      console.log('is form valid?', form.valid);
      if (form.valid) {
        let observable: Observable<TournamentDetailDto>;
        let tournamentDetail: TournamentDetailDto = {
          id: this.standings.id,
          name: this.standings.name,
          startDate: this.startDate,
          endDate: this.endDate,
          participants: this.getParticipantsDetailDto()
        }
        observable = this.service.updateTournamentStandings(tournamentDetail);
        observable.subscribe({
          next: data => {
            this.notification.success(`Tournament ${this.standings.name} successfully updated.`);
          },
          error: error => {
            console.error('Error updating tournament', error);
            this.displayErrorMessageOnScreen(error)
          }
        });
      }
  }

  public generateFirstRound() {
    if (!this.standings)
      return;
    // TODO implement
  }

  // update tree with tree received from the tree root
  public updateTree(treeToUpdate: TournamentStandingsTreeDto) {
    this.standings.tree = treeToUpdate;
  }

  // converts the tree to a TournamentDetailParticipantDto[] and assigns each entry information about their position in the tree
  private getParticipantsDetailDto(): TournamentDetailParticipantDto[] {
    this.participantRoundReachedUpdateHelper = []; // each entry has their roundReached value set to their round reached in this tournament
    this.participantEntryNumberUpdateHelper = []; // each entry is ordered according to their entry number
    this.treeValueExctractorHelper(this.standings.tree, 4, 0); // fills the two array above with the tree entries
    let participants: TournamentDetailParticipantDto[] = [];
    this.participantEntryNumberUpdateHelper.forEach(orderedParticipants => {
      let foundSameParticipant: boolean = false;
      this.participantRoundReachedUpdateHelper.forEach(participantWithRoundNumber => {
        // finds all participants which are already in a round (have a round number)
        if (participantWithRoundNumber.horseId == orderedParticipants.horseId) {
          foundSameParticipant = true;
          participants?.push({
            horseId: participantWithRoundNumber.horseId,
            name: participantWithRoundNumber.name,
            dateOfBirth: participantWithRoundNumber.dateOfBirth,
            entryNumber: orderedParticipants.entryNumber,
            roundReached: participantWithRoundNumber.roundReached
          })
        }
      });
      if (!foundSameParticipant) { // finds all participants which are not in a round (have no round number)
        participants?.push({
          horseId: orderedParticipants.horseId,
          name: orderedParticipants.name,
          dateOfBirth: orderedParticipants.dateOfBirth,
          entryNumber: orderedParticipants.entryNumber,
          roundReached: orderedParticipants.roundReached
        })
      }
    });
    return participants;
  }

  // helper for getParticipantsDetailDto()
  private treeValueExctractorHelper(curNode: TournamentStandingsTreeDto, round: number, nextEntryIndex: number): number {
    if (curNode != null && curNode.thisParticipant != null) {
      let foundRoundEntryOfHorse: Boolean = false; // we only want to save the highest round entry of a horse.
      this.participantRoundReachedUpdateHelper.forEach(participant => {
        if (participant.horseId == curNode.thisParticipant?.horseId) foundRoundEntryOfHorse = true;
      });
      if (!foundRoundEntryOfHorse) { // only when no round entry of this horse was found will we save the round (we only want the furthest round a horse got)
        curNode.thisParticipant.roundReached = round;
        this.participantRoundReachedUpdateHelper.push(curNode.thisParticipant);
      }
    }
    if (curNode.branches == undefined) {
      // we are in a leave - which means we can derive the correct entry number from it
      if (curNode.thisParticipant != null) {
        curNode.thisParticipant.entryNumber = nextEntryIndex;
        this.participantEntryNumberUpdateHelper.push(curNode.thisParticipant);
      }
      nextEntryIndex += 1;
    } else {
      // we are not in a leave - which means it contains the correct round number
      let branchesCopy = curNode.branches.slice(); // we only want to edit the copy, not the original!
      let lowerBranch = branchesCopy.pop();
      let upperBranch = branchesCopy.pop();
      if (upperBranch != undefined && lowerBranch != undefined) { // this is always true since we already checked above that we are not in a leave
        nextEntryIndex = this.treeValueExctractorHelper(upperBranch, round - 1, nextEntryIndex);
        nextEntryIndex = this.treeValueExctractorHelper(lowerBranch, round - 1, nextEntryIndex);
      }
    }
    return nextEntryIndex;
  }

  private generateStandingsTree(participants: TournamentDetailParticipantDto[]): TournamentStandingsTreeDto {
    let orderedParticipants: TournamentDetailParticipantDto[] = [];
    let wantedEntryNumber: number = 0;
    for (let i = 0; i < 8; i++) {
      participants.forEach(participant => {
        if (participant.entryNumber != undefined && participant.entryNumber == wantedEntryNumber) {
          orderedParticipants.push(participant);
        };
      });
      wantedEntryNumber++;
    }
    orderedParticipants.forEach(participant => console.log(participant))
    const root: TournamentStandingsTreeDto = this.treeGenerationHelper(4, 0, 7,orderedParticipants);
    return root;
  }

  /**
   * A small helper method to build the tree structure
   *
   * @param curRound the round which the current root of the tree is
   * @param participants all participants of the current segment
   * @private the fully built tree structure for the current root element
   */
  private treeGenerationHelper(curRound: number, lowerBound: number, upperBound: number, participants: TournamentDetailParticipantDto[]): TournamentStandingsTreeDto {
    if (curRound <= 1) {
      // if we are in a leaf we just return a leaf node with no further nodes
      if (participants.length == 0) {
        return {thisParticipant: null, branches: undefined};
      } else {
        let returnValue: TournamentDetailParticipantDto | undefined = participants.pop();
        if (returnValue != undefined) {
          return {thisParticipant: returnValue, branches: undefined};
        } else {
          return {thisParticipant: null, branches: undefined};
        }
      }
    }
    const splitPoint = Math.floor((upperBound - lowerBound) / 2) + lowerBound;
    let upperParticipants: TournamentDetailParticipantDto[] = [];
    let lowerParticipants: TournamentDetailParticipantDto[] = [];
    let winner: TournamentDetailParticipantDto | null = null; // winner is by default null
    participants.slice().forEach(participant =>
    {
      if (participant.roundReached != undefined && participant.roundReached >= curRound) winner = participant;
      if (participant.entryNumber != undefined) {
        if (participant.entryNumber >= 0 && participant.entryNumber <= splitPoint) upperParticipants.push(participant);
      else if (participant.entryNumber > splitPoint && participant.entryNumber <= upperBound) lowerParticipants.push(participant);
      }
    });
    return {thisParticipant: winner, branches:
        [this.treeGenerationHelper(curRound - 1, lowerBound, splitPoint, upperParticipants), this.treeGenerationHelper(curRound - 1, splitPoint + 1, upperBound, lowerParticipants)]};
  }

  private displayErrorMessageOnScreen(error: any): void {
    if (error.status === 0){
      this.notification.error("Couldn't load server data"); // If the backend isn't up.
    }
    else {
      this.notification.error(this.errorFormatter.format(error), 'Error', {enableHtml: true});
    }
  }
}
