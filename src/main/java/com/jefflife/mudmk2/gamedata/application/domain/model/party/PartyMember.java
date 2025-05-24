package com.jefflife.mudmk2.gamedata.application.domain.model.party;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;
    
    private UUID characterId;

    @CreationTimestamp
    private LocalDateTime joinedAt = LocalDateTime.now();
    
    public PartyMember(Party party, UUID characterId) {
        this.party = party;
        this.characterId = characterId;
    }
}