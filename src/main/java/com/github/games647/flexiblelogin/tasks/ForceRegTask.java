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
package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.storage.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

public class ForceRegTask implements Runnable {

    private final FlexibleLogin plugin;

    private final CommandSource src;
    private final UUID accountIdentifier;
    private final String password;

    public ForceRegTask(FlexibleLogin plugin, CommandSource src, UUID accountIdentifier, String password) {
        this.plugin = plugin;
        this.src = src;
        this.accountIdentifier = accountIdentifier;
        this.password = password;
    }

    @Override
    public void run() {
        Optional<Account> optAccount = plugin.getDatabase().loadAccount(accountIdentifier);

        if (optAccount.isPresent()) {
            src.sendMessage(plugin.getConfigManager().getText().getAccountAlreadyExists());
        } else {
            try {
                String hash = plugin.getHasher().hash(password);
                Account account = new Account(accountIdentifier, "", hash, null);
                plugin.getDatabase().createAccount(account);

                if (Sponge.getServer().getPlayer(accountIdentifier).isPresent()) {
                    plugin.getDatabase().addCache(accountIdentifier, account);
                }

                src.sendMessage(plugin.getConfigManager().getText().getForceRegisterSuccess());
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash", ex);
                src.sendMessage(plugin.getConfigManager().getText().getErrorExecutingCommand());
            }
        }
    }
}
