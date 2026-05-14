package com.jefflife.mudmk2.gameplay.application.service.sync;

/**
 * 인메모리 상태를 DB에 일괄 저장하는 컨트랙트.
 * 구현체는 자신의 syncToDb()에 @Transactional을 직접 부여한다.
 * 한 구현체의 실패가 다른 구현체로 전파되지 않도록 트랜잭션은 구현체별로 끊긴다.
 * <p>
 * GameWorldScheduler.persist() 가 60초마다 모든 BatchSyncable 빈을 호출한다.
 */
public interface BatchSyncable {
    void syncToDb();
}
