package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest extends TestBase {

  @Autowired
  HorseService horseService;

  @Test
  public void searchByBreedWelFindsThreeHorses() {
    var searchDto = new HorseSearchDto(null, null, null, null, "Wel", null);
    var horses = horseService.search(searchDto);
    assertNotNull(horses);
    // We don't have height and weight of the horses here, so no reason to test for them.
    assertThat(horses)
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
    var horses = horseService.search(searchDto);
    assertNotNull(horses);
    assertThat(horses)
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
