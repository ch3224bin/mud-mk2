package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.combat.*;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.tick.TickListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CombatService implements TickListener {

    private final Map<UUID, ATBCombat> combatMap = new ConcurrentHashMap<>();

    private final GameWorldService gameWorldService;
    private final ActiveRoomRepository rooms;
    private final SendMessageToUserPort sendMessageToUserPort;
    private final CombatNarrativeFormatter narrativeFormatter;

    public CombatService(GameWorldService gameWorldService,
                         ActiveRoomRepository rooms,
                         SendMessageToUserPort sendMessageToUserPort,
                         CombatNarrativeFormatter narrativeFormatter) {
        this.gameWorldService = gameWorldService;
        this.rooms = rooms;
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.narrativeFormatter = narrativeFormatter;
    }

    @Override
    @Async("combatTaskExecutor")
    public void onTick(long tickCount) {
        combatMap.values().forEach(combat -> {
            CombatActionResult result = combat.tick();
            if (result.isActed()) {
                sendCombatMessages(combat, result);
            }
            if (combat.isFinished()) {
                handleCombatEnd(combat);
                combat.getAllyUsers().forEach(player ->
                    sendMessageToUserPort.messageToUser(player.getUserId(), "[전투 종료]"));
                combatMap.remove(combat.getId());
            }
        });
    }

    public void startCombat(PlayerCharacter attacker, Combatable defender) {
        List<ATBCombatParticipant> participants = new ArrayList<>();

        Party party = gameWorldService.getPartyByPlayerId(attacker.getId()).orElse(null);
        if (party != null) {
            party.getMembers().getMembers().stream()
                .filter(m -> m instanceof Combatable)
                .map(m -> (Combatable) m)
                .forEach(c -> participants.add(new ATBCombatParticipant(c, CombatGroupType.ALLY)));
        } else {
            participants.add(new ATBCombatParticipant(attacker, CombatGroupType.ALLY));
        }

        Combatable enemy = defender;
        participants.add(createEnemyParticipant(enemy));

        ATBCombat combat = new ATBCombat(UUID.randomUUID(), participants, new DefaultRandomGenerator());
        combat.start();
        combatMap.put(combat.getId(), combat);

        combat.getAllyUsers().forEach(player ->
            sendMessageToUserPort.messageToUser(player.getUserId(),
                String.format("[전투 시작] %s과(와) %s의 전투가 시작되었습니다!",
                    player.getName(), enemy.getName())));
    }

    private ATBCombatParticipant createEnemyParticipant(Combatable combatable) {
        if (combatable instanceof Monster monster) {
            return new ATBCombatParticipant(combatable, CombatGroupType.ENEMY,
                monster.getWeaponBaseDamage(), monster.getEquipmentArmor(), monster.getEquipmentArmorPct());
        }
        return new ATBCombatParticipant(combatable, CombatGroupType.ENEMY);
    }

    private void sendCombatMessages(ATBCombat combat, CombatActionResult result) {
        List<PlayerCharacter> users = combat.getAllyUsers();
        result.getLogs().forEach(log -> {
            String message = narrativeFormatter.format(log);
            users.forEach(user ->
                sendMessageToUserPort.messageToUser(user.getUserId(), message));
        });
    }

    private void handleCombatEnd(ATBCombat combat) {
        combat.getAllyUsers().forEach(player -> {
            if (!player.isAlive()) {
                Long roomId = player.getCurrentRoomId();
                Optional<Room> room = rooms.findById(roomId);
                if (room.isPresent() && room.get().isSimulationRoom()) {
                    player.fullRestore();
                    sendMessageToUserPort.messageToUser(player.getUserId(),
                        "시뮬레이션 전투 종료. HP가 완전히 회복되었습니다.");
                }
            }
        });
    }
}
