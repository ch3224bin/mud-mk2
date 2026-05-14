package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.RecruitCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.PartyService;
import com.jefflife.mudmk2.gameplay.application.service.provided.RecruitUseCase;
import com.jefflife.mudmk2.gameplay.application.service.query.CreatureLookupQuery;
import com.jefflife.mudmk2.gameplay.application.service.query.PartyMembershipQuery;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RecruitCommandService implements RecruitUseCase {
    private static final Logger logger = LoggerFactory.getLogger(RecruitCommandService.class);

    private final CreatureLookupQuery creatureLookup;
    private final PartyMembershipQuery partyMembership;
    private final PartyService partyService;
    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public RecruitCommandService(
            final CreatureLookupQuery creatureLookup,
            final PartyMembershipQuery partyMembership,
            final PartyService partyService,
            final ActivePlayerRepository players,
            final SendMessageToUserPort sendMessageToUserPort
    ) {
        this.creatureLookup = creatureLookup;
        this.partyMembership = partyMembership;
        this.partyService = partyService;
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void recruit(final RecruitCommand recruitCommand) {
        // 1. 플레이어 정보 가져오기
        PlayerCharacter player = players.findByUserId(recruitCommand.userId())
                .orElseThrow(() -> new PlayerNotFoundException(recruitCommand.userId()));
        UUID playerId = player.getId();
        Long playerRoomId = player.getCurrentRoomId();

        // 2. 초대할 NPC 찾기
        Optional<NonPlayerCharacter> targetNpcOpt = creatureLookup.findNpcByName(recruitCommand.npcName());
        if (targetNpcOpt.isEmpty()) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    "대상을 찾을 수 없습니다: " + recruitCommand.npcName()
            );
            return;
        }
        NonPlayerCharacter targetNpc = targetNpcOpt.get();

        // 3. NPC가 같은 방에 있는지 확인
        if (!targetNpc.getCurrentRoomId().equals(playerRoomId)) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    targetNpc.getName() + "는 당신과 같은 방에 있지 않습니다."
            );
            return;
        }

        // 4. NPC가 이미 다른 파티에 속해 있는지 확인 (NPC ID를 사용)
        UUID npcId = targetNpc.getId();
        if (partyMembership.isInParty(npcId)) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    targetNpc.getName() + "는 이미 다른 파티에 속해 있습니다."
            );
            return;
        }

        // 5. 플레이어의 파티가 있는지 확인, 없으면 생성
        Party party = partyMembership.findByMemberId(playerId)
                .orElseGet(() -> createParty(playerId));

        if (!party.isLeader(playerId)) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    "파티 리더만 NPC를 초대할 수 있습니다."
            );
            return;
        }

        // 6. NPC를 파티에 추가
        Party.AddPartyMemberResult result = party.addMember(npcId);
        switch (result) {
            case PARTY_FULL -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        "파티가 가득 찼습니다."
                );
            }
            case ALREADY_IN_SAME_PARTY -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        targetNpc.getName() + "는 이미 당신의 파티에 있습니다."
                );
            }
            case ALREADY_IN_OTHER_PARTY -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        targetNpc.getName() + "는 이미 다른 파티에 속해 있습니다."
                );
            }
            case SUCCESS -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        targetNpc.getName() + "가 당신의 파티에 합류했습니다."
                );
                logger.info("NPC {} joined player {}'s party", npcId, playerId);
            }
        }
    }

    private Party createParty(UUID playerId) {
        Party newParty = Party.createParty(playerId);
        partyService.create(newParty);
        logger.info("Created new party for player {}", playerId);
        return newParty;
    }
}
