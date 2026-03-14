package me.moirai.storyengine.core.port.inbound.persona;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.HashSet;
import java.util.Set;

import me.moirai.storyengine.common.usecases.UseCase;
import reactor.core.publisher.Mono;

public final class UpdatePersona extends UseCase<Mono<PersonaDetails>> {

    private final String id;
    private final String name;
    private final String personality;
    private final String visibility;
    private final String requesterId;
    private final Set<String> usersAllowedToWriteToAdd;
    private final Set<String> usersAllowedToWriteToRemove;
    private final Set<String> usersAllowedToReadToAdd;
    private final Set<String> usersAllowedToReadToRemove;

    private UpdatePersona(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.personality = builder.personality;
        this.visibility = builder.visibility;
        this.requesterId = builder.requesterId;

        this.usersAllowedToWriteToAdd = isEmpty(builder.usersAllowedToWriteToAdd) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToWriteToAdd);

        this.usersAllowedToWriteToRemove = isEmpty(builder.usersAllowedToWriteToRemove) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToWriteToRemove);

        this.usersAllowedToReadToAdd = isEmpty(builder.usersAllowedToReadToAdd) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToReadToAdd);

        this.usersAllowedToReadToRemove = isEmpty(builder.usersAllowedToReadToRemove) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToReadToRemove);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
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

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public Set<String> getUsersAllowedToWriteToAdd() {
        return usersAllowedToWriteToAdd;
    }

    public Set<String> getUsersAllowedToWriteToRemove() {
        return usersAllowedToWriteToRemove;
    }

    public Set<String> getUsersAllowedToReadToAdd() {
        return usersAllowedToReadToAdd;
    }

    public Set<String> getUsersAllowedToReadToRemove() {
        return usersAllowedToReadToRemove;
    }

    public static final class Builder {
        private String id;
        private String name;
        private String personality;
        private String visibility;
        private String requesterId;
        private Set<String> usersAllowedToWriteToAdd = new HashSet<>();
        private Set<String> usersAllowedToWriteToRemove = new HashSet<>();
        private Set<String> usersAllowedToReadToAdd = new HashSet<>();
        private Set<String> usersAllowedToReadToRemove = new HashSet<>();

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
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

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public Builder usersAllowedToWriteToAdd(Set<String> usersAllowedToWriteToAdd) {

            if (usersAllowedToWriteToAdd != null) {
                this.usersAllowedToWriteToAdd = usersAllowedToWriteToAdd;
            }

            return this;
        }

        public Builder usersAllowedToWriteToRemove(Set<String> usersAllowedToWriteToRemove) {

            if (usersAllowedToWriteToRemove != null) {
                this.usersAllowedToWriteToRemove = usersAllowedToWriteToRemove;
            }

            return this;
        }

        public Builder usersAllowedToReadToAdd(Set<String> usersAllowedToReadToAdd) {

            if (usersAllowedToReadToAdd != null) {
                this.usersAllowedToReadToAdd = usersAllowedToReadToAdd;
            }

            return this;
        }

        public Builder usersAllowedToReadToRemove(Set<String> usersAllowedToReadToRemove) {

            if (usersAllowedToReadToRemove != null) {
                this.usersAllowedToReadToRemove = usersAllowedToReadToRemove;
            }

            return this;
        }

        public UpdatePersona build() {
            return new UpdatePersona(this);
        }
    }
}