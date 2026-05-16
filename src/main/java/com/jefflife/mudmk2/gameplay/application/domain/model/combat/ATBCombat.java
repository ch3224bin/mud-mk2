package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ATBCombat {

    public static final int SPEED_DIVISOR = 10;
    public static final int MAX_TICKS = 1000;

    private final UUID id;
    private final List<ATBCombatParticipant> participants;
    private final RandomGenerator randomGenerator;
    private CombatState state;
    private int tickCount;

    public ATBCombat(UUID id, List<ATBCombatParticipant> participants, RandomGenerator randomGenerator) {
        this.id = id;
        this.participants = new ArrayList<>(participants);
        this.randomGenerator = randomGenerator;
        this.state = CombatState.WAITING;
        this.tickCount = 0;
    }

    public void start() {
        state = CombatState.ACTIVE;
        participants.forEach(p -> p.getCombatable().enterCombatState());
    }

    public CombatActionResult tick() {
        if (state != CombatState.ACTIVE) {
            return CombatActionResult.NOT_ACTED;
        }
        if (tickCount >= MAX_TICKS) {
            state = CombatState.FINISHED;
            return CombatActionResult.NOT_ACTED;
        }
        tickCount++;

        participants.forEach(p -> p.advanceAtb(SPEED_DIVISOR));

        List<ATBCombatParticipant> readyList = participants.stream()
            .filter(p -> !p.isDefeated() && p.isReadyToAct())
            .sorted(Comparator.comparingDouble(ATBCombatParticipant::getAtbGauge).reversed()
                .thenComparing(Comparator.comparingInt((ATBCombatParticipant p) -> p.getCombatable().getStats().agility()).reversed()))
            .toList();

        CombatActionResult result = new CombatActionResult();
        for (ATBCombatParticipant actor : readyList) {
            if (actor.isDefeated()) continue;

            actor.recoverResourcesOnAction();
            actor.resetAtb();

            ATBCombatParticipant target = selectTarget(actor);
            if (target == null) {
                state = CombatState.FINISHED;
                break;
            }

            CombatLog log = executeAttack(actor, target);
            result.addLog(log);

            if (isGroupDefeated(actor.getGroup() == CombatGroupType.ALLY
                    ? CombatGroupType.ENEMY : CombatGroupType.ALLY)) {
                state = CombatState.FINISHED;
                break;
            }
        }
        return result;
    }

    private ATBCombatParticipant selectTarget(ATBCombatParticipant actor) {
        CombatGroupType targetGroup = actor.getGroup() == CombatGroupType.ALLY
            ? CombatGroupType.ENEMY : CombatGroupType.ALLY;
        return participants.stream()
            .filter(p -> p.getGroup() == targetGroup && !p.isDefeated())
            .min(Comparator.comparingInt(ATBCombatParticipant::getCurrentHp))
            .orElse(null);
    }

    private CombatLog executeAttack(ATBCombatParticipant actor, ATBCombatParticipant target) {
        CharacterStats actorStats = actor.getCombatable().getStats();
        CharacterStats targetStats = target.getCombatable().getStats();

        double accuracy = CombatFormulas.accuracy(actorStats, actor.getWeaponSkill(), 0);
        double evasion = CombatFormulas.evasion(targetStats);
        int evasionRate = CombatFormulas.evasionRate(evasion, accuracy);

        boolean evaded = (target.getCurrentAp() >= 5) && (randomGenerator.nextInt(100) < evasionRate);
        if (evaded) {
            target.spendAp(5);
            return buildEvadeLog(actor, target);
        }

        int raw = CombatFormulas.baseDamage(actorStats, actor.getWeaponBaseDamage(),
            actor.getWeaponSkill(), randomGenerator);
        boolean isCrit = randomGenerator.nextInt(100) < CombatFormulas.critRate(actorStats);
        if (isCrit) raw = (int)(raw * 1.5);

        int arm = CombatFormulas.armor(targetStats, target.getEquipArmor());
        int armPct = CombatFormulas.armorPct(targetStats, target.getEquipArmorPct(),
            target.getWeaponSkill());
        int finalDamage = CombatFormulas.applyDefense(raw, arm, armPct);

        target.applyDamage(finalDamage);

        return CombatLog.builder()
            .attackerId(actor.getCombatable().getId())
            .attackerName(actor.getCombatable().getName())
            .targetId(target.getCombatable().getId())
            .targetName(target.getCombatable().getName())
            .hitSuccess(true)
            .baseDamage(raw)
            .finalDamage(finalDamage)
            .defenseValue(arm)
            .targetRemainingHp(target.getCurrentHp())
            .targetDefeated(target.isDefeated())
            .evaded(false)
            .isCrit(isCrit)
            .attackerApAfter(actor.getCurrentAp())
            .targetApAfter(target.getCurrentAp())
            .weaponTypeName(actor.getWeaponTypeName())
            .weaponName(actor.getWeaponName())
            .attackRoll(0).attackModifier(0).attackTotal(0)
            .defenseRoll(0).defenseModifier(0).defenseTotal(0)
            .damageModifier(0).damageTotal(raw)
            .build();
    }

    private CombatLog buildEvadeLog(ATBCombatParticipant actor, ATBCombatParticipant target) {
        return CombatLog.builder()
            .attackerId(actor.getCombatable().getId())
            .attackerName(actor.getCombatable().getName())
            .targetId(target.getCombatable().getId())
            .targetName(target.getCombatable().getName())
            .hitSuccess(false)
            .evaded(true)
            .isCrit(false)
            .baseDamage(0).finalDamage(0).defenseValue(0)
            .targetRemainingHp(target.getCurrentHp())
            .targetDefeated(false)
            .attackerApAfter(actor.getCurrentAp())
            .targetApAfter(target.getCurrentAp())
            .weaponTypeName(actor.getWeaponTypeName())
            .weaponName(actor.getWeaponName())
            .attackRoll(0).attackModifier(0).attackTotal(0)
            .defenseRoll(0).defenseModifier(0).defenseTotal(0)
            .damageModifier(0).damageTotal(0)
            .build();
    }

    private boolean isGroupDefeated(CombatGroupType group) {
        return participants.stream()
            .filter(p -> p.getGroup() == group)
            .allMatch(ATBCombatParticipant::isDefeated);
    }

    public boolean isFinished() {
        return state == CombatState.FINISHED;
    }

    public UUID getId() {
        return id;
    }

    public List<Long> getAllyUserIds() {
        return participants.stream()
            .filter(p -> p.getGroup() == CombatGroupType.ALLY)
            .map(ATBCombatParticipant::getCombatable)
            .filter(c -> c instanceof PlayerCharacter)
            .map(c -> ((PlayerCharacter) c).getUserId())
            .toList();
    }

    public List<PlayerCharacter> getAllyUsers() {
        return participants.stream()
            .filter(p -> p.getGroup() == CombatGroupType.ALLY)
            .map(ATBCombatParticipant::getCombatable)
            .filter(c -> c instanceof PlayerCharacter)
            .map(c -> (PlayerCharacter) c)
            .toList();
    }
}
