package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Statable;
import com.jefflife.mudmk2.gameplay.application.domain.model.combat.*;
import com.jefflife.mudmk2.gameplay.application.port.out.SendCombatMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.CombatStartVariables;
import com.jefflife.mudmk2.gameplay.application.tick.TickListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CombatService implements TickListener {
    private static final InitiativeSystem initiativeSystem = new InitiativeSystem(new DefaultRandomGenerator());
    private static final Map<UUID, Combat> combatMap = new ConcurrentHashMap<>();

    private final GameWorldService gameWorldService;
    private final SendCombatMessagePort sendCombatMessagePort;

    public CombatService(GameWorldService gameWorldService, SendCombatMessagePort sendCombatMessagePort) {
        this.gameWorldService = gameWorldService;
        this.sendCombatMessagePort = sendCombatMessagePort;
    }

    @Override
    @Async("combatTaskExecutor")
    public void onTick(long tickCount) {
        combatMap.values()
                .forEach(combat -> {
                    combat.action();
                    if (combat.isFinished()) {
                        combat.close();
                        combatMap.remove(combat.getId());
                        return;
                    }
                });
    }

    public void startCombat(PlayerCharacter attacker, Statable defender) {
        // PC, NPC, 몬스터의 상태를 전투중으로 변경

        CombatGroup allyGroup = createAllyGroup(attacker);
        CombatGroup enemyGroup = createEnemyGroup(defender);

        Combat combat = new Combat(UUID.randomUUID(), allyGroup, enemyGroup, initiativeSystem::rollInitiative);
        CombatStartResult startResult = combat.start();
        combatMap.put(combat.getId(), combat);

        // 전투 시작 알림
        sendStartCombatMessage(combat, startResult);
    }

    private CombatGroup createAllyGroup(PlayerCharacter attacker) {
        CombatGroup allyGroup = new CombatGroup(CombatGroupType.ALLY);
        Party party = gameWorldService.getPartyByPlayerId(attacker.getId()).orElse(null);

        if (party != null) {
            addPartyMembersToCombat(party, allyGroup);
        } else {
            allyGroup.addParticipant(new CombatParticipant(attacker));
        }

        return allyGroup;
    }

    private void addPartyMembersToCombat(Party party, CombatGroup allyGroup) {
        party.getMembers().getMembers().stream()
                .filter(member -> member instanceof Combatable)
                .map(member -> (Combatable) member)
                .map(CombatParticipant::new)
                .forEach(allyGroup::addParticipant);
    }

    private CombatGroup createEnemyGroup(Statable defender) {
        CombatGroup enemyGroup = new CombatGroup(CombatGroupType.ENEMY);
        enemyGroup.addParticipant(new CombatParticipant((Combatable) defender));
        return enemyGroup;
    }

    private void sendStartCombatMessage(Combat combat, CombatStartResult startResult) {
        List<Long> allyUserIds = combat.getAllyUserIds();
        for (Long allyUserId : allyUserIds) {
            sendCombatMessagePort.sendCombatStartMessageToUser(new CombatStartVariables(allyUserId, startResult));
        }
    }
}
