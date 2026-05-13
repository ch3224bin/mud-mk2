package com.jefflife.mudmk2.gameplay.application.service.sync;

/**
 * 인메모리 상태를 DB에 일괄 저장하는 컨트랙트.
 * 구현체는 자신의 syncToDb()에 @Transactional을 직접 부여한다.
 * 한 구현체의 실패가 다른 구현체로 전파되지 않도록 트랜잭션은 구현체별로 끊긴다.
 * <p>
 * 마이그레이션 노트: 기존 *SyncService 구현체는 후속 단계에서 InMemory*Repository로 흡수되며,
 * 그 시점에 본 계약을 완전히 따른다. 그 전까지는 PersistenceManager.persistGameState()의
 * 외부 @Transactional이 임시로 트랜잭션 경계를 제공한다.
 */
public interface BatchSyncable {
    void syncToDb();
}
