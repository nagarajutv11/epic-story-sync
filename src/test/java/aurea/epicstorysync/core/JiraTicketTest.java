package aurea.epicstorysync.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.List;
import org.junit.Test;

public class JiraTicketTest {

    private static final String TYPE_EPIC = "Faster Epic";
    private static final String TYPE_STORY = "Faster Story";
    private static final String TYPE_BASELINE = "Faster Baseline";
    private static final String TICKET_ID = "123454";
    private static final String TICKET_KEY = "BOOTCAMP-22";
    private static final String STATUS_BACKLOG = "Backlog";

    @Test
    public void givenLinkedTicketsWhenGetLinkedTicketsByTypeThenCheckResult() {
        // Arrange
        JiraTicket ticket = new JiraTicket(TICKET_ID, TICKET_KEY, STATUS_BACKLOG, TYPE_EPIC);
        JiraTicket story = new JiraTicket(TICKET_ID, TICKET_KEY, STATUS_BACKLOG, TYPE_STORY);
        ticket.addLinkedTicket(story);
        ticket.addLinkedTicket(new JiraTicket(TICKET_ID, TICKET_KEY, STATUS_BACKLOG, TYPE_BASELINE));
        ticket.addLinkedTicket(new JiraTicket(TICKET_ID, TICKET_KEY, STATUS_BACKLOG, TYPE_BASELINE));

        // Act
        List<JiraTicket> stories = ticket.getLinkedTicketsByType(TYPE_STORY);

        // Assert
        assertThat(stories.get(0), is(story));
    }
}
