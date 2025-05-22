package com.jefflife.mudmk2.gamedata.application.domain.model.party;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Party extends AbstractAggregateRoot<Party> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long leaderId;
    
    @Enumerated(EnumType.STRING)
    private PartyStatus status = PartyStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private LootDistribution lootDistribution = LootDistribution.FREE_FOR_ALL;

    @Embedded
    private PartyMembers members = new PartyMembers();
    
    // 그룹 생성 메서드
    public static Party createParty(PartyPolicy partyPolicy, Long leaderId) {
        Party party = new Party();
        party.leaderId = leaderId;
        party.addMember(partyPolicy, leaderId);
        return party;
    }
    
    // 멤버 추가 메서드
    public boolean addMember(PartyPolicy partyPolicy, Long memberId) {
        if (partyPolicy.isValidPartySize(members)) {
            return false;
        }
        
        // 이미 그룹에 있는지 확인
        if (members.contains(memberId)) {
            return false;
        }
        
        members.add(this, memberId);
        return true;
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
}
