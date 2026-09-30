package me.moirai.storyengine.core.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsers;

public class DeleteUsersTest {

    @Test
    public void shouldKeepTheFirstSpellingInRequestOrderWhenCaseVariantsOfOneHandleAreSubmitted() {

        // when
        var result = new DeleteUsers(List.of("Merlin", "bob", "MERLIN", "Bob", "merlin"), "requesting.admin");

        // then
        assertThat(result.usernames()).containsExactly("Merlin", "bob");
    }

    @Test
    public void shouldHoldAnEmptyListWhenTheSelectionIsNull() {

        // when
        var result = new DeleteUsers(null, "requesting.admin");

        // then
        assertThat(result.usernames()).isEmpty();
    }
}
