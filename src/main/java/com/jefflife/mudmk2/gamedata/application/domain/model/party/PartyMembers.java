package com.jefflife.mudmk2.gamedata.application.domain.model.party;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Getter;

import java.util.*;

@Getter
@Embeddable
public class PartyMembers {
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyMember> members = new ArrayList<>();

    public int size() {
        return members.size();
    }

    public boolean contains(final Long memberId) {
        return members.stream()
                .anyMatch(member -> Objects.equals(member.getCharacterId(), memberId));
    }

    public void add(final Party party, final Long memberId) {
        members.add(new PartyMember(party, memberId));
    }

    public boolean remove(final Long characterId) {
        return members.removeIf(member -> Objects.equals(member.getCharacterId(), characterId));
    }

    public Optional<PartyMember> getOldestMember() {
        return members.stream()
                .min(Comparator.comparing(PartyMember::getJoinedAt));
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean isMember(final Long memberId) {
        return members.stream()
                .anyMatch(member -> Objects.equals(member.getCharacterId(), memberId));
    }
}
