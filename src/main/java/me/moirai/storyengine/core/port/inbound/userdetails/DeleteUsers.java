package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.util.Functions;

public record DeleteUsers(
        List<String> usernames,
        String requesterUsername) implements Command<DeleteUsersResult> {

    public DeleteUsers {
        usernames = Functions.mapOrDefault(usernames, List.of(), DeleteUsers::distinctIgnoringCase);
    }

    private static List<String> distinctIgnoringCase(List<String> usernames) {

        return List.copyOf(usernames.stream()
                .collect(Collectors.toMap(
                        username -> username.toLowerCase(Locale.ROOT),
                        username -> username,
                        (first, duplicate) -> first,
                        LinkedHashMap::new))
                .values());
    }
}
