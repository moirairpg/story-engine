package me.moirai.storyengine.core.domain.chronicle;

public class ChronicleSegmentFixture {

    public static ChronicleSegment.Builder chronicleSegment() {

        return ChronicleSegment.builder()
                .adventureId(1L)
                .content("The adventurers explored the dungeon and defeated the dragon.");
    }
}
