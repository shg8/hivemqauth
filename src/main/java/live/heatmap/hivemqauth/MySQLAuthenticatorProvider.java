package live.heatmap.hivemqauth;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.auth.Authenticator;
import com.hivemq.extension.sdk.api.auth.SimpleAuthenticator;
import com.hivemq.extension.sdk.api.auth.parameter.AuthenticatorProviderInput;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthInput;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthOutput;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.services.auth.provider.AuthenticatorProvider;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class MySQLAuthenticatorProvider implements AuthenticatorProvider, SimpleAuthenticator {
    private final Logger log;
    private final MySQLBackend mysqlBackend;

    public MySQLAuthenticatorProvider(@NotNull Logger log, MySQLBackend mysqlBackend) {
        this.log = log;
        this.mysqlBackend = mysqlBackend;
    }

    @Override
    public @Nullable Authenticator getAuthenticator(@NotNull AuthenticatorProviderInput authenticatorProviderInput) {
        return this;
    }

    @Override
    public void onConnect(@NotNull SimpleAuthInput simpleAuthInput, @NotNull SimpleAuthOutput simpleAuthOutput) {
        ConnectPacket connect = simpleAuthInput.getConnectPacket();

        if (connect.getUserName().isEmpty() || connect.getPassword().isEmpty()) {
            simpleAuthOutput.failAuthentication();
        }

        String username = connect.getUserName().get();
        String password = StandardCharsets.UTF_8.decode(connect.getPassword().get()).toString();

        try {
            if (mysqlBackend.checkMatch(username, password)) {
                simpleAuthOutput.authenticateSuccessfully();
                return;
            }
        } catch (Exception e) {
            log.warn("Failed to execute SQL.", e);
        }
        simpleAuthOutput.failAuthentication();
    }
}
