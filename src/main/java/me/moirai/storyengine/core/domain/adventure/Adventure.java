package me.moirai.storyengine.core.domain.adventure;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "adventure")
public class Adventure extends ShareableAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @RandomUuid
    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "name")
    private String name;

    @Column(name = "world_id")
    private UUID worldId;

    @Embedded
    private Narrator narrator;

    @Column(name = "description")
    private String description;

    @Column(name = "adventure_start")
    private String adventureStart;

    @Column(name = "is_multiplayer")
    private boolean isMultiplayer;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation")
    private Moderation moderation;

    @Embedded
    private ContextAttributes contextAttributes;

    @Embedded
    private ModelConfiguration modelConfiguration;

    @ElementCollection
    @CollectionTable(name = "adventure_permissions", joinColumns = @JoinColumn(name = "adventure_id"))
    private List<Permission> permissions = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "adventure_id")
    private List<AdventureLorebookEntry> lorebook = new ArrayList<>();

    @Override
    protected List<Permission> permissions() {
        return permissions;
    }

    private Adventure(Builder builder) {

        super(builder.visibility);

        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
        this.worldId = builder.worldId;
        this.narrator = new Narrator(builder.narratorName, builder.narratorPersonality);
        this.contextAttributes = builder.contextAttributes;
        this.modelConfiguration = builder.modelConfiguration;
        this.moderation = builder.moderation;
        this.isMultiplayer = builder.isMultiplayer;
        this.permissions.addAll(builder.permissions);
    }

    protected Adventure() {
        super();
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

    public UUID getWorldId() {
        return worldId;
    }

    public Narrator getNarrator() {
        return narrator;
    }

    public String getNarratorName() {
        return Optional.ofNullable(narrator).map(Narrator::narratorName).orElse("Narrator");
    }

    public String getNarratorPersonality() {
        return Optional.ofNullable(narrator).map(Narrator::narratorPersonality).orElse(null);
    }

    public ModelConfiguration getModelConfiguration() {
        return modelConfiguration;
    }

    public Moderation getModeration() {
        return moderation;
    }

    public ContextAttributes getContextAttributes() {
        return contextAttributes;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public List<AdventureLorebookEntry> getLorebook() {
        return Collections.unmodifiableList(lorebook);
    }

    public static Builder builder() {

        return new Builder();
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

    public void updateModeration(Moderation moderation) {

        this.moderation = moderation;
    }

    public void updateAiModel(ArtificialIntelligenceModel aiModel) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updateAiModel(aiModel);
        this.modelConfiguration = newModelConfiguration;
    }

    public void updateMaxTokenLimit(int maxTokenLimit) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updateMaxTokenLimit(maxTokenLimit);
        this.modelConfiguration = newModelConfiguration;
    }

    public void updateTemperature(double temperature) {

        ModelConfiguration newModelConfiguration = this.modelConfiguration.updateTemperature(temperature);
        this.modelConfiguration = newModelConfiguration;
    }

    public void makeMultiplayer() {
        this.isMultiplayer = true;
    }

    public void makeSinglePlayer() {
        this.isMultiplayer = false;
    }

    public void updateNudge(String nudge) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateNudge(nudge);
        this.contextAttributes = newContextAttributes;
    }

    public void updateBump(String bump) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateBump(bump);
        this.contextAttributes = newContextAttributes;
    }

    public void updateBumpFrequency(Integer bumpFrequency) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateBumpFrequency(bumpFrequency);
        this.contextAttributes = newContextAttributes;
    }

    public void updateAuthorsNote(String authorsNote) {

        ContextAttributes newContextAttributes = this.contextAttributes.updateAuthorsNote(authorsNote);
        this.contextAttributes = newContextAttributes;
    }

    public void updateScene(String scene) {

        var newContextAttributes = this.contextAttributes.updateScene(scene);
        this.contextAttributes = newContextAttributes;
    }

    public AdventureLorebookEntry addLorebookEntry(String name, String description, String playerId) {

        var entry = AdventureLorebookEntry.builder()
                .name(name)
                .description(description)
                .playerId(playerId)
                .build();

        lorebook.add(entry);
        return entry;
    }

    public AdventureLorebookEntry updateLorebookEntry(
            UUID entryId,
            String name,
            String description,
            String playerId) {

        AdventureLorebookEntry entry = getLorebookEntryById(entryId);

        entry.updateName(name);
        entry.updateDescription(description);

        if (isBlank(playerId)) {
            entry.unassignPlayer();
        } else {
            entry.assignPlayer(playerId);
        }

        return entry;
    }

    public void removeLorebookEntry(UUID entryId) {

        AdventureLorebookEntry entry = getLorebookEntryById(entryId);
        lorebook.remove(entry);
    }

    public AdventureLorebookEntry getLorebookEntryById(UUID entryId) {

        return lorebook.stream()
                .filter(e -> entryId.equals(e.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Lorebook entry not found"));
    }

    public Optional<AdventureLorebookEntry> getLorebookEntryByPlayerId(String playerId) {

        return lorebook.stream()
                .filter(e -> playerId.equals(e.getPlayerId()))
                .findFirst();
    }

    public static final class Builder {

        private String name;
        private String description;
        private String adventureStart;
        private UUID worldId;
        private String narratorName;
        private String narratorPersonality;
        private boolean isMultiplayer;
        private ContextAttributes contextAttributes;
        private ModelConfiguration modelConfiguration;
        private Moderation moderation;
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

        public Builder worldId(UUID worldId) {

            this.worldId = worldId;
            return this;
        }

        public Builder narrator(String narratorName, String narratorPersonality) {

            this.narratorName = narratorName;
            this.narratorPersonality = narratorPersonality;
            return this;
        }

        public Builder isMultiplayer(boolean isMultiplayer) {

            this.isMultiplayer = isMultiplayer;
            return this;
        }

        public Builder modelConfiguration(ModelConfiguration modelConfiguration) {

            this.modelConfiguration = modelConfiguration;
            return this;
        }

        public Builder moderation(Moderation moderation) {

            this.moderation = moderation;
            return this;
        }

        public Builder contextAttributes(ContextAttributes contextAttributes) {

            this.contextAttributes = contextAttributes;
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

        public Adventure build() {

            if (isBlank(name)) {
                throw new BusinessRuleViolationException("Adventure name cannot be null or empty");
            }

            if (modelConfiguration == null) {
                throw new BusinessRuleViolationException("Model configuration cannot be null");
            }

            if (moderation == null) {
                throw new BusinessRuleViolationException("Moderation cannot be null");
            }

            if (visibility == null) {
                throw new BusinessRuleViolationException("Visibility cannot be null");
            }

            return new Adventure(this);
        }
    }
}
