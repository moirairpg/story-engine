package me.moirai.storyengine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractDiscordTest {

    protected static final String CHANNEL_ID = "CHID";
    protected static final String GUILD_ID = "GDID";
    protected static final String MESSAGE_ID = "MSGID";
    protected static final String NICKNAME = "nickname";
    protected static final String USERNAME = "user.name";
    protected static final String USER_ID = "USRID";

    @Mock
    protected SelfUser selfUser;

    @Mock
    protected User user;

    @Mock
    protected Member member;

    @Mock
    protected Guild guild;

    @Mock
    protected Message message;

    @Mock
    protected TextChannel textChannel;

    @Mock
    protected MessageChannelUnion channelUnion;

    @Mock
    protected JDA jda;

    @BeforeEach
    public void setUpBaseMocks() {

        CacheRestAction<Member> guildMemberRetrievalAction = mock(CacheRestAction.class);

        when(guildMemberRetrievalAction.complete()).thenReturn(member);
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(textChannel.getId()).thenReturn(CHANNEL_ID);
        when(guild.retrieveMember(any())).thenReturn(guildMemberRetrievalAction);
        when(guild.getId()).thenReturn(GUILD_ID);
        when(channelUnion.getId()).thenReturn(CHANNEL_ID);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(user.getId()).thenReturn(USER_ID);
        when(user.getName()).thenReturn(USERNAME);
        when(member.getId()).thenReturn(USER_ID);
        when(member.getUser()).thenReturn(user);
        when(member.getNickname()).thenReturn(NICKNAME);
        when(message.getId()).thenReturn(MESSAGE_ID);
        when(message.getAuthor()).thenReturn(user);
    }
}
