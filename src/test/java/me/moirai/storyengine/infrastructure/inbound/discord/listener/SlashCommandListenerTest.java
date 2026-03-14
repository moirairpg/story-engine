package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.application.usecase.adventure.request.GetAdventureByChannelId;
import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureResultFixture;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.GoCommand;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.RetryCommand;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.StartCommand;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResult;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SuppressWarnings("unchecked")
public class SlashCommandListenerTest extends AbstractDiscordTest {

    private static final String GUILD_ID = "GLDID";
    private static final String CHANNEL_ID = "CHID";
    private static final String USERNAME = "user.name";
    private static final String NICKNAME = "nickname";

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private UseCaseRunner useCaseRunner;

    @Mock
    private DiscordListenerErrorHandler errorHandler;

    @Mock
    private DiscordListenerHelper discordListenerHelper;

    private SlashCommandListener listener;

    @BeforeEach
    public void beforeEach() {

        List<String> commandBefore = singletonList("Running command");
        List<String> commandAfter = singletonList("Command run");
        listener = new SlashCommandListener(useCaseRunner, errorHandler, discordListenerHelper,
                commandBefore, commandAfter, commandBefore, commandAfter, commandBefore, commandAfter);

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction eventReplyAction = mock(ReplyCallbackAction.class);
        CacheRestAction<Member> memberRetrievalAction = mock(CacheRestAction.class);
        MessageChannelUnion baseChannel = mock(MessageChannelUnion.class);
        WebhookMessageEditAction<Message> editAction = mock(WebhookMessageEditAction.class);

        when(event.getJDA()).thenReturn(jda);
        when(event.getChannel()).thenReturn(baseChannel);
        when(baseChannel.asTextChannel()).thenReturn(textChannel);
        when(event.getGuild()).thenReturn(guild);
        when(event.getUser()).thenReturn(user);
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(guild.retrieveMember(any(User.class))).thenReturn(memberRetrievalAction);
        when(guild.retrieveMember(any(SelfUser.class))).thenReturn(memberRetrievalAction);
        when(memberRetrievalAction.complete()).thenReturn(member);
        when(event.reply(anyString())).thenReturn(eventReplyAction);
        when(eventReplyAction.setEphemeral(anyBoolean())).thenReturn(eventReplyAction);
        when(eventReplyAction.complete()).thenReturn(interactionHook);
        when(user.getName()).thenReturn(USERNAME);
        when(member.getNickname()).thenReturn(NICKNAME);
        when(guild.getId()).thenReturn(GUILD_ID);
        when(textChannel.getId()).thenReturn(CHANNEL_ID);
        when(member.getUser()).thenReturn(user);
        when(interactionHook.editOriginal(anyString())).thenReturn(editAction);
        when(editAction.complete()).thenReturn(message);
    }

    @Test
    public void whenSlashCommandCalled_whenAuthorIsBot_thenDoNothing() {

        // Given
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(0)).run(any());
    }

    @Test
    public void whenModalCalled_whenInvalidModal_thenDoNothing() {

        // Given
        String command = "invalidCommand";

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(0)).run(any());
    }

    @Test
    public void retryCommand_whenSuccessfulExecution_thenShouldUpdateNotification() {

        // Given
        String nickname = "nick.name";
        String username = "user.name";
        String command = "retry";
        Mono<Void> commandResult = Mono.just(mock(Void.class));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(RetryCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(user.getName()).thenReturn(username);
        when(member.getNickname()).thenReturn(nickname);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(RetryCommand.class));

        StepVerifier.create(commandResult)
                .assertNext(message -> assertThat(message).isNotNull())
                .verifyComplete();
    }

    @Test
    public void goCommand_whenSuccessfulExecution_thenShouldUpdateNotification() {

        // Given
        String nickname = "nick.name";
        String username = "user.name";
        String command = "go";
        Mono<Void> commandResult = Mono.just(mock(Void.class));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(GoCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(user.getName()).thenReturn(username);
        when(member.getNickname()).thenReturn(nickname);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(GoCommand.class));

        StepVerifier.create(commandResult)
                .assertNext(message -> assertThat(message).isNotNull())
                .verifyComplete();
    }

    @Test
    public void startCommand_whenSuccessfulExecution_thenShouldUpdateNotification() {

        // Given
        String nickname = "nick.name";
        String username = "user.name";
        String command = "start";
        Mono<Void> commandResult = Mono.just(mock(Void.class));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(StartCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(user.getName()).thenReturn(username);
        when(member.getNickname()).thenReturn(nickname);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(StartCommand.class));

        StepVerifier.create(commandResult)
                .assertNext(message -> assertThat(message).isNotNull())
                .verifyComplete();
    }

    @Test
    public void retryCommand_whenSuccessfulExecutionAndNickNameIsNull_thenShouldUpdateNotification() {

        // Given
        String nickname = null;
        String username = "user.name";
        String command = "retry";
        Mono<Void> commandResult = Mono.just(mock(Void.class));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(RetryCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(user.getName()).thenReturn(username);
        when(member.getNickname()).thenReturn(nickname);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(RetryCommand.class));

        StepVerifier.create(commandResult)
                .assertNext(message -> assertThat(message).isNotNull())
                .verifyComplete();
    }

    @Test
    public void goCommand_whenSuccessfulExecutionAndNickNameIsNull_thenShouldUpdateNotification() {

        // Given
        String nickname = null;
        String username = "user.name";
        String command = "go";
        Mono<Void> commandResult = Mono.just(mock(Void.class));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(GoCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(user.getName()).thenReturn(username);
        when(member.getNickname()).thenReturn(nickname);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(GoCommand.class));

        StepVerifier.create(commandResult)
                .assertNext(message -> assertThat(message).isNotNull())
                .verifyComplete();
    }

    @Test
    public void startCommand_whenSuccessfulExecutionAndNickNameIsNull_thenShouldUpdateNotification() {

        // Given
        String nickname = null;
        String username = "user.name";
        String command = "start";
        Mono<Void> commandResult = Mono.just(mock(Void.class));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(StartCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(user.getName()).thenReturn(username);
        when(member.getNickname()).thenReturn(nickname);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(StartCommand.class));

        StepVerifier.create(commandResult)
                .assertNext(message -> assertThat(message).isNotNull())
                .verifyComplete();
    }

    @Test
    public void retryCommand_whenErrorThrown_thenShouldUpdateNotificationWithError() {

        // Given
        String command = "retry";
        Mono<Void> commandResult = Mono.error(new IllegalStateException("Error"));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(RetryCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(RetryCommand.class));

        StepVerifier.create(commandResult)
                .verifyErrorMessage("Error");
    }

    @Test
    public void goCommand_whenErrorThrown_thenShouldUpdateNotificationWithError() {

        // Given
        String command = "go";
        Mono<Void> commandResult = Mono.error(new IllegalStateException("Error"));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(GoCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(GoCommand.class));

        StepVerifier.create(commandResult)
                .verifyErrorMessage("Error");
    }

    @Test
    public void startCommand_whenErrorThrown_thenShouldUpdateNotificationWithError() {

        // Given
        String command = "start";
        Mono<Void> commandResult = Mono.error(new IllegalStateException("Error"));
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(useCaseRunner.run(any(StartCommand.class))).thenReturn(commandResult);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any(StartCommand.class));

        StepVerifier.create(commandResult)
                .verifyErrorMessage("Error");
    }

    @Test
    public void sayCommand_whenInputIsValid_thenOpenModal() {

        // Given
        String command = "say";
        String modalId = "sayAsBot";

        ModalCallbackAction replyModalAction = mock(ModalCallbackAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        ArgumentCaptor<Modal> modalCaptor = ArgumentCaptor.forClass(Modal.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(event.replyModal(modalCaptor.capture())).thenReturn(replyModalAction);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        Modal createdModal = modalCaptor.getValue();

        assertThat(createdModal).isNotNull();
        assertThat(createdModal.getId()).isEqualTo(modalId);
    }

    @Test
    public void tokenizeCommand_whenCalled_thenShouldCallUseCase() {

        // Given
        String command = "tokenize";
        String textToBeTokenized = "This is some text.";
        String tokens = "This| is| some| text|.";
        int[] tokenIds = { 1212, 318, 617, 2420, 13 };
        int tokenCount = 5;

        OptionMapping commandParameterContent = mock(OptionMapping.class);
        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction eventReplyAction = mock(ReplyCallbackAction.class);
        CacheRestAction<Member> memberRetrievalAction = mock(CacheRestAction.class);
        MessageChannelUnion baseChannel = mock(MessageChannelUnion.class);
        WebhookMessageEditAction<Message> editAction = mock(WebhookMessageEditAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        TokenizeResult expectedAdapterResult = TokenizeResult.builder()
                .tokens(tokens)
                .tokenCount(tokenCount)
                .tokenIds(tokenIds)
                .characterCount(textToBeTokenized.length())
                .build();

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(baseChannel);
        when(baseChannel.asTextChannel()).thenReturn(textChannel);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(guild.retrieveMember(any(SelfUser.class))).thenReturn(memberRetrievalAction);
        when(memberRetrievalAction.complete()).thenReturn(member);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(event.reply(anyString())).thenReturn(eventReplyAction);
        when(eventReplyAction.setEphemeral(anyBoolean())).thenReturn(eventReplyAction);
        when(eventReplyAction.complete()).thenReturn(interactionHook);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(interactionHook.editOriginal(anyString())).thenReturn(editAction);
        when(editAction.complete()).thenReturn(message);
        when(event.getOption(anyString())).thenReturn(commandParameterContent);
        when(commandParameterContent.getAsString()).thenReturn(textToBeTokenized);
        when(useCaseRunner.run(any())).thenReturn(Optional.of(expectedAdapterResult));
        when(baseChannel.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any());
    }

    @Test
    public void tokenizeCommand_whenTooManyCharacters_thenNotifyUser() {

        // Given
        String command = "tokenize";
        String textToBeTokenized = "This is some text.";
        String tokens = "uJnA;1,!S76XaFZJH.?Q6j6yn9r-CTS2&z/1N4rYQ,iN7GgT-1V)d]_vB6Sj3bLK5qt%SL_pRd?RFS2y.U#mxmVgd!;cf=$d-:fb}[v7APQ)i]-eqk0z:mMG9HE[m7$xV[gdtn9Cgb$fE1=[eBQbqgchbtEH;Fg?w!r%qcHQbJm?HhcXS/&7a+/[c1/mWtDz#ux}KDGErE,V]nfN4uQv]JJq,Q!1*.gZh5=nED7&J!+AzJ#;[E4rMrKezZneAuB9EKbLyu.Zw+CGtqAF*0L6#&h]aS,E+0)%kBJz}MEZ1Y1%}+uVgU]f,mNjcXJ]XarutR#(3x;p&pXPmK!ddG,L&%(258wJf7VCg#yJe$EA&}8b9;Yv#e4+$@tpAFKJ{=en$)x!(YLMLj0n2xHt%2VQ-ExrDZVPjySa#wZg,U!uX=}uN=d8t9w]1.i!}cwfvZ3:212TaSW$Q#:M+&HB?%{!Kr?fqkNKR7X_?ca@!@EM[*v!pQk+}w09mNrk9=x/{W:P)&jz&uE(XAdvzUpzhapM.RC.5L?(b%z*HJTw!g:e5i2H,YJ=G-UD7v(?[:B)iuLSrXMrh[_NwLH3SXp1HqU2}3z3pFv5wevH$6/.[J&dU&*/b!fpeZzf8N6)cCa7%/W5iJ]jY)uLntG7qheU4!:A26+w}C)N3QhLhVPP1B(k-YDxv1Y4SL#D7/AbzYmiyr;yC%15SFP-F$_(Atzt:L;!EeN%;kS[wa1q!vceX.E78Y*9eWFH/&j_Jb%77YC@*iehaxp=2S3{NMR_,b[;{3Ycit{(KA@cK7R,qB*_.DB.FwA7!]%Q-Yu+]SG;p=fxVDU-v[?*QT:hX%{+;qQrJ529;#udcjUH!v99MA.!v?u??9z.X;krxYLf.k-M_S,a:ZuDFq?Gu*j#MqXb90y&v&U&&Ux].(r9UQ*D6qcX)nm9Jgew+xWdY]zNk)$VYgN8@V93?U#wzi$vW5FcZN9XU86.ZW3TaR&MjaFG!5zu#JBS]J#,i?Cm6iHqK$HZ%QN-B#qJp=bJE+BryCntVPu68PUN=aK$AA#C@{?W@C7p07(CDzSBZ1m+6uFbXpRYNDA2GNVBm:vJ@%+zqDaSmNR=evP/M)=GGwK1-vA(gjD8?+yCSNFp33g?3%3b9*Dbq}kH6pg)h3BvW=A*0MYWyu#!1@}/9fmqh0umr}_CN{QSa;)hM#*u]}V5GGqw/i;NpRH)v;&D$)K9NJQF=0eV-A#gZC+ByRaqrAn&8gwn6A=Wp4Ei]rpm@DK%Zed(T))EKV_3jr@V6)bJ%Y2qQhUNr(,pEh7)6knw[Zx4SwwiF.CX1uZvDiyJ+&+gj+H,](Sr4JGGFB&bYA}Kk3wmq1ZJR1W!JY:2*@pxj4([6d{}cgMqFuWJxcGa621-WEA$aw2KzPQ12v+R8Am7ruC/2eP7p_4HcS!2dcJ46{:UB;{E.]=$?83W5&Xj].f;=(60--AW0SbZ@,w,uAZ4BWRu=98#nhj[mqf*e[,_};K%TJk?piZ{Z#3H#p_/9QMT!zcbucx9Qm#K/jp/duM0SmiV5S&3D8nfpN_h78ZB-NLwE{x0k21:!$U/1Q}.J:h*6&0K!@hX=aKybd9qF8MbAH:Sf4Fe?$0ATAJY)q-)B/y0=H*nNzDm$ABg/aa4D3xpNE_Mh[5+:-PMa(gE{/BkRT4j0n0NyAKW_YYjHS5%MF7z5rAdU}9dqSeHvY/C5%%$qj/um)0P}LTGdqwiMX/$g&fVZ{ZbLjrr7G9Bx$S-?;R5}{gA}*heMzJ3Ah1/B.BpavUNv$d&SR.-+LJK*3/mb8xi)Pt_8JK=]@pJuEwe?@4u8953*XXB{T[GQMawHnbW@K(g}GiR3kZcr*4#A&9By;[n{V)!MMF:2t2!]1,ge/SW/wD@u(!@M]=$2@TAgiRE-dD#P(MC8zGZR,B(b-+/JdjfvSZ#XW%k:Hi..ZmFWRAwNWG)%U/x-Yqhbm_Em?c%(j(!?F+92v_Q5.d!w{[n)b1mF]K}Wh*-:";
        int[] tokenIds = { 1212, 318, 617, 2420, 13 };
        int tokenCount = 5;

        OptionMapping commandParameterContent = mock(OptionMapping.class);
        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction eventReplyAction = mock(ReplyCallbackAction.class);
        CacheRestAction<Member> memberRetrievalAction = mock(CacheRestAction.class);
        MessageChannelUnion baseChannel = mock(MessageChannelUnion.class);
        WebhookMessageEditAction<Message> editAction = mock(WebhookMessageEditAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        TokenizeResult expectedAdapterResult = TokenizeResult.builder()
                .tokens(tokens)
                .tokenCount(tokenCount)
                .tokenIds(tokenIds)
                .characterCount(textToBeTokenized.length())
                .build();

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(baseChannel);
        when(baseChannel.asTextChannel()).thenReturn(textChannel);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(guild.retrieveMember(any(SelfUser.class))).thenReturn(memberRetrievalAction);
        when(memberRetrievalAction.complete()).thenReturn(member);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(event.reply(anyString())).thenReturn(eventReplyAction);
        when(eventReplyAction.setEphemeral(anyBoolean())).thenReturn(eventReplyAction);
        when(eventReplyAction.complete()).thenReturn(interactionHook);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(editAction.complete()).thenReturn(message);
        when(event.getOption(anyString())).thenReturn(commandParameterContent);
        when(commandParameterContent.getAsString()).thenReturn(textToBeTokenized);
        when(useCaseRunner.run(any())).thenReturn(Optional.of(expectedAdapterResult));
        when(baseChannel.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any());
    }

    @Test
    public void tokenizeCommand_whenNoResultReturned_thenThrowError() {

        // Given
        String command = "tokenize";
        String textToBeTokenized = "This is some text.";

        OptionMapping commandParameterContent = mock(OptionMapping.class);
        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction eventReplyAction = mock(ReplyCallbackAction.class);
        CacheRestAction<Member> memberRetrievalAction = mock(CacheRestAction.class);
        MessageChannelUnion baseChannel = mock(MessageChannelUnion.class);
        WebhookMessageEditAction<Message> editAction = mock(WebhookMessageEditAction.class);
        MessageCreateAction messageCreationMock = mock(MessageCreateAction.class);
        AuditableRestAction<Void> deleteAction = mock(AuditableRestAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        ArgumentCaptor<String> notificationCaptor = ArgumentCaptor.forClass(String.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(baseChannel);
        when(baseChannel.asTextChannel()).thenReturn(textChannel);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(guild.retrieveMember(any(SelfUser.class))).thenReturn(memberRetrievalAction);
        when(memberRetrievalAction.complete()).thenReturn(member);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(event.reply(anyString())).thenReturn(eventReplyAction);
        when(eventReplyAction.setEphemeral(anyBoolean())).thenReturn(eventReplyAction);
        when(eventReplyAction.complete()).thenReturn(interactionHook);
        when(interactionHook.editOriginal(notificationCaptor.capture())).thenReturn(editAction);
        when(editAction.complete()).thenReturn(message);
        when(event.getOption(anyString())).thenReturn(commandParameterContent);
        when(commandParameterContent.getAsString()).thenReturn(textToBeTokenized);
        when(useCaseRunner.run(any())).thenReturn(Optional.empty());

        when(event.getChannel()).thenReturn(channelUnion);
        when(channelUnion.sendMessage(anyString())).thenReturn(messageCreationMock);
        when(messageCreationMock.complete()).thenReturn(message);
        when(message.delete()).thenReturn(deleteAction);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any());
    }

    @Test
    public void rememberCommand_whenReceived_thenUpdateRemember() {

        // Given
        String modalId = "remember";
        String command = "remember";

        ModalCallbackAction replyModalAction = mock(ModalCallbackAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        ArgumentCaptor<Modal> modalCaptor = ArgumentCaptor.forClass(Modal.class);

        when(useCaseRunner.run(any(GetAdventureByChannelId.class)))
                .thenReturn(GetAdventureResultFixture.privateMultiplayerAdventure().build());

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(event.replyModal(modalCaptor.capture())).thenReturn(replyModalAction);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        Modal createdModal = modalCaptor.getValue();

        assertThat(createdModal).isNotNull();
        assertThat(createdModal.getId()).isEqualTo(modalId);
    }

    @Test
    public void nudgeCommand_whenReceived_thenUpdateRemember() {

        // Given
        String modalId = "nudge";
        String command = "nudge";

        ModalCallbackAction replyModalAction = mock(ModalCallbackAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        ArgumentCaptor<Modal> modalCaptor = ArgumentCaptor.forClass(Modal.class);

        when(useCaseRunner.run(any(GetAdventureByChannelId.class)))
                .thenReturn(GetAdventureResultFixture.privateMultiplayerAdventure().build());

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(event.replyModal(modalCaptor.capture())).thenReturn(replyModalAction);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        Modal createdModal = modalCaptor.getValue();

        assertThat(createdModal).isNotNull();
        assertThat(createdModal.getId()).isEqualTo(modalId);
    }

    @Test
    public void authorsNoteCommand_whenReceived_thenUpdateRemember() {

        // Given
        String modalId = "authorsNote";
        String command = "authorsnote";

        ModalCallbackAction replyModalAction = mock(ModalCallbackAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        ArgumentCaptor<Modal> modalCaptor = ArgumentCaptor.forClass(Modal.class);

        when(useCaseRunner.run(any(GetAdventureByChannelId.class)))
                .thenReturn(GetAdventureResultFixture.privateMultiplayerAdventure().build());

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(event.replyModal(modalCaptor.capture())).thenReturn(replyModalAction);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        Modal createdModal = modalCaptor.getValue();

        assertThat(createdModal).isNotNull();
        assertThat(createdModal.getId()).isEqualTo(modalId);
    }

    @Test
    public void bumpCommand_whenReceived_thenUpdateRemember() {

        // Given
        String modalId = "bump";
        String command = "bump";

        ModalCallbackAction replyModalAction = mock(ModalCallbackAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        ArgumentCaptor<Modal> modalCaptor = ArgumentCaptor.forClass(Modal.class);

        when(useCaseRunner.run(any(GetAdventureByChannelId.class)))
                .thenReturn(GetAdventureResultFixture.privateMultiplayerAdventure().build());

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);
        when(event.replyModal(modalCaptor.capture())).thenReturn(replyModalAction);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        Modal createdModal = modalCaptor.getValue();

        assertThat(createdModal).isNotNull();
        assertThat(createdModal.getId()).isEqualTo(modalId);
    }

    @Test
    public void helpCommand_whenReceived_thenShowHelpPage() {

        // Given
        String command = "help";
        String embedContent = "Content";

        ReplyCallbackAction callbackAction = mock(ReplyCallbackAction.class);
        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);

        when(event.getOption(anyString())).thenReturn(null);
        when(useCaseRunner.run(any())).thenReturn(embedContent);
        when(event.replyEmbeds(embedCaptor.capture())).thenReturn(callbackAction);
        when(callbackAction.setEphemeral(anyBoolean())).thenReturn(callbackAction);
        when(callbackAction.complete()).thenReturn(mock(InteractionHook.class));

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        MessageEmbed embedSent = embedCaptor.getValue();
        assertThat(embedSent).isNotNull();
        assertThat(embedSent.getDescription()).isEqualTo(embedContent);

        verify(useCaseRunner, times(1)).run(any());
        verify(event, times(1)).replyEmbeds(any(MessageEmbed.class));
    }

    @Test
    public void helpCommand_whenReceived_andSpecificCommandSupplied_thenShowHelpPage() {

        // Given
        String command = "help";
        String embedContent = "Content";
        String commandToBeDescribed = null;

        OptionMapping option = mock(OptionMapping.class);
        ReplyCallbackAction callbackAction = mock(ReplyCallbackAction.class);
        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);

        when(event.getFullCommandName()).thenReturn(command);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(false);
        when(channelUnion.asTextChannel()).thenReturn(textChannel);

        when(event.getOption(anyString())).thenReturn(option);
        when(option.getAsString()).thenReturn(commandToBeDescribed);
        when(useCaseRunner.run(any())).thenReturn(embedContent);
        when(event.replyEmbeds(embedCaptor.capture())).thenReturn(callbackAction);
        when(callbackAction.setEphemeral(anyBoolean())).thenReturn(callbackAction);
        when(callbackAction.complete()).thenReturn(mock(InteractionHook.class));

        // When
        listener.onSlashCommandInteraction(event);

        // Then
        MessageEmbed embedSent = embedCaptor.getValue();
        assertThat(embedSent).isNotNull();
        assertThat(embedSent.getDescription()).isEqualTo(embedContent);

        verify(useCaseRunner, times(1)).run(any());
        verify(event, times(1)).replyEmbeds(any(MessageEmbed.class));
    }
}
