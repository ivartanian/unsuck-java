package awsm.application.trading;

import static awsm.application.trading.impl.$.$;
import static awsm.infrastructure.middleware.ReturnsNothing.NOTHING;
import static com.google.common.base.Preconditions.checkState;

import awsm.application.trading.impl.Offers;
import awsm.infrastructure.hashing.UnhashId;
import awsm.infrastructure.middleware.Command;
import awsm.infrastructure.middleware.ReturnsNothing;
import awsm.infrastructure.middleware.impl.react.Reaction;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

class AcceptOffer implements Command<ReturnsNothing> {

  private final String offerId;
  private final BigDecimal limit;

  private AcceptOffer(String offerId, BigDecimal limit) {
    this.offerId = offerId;
    this.limit = limit;
  }

  @RestController
  static class Http {
    @PostMapping("/offers/{offerId}/accept/limit/{limit}")
    ReturnsNothing accept(@PathVariable String offerId, @PathVariable BigDecimal limit) {
      return new AcceptOffer(offerId, limit).execute();
    }
  }

  @Component
  static class Re implements Reaction<AcceptOffer, ReturnsNothing> {

    private final Offers offers;

    Re(Offers offers) {
      this.offers = offers;
    }

    @Override
    public ReturnsNothing react(AcceptOffer cmd) {
      var offerId = new UnhashId(cmd.offerId).asLong();
      var offer = offers.singleById(offerId).orElseThrow();

      var limit = $(cmd.limit);
      var notReached = offer.price().isAtMost(limit);

      checkState(notReached, "Offer price is not within a limit (%s/%s)", offer.price(), cmd.limit);

      offer.accept();

      return NOTHING;
    }
  }
}