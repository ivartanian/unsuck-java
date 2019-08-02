package lightweight4j.lib.pipeline;

import an.awesome.pipelinr.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class Tx<C extends ExecutableCommand<R>, R> implements ExecutableCommand<R> {

    private final C origin;

    public Tx(C origin) {
        this.origin = origin;
    }

    @Component
    static class Handler<R, C extends ExecutableCommand<R>> implements Command.Handler<Tx<C, R>, R> {

        private final PlatformTransactionManager txManager;

        public Handler(PlatformTransactionManager txManager) {
            this.txManager = txManager;
        }

        @Override
        public R handle(Tx<C, R> txCommand) {
            var origin = txCommand.origin;
            var tx = new TransactionTemplate(txManager);
            tx.setReadOnly(origin instanceof ReadOnly);
            return tx.execute(status -> origin.execute());
        }

    }

}
