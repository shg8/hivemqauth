
/*
 * Copyright 2018 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package live.heatmap.hivemqauth;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.parameter.*;
import com.hivemq.extension.sdk.api.services.Services;
import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * This is the main class of the extension,
 * which is instantiated either during the HiveMQ start up process (if extension is enabled)
 * or when HiveMQ is already started by enabling the extension.
 *
 * @author Florian Limpöck
 * @since 4.0.0
 */
public class HiveMQAuthMain implements ExtensionMain {

    private static final @NotNull Logger log = LoggerFactory.getLogger(HiveMQAuthMain.class);
    private MySQLBackend mysqlBackend;

    @Override
    public void extensionStart(final @NotNull ExtensionStartInput extensionStartInput, final @NotNull ExtensionStartOutput extensionStartOutput) {

        try {

            File directory = new File(HiveMQAuthMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File mysql = new File(directory, "mysql.properties");
            File config = new File(directory, "config.properties");
            if (createConfig(mysql) | createConfig(config)) {
                log.error("Configuration copied to extension directory. Please edit the configuration and restart.");
                System.exit(0);
            } else {
                Properties properties = new Properties();
                properties.load(FileUtils.openInputStream(config));
                this.mysqlBackend = new MySQLBackend(properties, new HikariConfig(config.getAbsolutePath()));
            }

            Services.securityRegistry().setAuthenticatorProvider(new MySQLAuthenticatorProvider(log, mysqlBackend));

            final ExtensionInformation extensionInformation = extensionStartInput.getExtensionInformation();
            log.info("Started " + extensionInformation.getName() + ":" + extensionInformation.getVersion());

        } catch (Exception e) {
            log.error("Exception thrown at extension start: ", e);
        }

    }

    private boolean createConfig(File config) throws IOException {
        if (!config.exists()) {
            FileUtils.copyToFile(getClass().getResourceAsStream("/mysql.properties"), config);
            return true;
        }
        return false;
    }

    @Override
    public void extensionStop(final @NotNull ExtensionStopInput extensionStopInput, final @NotNull ExtensionStopOutput extensionStopOutput) {

        final ExtensionInformation extensionInformation = extensionStopInput.getExtensionInformation();
        log.info("Stopped " + extensionInformation.getName() + ":" + extensionInformation.getVersion());

    }

}
