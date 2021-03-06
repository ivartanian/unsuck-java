package awsm.domain.banking;

import awsm.domain.core.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class SendTransactionForManualApproval implements DomainEvent.SideEffect<WithdrawalHappened> {

    private final BankAccountRepository accounts;

    SendTransactionForManualApproval(BankAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public void trigger(WithdrawalHappened event) {
        var account = accounts.getOne(event.iban());
        var txUid = event.txUid();
        var tx = account.tx(txUid);

        if (tx.satisfies(new IsManualApprovalNeeded())) {
            // send for approval
        }
    }
}
