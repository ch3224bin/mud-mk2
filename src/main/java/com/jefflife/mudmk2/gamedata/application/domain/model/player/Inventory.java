package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    public static final int DEFAULT_MAX_WEIGHT_CAPACITY = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int maxWeightCapacity;

    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "inventory_id")
    private List<ItemInstance> items = new ArrayList<>();

    public List<ItemInstance> getItems() {
        return Collections.unmodifiableList(items);
    }

    private Inventory(int maxWeightCapacity) {
        this.maxWeightCapacity = maxWeightCapacity;
    }

    public static Inventory create(int maxWeightCapacity) {
        return new Inventory(maxWeightCapacity);
    }

    public boolean canAdd(ItemTemplate template, int qty) {
        return currentWeight() + template.getWeight() * qty <= maxWeightCapacity;
    }

    public int currentWeight() {
        return items.stream()
                .mapToInt(i -> i.getTemplate().getWeight() * i.getQuantity())
                .sum();
    }

    /**
     * 인벤토리에 아이템을 추가한다. detached 캐시 invariant 를 위해 아이템의 LAZY 그래프를
     * 강제 초기화한 뒤 처리한다 (stackable merge / 새 인스턴스 추가 두 경로 모두에서) —
     * 이 메서드를 통과한 아이템은 세션 없이도 template / 하위 그래프에 안전히 접근할 수 있음이 보장된다.
     */
    public void addItem(ItemInstance instance) {
        instance.initializeAssociatedEntities();
        if (instance.getTemplate().isStackable()) {
            Optional<ItemInstance> existing = items.stream()
                    .filter(i -> i.getTemplate() == instance.getTemplate())
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().addQuantity(instance.getQuantity());
                return;
            }
        }
        items.add(instance);
    }

    public void removeItem(ItemInstance instance) {
        items.remove(instance);
    }

    public boolean consumeOne(ItemInstance instance) {
        instance.decreaseQuantity(1);
        if (instance.getQuantity() <= 0) {
            items.remove(instance);
            return true;
        }
        return false;
    }

    public List<ItemInstance> findItemsByName(String name) {
        return items.stream()
                .filter(i -> i.getTemplate().getName().equals(name))
                .toList();
    }

    /**
     * 인메모리 캐시 적재 시점에 LAZY 컬렉션 + 각 아이템의 template 그래프를 강제 초기화한다.
     */
    public void initializeAssociatedEntities() {
        this.items.size();
        for (ItemInstance item : this.items) {
            item.initializeAssociatedEntities();
        }
    }
}
