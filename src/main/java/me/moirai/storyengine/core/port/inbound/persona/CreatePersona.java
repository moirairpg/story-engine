package me.moirai.storyengine.core.port.inbound.persona;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import me.moirai.storyengine.common.usecases.UseCase;
import reactor.core.publisher.Mono;

public final class CreatePersona extends UseCase<Mono<CreatePersonaResult>> {

    private final String name;
    private final String personality;
    private final String visibility;
    private final String requesterId;
    private final Set<String> usersAllowedToWrite;
    private final Set<String> usersAllowedToRead;

    public CreatePersona(Builder builder) {
        this.name = builder.name;
        this.personality = builder.personality;
        this.visibility = builder.visibility;
        this.requesterId = builder.requesterId;
        this.usersAllowedToWrite = unmodifiableSet(builder.usersAllowedToWrite);
        this.usersAllowedToRead = unmodifiableSet(builder.usersAllowedToRead);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getPersonality() {
        return personality;
    }

    public String getVisibility() {
        return visibility;
    }

    public Set<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
    }

    public Set<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String name;
        private String personality;
        private String visibility;
        private String requesterId;
        private Set<String> usersAllowedToWrite = new HashSet<>();
        private Set<String> usersAllowedToRead = new HashSet<>();

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder personality(String personality) {
            this.personality = personality;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder usersAllowedToWrite(Set<String> usersAllowedToWrite) {

            if (usersAllowedToWrite != null) {
                this.usersAllowedToWrite = usersAllowedToWrite;
            }

            return this;
        }

        public Builder usersAllowedToRead(Set<String> usersAllowedToRead) {

            if (usersAllowedToRead != null) {
                this.usersAllowedToRead = usersAllowedToRead;
            }

            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public CreatePersona build() {
            return new CreatePersona(this);
        }
    }
}
