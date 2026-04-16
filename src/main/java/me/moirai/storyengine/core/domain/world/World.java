package me.moirai.storyengine.core.domain.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.RandomUuid;
import me.moirai.storyengine.common.domain.Narrator;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.domain.ShareableAsset;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "world")
public class World extends ShareableAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @RandomUuid
    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "adventure_start")
    private String adventureStart;

    @Column(name = "image_key")
    private String imageKey;

    @Embedded
    private Narrator narrator;

    @ElementCollection
    @CollectionTable(name = "world_permissions", joinColumns = @JoinColumn(name = "world_id"))
    private List<Permission> permissions = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "world_id")
    private List<WorldLorebookEntry> lorebook = new ArrayList<>();

    @Override
    protected List<Permission> permissions() {
        return permissions;
    }

    private World(Builder builder) {

        super(builder.visibility);

        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
        this.narrator = new Narrator(builder.narratorName, builder.narratorPersonality);
        this.permissions.addAll(builder.permissions);
    }

    protected World() {
        super();
    }

    public static Builder builder() {

        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
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

    public Narrator getNarrator() {
        return narrator;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public String getNarratorName() {
        return Optional.ofNullable(narrator).map(Narrator::narratorName).orElse("Narrator");
    }

    public String getNarratorPersonality() {
        return Optional.ofNullable(narrator).map(Narrator::narratorPersonality).orElse(null);
    }

    public List<WorldLorebookEntry> getLorebook() {
        return Collections.unmodifiableList(lorebook);
    }

    public void updateName(String name) {

        this.name = name;
    }

    public void updateDescription(String description) {

        this.description = description;
    }

    public void updateAdventureStart(String adventureStart) {

        this.adventureStart = adventureStart;
    }

    public void updateNarrator(String narratorName, String narratorPersonality) {

        this.narrator = new Narrator(narratorName, narratorPersonality);
    }

    public WorldLorebookEntry addLorebookEntry(
            String name,
            String description) {

        WorldLorebookEntry entry = WorldLorebookEntry.builder()
                .name(name)
                .description(description)
                .build();

        lorebook.add(entry);
        return entry;
    }

    public WorldLorebookEntry updateLorebookEntry(
            UUID entryId,
            String name,
            String description) {

        WorldLorebookEntry entry = getLorebookEntryById(entryId);
        entry.updateName(name);
        entry.updateDescription(description);

        return entry;
    }

    public void removeLorebookEntry(UUID entryId) {

        WorldLorebookEntry entry = getLorebookEntryById(entryId);
        lorebook.remove(entry);
    }

    public WorldLorebookEntry getLorebookEntryById(UUID entryId) {

        return lorebook.stream()
                .filter(e -> e.getPublicId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Lorebook entry not found"));
    }

    public static final class Builder {

        private String name;
        private String description;
        private String adventureStart;
        private String narratorName;
        private String narratorPersonality;
        private Visibility visibility;
        private Set<Permission> permissions = new HashSet<>();

        private Builder() {
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

        public Builder narrator(String narratorName, String narratorPersonality) {

            this.narratorName = narratorName;
            this.narratorPersonality = narratorPersonality;
            return this;
        }

        public Builder visibility(Visibility visibility) {

            this.visibility = visibility;
            return this;
        }

        public Builder permissions(Permission... permissions) {

            this.permissions.addAll(Set.of(permissions));
            return this;
        }

        public World build() {

            if (StringUtils.isBlank(name)) {
                throw new BusinessRuleViolationException("Persona name cannot be null or empty");
            }

            if (visibility == null) {
                throw new BusinessRuleViolationException("Visibility cannot be null");
            }

            return new World(this);
        }
    }
}
