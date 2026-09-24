package com.chat.aj.expensetracker.common.Utility;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class AfterCommitTest {
    @AfterEach
    void clear() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void runsImmediatelyWhenThereIsNoTransaction() {
        AtomicBoolean ran = new AtomicBoolean();
        AfterCommit.run(() -> ran.set(true));
        assertThat(ran).isTrue();
    }

    @Test
    void waitsUntilTheTransactionCommits() {
        TransactionSynchronizationManager.initSynchronization();
        AtomicBoolean ran = new AtomicBoolean();

        AfterCommit.run(() -> ran.set(true));
        assertThat(ran).isFalse();

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCommit());
        assertThat(ran).isTrue();
    }
}
