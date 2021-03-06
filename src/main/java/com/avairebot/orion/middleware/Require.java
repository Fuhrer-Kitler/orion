package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.AbstractMiddleware;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.permissions.Permissions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Require extends AbstractMiddleware {

    public Require(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return stack.next();
        }

        List<Permissions> missingPermissions = new ArrayList<>();

        for (String permissionNode : args) {
            Permissions permission = Permissions.fromNode(permissionNode);
            if (permission == null) {
                orion.logger.warning("Invalid permission node given for the \"%s\" command: %s", stack.getCommand().getName(), permissionNode);
                return false;
            }

            if (!message.getMember().hasPermission(permission.getPermission())) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            MessageFactory.makeError(
                    message,
                    "You're missing the required permission node for this command:\n`%s`",
                    missingPermissions.stream()
                            .map(Permissions::getPermission)
                            .map(Permission::getName)
                            .collect(Collectors.joining("`, `"))
            ).queue();
            return true;
        }

        return stack.next();
    }
}
