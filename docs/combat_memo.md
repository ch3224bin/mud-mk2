## Combat 스펙 정리

- 아군 그룹과 적군 그룹으로 나뉜다
- 선제권을 갖고 있는 그룹이 먼저 행동한다
- 플레이어가 전투를 시작하면, 해당 플레이어의 그룹에 속한 모든 캐릭터가 전투에 참여한다
- 전투가 열리자 마자 첫턴이 사용된다.
- 10 tick이 한 턴이다.
- 적군 그룹 또는 아군 그룹이 모두 패배하면 전투가 종료된다.
- 상대 그룹의 어그로 점수가 가장 높은 대상을 공격한다
- 어그로 점수는 타격, 치유, 지원 행동에 따라 증가한다



```java
@Service
@RequiredArgsConstructor
public class GroupCombatService {
    private final CharacterRepository characterRepository;
    private final CombatRepository combatRepository;
    private final PartyRepository partyRepository;
    private final NPCBehaviorService npcBehaviorService;
    private final WebSocketService webSocketService;
    
    /**
     * 전투 시작시 그룹 전투 처리
     */
    @Transactional
    public Combat startGroupCombat(String initiatorId, String enemyId, String locationId) {
        Character initiator = characterRepository.findById(initiatorId)
            .orElseThrow(() -> new EntityNotFoundException("Character not found"));
            
        Character enemy = characterRepository.findById(enemyId)
            .orElseThrow(() -> new EntityNotFoundException("Enemy not found"));
            
        // 전투 시작
        Combat combat = combatRepository.save(new Combat(initiator, enemy, locationId));
        
        // 그룹이 있는지 확인
        Party party = initiator.getCurrentParty();
        if (party != null) {
            // 그룹원들을 전투에 참여시킴
            addPartyMembersToCombat(combat, party);
        }
        
        return combat;
    }
    
    /**
     * 그룹원들을 전투에 참여시킴
     */
    private void addPartyMembersToCombat(Combat combat, Party party) {
        // 전투 시작자를 제외한 모든 그룹원
        List<Character> partyMembers = party.getMembers().stream()
            .map(PartyMember::getCharacter)
            .filter(c -> !c.getId().equals(combat.getInitiator().getId()))
            .collect(Collectors.toList());
            
        for (Character member : partyMembers) {
            // NPC의 경우 AI 기반 전투 행동 결정
            if (member instanceof NPC) {
                NPC npc = (NPC) member;
                combat.addCombatant(member);
                
                // 비동기로 NPC 행동 처리
                npcBehaviorService.scheduleNPCCombatAction(npc, combat);
            } 
            // 플레이어의 경우 전투 참여 알림
            else if (member instanceof Player) {
                combat.addCombatant(member);
                webSocketService.notifyCombatJoin(member.getId(), combat.getId());
            }
        }
        
        // 변경사항 저장
        combatRepository.save(combat);
    }
    
    /**
     * NPC의 전투 행동 처리
     */
    @Transactional
    public void processNPCCombatAction(String npcId, String combatId) {
        NPC npc = (NPC) characterRepository.findById(npcId)
            .orElseThrow(() -> new EntityNotFoundException("NPC not found"));
            
        Combat combat = combatRepository.findById(combatId)
            .orElseThrow(() -> new EntityNotFoundException("Combat not found"));
            
        // 전투가 진행 중인지 확인
        if (!combat.isActive()) {
            return;
        }
        
        // NPC 행동 타입 결정
        NPCCombatAction action = determineNPCAction(npc, combat);
        
        // 행동 실행
        executeNPCAction(npc, combat, action);
        
        // 전투 결과 갱신
        combatRepository.save(combat);
    }
    
    /**
     * NPC 행동 타입 결정
     */
    private NPCCombatAction determineNPCAction(NPC npc, Combat combat) {
        NPCBehavior behavior = npc.getDefaultBehavior();
        
        // 파티 리더의 HP가 낮은 경우 지원 행동 우선
        Character leader = combat.getInitiator();
        if (leader.getHp() < leader.getMaxHp() * 0.3 && behavior != NPCBehavior.AGGRESSIVE) {
            return NPCCombatAction.SUPPORT;
        }
        
        // 기본 행동 기반으로 결정
        switch (behavior) {
            case AGGRESSIVE:
                return NPCCombatAction.ATTACK;
            case DEFENSIVE:
                return npc.getHp() < npc.getMaxHp() * 0.5 ? 
                    NPCCombatAction.DEFEND : NPCCombatAction.ATTACK;
            case SUPPORT:
                // 파티원 중 HP가 낮은 멤버가 있는지 확인
                for (CombatParticipant participant : combat.getParticipants()) {
                    Character character = participant.getCharacter();
                    if (character.getHp() < character.getMaxHp() * 0.5 && 
                        !character.getId().equals(npc.getId())) {
                        return NPCCombatAction.SUPPORT;
                    }
                }
                return NPCCombatAction.ATTACK;
            case PASSIVE:
                return NPCCombatAction.DEFEND;
            default:
                return NPCCombatAction.ATTACK;
        }
    }
    
    /**
     * NPC 행동 실행
     */
    private void executeNPCAction(NPC npc, Combat combat, NPCCombatAction action) {
        switch (action) {
            case ATTACK:
                executeNPCAttack(npc, combat);
                break;
            case DEFEND:
                executeNPCDefend(npc, combat);
                break;
            case SUPPORT:
                executeNPCSupport(npc, combat);
                break;
            case FLEE:
                executeNPCFlee(npc, combat);
                break;
        }
    }
    
    /**
     * NPC 공격 행동
     */
    private void executeNPCAttack(NPC npc, Combat combat) {
        // 적 대상 선택
        Character target = selectEnemyTarget(combat);
        if (target == null) return;
        
        // 공격력 계산
        int damage = calculateNPCDamage(npc);
        
        // 대상에게 데미지 적용
        target.setHp(Math.max(0, target.getHp() - damage));
        
        // 전투 메시지 생성
        String message = npc.getName() + " attacks " + target.getName() + " for " + damage + " damage.";
        combat.addCombatLog(message);
        
        // WebSocket으로 전투 업데이트 전송
        webSocketService.notifyCombatUpdate(combat.getId(), message);
        
        // 대상 사망 체크
        if (target.getHp() <= 0) {
            handleCharacterDefeat(target, combat);
        }
    }
    
    /**
     * NPC 방어 행동
     */
    private void executeNPCDefend(NPC npc, Combat combat) {
        // 방어 효과 적용 (다음 턴에 받는 데미지 감소)
        combat.applyDefenseBonus(npc.getId(), 0.5); // 50% 데미지 감소
        
        // 전투 메시지 생성
        String message = npc.getName() + " takes defensive stance.";
        combat.addCombatLog(message);
        
        // WebSocket으로 전투 업데이트 전송
        webSocketService.notifyCombatUpdate(combat.getId(), message);
    }
    
    /**
     * NPC 지원 행동 (예: 치유)
     */
    private void executeNPCSupport(NPC npc, Combat combat) {
        // 지원 대상 선택 (HP가 가장 낮은 아군)
        Character target = selectAlliedTarget(npc, combat);
        if (target == null) return;
        
        // 특별한 NPC 능력 사용 (예: 소연의 응급처치)
        if ("소연".equals(npc.getName())) {
            int healAmount = 20 + (npc.getLevel() * 5); // 레벨에 따라 치유량 증가
            
            // 대상 치유
            target.setHp(Math.min(target.getMaxHp(), target.getHp() + healAmount));
            
            // 전투 메시지 생성
            String message = npc.getName() + " uses 응급처치 on " + target.getName() + 
                             ", healing for " + healAmount + " HP.";
            combat.addCombatLog(message);
            
            // WebSocket으로 전투 업데이트 전송
            webSocketService.notifyCombatUpdate(combat.getId(), message);
        } else {
            // 기본 지원 행동
            int healAmount = 10;
            target.setHp(Math.min(target.getMaxHp(), target.getHp() + healAmount));
            
            // 전투 메시지 생성
            String message = npc.getName() + " supports " + target.getName() + 
                             ", healing for " + healAmount + " HP.";
            combat.addCombatLog(message);
            
            // WebSocket으로 전투 업데이트 전송
            webSocketService.notifyCombatUpdate(combat.getId(), message);
        }
    }
    
    /**
     * NPC 도주 행동
     */
    private void executeNPCFlee(NPC npc, Combat combat) {
        // 도주 성공 확률 계산
        boolean success = Math.random() < 0.7; // 70% 성공률
        
        if (success) {
            // 전투에서 제외
            combat.removeCombatant(npc.getId());
            
            // 전투 메시지 생성
            String message = npc.getName() + " successfully flees from combat.";
            combat.addCombatLog(message);
            
            // WebSocket으로 전투 업데이트 전송
            webSocketService.notifyCombatUpdate(combat.getId(), message);
        } else {
            // 도주 실패
            String message = npc.getName() + " tries to flee but fails.";
            combat.addCombatLog(message);
            
            // WebSocket으로 전투 업데이트 전송
            webSocketService.notifyCombatUpdate(combat.getId(), message);
        }
    }
    
    /**
     * 적 대상 선택
     */
    private Character selectEnemyTarget(Combat combat) {
        // 몬스터 대상 선택
        List<Character> enemies = combat.getParticipants().stream()
            .map(CombatParticipant::getCharacter)
            .filter(c -> c.getId().equals(combat.getEnemy().getId()) || isAlly(c, combat.getEnemy()))
            .collect(Collectors.toList());
            
        if (enemies.isEmpty()) return null;
        
        // 가장 HP가 낮은 대상 선택
        return enemies.stream()
            .min(Comparator.comparing(Character::getHp))
            .orElse(enemies.get(0));
    }
    
    /**
     * 아군 대상 선택
     */
    private Character selectAlliedTarget(NPC npc, Combat combat) {
        // 아군 대상 선택 (HP 비율이 가장 낮은 캐릭터)
        List<Character> allies = combat.getParticipants().stream()
            .map(CombatParticipant::getCharacter)
            .filter(c -> !c.getId().equals(combat.getEnemy().getId()) && !isAlly(c, combat.getEnemy()))
            .collect(Collectors.toList());
            
        if (allies.isEmpty()) return null;
        
        // HP 비율이 가장 낮은 아군 선택
        return allies.stream()
            .min(Comparator.comparing(c -> (double) c.getHp() / c.getMaxHp()))
            .orElse(allies.get(0));
    }
    
    /**
     * 캐릭터 패배 처리
     */
    private void handleCharacterDefeat(Character character, Combat combat) {
        // 전투에서 제외
        combat.removeCombatant(character.getId());
        
        // 적 패배 시 전투 종료 체크
        if (character.getId().equals(combat.getEnemy().getId()) || isAlly(character, combat.getEnemy())) {
            if (areAllEnemiesDefeated(combat)) {
                endCombatWithVictory(combat);
            }
        } 
        // 플레이어/NPC 패배 시 패배 처리
        else {
            if (areAllPlayersSideDefeated(combat)) {
                endCombatWithDefeat(combat);
            }
        }
    }
    
    // 기타 헬퍼 메서드들...
}

// NPC 전투 행동 열거형
enum NPCCombatAction {
    ATTACK, DEFEND, SUPPORT, FLEE
}
```