/*
 * Copyright © 2019 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.sros.init;

import com.google.common.base.Preconditions;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SrosCliInitializationStrategy implements SessionInitializationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(SrosCliInitializationStrategy.class);

    private static final String PRIVILEGED_PROMPT_SUFFIX = "#";
    private static final Pattern CONFIG_PROMPT_SUFFIX_PATTERN = Pattern.compile("^.*>edit-cfg.*#.*$");
    private static final String SET_TERMINAL_LENGTH_COMMAND = "environment no more";

    private static final int READ_TIMEOUT_SECONDS = 1;
    private static final int WRITE_TIMEOUT_SECONDS = 10;

    private static final Command CONFIG_COMMAND = Command.writeCommand("candidate edit exclusive");
    private static final Command ABORT_COMMAND = Command.writeCommand("candidate discard now");
    private static final String COMMIT = "candidate commit no-checkpoint";
    private static final Command ADMIN_SAVE_COMMAND = Command.writeCommand("admin save");

    private static final Predicate<String> IS_CONFIGURATION_PROMPT =
        s -> CONFIG_PROMPT_SUFFIX_PATTERN.matcher(s).matches();
    private static final Predicate<String> IS_PRIVELEGE_PROMPT =
        s -> s.endsWith(PRIVILEGED_PROMPT_SUFFIX) && !IS_CONFIGURATION_PROMPT.test(s);

    @Override
    public void accept(Session session, String newline) {
        try {

            String prompt = SrosPromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();

            Preconditions.checkState(IS_PRIVELEGE_PROMPT.test(prompt),
                    "%s: SROS cli session initialization failed to enter cli mode. Current prompt: %s",
                    session, prompt);

            LOG.debug("{}: Disable pager to prevent \"Press any key to continue (Q to quit)\" situation", session);
            write(session, newline, SET_TERMINAL_LENGTH_COMMAND);
            session.readUntilTimeout(READ_TIMEOUT_SECONDS);

            LOG.info("{}: SROS cli session initialized successfully", session);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (SessionException | ExecutionException | TimeoutException e) {
            LOG.warn("{}: Unable to initialize device", session, e);
            throw new IllegalStateException(session + ": Unable to initialize device", e);
        }
    }

    static void tryToEnterConfigurationMode(Cli cli)
            throws InterruptedException, ExecutionException, TimeoutException {

        LOG.debug("{}: Entering configuration mode.", cli);
        cli.executeAndSwitchPrompt(CONFIG_COMMAND, IS_CONFIGURATION_PROMPT)
                .toCompletableFuture()
                .get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    static void tryToAbort(Cli cli)
            throws InterruptedException, ExecutionException, TimeoutException {

        LOG.debug("{}: Executing rollback command and exit configuration mode.", cli);
        cli.executeAndSwitchPrompt(ABORT_COMMAND, IS_PRIVELEGE_PROMPT)
                .toCompletableFuture()
                .get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    static void tryToCommitAndSave(Cli cli, Set<Pattern> errorCommitPatterns)
            throws InterruptedException, ExecutionException {

        LOG.debug("{}: Executing commit command.", cli);
        Command commit = Command.writeCommandCustomChecks(COMMIT, errorCommitPatterns);
        cli.executeAndSwitchPrompt(commit, IS_PRIVELEGE_PROMPT)
                .toCompletableFuture()
                .get();

        LOG.debug("{}: Executing admin save command.", cli);
        cli.executeAndSwitchPrompt(ADMIN_SAVE_COMMAND, IS_PRIVELEGE_PROMPT)
                .toCompletableFuture()
                .get();
    }
}