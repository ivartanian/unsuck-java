package awsm.infrastructure.clock;

import java.time.Clock;
import java.time.LocalDate;

public class TimeMachine {

  private static final ThreadLocal<Clock> clock = ThreadLocal.withInitial(Clock::systemUTC);

  public static Clock clock() {
    return clock.get();
  }

  public static void set(Clock clock) {
    TimeMachine.clock.set(clock);
  }

  public static LocalDate today() {
    return LocalDate.now(clock());
  }

}
