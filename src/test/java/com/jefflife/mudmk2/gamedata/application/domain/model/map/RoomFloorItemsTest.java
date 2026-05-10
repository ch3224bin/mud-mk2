package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RoomFloorItemsTest {

    private Room room;
    private WeaponTemplate swordTemplate;

    @BeforeEach
    void setUp() {
        room = Room.builder()
                .areaId(1L).name("테스트 방").summary("테스트").description("테스트용 방").build();
        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD).statModifiers(List.of()).build();
    }

    @Test
    void addFloorItem_addsItemToFloor() {
        ItemInstance item = new ItemInstance(swordTemplate, 1);
        room.addFloorItem(item);
        assertThat(room.getFloorItems()).hasSize(1);
    }

    @Test
    void removeFloorItem_removesItemFromFloor() {
        ItemInstance item = new ItemInstance(swordTemplate, 1);
        room.addFloorItem(item);
        room.removeFloorItem(item);
        assertThat(room.getFloorItems()).isEmpty();
    }

    @Test
    void findFloorItemsByName_returnsMatchingItemsInOrder() {
        ItemInstance sword1 = new ItemInstance(swordTemplate, 1);
        ItemInstance sword2 = new ItemInstance(swordTemplate, 1);
        room.addFloorItem(sword1);
        room.addFloorItem(sword2);
        List<ItemInstance> found = room.findFloorItemsByName("철검");
        assertThat(found).hasSize(2);
        assertThat(found.get(0)).isSameAs(sword1);
        assertThat(found.get(1)).isSameAs(sword2);
    }

    @Test
    void findFloorItemsByName_returnsEmptyWhenNoMatch() {
        assertThat(room.findFloorItemsByName("없는아이템")).isEmpty();
    }
}
