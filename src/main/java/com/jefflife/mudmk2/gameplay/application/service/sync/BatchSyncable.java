package com.jefflife.mudmk2.gameplay.application.service.sync;

/**
 * 인메모리 상태를 DB에 일괄 저장하는 컨트랙트.
 * PersistenceManager의 @Scheduled 배치에서 호출되므로 구현체는 별도 @Transactional 불필요.
 * 예외 발생 시 해당 배치 전체가 롤백되므로 구현체는 멱등성을 보장해야 한다.
 */
public interface BatchSyncable {
    void syncToDb();
}
