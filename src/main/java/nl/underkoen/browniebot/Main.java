package nl.underkoen.browniebot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import nl.underkoen.browniebot.utils.FileUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Under_Koen on 01/04/2020.
 */
public class Main implements EventListener {
    public static final char PREFIX = '\\';
    public static final long OWNER = 223083828367851521L;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) return;
        new Main(args[0]);
    }

    private BrowniePointLedger ledger;
    private Map<String, Consumer<MessageReceivedEvent>> subCommands = Map.of(
            "give", (e) -> {
                User self = e.getAuthor();
                boolean isOwner = self.getIdLong() == OWNER;
                String[] pieces = e.getMessage().getContentRaw().split(" ");
                List<User> mentions = e.getMessage().getMentionedUsers();
                if (mentions.size() == 1) {
                    User who = mentions.get(0);
                    for (int i = 2; i < pieces.length; i++) {
                        try {
                            int amount = Integer.parseInt(pieces[i]);
                            if (amount > 0 && ledger.getPoints(self) >= amount || isOwner) {
                                if (!isOwner) ledger.removePoints(self, amount);
                                ledger.addPoints(who, amount);
                                sendMessage(String.format("You gave %s **%s** Brownie Points!", who.getAsMention(), amount), e.getChannel(), e.getAuthor());
                            } else {
                                sendMessage(String.format("You do not have enough Brownie Points to give %s **%s** Brownie Points!", who.getAsMention(), amount), e.getChannel(), e.getAuthor());
                            }
                            return;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                sendMessage("This message needs a mention and a number!", e.getChannel(), e.getAuthor());
            },
            "help", (e) -> {
                sendMessage("**Commands**" +
                        "\n - \\bp" +
                        "\n - \\bp give" +
                        "\n - \\bp leaderboard", e.getChannel(), e.getAuthor());
            },
            "leaderboard", (e) -> {
                List<Map.Entry<String, Integer>> leaderBoard = ledger.getLedger().points.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(5)
                        .collect(Collectors.toList());

                MessageChannel channel = e.getChannel();
                User user = e.getAuthor();
                JDA jda = e.getJDA();

                EmbedBuilder msg = new EmbedBuilder();
                msg.setColor(new Color(137, 23, 12));
                msg.setTitle("Brownie Points");

                int i = 0;
                for (Map.Entry<String, Integer> entry : leaderBoard) {
                    User u = jda.getUserById(entry.getKey());
                    if (u == null) u = jda.retrieveUserById(entry.getKey()).complete();
                    if (u == null) continue;
                    msg.addField(String.format("**#%s**", ++i), String.format("%s - **%s**", u.getAsMention(), entry.getValue()), true);
                }

                msg.setFooter(user.getName(), user.getAvatarUrl());
                channel.sendMessage(msg.build()).complete();
            }
    );

    public Main(String key) throws Exception {
        JDA jda = JDABuilder.createDefault(key).build();
        jda.addEventListener(this);
        jda.getPresence().setActivity(Activity.watching(PREFIX + "bp help"));

        ledger = new BrowniePointLedger(getLedgerFile());
        ledger.read();
    }

    private File getLedgerFile() throws IOException {
        File file = new File(FileUtil.getRunningDir(), "ledger.json");
        if (!file.exists()) {
            if (!file.createNewFile()) System.exit(-1);
            FileUtil.writeContent(file, "{}");
        }
        return file;
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent received = (MessageReceivedEvent) event;
            String message = received.getMessage().getContentRaw();
            //valid command
            if (message.length() > 1 && message.charAt(0) == PREFIX) {
                String[] pieces = message.substring(1).split(" ");
                if ("bp".equals(pieces[0].toLowerCase())) {
                    if (pieces.length > 1) {
                        Consumer<MessageReceivedEvent> subCommand = subCommands.get(pieces[1].toLowerCase());
                        if (subCommand != null) {
                            subCommand.accept(received);
                            return;
                        }
                    }
                    List<User> mentions = received.getMessage().getMentionedUsers();
                    if (mentions.size() == 1) {
                        User who = mentions.get(0);
                        int points = ledger.getPoints(who);
                        sendMessage(String.format("%s has **%s** Brownie Points!", who.getAsMention(), points), received.getChannel(), received.getAuthor());
                    } else {
                        int points = ledger.getPoints(received.getAuthor());
                        sendMessage(String.format("You have **%s** Brownie Points!", points), received.getChannel(), received.getAuthor());
                    }
                }
            }
        }
    }

    public void sendMessage(String message, MessageChannel channel, User user) {
        EmbedBuilder msg = new EmbedBuilder();
        msg.setColor(new Color(137, 23, 12));
        msg.setTitle("Brownie Points");
        msg.setDescription(message);
        msg.setFooter(user.getName(), user.getAvatarUrl());
        channel.sendMessage(msg.build()).complete();
    }
}
