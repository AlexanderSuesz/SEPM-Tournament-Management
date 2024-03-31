package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class TournamentEndpointTest extends TestBase {

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
  public void gettingAllTournaments() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/tournaments")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<TournamentListDto> tournamentResult = objectMapper.readerFor(TournamentListDto.class)
        .<TournamentListDto>readValues(body).readAll();

    assertThat(tournamentResult).isNotNull();
    assertThat(tournamentResult)
        .hasSize(10)
        .extracting(TournamentListDto::id, TournamentListDto::name, TournamentListDto::startDate, TournamentListDto::endDate)
        .contains(
            tuple(-1L, "Noobz", LocalDate.of(2007, 8, 5), LocalDate.of(2007, 8, 10)),
            tuple(-4L, "Heavens Match", LocalDate.of(2010, 8, 5), LocalDate.of(2010, 8, 10)),
            tuple(-10L, "Road to 42", LocalDate.of(2016, 8, 5), LocalDate.of(2016, 8, 10)),
            tuple(-6L, "Trivial Matters", LocalDate.of(2012, 8, 5), LocalDate.of(2012, 8, 10)));
  }

  @Test
  public void searchByNameBigFindsTwoTournaments() throws Exception {
    var body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/tournaments")
            .queryParam("name", "Big")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    var tournamentIterator = objectMapper.readerFor(TournamentListDto.class)
        .<TournamentListDto>readValues(body);
    assertNotNull(tournamentIterator);
    var tournaments = new ArrayList<TournamentListDto>();
    tournamentIterator.forEachRemaining(tournaments::add);
    assertThat(tournaments)
        .extracting("id", "name", "startDate", "endDate")
        // .as("ID, Name, Start Date, End Date")
        .containsExactlyInAnyOrder(
            tuple(-3L, "The Big Ones", LocalDate.of(2009, 8, 5), LocalDate.of(2009, 8, 10)),
            tuple(-9L, "The big 8", LocalDate.of(2015, 8, 5), LocalDate.of(2015, 8, 10))
        );
  }

  @Test
  public void searchByDateDateBetween2012And2015ReturnsFourTournaments() throws Exception {
    var body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/tournaments")
            .queryParam("earliestTournamentDay", LocalDate.of(2012, 8, 5).toString())
            .queryParam("latestTournamentDay", LocalDate.of(2015, 8, 10).toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    var tournamentResult = objectMapper.readerFor(TournamentListDto.class)
        .<TournamentListDto>readValues(body);
    assertNotNull(tournamentResult);

    var tournaments = new ArrayList<TournamentListDto>();
    tournamentResult.forEachRemaining(tournaments::add);

    assertThat(tournaments)
        .hasSize(4)
        .extracting("id", "name", "startDate", "endDate")
        .containsExactlyInAnyOrder(
            tuple(-6L, "Trivial Matters", LocalDate.of(2012, 8, 5), LocalDate.of(2012, 8, 10)),
            tuple(-7L, "Horses Go Brrrr", LocalDate.of(2013, 8, 5), LocalDate.of(2013, 8, 10)),
            tuple(-8L, "Hungry for glory", LocalDate.of(2014, 8, 5), LocalDate.of(2014, 8, 10)),
            tuple(-9L, "The big 8", LocalDate.of(2015, 8, 5), LocalDate.of(2015, 8, 10))
        );
  }

  @Test
  public void searchByDateDateBetween2015And2012ShouldThrow() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/tournaments")
            .queryParam("earliestTournamentDay", LocalDate.of(2015, 8, 5).toString())
            .queryParam("latestTournamentDay", LocalDate.of(2012, 8, 10).toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity());
  }
}
