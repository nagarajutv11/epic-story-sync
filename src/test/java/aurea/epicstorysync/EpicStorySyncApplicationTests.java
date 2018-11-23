package aurea.epicstorysync;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import aurea.epicstorysync.core.Configuration;
import aurea.epicstorysync.core.JiraStatus;
import aurea.epicstorysync.core.JiraTicket;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    EpicStorySyncApplication.class,
    EpicStorySyncApplicationTests.TestConfig.class
})
@ActiveProfiles("test")
public class EpicStorySyncApplicationTests {

    private static final String TICKET_KEY1 = "BOOTCAMP-22";
    private static final String TICKET_KEY2 = "BOOTCAMP-23";
    private static final String TICKET_ID = "123454";
    private static final String TYPE_STORY = "Faster Story";
    private static final String TYPE_EPIC = "Faster Epic";
    private static final String STATUS_TO_DEFINE = "To Define";
    private static final String STATUS_BACKLOG = "Backlog";
    private static final String ACTION_BACKLOG = "Backlog";
    private static final String STATUS_NEW = "New";
    private static final int GET_REQUESTS_COUNT = 8;
    private static final int POST_REQUESTS_COUNT = 7;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Configuration configuration;

    @SuppressWarnings("unchecked")
    @Test
    public void contextLoads() {

        // Assert
        verify(restTemplate, times(GET_REQUESTS_COUNT)).getForEntity(anyString(), any());
        verify(restTemplate, times(POST_REQUESTS_COUNT)).postForEntity(anyString(), any(HttpEntity.class),
                any(Class.class));
    }

    @Test
    public void givenTicketsWhenFindHigherPriorityEpicStatusThenCheckResult() throws Exception {
        // Arrange
        JiraTicket ticket1 = new JiraTicket(TICKET_ID, TICKET_KEY1, STATUS_BACKLOG, TYPE_STORY);
        JiraTicket ticket2 = new JiraTicket(TICKET_ID, TICKET_KEY2, STATUS_TO_DEFINE, TYPE_STORY);
        List<JiraTicket> tickets = Arrays.asList(ticket1, ticket2);

        // Act
        JiraStatus status = configuration.findHigherPriorityEpicStatus(tickets);

        // Assert
        assertThat(status.getStoryStatus(), is(STATUS_TO_DEFINE));
    }

    @Test
    public void givenTicketWhenGetActionThenCheckResult() throws Exception {
        // Arrange
        JiraTicket ticket = new JiraTicket("", TICKET_KEY1, STATUS_NEW, TYPE_EPIC);

        // Act
        String[] result = configuration.getAction(ticket, STATUS_BACKLOG);

        // Assert
        assertThat(result[0], is(ACTION_BACKLOG));
    }

    @TestConfiguration
    public static class TestConfig {

        @SuppressWarnings("unchecked")
        @Bean
        @Profile("test")
        public RestTemplate restTemplate() throws IOException {
            RestTemplate restTemplate = mock(RestTemplate.class);
            ResponseEntity<String> allTickets = new ResponseEntity<>(loadData("all_tickets.json"), HttpStatus.OK);
            ResponseEntity<String> transactions = new ResponseEntity<>(loadData("transactions.json"), HttpStatus.OK);
            when(restTemplate.getForEntity(anyString(), any(Class.class))).thenReturn(allTickets, transactions);
            return restTemplate;
        }

        private String loadData(String file) throws IOException {
            return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(file));
        }
    }
}
