package awsm.application.registration.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.testing.EqualsTester;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("an email")
class EmailTest {

  @Test
  void rejects_blank_values() {
    assertThrows(IllegalArgumentException.class, () -> uniqueAndNotBlacklisted(" "));
  }

  @Test
  void can_be_turned_to_string() {
    var email = uniqueAndNotBlacklisted("whatever@email.com");
    assertThat(email + "").isEqualTo("whatever@email.com");
  }

  @Test
  void has_proper_equals_and_hash_code() {
    new EqualsTester()
        .addEqualityGroup(uniqueAndNotBlacklisted("one@domain.com"), uniqueAndNotBlacklisted("one@domain.com"))
        .addEqualityGroup(uniqueAndNotBlacklisted("another@domain.com"))
        .testEquals();
  }

  private Email uniqueAndNotBlacklisted(String email) {
    return new Email(email, Email.Uniqueness.ALWAYS_GUARANTEED, Email.Blacklist.ALWAYS_ALLOWS);
  }

  @Nested
  @DisplayName("external blacklist")
  static class ExternalBlacklistFailoverTest {

    @Test
    void timeouts_and_allows_all_emails_if_host_is_unreachable() {
      var errorThreshold = ofSeconds(1);
      var blacklist = new Email.Blacklist.External("http://unreachable.host", 9999);
      await()
          .atMost(blacklist.timeout().plus(errorThreshold))
          .until(() -> blacklist.allows(anyEmail()));
    }

    @Test
    void short_circuits_after_four_timeouts() {
      var wireMock = new WireMockServer(wireMockConfig().dynamicPort().dynamicPort());

      // make the server slow
      wireMock
          .stubFor(get(anyUrl())
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withFixedDelay(5000)));
      wireMock.start();

      var blacklist = new Email.Blacklist.External("http://localhost", wireMock.port());

      // circuit must open after 4 exceptions
      performNumberOfEmailChecks(blacklist, 10);
      wireMock.verify(4, allRequests());

      // ok, let's check again
      blacklist.halfOpenCircuit();

      // still failing
      // circuit must open after 4 exceptions
      performNumberOfEmailChecks(blacklist, 10);
      wireMock.verify(8, allRequests());

      // fix the server
      wireMock
          .stubFor(get(anyUrl())
              .willReturn(
                  aResponse()
                      .withStatus(200)));

      // check after the fix
      blacklist.halfOpenCircuit();

      // not failing anymore
      performNumberOfEmailChecks(blacklist, 10);
      wireMock.verify(18, allRequests());

    }

    private void performNumberOfEmailChecks(Email.Blacklist blacklist, int count) {
      Stream.generate(() -> anyEmail()).limit(count).forEach(email -> blacklist.allows(email));
    }

  }

  private static String anyEmail() {
    return new Faker().internet().emailAddress();
  }

}