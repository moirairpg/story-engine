package me.moirai.storyengine.core.port.inbound.world;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.HashSet;
import java.util.Set;

import me.moirai.storyengine.common.usecases.UseCase;
import reactor.core.publisher.Mono;

public final class UpdateWorld extends UseCase<Mono<UpdateWorldResult>> {

    private final String id;
    private final String name;
    private final String description;
    private final String adventureStart;
    private final String visibility;
    private final String requesterId;
    private final Set<String> usersAllowedToWriteToAdd;
    private final Set<String> usersAllowedToWriteToRemove;
    private final Set<String> usersAllowedToReadToAdd;
    private final Set<String> usersAllowedToReadToRemove;

    private UpdateWorld(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
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

    public String getDescription() {
        return description;
    }

    public String getAdventureStart() {
        return adventureStart;
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
        private String description;
        private String adventureStart;
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

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder adventureStart(String adventureStart) {
            this.adventureStart = adventureStart;
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

            this.usersAllowedToWriteToAdd = usersAllowedToWriteToAdd;
            return this;
        }

        public Builder usersAllowedToWriteToRemove(Set<String> usersAllowedToWriteToRemove) {

            this.usersAllowedToWriteToRemove = usersAllowedToWriteToRemove;
            return this;
        }

        public Builder usersAllowedToReadToAdd(Set<String> usersAllowedToReadToAdd) {

            this.usersAllowedToReadToAdd = usersAllowedToReadToAdd;
            return this;
        }

        public Builder usersAllowedToReadToRemove(Set<String> usersAllowedToReadToRemove) {

            this.usersAllowedToReadToRemove = usersAllowedToReadToRemove;
            return this;
        }

        public UpdateWorld build() {
            return new UpdateWorld(this);
        }
    }
}