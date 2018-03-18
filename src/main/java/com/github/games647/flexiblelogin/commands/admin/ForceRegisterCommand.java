/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.flexiblelogin.commands.admin;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.commands.AbstractCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.storage.Account;
import com.github.games647.flexiblelogin.tasks.ForceRegTask;
import com.github.games647.flexiblelogin.validation.NamePredicate;
import com.github.games647.flexiblelogin.validation.UUIDPredicate;
import com.google.inject.Inject;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class ForceRegisterCommand extends AbstractCommand {

    @Inject
    private UUIDPredicate uuidPredicate;

    @Inject
    private NamePredicate namePredicate;

    @Inject
    ForceRegisterCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        String accountId = args.<String>getOne("account").get();
        String password = args.<String>getOne("password").get();
        if (uuidPredicate.test(accountId)) {
            onUuidRegister(accountId, src, password);

            return CommandResult.success();
        } else if (namePredicate.test(accountId)) {
            onNameRegister(src, accountId, password);
            return CommandResult.success();
        }

        return CommandResult.success();
    }

    private void onNameRegister(CommandSource src, String accountId, String password) {
        Optional<Player> player = Sponge.getServer().getPlayer(accountId);
        if (player.isPresent()) {
            src.sendMessage(settings.getText().getForceRegisterOnline());
        } else {
            UUID offlineUUID = Account.getOfflineUUID(accountId);

            Task.builder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new ForceRegTask(plugin, src, offlineUUID, password))
                    .submit(plugin);
        }
    }

    private void onUuidRegister(String accountId, CommandSource src, String password) {
        //check if the account is an UUID
        UUID uuid = UUID.fromString(accountId);
        Optional<Player> player = Sponge.getServer().getPlayer(uuid);
        if (player.isPresent()) {
            src.sendMessage(settings.getText().getForceRegisterOnline());
        } else {
            Task.builder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new ForceRegTask(plugin, src, uuid, password))
                    .submit(plugin);
        }
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(
                        onlyOne(
                                string(of("account"))), string(of("password")))
                .build();
    }
}

