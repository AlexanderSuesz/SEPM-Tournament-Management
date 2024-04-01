package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * TournamentMapper is responsible for converting Tournament entities to DTOs (Data Transfer Objects).
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
}
