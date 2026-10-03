package com.example.demo.framework.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 基于 Spring 命令式事务的提交后回调工具，不创建事务、不切换线程、不自动重试。
 * <p>
 * 回调跟随注册时的事务：提交成功后同步执行，回滚时不执行。
 * 回调执行时数据库已经提交，回调失败不能撤销已提交的数据。
 * 回调如需写数据库，应通过另一个 Spring Bean 开启 REQUIRES_NEW 事务。
 */
public final class TransactionUtil {

    private TransactionUtil() {
    }

    /**
     * 注册提交后回调。加入外层事务时，等待外层事务提交。
     * <p>
     * 回调的运行时异常会向调用方传播，也可能阻止后续提交回调执行。
     * 普通通知可使用带异常处理器的重载，明确处理发送失败。
     * 应在注册前取得 ID、消息内容等快照，避免回调依赖后续被修改的对象。
     *
     * @param action 提交成功后执行的操作，不允许为 null
     * @throws NullPointerException action 为 null
     * @throws IllegalStateException 当前没有活动事务或事务同步未启用
     */
    public static void afterCommit(Runnable action) {
        Objects.requireNonNull(action, "action 不能为空");
        requireTransaction();

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
    }

    /**
     * 注册提交后回调，并将回调的运行时异常交给指定处理器。
     * <p>
     * 处理器正常返回时，原异常不再传播；处理器自身的异常仍会传播。
     * 不捕获 Error，不处理注册阶段的异常，也不提供持久化或重试保证。
     * Runnable 不支持受检异常，调用方需在 action 内自行处理。
     *
     * @param action 提交成功后执行的操作，不允许为 null
     * @param onFailure 回调失败后的处理器，可记录日志、指标或安排补偿
     * @throws NullPointerException action 或 onFailure 为 null
     * @throws IllegalStateException 当前没有活动事务或事务同步未启用
     */
    public static void afterCommit(Runnable action, Consumer<RuntimeException> onFailure) {
        Objects.requireNonNull(action, "action 不能为空");
        Objects.requireNonNull(onFailure, "onFailure 不能为空");

        afterCommit(() -> {
            try {
                action.run();
            } catch (RuntimeException e) {
                onFailure.accept(e);
            }
        });
    }

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
            || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("afterCommit 必须在启用事务同步的活动事务中调用");
        }
    }
}
