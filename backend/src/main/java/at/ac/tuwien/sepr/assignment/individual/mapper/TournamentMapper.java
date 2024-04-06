package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * TournamentMapper is responsible for converting Tournament entities to DTOs (Data Transfer Objects) or
 * tournament DTOs in other tournament DTOs.
 */
@Component
public class TournamentMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Converts a Tournament to a TournamentListDto
   *
   * @param tournament the tournament which will be converted
   * @return a TournamentListDto which is the result of the conversion
   */
  public TournamentListDto entityToListDto(Tournament tournament) {
    LOG.trace("entityToListDto({})", tournament);
    if (tournament == null) {
      return null;
    }

    return new TournamentListDto(
        tournament.getId(),
        tournament.getName(),
        tournament.getStartDate(),
        tournament.getEndDate()
    );
  }

  /**
   * Creates a TournamentDetailDto from a Tournament object and a Horse Array. Each TournamentDetailParticipantDto will have their
   * entryNumber and roundReached set to null.
   *
   * @param tournament the tournament used for the creation of the TournamentDetailDto
   * @param horses the horses used for the creation of the TournamentDetailParticipantDto which are part of the TournamentDetailDto
   * @return a TournamentDetailDto representing a freshly created tournament with no standings
   */
  public TournamentDetailDto entityToDetailDto(Tournament tournament, Horse[] horses) {
    LOG.trace("entityToListDto({})", tournament);
    if (tournament == null || horses == null) {
      return null;
    }
    TournamentDetailParticipantDto[] participants = new TournamentDetailParticipantDto[horses.length];
    for (int i = 0; i < horses.length; i++) {
      if (horses[i] == null) {
        return null;
      }
      participants[i] = new TournamentDetailParticipantDto(
        horses[i].getId(),
        horses[i].getName(),
        horses[i].getDateOfBirth(),
          null,
          null
      );
    }

    return new TournamentDetailDto(
        tournament.getId(),
        tournament.getName(),
        tournament.getStartDate(),
        tournament.getEndDate(),
        participants
    );
  }

  /**
   * Converts a TournamentUpdateDto to a TournamentDetailDto and thereby sets every unknown data to null.
   *
   * @param tournamentUpdateDto the TournamentUpdateDto to be converted
   * @return the converted TournamentDetailDto
   */
  public TournamentDetailDto updateDtoToDetailDto(TournamentUpdateDto tournamentUpdateDto) {
    LOG.trace("updateDtoToDetailDto({})", tournamentUpdateDto);
    if (tournamentUpdateDto == null || tournamentUpdateDto.participants() == null) {
      return null;
    }
    TournamentDetailParticipantDto[] participantDetails = new TournamentDetailParticipantDto[tournamentUpdateDto.participants().length];
    for (int i = 0; i < tournamentUpdateDto.participants().length; i++) {
      participantDetails[i] = new TournamentDetailParticipantDto(
          tournamentUpdateDto.participants()[i].horseId(),
          null,
          null,
          tournamentUpdateDto.participants()[i].entryNumber(),
          tournamentUpdateDto.participants()[i].roundReached()
      );
    }
    return new TournamentDetailDto(
        tournamentUpdateDto.id(),
        null,
        null,
        null,
        participantDetails
    );
  }


  /**
   * Converts a TournamentDetailDto to a tree structure.
   *
   * @param tournamentDetails the horses with their tournament details which should be converted to a tree
   * @return the resulting tree structure
   */
  public TournamentStandingsTreeDto tournamentDetailsDtoToTournamentStandingTree(TournamentDetailDto tournamentDetails) {
    LOG.trace("tournamentDetailsDtoToTournamentStandingTree({})", tournamentDetails);
    LOG.debug("function tournamentDetailsDtoToTournamentStandingTree called with argument tournamentDetails: "
        + Arrays.toString(tournamentDetails.participants()));
    ArrayList<TournamentDetailParticipantDto> orderedParticipants = new ArrayList<>();
    long wantedEntryNumber = 0;
    for (int i = 0; i <= 7; i++) {
      boolean foundMatch = false;
      for (int j = 0; j < tournamentDetails.participants().length; j++) {
        if (tournamentDetails.participants()[j].entryNumber() != null && tournamentDetails.participants()[j].entryNumber() == wantedEntryNumber) {
          orderedParticipants.add(tournamentDetails.participants()[j]);
          foundMatch = true;
          break;
        }
      }
      if (!foundMatch) {
        orderedParticipants.add(null); // for every entry number which doesn't have a horse yet, null will be used instead
      }
      wantedEntryNumber = wantedEntryNumber + 1;
    }
    LOG.debug("The ordered participants are as follows: " + orderedParticipants);
    TournamentStandingsTreeDto root;
    if (orderedParticipants.isEmpty()) {
      root = this.emptyTreeCreator(4);
      LOG.debug("created empty tree:");
      LOG.debug(root.toStringSmaller());
    } else {
      root = this.tournamentTreeGeneratior(4, 0, 7,
          orderedParticipants.toArray(new TournamentDetailParticipantDto[orderedParticipants.size()]));
      LOG.debug("created the following tree: ");
      LOG.debug(root.toStringSmaller());
    }
    return root;
  }

  /**
   * A small helper method to quickly build and empty tree structure
   *
   * @param curRound the round which the current root of the tree is
   * @return the fully built empty tree structure for the current root element
   */
  private TournamentStandingsTreeDto emptyTreeCreator(long curRound) {
    if (curRound <= 1) {
      return new TournamentStandingsTreeDto(
          null,
          null
      );
    }
    return new TournamentStandingsTreeDto(
        null,
        new TournamentStandingsTreeDto[] {
            emptyTreeCreator(curRound - 1),
            emptyTreeCreator(curRound - 1)
        }
    );
  }

  /**
   * A small helper method to build the tree structure
   *
   * @param curRound the round which the current root of the tree is
   * @param participants all participants of the current segment
   * @private the fully built tree structure for the current root element
   */
  private TournamentStandingsTreeDto tournamentTreeGeneratior(long curRound, long lowerBound, long upperBound, TournamentDetailParticipantDto[] participants) {
    if (curRound <= 1) {
      // if we are in a leaf we just return a leaf node with no further nodes
      if (participants.length == 0) {
        return new TournamentStandingsTreeDto(null, null);
      } else {
        return new TournamentStandingsTreeDto(participants[0], null);
      }
    }
    ArrayList<TournamentDetailParticipantDto> upperParticipants = new ArrayList<>();
    ArrayList<TournamentDetailParticipantDto> lowerParticipants = new ArrayList<>();
    TournamentDetailParticipantDto winner = null;
    long splitPoint = (upperBound - lowerBound) / 2 + lowerBound;
    for (int i = 0; i < participants.length; i++) {
      if (participants[i] != null) {
        if (participants[i].roundReached() >= curRound) {
          winner = participants[i];
        }
        if (participants[i].entryNumber() >= 0 && participants[i].entryNumber() <= splitPoint) {
          upperParticipants.add(participants[i]);
        } else if (participants[i].entryNumber() > splitPoint && participants[i].entryNumber() <= upperBound) {
          lowerParticipants.add(participants[i]);
        }
      }
    }
    return new TournamentStandingsTreeDto(
        winner,
        new TournamentStandingsTreeDto[]{
            this.tournamentTreeGeneratior(curRound - 1, lowerBound, splitPoint,
                upperParticipants.toArray(new TournamentDetailParticipantDto[upperParticipants.size()])),
            this.tournamentTreeGeneratior(curRound - 1, splitPoint + 1, upperBound,
                lowerParticipants.toArray(new TournamentDetailParticipantDto[lowerParticipants.size()]))
        }
    );
  }
}