package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipmentViewUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EquipmentViewCommandService implements EquipmentViewUseCase {

    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public EquipmentViewCommandService(ActivePlayerRepository players, SendMessageToUserPort sendMessageToUserPort) {
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void showEquipment(EquipmentViewCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        EquippedItems equipped = player.getEquippedItems();

        StringBuilder sb = new StringBuilder("[ 장비 ]\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            sb.append(String.format("%-10s: ", slot.displayName()));
            Optional<ItemInstance> opt = equipped.getSlot(slot);
            if (opt.isEmpty()) {
                sb.append("(없음)\n");
            } else {
                ItemInstance inst = opt.get();
                sb.append(inst.getTemplate().getName());
                if (inst.getTemplate() instanceof EquippableItemTemplate eq && !eq.getStatModifiers().isEmpty()) {
                    sb.append(" (");
                    for (int i = 0; i < eq.getStatModifiers().size(); i++) {
                        StatModifier mod = eq.getStatModifiers().get(i);
                        if (i > 0) sb.append(", ");
                        sb.append(mod.getValue() >= 0 ? "+" : "").append(mod.getValue())
                          .append(" ").append(mod.getStatType());
                    }
                    sb.append(")");
                }
                sb.append("\n");
            }
        }

        CharacterStats base = player.getBaseStats();
        CharacterStats eff = player.getStats();
        StringBuilder diff = new StringBuilder("\n[ 적용 스탯 ] (base → effective)\n");
        boolean any = false;
        any |= appendDiff(diff, "VIGOR", base.vigor(), eff.vigor());
        any |= appendDiff(diff, "PHYSIQUE", base.physique(), eff.physique());
        any |= appendDiff(diff, "AGILITY", base.agility(), eff.agility());
        any |= appendDiff(diff, "INTELLECT", base.intellect(), eff.intellect());
        any |= appendDiff(diff, "WILL", base.will(), eff.will());
        any |= appendDiff(diff, "MERIDIAN", base.meridian(), eff.meridian());
        any |= appendDiff(diff, "INNER_POWER", base.innerPower(), eff.innerPower());
        any |= appendDiff(diff, "SPECIAL_TECHNIQUE", base.specialTechnique(), eff.specialTechnique());
        any |= appendDiff(diff, "LIGHT_STEP", base.lightStep(), eff.lightStep());
        any |= appendDiff(diff, "FISTS_AND_PALMS", base.fistsAndPalms(), eff.fistsAndPalms());
        any |= appendDiff(diff, "SWORD_METHOD", base.swordMethod(), eff.swordMethod());
        any |= appendDiff(diff, "BLADE_METHOD", base.bladeMethod(), eff.bladeMethod());
        any |= appendDiff(diff, "LONG_WEAPON", base.longWeapon(), eff.longWeapon());
        any |= appendDiff(diff, "ESOTERIC_WEAPON", base.esotericWeapon(), eff.esotericWeapon());
        any |= appendDiff(diff, "ARCHERY", base.archery(), eff.archery());
        if (any) sb.append(diff);

        sendMessageToUserPort.messageToUser(command.userId(), sb.toString());
    }

    private boolean appendDiff(StringBuilder sb, String label, int base, int eff) {
        if (base == eff) return false;
        sb.append(String.format("%-18s: %d → %d%n", label, base, eff));
        return true;
    }
}
