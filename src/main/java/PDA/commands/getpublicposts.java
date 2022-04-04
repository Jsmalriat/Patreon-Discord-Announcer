package PDA.commands;

import PDA.PDA;
import PDA.apis.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import org.openqa.selenium.WebElement;

public class getpublicposts implements BotCommand {

	Guild guild;

	@Override
	public void execute(DiscordBot bot) {
		bot.setTitle("Public Posts:", guild);

		for (WebElement currentPost : PDA.publicPosts) {
			bot.setDescription(currentPost.getText(), guild);
			bot.send(guild);
		}
		bot.clearEmbed(guild);
	}

	@Override
	public void setArgs(String[] args) {

	}

	@Override
	public void setGuildID(Guild guild) {
		this.guild = guild;
	}
}