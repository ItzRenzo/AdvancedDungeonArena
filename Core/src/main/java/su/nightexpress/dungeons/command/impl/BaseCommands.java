package su.nightexpress.dungeons.command.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.api.type.GameState;
import su.nightexpress.dungeons.command.CommandArguments;
import su.nightexpress.dungeons.command.CommandFlags;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.config.Perms;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.dungeon.Party.PartyManager;
import su.nightexpress.dungeons.dungeon.config.DungeonConfig;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.level.Level;
import su.nightexpress.dungeons.dungeon.player.SoloManager;
import su.nightexpress.dungeons.dungeon.spot.Spot;
import su.nightexpress.dungeons.dungeon.spot.SpotState;
import su.nightexpress.dungeons.dungeon.stage.Stage;
import su.nightexpress.dungeons.kit.impl.Kit;
import su.nightexpress.dungeons.selection.SelectionType;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import java.util.UUID;

import java.util.ArrayList;
import java.util.Collections;

public class BaseCommands {

    public static void load(@NotNull DungeonPlugin plugin, @NotNull HubNodeBuilder root) {
        root.branch(Commands.literal("reload")
            .description(CoreLang.COMMAND_RELOAD_DESC)
            .permission(Perms.COMMAND_RELOAD)
            .executes((context, arguments) -> {
                plugin.doReload(context.getSender());
                return true;
            })
        );

        root.branch(Commands.literal(Placeholders.ALIAS_WAND)
            .playerOnly()
            .description(Lang.COMMAND_WAND_DESC)
            .permission(Perms.COMMAND_WAND)
            .withArguments(CommandArguments.forSelectionType(plugin))
            .executes((context, arguments) -> getWand(plugin, context, arguments))
        );

        root.branch(Commands.literal("browse")
            .description(Lang.COMMAND_BROWSE_DESC)
            .permission(Perms.COMMAND_BROWSE)
            .withArguments(Arguments.player(CommandArguments.PLAYER).permission(Perms.COMMAND_BROWSE_OTHERS).optional())
            .executes((context, arguments) -> browseDungeons(plugin, context, arguments))
        );

        root.branch(Commands.literal("createparty")
                .playerOnly()
                .description(Lang.COMMAND_CREATE_PARTY_DESC)
                .permission(Perms.COMMAND_CREATE_PARTY)
                .executes((context, arguments) -> createParty(plugin, context, arguments))
        );


        root.branch(Commands.literal(Placeholders.ALIAS_JOIN)
            .description(Lang.COMMAND_JOIN_DESC)
            .permission(Perms.COMMAND_JOIN)
            .withArguments(CommandArguments.forDungeon(plugin))
            .executes((context, arguments) -> joinDungeon(plugin, context, arguments))
        );

        root.branch(Commands.literal("send")
            .description(Lang.COMMAND_SEND_DESC)
            .permission(Perms.COMMAND_SEND)
            .withArguments(
                Arguments.player(CommandArguments.PLAYER),
                CommandArguments.forDungeon(plugin),
                CommandArguments.forKit(plugin).optional()
            )
            .withFlags(CommandFlags.FORCE)
            .executes((context, arguments) -> sendToDungeon(plugin, context, arguments))
        );

        root.branch(Commands.literal("leave")
            .playerOnly()
            .description(Lang.COMMAND_LEAVE_DESC)
            .permission(Perms.COMMAND_LEAVE)
            .executes((context, arguments) -> leaveDungeon(plugin, context))
        );

        root.branch(Commands.literal("setstage")
            .playerOnly()
            .description(Lang.COMMAND_SET_STAGE_DESC)
            .permission(Perms.COMMAND_SET_STAGE)
            .withArguments(Arguments.string(CommandArguments.STAGE).localized(Lang.COMMAND_ARGUMENT_NAME_STAGE)
                .suggestions((reader, context) -> {
                    DungeonInstance instance = CommandArguments.getDungeonInstance(plugin, context);
                    return instance == null ? Collections.emptyList() : new ArrayList<>(instance.getConfig().getStageByIdMap().keySet());
                }))
            .executes((context, arguments) -> setStage(plugin, context, arguments))
        );

        root.branch(Commands.literal("setlevel")
            .playerOnly()
            .description(Lang.COMMAND_SET_LEVEL_DESC)
            .permission(Perms.COMMAND_SET_LEVEL)
            .withArguments(Arguments.string(CommandArguments.LEVEL).localized(Lang.COMMAND_ARGUMENT_NAME_LEVEL)
                .suggestions((reader, context) -> {
                    DungeonInstance instance = CommandArguments.getDungeonInstance(plugin, context);
                    return instance == null ? Collections.emptyList() : new ArrayList<>(instance.getConfig().getLevelByIdMap().keySet());
                }))
            .executes((context, arguments) -> setLevel(plugin, context, arguments))
        );

        root.branch(Commands.literal("setspot")
            .description(Lang.COMMAND_SET_SPOT_DESC)
            .permission(Perms.COMMAND_SET_SPOT)
            .withArguments(
                CommandArguments.forDungeon(plugin),
                Arguments.string(CommandArguments.SPOT).localized(Lang.COMMAND_ARGUMENT_NAME_SPOT)
                    .suggestions((reader, context) -> {
                        DungeonConfig config = CommandArguments.getDungeonConfig(plugin, context);
                        return config == null ? Collections.emptyList() : new ArrayList<>(config.getSpotByIdMap().keySet());
                    }),
                Arguments.string(CommandArguments.STATE).localized(Lang.COMMAND_ARGUMENT_NAME_STATE)
                    .suggestions((reader, context) -> {
                        Spot spot = CommandArguments.getSpot(plugin, context);
                        return spot == null ? Collections.emptyList() : new ArrayList<>(spot.getStateByIdMap().keySet());
                    })
            )
            .executes(BaseCommands::setSpotState)
        );

        root.branch(Commands.literal("start")
            .description(Lang.COMMAND_START_DESC)
            .permission(Perms.COMMAND_START)
            .withArguments(CommandArguments.forDungeon(plugin).optional())
            .executes((context, arguments) -> startGame(plugin, context, arguments))
        );

        root.branch(Commands.literal("stop")
            .description(Lang.COMMAND_STOP_DESC)
            .permission(Perms.COMMAND_STOP)
            .withArguments(CommandArguments.forDungeon(plugin).optional())
            .executes((context, arguments) -> stopGame(plugin, context, arguments))
        );


        //root.branch(Commands.literal("solomode")
                //.playerOnly()
                //.description(Lang.COMMAND_SOLOMODE_DESC)
                //.permission(Perms.COMMAND_SOLOMODE)
                //.withArguments(CommandArguments.forDungeon(plugin).optional())
                //.executes((context, arguments) -> triggerSoloMode(plugin, context, arguments))
        //);

        root.branch(Commands.literal("queue")
                .playerOnly()
                .permission("dungeons.command.queue")
                .withArguments(CommandArguments.forDungeon(plugin))
                .executes((context, arguments) -> joinQueue(plugin, context, arguments))
        );

        root.branch(Commands.literal("soloqueue")
                .playerOnly()
                .permission("dungeons.command.queue")
                .withArguments(CommandArguments.forDungeon(plugin))
                .executes((context, arguments) -> joinSoloQueue(plugin, context, arguments))
        );

        root.branch(Commands.literal("leavequeue")
                .playerOnly()
                .permission("dungeons.command.queue")
                .executes((context, arguments) -> leaveQueue(plugin, context, arguments))
        );


        root.branch(Commands.literal("invite")
                .playerOnly()
                .permission("dungeons.command.party")
                .withArguments(Arguments.player(CommandArguments.PLAYER))
                .executes((context, arguments) -> invitePlayer(plugin, context, arguments))
        );

        root.branch(Commands.literal("accept")
                .playerOnly()
                .permission("dungeons.command.party")
                .withArguments(Arguments.player(CommandArguments.PLAYER))
                .executes((context, arguments) -> acceptInvite(plugin, context, arguments))
        );

        root.branch(Commands.literal("decline")
                .playerOnly()
                .permission("dungeons.command.party")
                .withArguments(Arguments.player(CommandArguments.PLAYER))
                .executes((context, arguments) -> declineInvite(plugin, context, arguments))
        );

        root.branch(Commands.literal("partyleave")
                .playerOnly()
                .permission("dungeons.command.party")
                .executes((context, arguments) -> leaveParty(plugin, context, arguments))
        );

        root.branch(Commands.literal("kick")
                .playerOnly()
                .permission("dungeons.command.party")
                .withArguments(Arguments.player(CommandArguments.PLAYER))
                .executes((context, arguments) -> kickMember(plugin, context, arguments))
        );

        root.branch(Commands.literal("ready")
                .playerOnly()
                .permission("dungeons.command.party")
                .executes((context, arguments) -> toggleReady(plugin, context, arguments))
        );

        root.branch(Commands.literal("partyinfo")
                .playerOnly()
                .permission("dungeons.command.party")
                .executes((context, arguments) -> partyInfo(plugin, context, arguments))
        );

    }

    private static boolean getWand(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        SelectionType type = arguments.get(CommandArguments.TYPE, SelectionType.class);

        plugin.getSelectionManager().startSelection(player, type);
        return true;
    }

    private static boolean setStage(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        DungeonInstance instance = plugin.getDungeonManager().getInstance(player);
        if (instance == null) {
            context.send(Lang.DUNGEON_ERROR_MUST_BE_IN);
            return false;
        }

        String stageId = arguments.getString(CommandArguments.STAGE);
        Stage stage = instance.getConfig().getStageById(stageId);
        if (stage == null) {
            context.send(Lang.ERROR_COMMAND_INVALID_STAGE_ARGUMENT, replacer -> replacer.replace(Placeholders.GENERIC_VALUE, stageId));
            return false;
        }

        instance.setStage(stage);
        context.send(Lang.DUNGEON_ADMIN_SET_STAGE, replacer -> replacer.replace(instance.replacePlaceholders()).replace(stage.replacePlaceholders()));
        return true;
    }

    private static boolean setLevel(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        DungeonInstance instance = plugin.getDungeonManager().getInstance(player);
        if (instance == null) {
            context.send(Lang.DUNGEON_ERROR_MUST_BE_IN);
            return false;
        }

        String levelId = arguments.getString(CommandArguments.LEVEL);
        Level level = instance.getConfig().getLevelById(levelId);
        if (level == null) {
            context.send(Lang.ERROR_COMMAND_INVALID_LEVEL_ARGUMENT, replacer -> replacer.replace(Placeholders.GENERIC_VALUE, levelId));
            return false;
        }

        instance.setLevel(level);
        context.send(Lang.DUNGEON_ADMIN_SET_LEVEL, replacer -> replacer.replace(instance.replacePlaceholders()).replace(level.replacePlaceholders()));
        return true;
    }

    private static boolean setSpotState(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        DungeonConfig config = arguments.get(CommandArguments.DUNGEON, DungeonConfig.class);

        String spotId = arguments.getString(CommandArguments.SPOT);
        Spot spot = config.getSpotById(spotId);
        if (spot == null) {
            context.send(Lang.ERROR_COMMAND_INVALID_SPOT_ARGUMENT, replacer -> replacer.replace(Placeholders.GENERIC_VALUE, spotId));
            return false;
        }

        String stateId = arguments.getString(CommandArguments.STATE);
        SpotState state = spot.getState(stateId);
        if (state == null) {
            context.send(Lang.ERROR_COMMAND_INVALID_STATE_ARGUMENT, replacer -> replacer.replace(Placeholders.GENERIC_VALUE, stateId));
            return false;
        }

        DungeonInstance dungeon = config.getInstance();

        if (dungeon.getState() == GameState.INGAME) {
            dungeon.setSpotState(spot, state);
        }
        else if (dungeon.isActive()) {
            spot.build(dungeon.getWorld(), state);
        }

        context.send(Lang.DUNGEON_ADMIN_SET_SPOT, replacer -> replacer
            .replace(config.replacePlaceholders())
            .replace(spot.replacePlaceholders())
            .replace(state.replacePlaceholders())
        );
        return true;
    }

    private static boolean browseDungeons(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(CommandArguments.PLAYER) && !context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.isPlayer() ? context.getPlayerOrThrow() : arguments.getPlayer(CommandArguments.PLAYER);
        plugin.getDungeonManager().browseDungeons(player);
        return true;
    }

    private static boolean joinDungeon(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(CommandArguments.PLAYER) && !context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.isPlayer() ? context.getPlayerOrThrow() : arguments.getPlayer(CommandArguments.PLAYER);
        DungeonConfig config = arguments.get(CommandArguments.DUNGEON, DungeonConfig.class);
        plugin.getDungeonManager().prepareForInstance(player, config.getInstance());
        return true;
    }


    public static boolean createParty(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow(); // safe — .playerOnly() on the branch guards this

        PartyManager partyManager = plugin.getPartyManager();

        if (plugin.getDungeonManager().isPlaying(player)) {
            player.sendMessage("§cYou are already in a dungeon.");
            return false;
        }

        if (partyManager.hasParty(player.getUniqueId())) {
            context.send(Lang.PARTY_ERROR_ALREADY_IN_PARTY);
            return false;
        }


        partyManager.createParty(player.getUniqueId());
        context.send(Lang.PARTY_CREATED);

        partyManager.sendPartyInfo(player.getUniqueId());

        return true;
    }

    private static boolean sendToDungeon(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = arguments.getPlayer(CommandArguments.PLAYER);
        DungeonConfig config = arguments.get(CommandArguments.DUNGEON, DungeonConfig.class);
        Kit kit = arguments.contains(CommandArguments.KIT) ? arguments.get(CommandArguments.KIT, Kit.class) : null;
        boolean force = context.hasFlag(CommandFlags.FORCE);
        DungeonInstance instance = config.getInstance();

        boolean result = plugin.getDungeonManager().enterInstance(player, instance, kit, force);
        context.send((result ? Lang.DUNGEON_SEND_SENT : Lang.DUNGEON_SEND_FAIL), replacer -> replacer
            .replace(instance.replacePlaceholders())
            .replace(Placeholders.forPlayer(player))
        );
        return true;
    }

    private static boolean leaveDungeon(@NotNull DungeonPlugin plugin, @NotNull CommandContext context) {
        Player player = context.getPlayerOrThrow();

        plugin.getDungeonManager().leaveInstance(player);
        return true;
    }

    private static boolean startGame(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        DungeonInstance dungeon;

        if (arguments.contains(CommandArguments.DUNGEON)) {
            DungeonConfig config = arguments.get(CommandArguments.DUNGEON, DungeonConfig.class);
            dungeon = config.getInstance();
        }
        else {
            if (!context.isPlayer()) {
                context.errorPlayerOnly();
                return false;
            }

            Player player = context.getPlayerOrThrow();
            dungeon = plugin.getDungeonManager().getInstance(player);
            if (dungeon == null) {
                context.send(Lang.DUNGEON_ERROR_MUST_BE_IN);
                return false;
            }
        }

        if (!dungeon.isReadyToStart()) {
            context.send(Lang.DUNGEON_ERROR_NOT_READY_TO_GAME, replacer -> replacer.replace(dungeon.replacePlaceholders()));
            return false;
        }

        dungeon.setCountdown(0);
        context.send(Lang.DUNGEON_START_DONE, replacer -> replacer.replace(dungeon.replacePlaceholders()));
        return true;
    }

    private static boolean stopGame(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        DungeonInstance dungeon;

        if (arguments.contains(CommandArguments.DUNGEON)) {
            DungeonConfig config = arguments.get(CommandArguments.DUNGEON, DungeonConfig.class);
            dungeon = config.getInstance();
        }
        else {
            if (!context.isPlayer()) {
                context.errorPlayerOnly();
                return false;
            }

            Player player = context.getPlayerOrThrow();
            dungeon = plugin.getDungeonManager().getInstance(player);
            if (dungeon == null) {
                context.send(Lang.DUNGEON_ERROR_MUST_BE_IN);
                return false;
            }
        }

        if (dungeon.getState() != GameState.INGAME || dungeon.isAboutToEnd()) {
            context.send(Lang.DUNGEON_ERROR_NOT_IN_GAME, replacer -> replacer.replace(dungeon.replacePlaceholders()));
            return false;
        }

        dungeon.stop();
        context.send(Lang.DUNGEON_ADMIN_STOP, replacer -> replacer.replace(dungeon.replacePlaceholders()));
        return true;
    }


    private static boolean triggerSoloMode(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        if (plugin.getDungeonManager().isPlaying(player.getUniqueId())) {
            player.sendMessage("You're ingame, you can't toggle solo mode");
            return false;
        }


        SoloManager soloManager = plugin.getSoloManager();
        soloManager.toggleSoloOption(player.getUniqueId());

        player.sendMessage(soloManager.isSolo(player.getUniqueId())
                ? "SOLO MODE ON"
                : "SOLO MODE OFF");

        return true;
    }


    private static boolean joinSoloQueue(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        if (plugin.getDungeonManager().isPlaying(player)) {
            player.sendMessage("§cYou are already in a dungeon.");
            return false;
        }

        PartyManager partyManager = plugin.getPartyManager();
        if (partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are in a party");
            return false;
        }


        DungeonConfig config = arguments.get(CommandArguments.DUNGEON, DungeonConfig.class);
        DungeonInstance instance = config.getInstance();

        if (!instance.isActive()) {
            player.sendMessage("§cThat dungeon is not active.");
            return false;
        }

        if (instance.isInQueue(player)) {
            player.sendMessage("§cYou are already queued for this dungeon.");
            return false;
        }

        plugin.getSoloManager().makePlayerSoloOptionOn(player.getUniqueId());
        instance.addToQueue(player, null);
        player.sendMessage("§aSolo mode enabled! You joined the queue! Position: §f#" + instance.getQueuePosition(player));
        return true;
    }

    private static boolean leaveQueue(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        boolean removed = plugin.getDungeonManager().removeFromQueue(player);

        if (!removed) {
            player.sendMessage("§cYou are not in any queue.");
            return false;
        }

        player.sendMessage("§aYou left the queue.");
        return true;
    }

    private static boolean joinQueue(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (plugin.getDungeonManager().isPlaying(player)) {
            player.sendMessage("§cYou are already in a dungeon.");
            return false;
        }

        DungeonConfig config = arguments.get(CommandArguments.DUNGEON, DungeonConfig.class);
        DungeonInstance instance = config.getInstance();

        if (!instance.isActive()) {
            player.sendMessage("§cThat dungeon is not active.");
            return false;
        }

        if (partyManager.hasParty(player.getUniqueId())) {
            Party party = partyManager.getPartyOf(player.getUniqueId());

            if (!party.isLeader(player.getUniqueId())) {
                player.sendMessage("§cOnly the party leader can queue.");
                return false;
            }



            for (UUID memberId : party.getAllMembers()) {
                if (plugin.getDungeonManager().isPlaying(memberId)) {
                    player.sendMessage("§cA party member is already in a dungeon.");
                    return false;
                }
            }

            partyManager.setPendingQueue(player.getUniqueId(), config.getId());
            partyManager.resetReady(player.getUniqueId());


            // Party leader auto ready since he started it
            if (party.isLeader(player.getUniqueId())) {
                toggleReady(plugin, context, arguments);
            }

            player.sendMessage("§aReady request sent to your party!");
            partyManager.broadcastToParty(party, "§eLeader wants to queue for §f" + config.getId() + "§e. Type §f/dungeon ready §eto confirm!");
            return true;
        }

        if (instance.isInQueue(player)) {
            player.sendMessage("§cYou are already queued for this dungeon.");
            return false;
        }

        instance.addToQueue(player, null);
        player.sendMessage("§aYou joined the queue! Position: §f#" + instance.getQueuePosition(player));
        return true;
    }

    private static boolean invitePlayer(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (!partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return false;
        }

        Party party = partyManager.getPartyOf(player.getUniqueId());
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can invite players.");
            return false;
        }

        Player target = arguments.getPlayer(CommandArguments.PLAYER);

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot invite yourself.");
            return false;
        }

        if (partyManager.hasParty(target.getUniqueId())) {
            player.sendMessage("§cThat player is already in a party.");
            return false;
        }

        if (plugin.getDungeonManager().isPlaying(target)) {
            player.sendMessage("§cThat player is currently in a dungeon.");
            return false;
        }

        if (party.hasPendingInvite(target.getUniqueId())) {
            player.sendMessage("§cThat player already has a pending invite.");
            return false;
        }

        partyManager.invitePlayer(player.getUniqueId(), target.getUniqueId());
        player.sendMessage("§aInvite sent to §f" + target.getName() + "§a.");
        return true;
    }

    private static boolean acceptInvite(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (!partyManager.hasPendingInvite(player.getUniqueId())) {
            player.sendMessage("§cYou have no pending party invites.");
            return false;
        }

        if (partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are already in a party.");
            return false;
        }

        Player leader = arguments.getPlayer(CommandArguments.PLAYER);
        partyManager.acceptInvite(player.getUniqueId(), leader.getUniqueId());
        return true;
    }

    private static boolean declineInvite(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (!partyManager.hasPendingInvite(player.getUniqueId())) {
            player.sendMessage("§cYou have no pending party invites.");
            return false;
        }

        Player leader = arguments.getPlayer(CommandArguments.PLAYER);
        partyManager.declineInvite(player.getUniqueId(), leader.getUniqueId());
        return true;
    }

    private static boolean leaveParty(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (!partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return false;
        }

        partyManager.leaveParty(player.getUniqueId());
        return true;
    }

    private static boolean kickMember(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (!partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return false;
        }

        Party party = partyManager.getPartyOf(player.getUniqueId());
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can kick members.");
            return false;
        }

        Player target = arguments.getPlayer(CommandArguments.PLAYER);

        if (!party.isMember(target.getUniqueId())) {
            player.sendMessage("§cThat player is not in your party.");
            return false;
        }

        if (party.isLeader(target.getUniqueId())) {
            player.sendMessage("§cYou cannot kick yourself.");
            return false;
        }

        partyManager.kickMember(player.getUniqueId(), target.getUniqueId());
        return true;
    }

    private static boolean toggleReady(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (!partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return false;
        }

        partyManager.toggleReady(player.getUniqueId());

        Party party = partyManager.getPartyOf(player.getUniqueId());

        if (partyManager.isPartyReady(party.getLeader())) {
            String dungeonId = partyManager.getPendingQueue(party.getLeader());
            if (dungeonId != null) {
                DungeonInstance instance = plugin.getDungeonManager().getInstanceById(dungeonId);
                if (instance != null) {
                    partyManager.broadcastToParty(party, "§aAll members ready! Joining queue for §f" + dungeonId + "§a.");
                    instance.addPartyToQueue(party, null);
                    partyManager.clearPendingQueue(party.getLeader());
                    partyManager.resetReady(party.getLeader());
                }
            }
        }

        return true;
    }

    private static boolean partyInfo(@NotNull DungeonPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        PartyManager partyManager = plugin.getPartyManager();

        if (!partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return false;
        }

        partyManager.sendPartyInfo(player.getUniqueId());
        return true;
    }


}
