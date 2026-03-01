package org.yomirein.sochatserver.chats;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.utils.KeyParser;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO  {
    private long id;
    private String username;
}

