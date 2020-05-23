package com.fast.utils.lock;

/**
 * 锁基类
 *
 * @author 张亚伟 https://github.com/kaixinzyw
 */
public abstract class BaseLock {

    /**
     * 锁key
     */
    protected final String lockKey;

    public BaseLock(String lockKey) {
        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }

    /**
     * 释放锁
     */
    public abstract  void release();

    /**
     * @return 检测锁是否存在,此查询为实时查询
     */
    public Boolean lockIsExist() {
        return FastRedisLock.lockIsExist(lockKey);
    }
}
