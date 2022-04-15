package PDA.commands;

import PDA.PDA;
import PDA.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;

public class removelink implements BotCommand {

	private String[] args = null;
	private Guild guild;

	@Override
	public void execute(DiscordBot bot) {
		if (args == null) {
			System.out.println("no arguments provided");
			return;
		}

		if (args.length <= 1) {
			bot.send("No link provided", guild);
		} else {

			ArrayList<Guild> guilds;
			if ((guilds = PDA.patreonUrls.get(args[1])) == null){
				bot.send(args[1] + " is not in the list of links", guild);
				return;
			}

			if (PDA.patreonUrls.containsKey(args[1]) && guilds.remove(guild)) {
				PDA.patreonUrls.put(args[1], guilds);
				bot.send(args[1] + " has been removed from the patreon link list", guild);
			}
		}
	}

	@Override
	public void setArgs(String[] args) {
		this.args = args;
	}

	@Override
	public void setGuildID(Guild guild) {
		this.guild = guild;
	}
}
