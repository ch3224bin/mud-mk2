package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryRoomRepositoryTest {

    @Mock private RoomRepository jpa;
    private InMemoryRoomRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRoomRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllRoomsFromJpa_andInitializesAssociations() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        when(room1.getId()).thenReturn(1L);
        when(room2.getId()).thenReturn(2L);
        when(jpa.findAll()).thenReturn(List.of(room1, room2));

        repository.bootstrap();

        verify(room1).initializeAssociatedEntities();
        verify(room2).initializeAssociatedEntities();
        assertThat(repository.findById(1L)).contains(room1);
        assertThat(repository.findById(2L)).contains(room2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedRooms() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        when(room1.getId()).thenReturn(1L);
        when(room2.getId()).thenReturn(2L);
        when(jpa.findAll()).thenReturn(List.of(room1, room2));
        repository.bootstrap();

        assertThat(repository.findAll()).containsExactlyInAnyOrder(room1, room2);
    }

    @Test
    void remove_dropsTheRoomFromCache() {
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(1L);
        when(jpa.findAll()).thenReturn(List.of(room));
        repository.bootstrap();

        repository.remove(1L);

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void syncToDb_savesCachedRoomsViaJpa() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        when(room1.getId()).thenReturn(1L);
        when(room2.getId()).thenReturn(2L);
        when(jpa.findAll()).thenReturn(List.of(room1, room2));
        repository.bootstrap();

        repository.syncToDb();

        verify(jpa).saveAll(argThat(rooms -> {
            List<Room> list = new ArrayList<>();
            rooms.forEach(list::add);
            return list.size() == 2 && list.contains(room1) && list.contains(room2);
        }));
    }

    @Test
    void add_putsRoomIntoCache() {
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(42L);

        repository.add(room);

        assertThat(repository.findById(42L)).contains(room);
    }
}
