package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles({"test", "datagen"})
// enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseDaoTest extends TestBase {

  @Autowired
  HorseDao horseDao;

  @Test
  public void deleteHorseWithExistingIDReturnsVoid() {
    int existingID = -1; // All horses in the test data have negative id.
    assertDoesNotThrow(() -> {
      try {
        horseDao.deleteById(existingID);
      } catch (NotFoundException e) {
        fail("NotFoundException occurred: " + e.getMessage());
      }
    });
  }

  @Test
  public void deleteHorseWithNotExistingIDShouldThrow() {
    int notExistingId = 1; // All horses in the test data have negative id.
    assertThrows(AssertionFailedError.class, () -> {
      try {
        horseDao.deleteById(notExistingId);
      } catch (NotFoundException e) {
        fail("NotFoundException occurred: " + e.getMessage());
      }
    });
  }

  @Test
  public void addHorseWithValidDataWithoutBreedReturnsHorseWithoutBreed() {
    HorseDetailDto horseToAdd = new HorseDetailDto(null, "Hugo", Sex.MALE, LocalDate.of(2020, 2, 20), 1, 1, null);
    Horse horse = horseDao.add(horseToAdd);
    assertNotNull(horse);
    assertAll(
        () -> assertNotNull(horse.getId()),
        () -> assertThat(horse.getName()).isEqualTo(horseToAdd.name()),
        () -> assertThat(horse.getSex()).isEqualTo(horseToAdd.sex()),
        () -> assertThat(horse.getDateOfBirth()).isEqualTo(horseToAdd.dateOfBirth()),
        () -> assertThat(horse.getHeight()).isEqualTo(horseToAdd.height()),
        () -> assertThat(horse.getWeight()).isEqualTo(horseToAdd.weight()),
        () -> assertNull(horse.getBreedId())
    );
  }

  @Test
  public void updateHorseWithExistingIdButWithNotExistingBreedShouldThrow() {
    HorseDetailDto horseToUpdate = new HorseDetailDto((long) -1, "Hugo", Sex.MALE, LocalDate.of(2020, 2, 20), 1, 1, new BreedDto(1, "NonExistingBreed"));
    Horse[] horse = new Horse[1];
    assertThrows(AssertionFailedError.class, () -> {
      try {
        horse[0] = horseDao.update(horseToUpdate);
      } catch (ConflictException e) {
        fail("ConflictException occurred: " + e.getMessage());
      }
    });
    assertNull(horse[0]);
  }

  @Test
  public void searchByBreedWelFindsThreeHorses() {
    var searchDto = new HorseSearchDto(null, null, null, null, "Wel", null);
    var horses = horseDao.search(searchDto);
    assertNotNull(horses);
    assertThat(horses)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            (new Horse())
                .setId(-32L)
                .setName("Luna")
                .setSex(Sex.FEMALE)
                .setDateOfBirth(LocalDate.of(2018, 10, 10))
                .setHeight(1.62f)
                .setWeight(670)
                .setBreedId(-19L),
            (new Horse())
                .setId(-21L)
                .setName("Bella")
                .setSex(Sex.FEMALE)
                .setDateOfBirth(LocalDate.of(2003, 7, 6))
                .setHeight(1.50f)
                .setWeight(580)
                .setBreedId(-19L),
            (new Horse())
                .setId(-2L)
                .setName("Hugo")
                .setSex(Sex.MALE)
                .setDateOfBirth(LocalDate.of(2020, 2, 20))
                .setHeight(1.20f)
                .setWeight(320)
                .setBreedId(-20L));
  }

  @Test
  public void searchByBirthDateBetween2017And2018ReturnsFourHorses() {
    var searchDto = new HorseSearchDto(null, null,
        LocalDate.of(2017, 3, 5),
        LocalDate.of(2018, 10, 10),
        null, null);
    var horses = horseDao.search(searchDto);
    assertNotNull(horses);
    assertThat(horses)
        .hasSize(4)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            (new Horse())
                .setId(-24L)
                .setName("Rocky")
                .setSex(Sex.MALE)
                .setDateOfBirth(LocalDate.of(2018, 8, 19))
                .setHeight(1.42f)
                .setWeight(480)
                .setBreedId(-6L),
            (new Horse())
                .setId(-26L)
                .setName("Daisy")
                .setSex(Sex.FEMALE)
                .setDateOfBirth(LocalDate.of(2017, 12, 1))
                .setHeight(1.28f)
                .setWeight(340)
                .setBreedId(-9L),
            (new Horse())
                .setId(-31L)
                .setName("Leo")
                .setSex(Sex.MALE)
                .setDateOfBirth(LocalDate.of(2017, 3, 5))
                .setHeight(1.70f)
                .setWeight(720)
                .setBreedId(-8L),
            (new Horse())
                .setId(-32L)
                .setName("Luna")
                .setSex(Sex.FEMALE)
                .setDateOfBirth(LocalDate.of(2018, 10, 10))
                .setHeight(1.62f)
                .setWeight(670)
                .setBreedId(-19L));
  }
}
