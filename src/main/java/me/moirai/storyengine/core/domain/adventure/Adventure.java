package me.moirai.storyengine.core.domain.adventure;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.replacePersonaNamePlaceholderWith;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.uuid.Generators;

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
import jakarta.persistence.Transient;
import me.moirai.storyengine.common.domain.DomainEvent;
import me.moirai.storyengine.common.domain.Narrator;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.domain.ShareableAsset;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.InvitationStatus;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.util.Functions;

@Entity
@Table(name = "adventure")
public class Adventure extends ShareableAsset {

    public static final int MAX_ROSTER_SIZE = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "image_key")
    private String imageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation")
    private Moderation moderation;

    @Column(name = "rpg_mechanics_enabled")
    private boolean rpgMechanicsEnabled = true;

    @Embedded
    private ContextAttributes contextAttributes;

    @Embedded
    private ModelConfiguration modelConfiguration;

    @ElementCollection
    @CollectionTable(name = "adventure_permissions", joinColumns = @JoinColumn(name = "adventure_id"))
    private List<Permission> permissions = new ArrayList<>();

    @Column(name = "ui_image_position_x")
    private Double uiImagePositionX;

    @Column(name = "ui_image_position_y")
    private Double uiImagePositionY;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "adventure_id")
    private List<AdventureLorebookEntry> lorebook = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "adventure_id", nullable = false, insertable = false, updatable = false)
    private List<ChronicleSegment> chronicleSegments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "adventure_id", nullable = false, insertable = false, updatable = false)
    private List<AdventureMembership> roster = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "adventure_id", nullable = false, insertable = false, updatable = false)
    private List<Invitation> invitations = new ArrayList<>();

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    @Override
    protected List<Permission> permissions() {
        return permissions;
    }

    private Adventure(Builder builder) {

        super(builder.visibility);

        this.publicId = Generators.timeBasedEpochGenerator().generate();
        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
        this.worldId = builder.worldId;
        this.narrator = new Narrator(builder.narratorName, builder.narratorPersonality);
        this.contextAttributes = builder.contextAttributes;
        this.modelConfiguration = builder.modelConfiguration;
        this.moderation = builder.moderation;
        this.permissions.addAll(builder.permissions);
        updateRpgMechanicsEnabled(builder.rpgMechanicsEnabled);
    }

    protected Adventure() {
        super();
    }

    public List<DomainEvent> drainEvents() {
        var snapshot = List.copyOf(domainEvents);
        domainEvents.clear();
        return snapshot;
    }

    public void communicateAdventureDeleted() {
        domainEvents.add(new AdventureDeletedEvent(this.id, this.publicId, this.imageKey));
    }

    @Override
    public void updatePermissions(Set<Permission> newPermissions) {

        var previousLevelsByUserId = getPermissions().stream()
                .filter(permission -> permission.level() != PermissionLevel.OWNER)
                .collect(Collectors.toMap(Permission::userId, Permission::level, PermissionLevel::weakest));

        var collapsedPermissions = collapseToWeakestLevel(newPermissions);

        super.updatePermissions(collapsedPermissions);

        collapsedPermissions.stream()
                .filter(permission -> permission.level() != PermissionLevel.OWNER)
                .forEach(permission -> {
                    var previousLevel = previousLevelsByUserId.remove(permission.userId());

                    if (previousLevel == null) {
                        domainEvents.add(new AdventureAccessGrantedEvent(
                                this.id, this.publicId, this.name, permission.userId(), permission.level()));
                    } else if (previousLevel != permission.level()) {
                        domainEvents.add(new AdventureAccessLevelChangedEvent(
                                this.id, this.publicId, this.name, permission.userId(), permission.level()));
                    }
                });

        previousLevelsByUserId.keySet()
                .forEach(userId -> domainEvents.add(new AdventureAccessRevokedEvent(
                        this.id, this.publicId, this.name, userId)));
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
        return Functions.mapOrDefault(narrator, "Narrator", Narrator::narratorName);
    }

    public String getNarratorPersonalityTemplate() {
        return Functions.mapOrNull(narrator, Narrator::narratorPersonality);
    }

    public String getNarratorPersonality() {

        var template = Functions.mapOrNull(narrator, Narrator::narratorPersonality);

        if (template == null) {
            return null;
        }

        return replacePersonaNamePlaceholderWith(getNarratorName()).apply(template);
    }

    public ModelConfiguration getModelConfiguration() {
        return modelConfiguration;
    }

    public Moderation getModeration() {
        return moderation;
    }

    public boolean isRpgMechanicsEnabled() {
        return rpgMechanicsEnabled;
    }

    public ContextAttributes getContextAttributes() {
        return contextAttributes;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public String generateImageKey(String fileExtension) {

        var imageId = Generators.timeBasedEpochGenerator().generate();
        this.imageKey = "adventures/" + this.publicId + "/" + imageId + "." + fileExtension;

        return this.imageKey;
    }

    public Double getUiImagePositionX() {
        return uiImagePositionX;
    }

    public Double getUiImagePositionY() {
        return uiImagePositionY;
    }

    public void updateUiImagePosition(Double uiImagePositionX, Double uiImagePositionY) {
        this.uiImagePositionX = uiImagePositionX;
        this.uiImagePositionY = uiImagePositionY;
    }

    public List<AdventureLorebookEntry> getLorebook() {
        return Collections.unmodifiableList(lorebook);
    }

    public List<ChronicleSegment> getChronicleSegments() {
        return Collections.unmodifiableList(chronicleSegments);
    }

    public List<AdventureMembership> getRoster() {
        return Collections.unmodifiableList(roster);
    }

    public List<AdventureLorebookEntry> getLorebookEntriesByIds(Collection<UUID> entryIds) {

        return lorebook.stream()
                .filter(entry -> entryIds.contains(entry.getPublicId()))
                .toList();
    }

    public List<ChronicleSegment> getChronicleSegmentsByIds(Collection<UUID> segmentIds) {

        return chronicleSegments.stream()
                .filter(segment -> segmentIds.contains(segment.getPublicId()))
                .toList();
    }

    public List<Long> getEnrolledCharacterIds() {

        return roster.stream()
                .map(AdventureMembership::getPlayerCharacterId)
                .toList();
    }

    public void enrollPlayerCharacter(Long playerCharacterId, Long playerId) {

        if (roster.size() >= MAX_ROSTER_SIZE) {
            throw new BusinessRuleViolationException("Adventure roster is full");
        }

        if (hasCharacter(playerCharacterId)) {
            throw new BusinessRuleViolationException("Character is already enrolled in this adventure");
        }

        if (hasPlayer(playerId)) {
            throw new BusinessRuleViolationException("Player already enrolled in this adventure");
        }

        roster.add(AdventureMembership.of(this.id, playerCharacterId, playerId));
    }

    public void leave(Long playerCharacterId) {

        var membership = removeMembership(playerCharacterId);

        domainEvents.add(new PlayerLeftAdventureEvent(
                this.id, this.publicId, this.name, membership.getPlayerId(), playerCharacterId));
    }

    public void expel(Long playerCharacterId) {

        var membership = removeMembership(playerCharacterId);

        domainEvents.add(new PlayerExpelledFromAdventureEvent(
                this.id, this.publicId, this.name, membership.getPlayerId(), playerCharacterId));
    }

    public void withdrawDeletedCharacter(Long playerCharacterId) {

        var membership = removeMembership(playerCharacterId);

        domainEvents.add(new EnrolledCharacterDeletedEvent(
                this.id, this.publicId, this.name, membership.getPlayerId(), playerCharacterId));
    }

    private AdventureMembership removeMembership(Long playerCharacterId) {

        var membership = roster.stream()
                .filter(entry -> entry.getPlayerCharacterId().equals(playerCharacterId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Character is not enrolled in this adventure"));

        roster.remove(membership);

        var playerId = membership.getPlayerId();

        if (canRead(playerId) && !canWrite(playerId)) {
            revoke(playerId);
        }

        return membership;
    }

    public boolean hasCharacter(Long playerCharacterId) {

        return roster.stream()
                .anyMatch(membership -> membership.getPlayerCharacterId().equals(playerCharacterId));
    }

    public boolean hasPlayer(Long playerId) {

        return roster.stream()
                .anyMatch(membership -> membership.getPlayerId().equals(playerId));
    }

    public void withdrawInvitationsInvolving(Long userId) {

        invitations.removeIf(invitation -> invitation.getUserId().equals(userId)
                || invitation.getInviterId().equals(userId));
    }

    public List<Invitation> getInvitations() {
        return Collections.unmodifiableList(invitations);
    }

    public Invitation invite(Long userId, Long inviterId) {

        var alreadyInvited = invitations.stream()
                .anyMatch(invitation -> invitation.getUserId().equals(userId) && invitation.isPending());

        if (alreadyInvited) {
            throw new BusinessRuleViolationException("User already has a pending invitation for this adventure");
        }

        var invitation = Invitation.builder()
                .adventureId(this.id)
                .userId(userId)
                .inviterId(inviterId)
                .build();

        invitations.add(invitation);
        domainEvents.add(new UserInvitedToAdventureEvent(invitation.getPublicId()));

        return invitation;
    }

    public void acceptInvitation(UUID invitationPublicId, Long playerCharacterId, Long playerId) {

        var invitation = getInvitationByPublicId(invitationPublicId);

        enrollPlayerCharacter(playerCharacterId, playerId);
        invitation.accept();

        if (!canRead(playerId)) {
            grant(new Permission(playerId, PermissionLevel.READ));
        }

        domainEvents.add(new AdventureInvitationAnsweredEvent(
                this.id, this.publicId, this.name, invitation.getUserId(), InvitationStatus.ACCEPTED));
    }

    public void declineInvitation(UUID invitationPublicId) {

        var invitation = getInvitationByPublicId(invitationPublicId);

        invitation.decline();

        domainEvents.add(new AdventureInvitationAnsweredEvent(
                this.id, this.publicId, this.name, invitation.getUserId(), InvitationStatus.DECLINED));
    }

    private Invitation getInvitationByPublicId(UUID invitationPublicId) {

        return invitations.stream()
                .filter(invitation -> invitationPublicId.equals(invitation.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Invitation not found"));
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

    public void updateRpgMechanicsEnabled(Boolean rpgMechanicsEnabled) {

        this.rpgMechanicsEnabled = rpgMechanicsEnabled == null || rpgMechanicsEnabled;
    }

    public void updateModelConfiguration(
            ArtificialIntelligenceModel aiModel,
            Integer maxTokenLimit,
            Double temperature) {

        this.modelConfiguration = ModelConfiguration.builder()
                .aiModel(aiModel)
                .maxTokenLimit(maxTokenLimit)
                .temperature(temperature)
                .build();
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

    public AdventureLorebookEntry addLorebookEntry(String name, String description) {

        var entry = AdventureLorebookEntry.builder()
                .name(name)
                .description(description)
                .build();

        lorebook.add(entry);
        return entry;
    }

    public AdventureLorebookEntry updateLorebookEntry(UUID entryId, String name, String description) {

        var entry = getLorebookEntryById(entryId);

        entry.updateName(name);
        entry.updateDescription(description);

        return entry;
    }

    public void removeLorebookEntry(UUID entryId) {

        AdventureLorebookEntry entry = getLorebookEntryById(entryId);
        lorebook.remove(entry);

        domainEvents.add(new AdventureLorebookEntryRemovedEvent(entryId));
    }

    public AdventureLorebookEntry getLorebookEntryById(UUID entryId) {

        return lorebook.stream()
                .filter(e -> entryId.equals(e.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Lorebook entry not found"));
    }

    public ChronicleSegment addChronicleSegment(String content) {
        var segment = ChronicleSegment.builder()
                .adventureId(this.id)
                .content(content)
                .build();

        this.chronicleSegments.add(segment);
        return segment;
    }

    public static final class Builder {

        private String name;
        private String description;
        private String adventureStart;
        private UUID worldId;
        private String narratorName;
        private String narratorPersonality;
        private ContextAttributes contextAttributes;
        private ModelConfiguration modelConfiguration;
        private Moderation moderation;
        private Boolean rpgMechanicsEnabled;
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

        public Builder modelConfiguration(ModelConfiguration modelConfiguration) {

            this.modelConfiguration = modelConfiguration;
            return this;
        }

        public Builder moderation(Moderation moderation) {

            this.moderation = moderation;
            return this;
        }

        public Builder rpgMechanicsEnabled(Boolean rpgMechanicsEnabled) {

            this.rpgMechanicsEnabled = rpgMechanicsEnabled;
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
