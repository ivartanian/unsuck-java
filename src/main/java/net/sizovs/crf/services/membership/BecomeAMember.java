package net.sizovs.crf.services.membership;

import lombok.Value;
import net.sizovs.crf.backbone.Command;

@Value
public class BecomeAMember implements Command<MemberId> {

    private final String email;

}
