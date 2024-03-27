package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest extends TestBase {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  @Test
  public void deleteHorseWithExistingIdReturns200() throws Exception {
    int existingID = -1; // All horses in the test data have negative id.
    var body = mockMvc
        .perform(MockMvcRequestBuilders
            .delete("/horses/" + existingID))
        .andExpect(status().isOk()) // checks for 200 response code
        .andReturn().getResponse().getContentAsByteArray();
    assertThat(body).hasSize(0); // should not have response body
  }

  @Test
  public void deleteHorseWithNotExistingIdReturns404() throws Exception {
    int notExistingID = 1; // All horses in the test data have negative id.
    var body = mockMvc
        .perform(MockMvcRequestBuilders
            .delete("/horses/" + notExistingID))
        .andExpect(status().isNotFound()) // checks for 404 response code
        .andReturn().getResponse().getContentAsByteArray();
    assertThat(body).hasSize(0); // should not have response body
  }

  @Test
  public void postHorseWithValidDataWithoutBreedReturnsHorseWithoutBreed() throws Exception {
    // Adds a new horse.
    String requestBody = objectMapper.writeValueAsString(Map.of(
        "name", "Umbrella",
        "sex", "FEMALE",
        "dateOfBirth", LocalDate.of(2017, 3, 5).toString(),
        "height", 1,
        "weight", 1
    ));
    // Perform the POST request with the JSON body
    var body = mockMvc
        .perform(MockMvcRequestBuilders
            .post("/horses")
            .contentType(MediaType.APPLICATION_JSON)  // Set content type to JSON
            .content(requestBody)  // Set the request body
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())  // Ensure the response status is Created (201)
        .andReturn().getResponse().getContentAsByteArray();

    // Process the response body
    var horsesResult = objectMapper.readerFor(HorseDetailDto.class)
        .<HorseDetailDto>readValues(body);

    // Assert that the result is not null and process it further
    assertNotNull(horsesResult);
    var horses = new ArrayList<HorseDetailDto>();
    horsesResult.forEachRemaining(horses::add);

    assertThat(horses).isNotEmpty();
    assertThat(horses)
        .hasSize(1)
        .extracting(
            HorseDetailDto::id,
            HorseDetailDto::name,
            HorseDetailDto::sex,
            HorseDetailDto::dateOfBirth,
            (h) -> h.breed() != null ? h.breed().name() : null, // Handle null breed case
            HorseDetailDto::height,
            HorseDetailDto::weight
        )
        .satisfies(tuple -> {
          assertThat(tuple).isNotNull();
        })
            .satisfies(tuple -> {
              Object[] horseElementsArray = tuple.get(0).toArray();
              assertNotNull(horseElementsArray[0]);
              assertThat(horseElementsArray[1]).isEqualTo("Umbrella");
              assertThat(horseElementsArray[2]).isEqualTo(Sex.FEMALE);
              assertThat(horseElementsArray[3]).isEqualTo(LocalDate.of(2017, 3, 5));
              assertNull(horseElementsArray[4]);
              assertThat(horseElementsArray[5]).isEqualTo((float) 1);
              assertThat(horseElementsArray[6]).isEqualTo((float) 1);
            });
  }

  @Test
  public void putHorseWithNonExistingIdReturns404() throws Exception {
    int notExistingId = 1; // All horses in the test data have negative id.
    String requestBody = objectMapper.writeValueAsString(Map.of(
        "id", notExistingId,
        "name", "Umbrella",
        "sex", "FEMALE",
        "dateOfBirth", LocalDate.of(2017, 3, 5).toString(),
        "height", 1,
        "weight", 1
    ));
    // Perform the POST request with the JSON body
    mockMvc
        .perform(MockMvcRequestBuilders
            .put("/horses/" + notExistingId)
            .contentType(MediaType.APPLICATION_JSON)  // Set content type to JSON
            .content(requestBody)  // Set the request body
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());  // Ensure the response status is Created (404)
  }

  @Test
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/asdf123")
        ).andExpect(status().isNotFound());
  }

  @Test
  public void gettingAllHorses() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class)
        .<HorseListDto>readValues(body).readAll();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult)
        .hasSize(32)
        .extracting(HorseListDto::id, HorseListDto::name, HorseListDto::sex, HorseListDto::dateOfBirth)
        .contains(
            tuple(-1L, "Wendy", Sex.FEMALE, LocalDate.of(2019, 8, 5)),
            tuple(-32L, "Luna", Sex.FEMALE, LocalDate.of(2018, 10, 10)),
            tuple(-21L, "Bella", Sex.FEMALE, LocalDate.of(2003, 7, 6)),
            tuple(-2L, "Hugo", Sex.MALE, LocalDate.of(2020, 2, 20)));
  }

  @Test
  public void searchByBreedWelFindsThreeHorses() throws Exception {
    var body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .queryParam("breed", "Wel")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    var horsesIterator = objectMapper.readerFor(HorseListDto.class)
        .<HorseListDto>readValues(body);
    assertNotNull(horsesIterator);
    var horses = new ArrayList<HorseListDto>();
    horsesIterator.forEachRemaining(horses::add);
    // We don't have height and weight of the horses here, so no reason to test for them.
    assertThat(horses)
        .extracting("id", "name", "sex", "dateOfBirth", "breed.name")
        .as("ID, Name, Sex, Date of Birth, Breed Name")
        .containsExactlyInAnyOrder(
            tuple(-32L, "Luna", Sex.FEMALE, LocalDate.of(2018, 10, 10), "Welsh Cob"),
            tuple(-21L, "Bella", Sex.FEMALE, LocalDate.of(2003, 7, 6), "Welsh Cob"),
            tuple(-2L, "Hugo", Sex.MALE, LocalDate.of(2020, 2, 20), "Welsh Pony")
        );
  }

  @Test
  public void searchByBirthDateBetween2017And2018ReturnsFourHorses() throws Exception {
    var body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .queryParam("bornEarliest", LocalDate.of(2017, 3, 5).toString())
            .queryParam("bornLatest", LocalDate.of(2018, 10, 10).toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    var horsesResult = objectMapper.readerFor(HorseListDto.class)
        .<HorseListDto>readValues(body);
    assertNotNull(horsesResult);

    var horses = new ArrayList<HorseListDto>();
    horsesResult.forEachRemaining(horses::add);

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
