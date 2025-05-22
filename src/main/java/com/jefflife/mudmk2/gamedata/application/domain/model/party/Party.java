package com.jefflife.mudmk2.gamedata.application.domain.model.party;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Party {
    private static final int MAX_PARTY_SIZE = 6;

    @Id
    private String id;
    
    private Long leaderId;
    
    @Enumerated(EnumType.STRING)
    private PartyStatus status = PartyStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private LootDistribution lootDistribution = LootDistribution.FREE_FOR_ALL;

    @Embedded
    private PartyMembers members = new PartyMembers();
    
    // 그룹 생성 메서드
    public static Party createParty(Long leaderId) {
        Party party = new Party();
        party.id = UUID.randomUUID().toString();
        party.leaderId = leaderId;
        party.addMember(leaderId);
        return party;
    }
    
    // 멤버 추가 메서드
    public AddPartyMemberResult addMember(Long memberId) {
        if (members.size() >= MAX_PARTY_SIZE) {
            return AddPartyMemberResult.PARTY_FULL;
        }

        // 이미 다른 파티에 속해있는지 확인
        
        // 이미 그룹에 있는지 확인
        if (members.contains(memberId)) {
            return AddPartyMemberResult.ALREADY_IN_SAME_PARTY;
        }
        
        members.add(this, memberId);
        return AddPartyMemberResult.SUCCESS;
    }
    
    // 멤버 제거 메서드
    public boolean removeMember(Long characterId) {
        boolean removed =members.remove(characterId);

        // 리더가 나간 경우 새 리더 지정
        if (removed && Objects.equals(characterId, leaderId) && !members.isEmpty()) {
            // 가장 오래된 멤버를 리더로 지정
            members.getOldestMember()
                    .ifPresent(oldestMember -> this.leaderId = oldestMember.getCharacterId());
        }
        
        // 모든 멤버가 나갔으면 그룹 비활성화
        if (members.isEmpty()) {
            this.status = PartyStatus.INACTIVE;
        }
        
        return removed;
    }
    
    // 리더 변경 메서드
    public boolean changeLeader(Long newLeaderId) {
        // 그룹 멤버인지 확인
        boolean isMember = members.isMember(newLeaderId);

        if (!isMember) {
            return false;
        }
        
        this.leaderId = newLeaderId;
        return true;
    }

    public boolean contains(final Long characterId) {
        return members.contains(characterId);
    }

    public boolean isInactive() {
        return status == PartyStatus.INACTIVE;
    }

    public List<Long> getMemberIds() {
        return members.getMemberIds();
    }

    public boolean isLeader(Long playerId) {
        return Objects.equals(leaderId, playerId);
    }

    public enum AddPartyMemberResult {
        SUCCESS,
        ALREADY_IN_OTHER_PARTY,
        ALREADY_IN_SAME_PARTY,
        PARTY_FULL
    }
}
