package awsm.infrastructure.middleware.impl.scheduler;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.EVERYTHING;
import static com.machinezoo.noexception.Exceptions.sneak;
import static java.time.ZoneOffset.UTC;
import static jooq.tables.ScheduledCommand.SCHEDULED_COMMAND;

import awsm.infrastructure.middleware.Command;
import awsm.infrastructure.middleware.CommandMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import jooq.tables.records.ScheduledCommandRecord;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

class ScheduledCommand {

  private static final ObjectMapper mapper = JsonMapper
      .builder()
      .activateDefaultTyping(BasicPolymorphicTypeValidator
          .builder()
          .allowIfBaseType(Command.class)
          .build(), EVERYTHING)
      .visibility(FIELD, ANY)
      .build();


  private final LocalDateTime creationDate;

  private final Command command;

  private final String commandId;

  private Optional<Long> id = Optional.empty();

  ScheduledCommand(Command command) {
    this(LocalDateTime.now(UTC), command);
  }

  private ScheduledCommand(LocalDateTime creationDate, Command command) {
    this.creationDate = creationDate;
    this.command = command;
    this.commandId = command.id();
  }

  void saveNew(Repository repository) {
    repository.insert(this);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SIMPLE_STYLE);
  }

  @Component
  static class Repository {

    private final DSLContext dsl;
    private final CommandMeta commandMeta;

    Repository(DSLContext dsl, CommandMeta commandMeta) {
      this.dsl = dsl;
      this.commandMeta = commandMeta;
    }

    Stream<Runnable> list(long limit) {
      return dsl
          .selectFrom(SCHEDULED_COMMAND)
          .limit(limit)
          .forUpdate()
          .fetchStream()
          .map(fromJooq())
          .map(this::runnable);
    }

    private Runnable runnable(ScheduledCommand self) {
      return () -> {
        self.command.execute();
        delete(self);
      };
    }

    private Function<ScheduledCommandRecord, ScheduledCommand> fromJooq() {
      return jooq -> {
        var commandId = jooq.getCommandId();
        var cmd = sneak().get(() -> mapper.readValue(jooq.getCommand(), commandMeta.typeOf(commandId)));
        var self = new ScheduledCommand(jooq.getCreationDate(), cmd);
        self.id = Optional.of(jooq.getId());
        return self;
      };
    }

    private void insert(ScheduledCommand self) {
      var id = dsl
          .insertInto(SCHEDULED_COMMAND)
            .set(SCHEDULED_COMMAND.CREATION_DATE, self.creationDate)
            .set(SCHEDULED_COMMAND.COMMAND_ID, self.commandId)
            .set(SCHEDULED_COMMAND.COMMAND, sneak().get(() -> mapper.writeValueAsString(self.command)))
            .returning(SCHEDULED_COMMAND.ID)
            .fetchOne()
            .getId();
      self.id = Optional.of(id);
    }

    private void delete(ScheduledCommand self) {
      dsl
          .deleteFrom(SCHEDULED_COMMAND)
          .where(SCHEDULED_COMMAND.ID.equal(self.id.orElseThrow()))
          .execute();
    }
  }

}
