package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest extends TestBase {

  @Autowired
  HorseService horseService;

  @Test
  void deleteWhenGivenNotExistingIdShouldThrow() {
    int notExistingId = 1; // All horses in the test data have negative id.
    assertThrows(AssertionFailedError.class, () -> {
      try {
        horseService.deleteById(notExistingId);
      } catch (NotFoundException e) {
        fail("NotFoundException occurred: " + e.getMessage());
      }
    });
  }

  @Test
  void deleteWhenGivenExistingIdReturnVoid() {
    int existingId = -1; // All horses in the test data have negative id.
    assertDoesNotThrow(() -> {
      try {
        horseService.deleteById(existingId);
      } catch (NotFoundException e) {
        fail("NotFoundException occurred: " + e.getMessage());
      }
    });
  }


  @Test
  void addWhenGivenInvalidHeightShouldThrow() {
    // Adds a horse with invalid height
    HorseDetailDto horse = new HorseDetailDto(null, "A Horse", Sex.FEMALE, LocalDate.of(2018, 8, 19), -1, 2, null);
    assertThrows(AssertionFailedError.class, () -> {
      try {
        horseService.add(horse);
      } catch (ValidationException e) {
        fail("ValidationException occurred: " + e.getMessage());
      }
    });
  }

  @Test
  void addWhenGivenValidDataWithBreedReturnsHorse() {
    HorseDetailDto horse = new HorseDetailDto(null, "A Horse", Sex.FEMALE, LocalDate.of(2018, 8, 19), 2, 2, new BreedDto(-1, "Andalusian"));
    HorseDetailDto[] addedHorse = new HorseDetailDto[1]; // We work with arrays since lambda statements only allow final variables
    assertDoesNotThrow(() -> {
      try {
        addedHorse[0] = horseService.add(horse);
      } catch (ValidationException e) {
        fail("ValidationException occurred: " + e.getMessage());
      }
    });
    assertNotNull(addedHorse[0]);
    assertAll(
        () -> assertNotNull(addedHorse[0].id()),
        () -> assertEquals(addedHorse[0].sex(), horse.sex()),
        () -> assertEquals(addedHorse[0].breed(), horse.breed()),
        () -> assertEquals(addedHorse[0].name(), horse.name()),
        () -> assertEquals(addedHorse[0].dateOfBirth(), horse.dateOfBirth()),
        () -> assertEquals(addedHorse[0].height(), horse.height()),
        () -> assertEquals(addedHorse[0].weight(), horse.weight())
    );
  }

  @Test
  void addWhenGivenValidDataWithoutBreedReturnsHorse() {
    HorseDetailDto horse = new HorseDetailDto(null, "A Horse", Sex.FEMALE, LocalDate.of(2018, 8, 19), 2, 2, null);
    HorseDetailDto[] addedHorse = new HorseDetailDto[1]; // We work with arrays since lambda statements only allow final variables
    assertDoesNotThrow(() -> {
      try {
        addedHorse[0] = horseService.add(horse);
      } catch (ValidationException e) {
        fail("ValidationException occurred: " + e.getMessage());
      }
    });
    assertNotNull(addedHorse[0]);
    assertAll(
        () -> assertNotNull(addedHorse[0].id()),
        () -> assertEquals(addedHorse[0].sex(), horse.sex()),
        () -> assertEquals(addedHorse[0].breed(), horse.breed()),
        () -> assertEquals(addedHorse[0].name(), horse.name()),
        () -> assertEquals(addedHorse[0].dateOfBirth(), horse.dateOfBirth()),
        () -> assertEquals(addedHorse[0].height(), horse.height()),
        () -> assertEquals(addedHorse[0].weight(), horse.weight())
    );
  }

  @Test
  public void getByIdByExistingIDReturnsHorse() {
    HorseDetailDto[] horse = new HorseDetailDto[1]; // We work with arrays since lambda statements only allow final variables
    assertDoesNotThrow(() -> {
      try {
        horse[0] = horseService.getById(-1);
      } catch (NotFoundException e) {
        fail("NotFoundError occured: " + e.getMessage());
      }
    });
    assertNotNull(horse);
    assertAll(
        () -> assertNotNull(horse[0].id()),
        () -> assertNotNull(horse[0].sex()),
        () -> assertNotNull(horse[0].name()),
        () -> assertNotNull(horse[0].dateOfBirth()),
        () -> assertThat(horse[0].height()).isGreaterThan(0),
        () -> assertThat(horse[0].weight()).isGreaterThan(0)
    );
  }

  @Test
  public void searchWhenGivenInvalidYearSpanFrom2018To2017ShouldThrow() {
    // Searches for horses born in an invalid time frame. (A valid search should start at earlier date and end with later date)
    var searchDto = new HorseSearchDto(null, null, LocalDate.of(2018, 8, 19), LocalDate.of(2017, 8, 19), "Wel", null);
    assertThrows(AssertionFailedError.class, () -> {
      try {
        horseService.search(searchDto);
      } catch (ValidationException e) {
        fail("ValidationException occurred: " + e.getMessage());
      }
    });
  }

  @Test
  public void searchByBreedWelFindsThreeHorses() {
    var searchDto = new HorseSearchDto(null, null, null, null, "Wel", null);
    Stream<HorseListDto>[] horseStream = new Stream[1]; // We work with arrays since lambda statements only allow final variables
    assertDoesNotThrow(() -> {
      try {
        horseStream[0] = horseService.search(searchDto);
      } catch (ValidationException e) {
        fail("ValidationException occured: " + e.getMessage());
      }
    });
    assertNotNull(horseStream[0]);
    // We don't have height and weight of the horses here, so no reason to test for them.
    assertThat(horseStream[0])
        .extracting("id", "name", "sex", "dateOfBirth", "breed.name")
        .as("ID, Name, Sex, Date of Birth, Breed Name")
        .containsOnly(
            tuple(-32L, "Luna", Sex.FEMALE, LocalDate.of(2018, 10, 10), "Welsh Cob"),
            tuple(-21L, "Bella", Sex.FEMALE, LocalDate.of(2003, 7, 6), "Welsh Cob"),
            tuple(-2L, "Hugo", Sex.MALE, LocalDate.of(2020, 2, 20), "Welsh Pony")
        );
  }

  @Test
  public void searchByBirthDateBetween2017And2018ReturnsFourHorses() {
    var searchDto = new HorseSearchDto(null, null,
        LocalDate.of(2017, 3, 5),
        LocalDate.of(2018, 10, 10),
        null, null);
    Stream<HorseListDto>[] horseStream = new Stream[1]; // We work with arrays since lambda statements only allow final variables
    assertDoesNotThrow(() -> {
      try {
        horseStream[0] = horseService.search(searchDto);
      } catch (ValidationException e) {
        fail("ValidationException occured: " + e.getMessage());
      }
    });
    assertNotNull(horseStream[0]);
    assertThat(horseStream[0])
        .hasSize(4)
        .extracting(HorseListDto::id, HorseListDto::name, HorseListDto::sex, HorseListDto::dateOfBirth, (h) -> h.breed().name())
        .containsExactlyInAnyOrder(
            tuple(-24L, "Rocky", Sex.MALE, LocalDate.of(2018, 8, 19),
                "Dartmoor Pony"),
            tuple(-26L, "Daisy", Sex.FEMALE, LocalDate.of(2017, 12, 1),
                "Hanoverian"),
            tuple(-31L, "Leo", Sex.MALE, LocalDate.of(2017, 3, 5),
                "Haflinger"),
            tuple(-32L, "Luna", Sex.FEMALE, LocalDate.of(2018, 10, 10),
                "Welsh Cob"));
  }
}
