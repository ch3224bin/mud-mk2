package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryMonsterRepositoryTest {

    @Mock private MonsterTypeRepository monsterTypes;
    private InMemoryMonsterRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMonsterRepository(monsterTypes);
    }

    @Test
    void bootstrap_createsMonstersForEachSpawnRoomTimesSpawnCount() {
        MonsterType type = monsterTypeWithSpawnRooms(
                spawnRoom(100L, 2),
                spawnRoom(200L, 3)
        );
        when(monsterTypes.findAll()).thenReturn(List.of(type));

        repository.bootstrap();

        List<Monster> loaded = toList(repository.findAll());
        assertThat(loaded).hasSize(5);
        long room100 = loaded.stream().filter(m -> m.getCurrentRoomId().equals(100L)).count();
        long room200 = loaded.stream().filter(m -> m.getCurrentRoomId().equals(200L)).count();
        assertThat(room100).isEqualTo(2);
        assertThat(room200).isEqualTo(3);
    }

    @Test
    void bootstrap_skipsMonsterTypeWhenMonsterSpawnRoomsContainerIsNull() {
        MonsterType type = MonsterType.builder().name("noSpawn").build();
        type.setMonsterSpawnRooms(null);
        when(monsterTypes.findAll()).thenReturn(List.of(type));

        repository.bootstrap();

        assertThat(toList(repository.findAll())).isEmpty();
    }

    @Test
    void bootstrap_skipsMonsterTypeWhenSpawnRoomsListIsEmpty() {
        MonsterType type = MonsterType.builder().name("emptySpawn").build();
        // monsterSpawnRooms 는 @Builder.Default 로 빈 컬렉션 초기화됨
        when(monsterTypes.findAll()).thenReturn(List.of(type));

        repository.bootstrap();

        assertThat(toList(repository.findAll())).isEmpty();
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_returnsAllAddedMonsters() {
        Monster m1 = simpleMonster(10L);
        Monster m2 = simpleMonster(20L);
        repository.add(m1);
        repository.add(m2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(m1, m2);
    }

    @Test
    void add_putsMonsterIntoCache_andFindByIdReturnsIt() {
        Monster monster = simpleMonster(10L);

        repository.add(monster);

        assertThat(repository.findById(monster.getId())).contains(monster);
    }

    @Test
    void remove_dropsMonsterFromCache() {
        Monster monster = simpleMonster(10L);
        repository.add(monster);

        repository.remove(monster.getId());

        assertThat(repository.findById(monster.getId())).isEmpty();
    }

    @Test
    void remove_isNoOp_whenIdNotPresent() {
        repository.remove(UUID.randomUUID());

        assertThat(toList(repository.findAll())).isEmpty();
    }

    @Test
    void syncToDb_isNoOp_andDoesNotThrow() {
        repository.add(simpleMonster(10L));

        assertThatNoException().isThrownBy(() -> repository.syncToDb());

        // 캐시는 변경되지 않음 (Monster는 JPA 영속 대상이 아님)
        assertThat(toList(repository.findAll())).hasSize(1);
    }

    // --- 테스트 헬퍼 ---

    private static Monster simpleMonster(Long roomId) {
        MonsterType type = MonsterType.builder().name("dummy").baseHp(10).build();
        return Monster.createFromType(type, 1, roomId);
    }

    private static MonsterType monsterTypeWithSpawnRooms(MonsterSpawnRoom... rooms) {
        MonsterType type = MonsterType.builder().name("spawning").baseHp(10).build();
        for (MonsterSpawnRoom room : rooms) {
            type.addSpawnRoom(room);
        }
        return type;
    }

    private static MonsterSpawnRoom spawnRoom(Long roomId, int spawnCount) {
        return MonsterSpawnRoom.builder()
                .roomId(roomId)
                .spawnCount(spawnCount)
                .build();
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new java.util.ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
