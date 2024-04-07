package at.ac.tuwien.sepr.assignment.individual.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The standings of horses in a tournament represented as a tree structure. This tree can be derived from a TournamentDetailDto.
 */
public class TournamentStandingsTreeDto {
  private TournamentDetailParticipantDto thisParticipant; // the root participant - in the tournament one round higher than its children
  private ArrayList<TournamentStandingsTreeDto> branches; // the child participants

  public TournamentStandingsTreeDto(TournamentDetailParticipantDto thisParticipant, TournamentStandingsTreeDto[] childs) {
    this.thisParticipant = thisParticipant;
    if (childs != null) {
      this.branches = new ArrayList<>();
      this.branches.addAll(List.of(childs));
    } else {
      this.branches = null;
    }
  }

  public TournamentDetailParticipantDto getThisParticipant() {
    return thisParticipant;
  }

  public void setThisParticipant(TournamentDetailParticipantDto thisParticipant) {
    this.thisParticipant = thisParticipant;
  }

  public ArrayList<TournamentStandingsTreeDto> getBranches() {
    return branches;
  }

  public void setBranches(ArrayList<TournamentStandingsTreeDto> branches) {
    this.branches = branches;
  }

  @Override
  public String toString() {
    if (this.branches != null) {
      return "TournamentStandingsTreeDto{"
          + "thisParticipant=" + thisParticipant
          + ", leftBranch=[" + branches.getFirst().toString()
          + "], rightBranch=[" + branches.getLast().toString()
          + "]}";
    } else {
      return "TournamentStandingsTreeDto{"
          + "thisParticipant=" + thisParticipant
          + ", leftBranch=[" + null
          + "], rightBranch=[" + null
          + "]}";
    }
  }

  /**
   * Returns a very compact graphical representation of this tree as a String where only the name of the participating horses and their position in the tree
   * are displayed
   *
   * @return a graphical representation of this tree
   */
  public String toStringSmaller() {
    return rekrsiveNamePrinter(this);
  }

  private String rekrsiveNamePrinter(TournamentStandingsTreeDto treeNode) {
    String participantName;
    if (treeNode.getThisParticipant() == null) {
      participantName = "null";
    } else {
      participantName = treeNode.thisParticipant.name();
    }
    if (treeNode.branches == null) {
      return participantName;
    }
    return participantName + ":" + "{" + rekrsiveNamePrinter(treeNode.branches.getFirst()) + ", "
        + rekrsiveNamePrinter(treeNode.branches.getLast()) + "}";
  }

}
