package awsm.infrastructure.middleware.impl.scheduler;

import static java.util.concurrent.CompletableFuture.allOf;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class BatchOfScheduledCommands {

  private static final int BATCH_SIZE = 10;

  private static final Executor THREAD_POOL = Executors.newFixedThreadPool(BATCH_SIZE);

  private final ScheduledCommand.Repository repository;

  private final PlatformTransactionManager transactionManager;

  public BatchOfScheduledCommands(ScheduledCommand.Repository repository, PlatformTransactionManager transactionManager) {
    this.repository = repository;
    this.transactionManager = transactionManager;
  }

  @Transactional(readOnly = true)
  @Scheduled(initialDelay = 5000, fixedDelay = 5000)
  public void runAndWaitForAll() {
    allOf(
        run().toArray(CompletableFuture[]::new)
    ).join();
  }

  private Stream<CompletableFuture> run() {
    return repository
        .list(BATCH_SIZE)
        .map(this::wrapInATx)
        .map(this::runInPool);
  }

  private Runnable wrapInATx(Runnable runnable) {
    var newTx = new TransactionTemplate(transactionManager);
    newTx.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
    return () -> newTx.executeWithoutResult(txStatus -> runnable.run());
  }

  private CompletableFuture<Void> runInPool(Runnable runnable) {
    return CompletableFuture.runAsync(runnable, THREAD_POOL);
  }


}
