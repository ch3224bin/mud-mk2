package com.jefflife.mudmk2.gamedata.application.domain.model.party;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Embeddable
public class PartyMembers {
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyMember> members = new ArrayList<>();

    public int size() {
        return members.size();
    }

    public boolean contains(final UUID memberId) {
        return members.stream()
                .anyMatch(member -> Objects.equals(member.getCharacterId(), memberId));
    }

    public void add(final Party party, final UUID memberId) {
        members.add(new PartyMember(party, memberId));
    }

    public boolean remove(final UUID characterId) {
        return members.removeIf(member -> Objects.equals(member.getCharacterId(), characterId));
    }

    public Optional<PartyMember> getOldestMember() {
        return members.stream()
                .min(Comparator.comparing(PartyMember::getJoinedAt));
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean isMember(final UUID memberId) {
        return members.stream()
                .anyMatch(member -> Objects.equals(member.getCharacterId(), memberId));
    }

    /**
     * 파티 멤버들의 ID 목록을 반환합니다.
     * @return 파티 멤버 ID 목록
     */
    public List<UUID> getMemberIds() {
        return members.stream()
                .map(PartyMember::getCharacterId)
                .toList();
    }
}
