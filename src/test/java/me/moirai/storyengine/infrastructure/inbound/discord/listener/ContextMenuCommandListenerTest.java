package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResult;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@SuppressWarnings("unchecked")
public class ContextMenuCommandListenerTest extends AbstractDiscordTest {

    @Mock
    private UseCaseRunner useCaseRunner;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @Mock
    private DiscordListenerErrorHandler errorHandler;

    @Mock
    private DiscordListenerHelper discordListenerHelper;

    @InjectMocks
    private ContextMenuCommandListener listener;

    @Test
    public void editMessageCommand_whenAuthorIsBot_thenDoNothing() {

        // Given
        String eventName = "(MoirAI) Edit message";

        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);
        when(user.isBot()).thenReturn(true);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        verify(event, times(0)).replyModal(any());
    }

    @Test
    public void editMessageCommand_whenInvalidCommand_thenDoNothing() {

        // Given
        String eventName = "invalidName";

        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        verify(event, times(0)).replyModal(any());
    }

    @Test
    public void editMessageCommand_whenBotIsNotAuthor_thenSendNotification() {

        // Given
        String botId = "BOTID";
        String eventName = "(MoirAI) Edit message";
        String messageContent = "It's only possible to edit messages sent by " + NICKNAME;

        InteractionHook interactionHook = mock(InteractionHook.class);
        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        when(member.getId()).thenReturn(botId);
        when(discordListenerHelper.sendNotification(any(), contentCaptor.capture())).thenReturn(interactionHook);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        String contentSent = contentCaptor.getValue();
        assertThat(contentSent).isNotNull()
                .isNotEmpty()
                .isEqualTo(messageContent);
    }

    @Test
    public void editMessageCommand_whenBotIsNotAuthorAndNicknameIsNull_thenSendNotification() {

        // Given
        String botId = "BOTID";
        String eventName = "(MoirAI) Edit message";
        String messageContent = "It's only possible to edit messages sent by " + USERNAME;

        InteractionHook interactionHook = mock(InteractionHook.class);
        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);

        when(member.getId()).thenReturn(botId);
        when(member.getNickname()).thenReturn(null);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        when(discordListenerHelper.sendNotification(any(), contentCaptor.capture())).thenReturn(interactionHook);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        String contentSent = contentCaptor.getValue();
        assertThat(contentSent).isNotNull()
                .isNotEmpty()
                .isEqualTo(messageContent);
    }

    @Test
    public void editMessageCommand_whenCalled_thenShouldEditSelectedMessage() {

        // Given
        String botId = "BOTID";
        String eventName = "(MoirAI) Edit message";
        String messageContent = "Some message";

        ModalCallbackAction modalAction = mock(ModalCallbackAction.class);
        ReplyCallbackAction messageReplyAction = mock(ReplyCallbackAction.class);
        InteractionHook interactionHook = mock(InteractionHook.class);
        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);

        ArgumentCaptor<Modal> modalCaptor = ArgumentCaptor.forClass(Modal.class);
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);

        when(user.getId()).thenReturn(botId);
        when(member.getId()).thenReturn(botId);
        when(event.reply(anyString())).thenReturn(messageReplyAction);
        when(messageReplyAction.setEphemeral(anyBoolean())).thenReturn(messageReplyAction);
        when(messageReplyAction.complete()).thenReturn(interactionHook);
        when(event.replyModal(modalCaptor.capture())).thenReturn(modalAction);
        when(message.getContentRaw()).thenReturn(messageContent);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        verify(event, times(1)).replyModal(any());

        Modal createdModal = modalCaptor.getValue();

        assertThat(createdModal).isNotNull();
    }

    @Test
    public void tokenizeMessage_whenAllowedNumberOfCharacters_thenShouldSendResult() {

        // Given
        String eventName = "(MoirAI) Tokenize content";
        String textToBeTokenized = "This is some text.";
        String tokens = "This| is| some| text|.";
        int[] tokenIds = { 1212, 318, 617, 2420, 13 };
        int tokenCount = 5;

        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);
        ReplyCallbackAction messageReplyAction = mock(ReplyCallbackAction.class);
        InteractionHook interactionHook = mock(InteractionHook.class);
        WebhookMessageEditAction<Message> messageEditAction = mock(WebhookMessageEditAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        TokenizeResult expectedAdapterResult = TokenizeResult.builder()
                .tokens(tokens)
                .tokenCount(tokenCount)
                .tokenIds(tokenIds)
                .characterCount(textToBeTokenized.length())
                .build();

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);

        when(event.reply(anyString())).thenReturn(messageReplyAction);
        when(messageReplyAction.setEphemeral(anyBoolean())).thenReturn(messageReplyAction);
        when(messageReplyAction.complete()).thenReturn(interactionHook);
        when(useCaseRunner.run(any())).thenReturn(Optional.of(expectedAdapterResult));

        when(interactionHook.editOriginal(anyString())).thenReturn(messageEditAction);
        when(interactionHook.deleteOriginal()).thenReturn(restAction);
        when(messageEditAction.complete()).thenReturn(message);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any());
    }

    @Test
    public void tokenizeMessage_whenTooManyCharacters_thenShouldSendResult() {

        // Given
        String eventName = "(MoirAI) Tokenize content";
        String textToBeTokenized = "This is some text.";
        String tokens = "uJnA;1,!S76XaFZJH.?Q6j6yn9r-CTS2&z/1N4rYQ,iN7GgT-1V)d]_vB6Sj3bLK5qt%SL_pRd?RFS2y.U#mxmVgd!;cf=$d-:fb}[v7APQ)i]-eqk0z:mMG9HE[m7$xV[gdtn9Cgb$fE1=[eBQbqgchbtEH;Fg?w!r%qcHQbJm?HhcXS/&7a+/[c1/mWtDz#ux}KDGErE,V]nfN4uQv]JJq,Q!1*.gZh5=nED7&J!+AzJ#;[E4rMrKezZneAuB9EKbLyu.Zw+CGtqAF*0L6#&h]aS,E+0)%kBJz}MEZ1Y1%}+uVgU]f,mNjcXJ]XarutR#(3x;p&pXPmK!ddG,L&%(258wJf7VCg#yJe$EA&}8b9;Yv#e4+$@tpAFKJ{=en$)x!(YLMLj0n2xHt%2VQ-ExrDZVPjySa#wZg,U!uX=}uN=d8t9w]1.i!}cwfvZ3:212TaSW$Q#:M+&HB?%{!Kr?fqkNKR7X_?ca@!@EM[*v!pQk+}w09mNrk9=x/{W:P)&jz&uE(XAdvzUpzhapM.RC.5L?(b%z*HJTw!g:e5i2H,YJ=G-UD7v(?[:B)iuLSrXMrh[_NwLH3SXp1HqU2}3z3pFv5wevH$6/.[J&dU&*/b!fpeZzf8N6)cCa7%/W5iJ]jY)uLntG7qheU4!:A26+w}C)N3QhLhVPP1B(k-YDxv1Y4SL#D7/AbzYmiyr;yC%15SFP-F$_(Atzt:L;!EeN%;kS[wa1q!vceX.E78Y*9eWFH/&j_Jb%77YC@*iehaxp=2S3{NMR_,b[;{3Ycit{(KA@cK7R,qB*_.DB.FwA7!]%Q-Yu+]SG;p=fxVDU-v[?*QT:hX%{+;qQrJ529;#udcjUH!v99MA.!v?u??9z.X;krxYLf.k-M_S,a:ZuDFq?Gu*j#MqXb90y&v&U&&Ux].(r9UQ*D6qcX)nm9Jgew+xWdY]zNk)$VYgN8@V93?U#wzi$vW5FcZN9XU86.ZW3TaR&MjaFG!5zu#JBS]J#,i?Cm6iHqK$HZ%QN-B#qJp=bJE+BryCntVPu68PUN=aK$AA#C@{?W@C7p07(CDzSBZ1m+6uFbXpRYNDA2GNVBm:vJ@%+zqDaSmNR=evP/M)=GGwK1-vA(gjD8?+yCSNFp33g?3%3b9*Dbq}kH6pg)h3BvW=A*0MYWyu#!1@}/9fmqh0umr}_CN{QSa;)hM#*u]}V5GGqw/i;NpRH)v;&D$)K9NJQF=0eV-A#gZC+ByRaqrAn&8gwn6A=Wp4Ei]rpm@DK%Zed(T))EKV_3jr@V6)bJ%Y2qQhUNr(,pEh7)6knw[Zx4SwwiF.CX1uZvDiyJ+&+gj+H,](Sr4JGGFB&bYA}Kk3wmq1ZJR1W!JY:2*@pxj4([6d{}cgMqFuWJxcGa621-WEA$aw2KzPQ12v+R8Am7ruC/2eP7p_4HcS!2dcJ46{:UB;{E.]=$?83W5&Xj].f;=(60--AW0SbZ@,w,uAZ4BWRu=98#nhj[mqf*e[,_};K%TJk?piZ{Z#3H#p_/9QMT!zcbucx9Qm#K/jp/duM0SmiV5S&3D8nfpN_h78ZB-NLwE{x0k21:!$U/1Q}.J:h*6&0K!@hX=aKybd9qF8MbAH:Sf4Fe?$0ATAJY)q-)B/y0=H*nNzDm$ABg/aa4D3xpNE_Mh[5+:-PMa(gE{/BkRT4j0n0NyAKW_YYjHS5%MF7z5rAdU}9dqSeHvY/C5%%$qj/um)0P}LTGdqwiMX/$g&fVZ{ZbLjrr7G9Bx$S-?;R5}{gA}*heMzJ3Ah1/B.BpavUNv$d&SR.-+LJK*3/mb8xi)Pt_8JK=]@pJuEwe?@4u8953*XXB{T[GQMawHnbW@K(g}GiR3kZcr*4#A&9By;[n{V)!MMF:2t2!]1,ge/SW/wD@u(!@M]=$2@TAgiRE-dD#P(MC8zGZR,B(b-+/JdjfvSZ#XW%k:Hi..ZmFWRAwNWG)%U/x-Yqhbm_Em?c%(j(!?F+92v_Q5.d!w{[n)b1mF]K}Wh*-:";
        int[] tokenIds = { 1212, 318, 617, 2420, 13 };
        int tokenCount = 5;

        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);
        ReplyCallbackAction messageReplyAction = mock(ReplyCallbackAction.class);
        InteractionHook interactionHook = mock(InteractionHook.class);
        WebhookMessageEditAction<Message> messageEditAction = mock(WebhookMessageEditAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        TokenizeResult expectedAdapterResult = TokenizeResult.builder()
                .tokens(tokens)
                .tokenCount(tokenCount)
                .tokenIds(tokenIds)
                .characterCount(textToBeTokenized.length())
                .build();

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);

        when(event.reply(anyString())).thenReturn(messageReplyAction);
        when(messageReplyAction.setEphemeral(anyBoolean())).thenReturn(messageReplyAction);
        when(messageReplyAction.complete()).thenReturn(interactionHook);
        when(useCaseRunner.run(any())).thenReturn(Optional.of(expectedAdapterResult));

        when(interactionHook.editOriginal(anyString())).thenReturn(messageEditAction);
        when(interactionHook.deleteOriginal()).thenReturn(restAction);
        when(messageEditAction.complete()).thenReturn(message);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any());
    }

    @Test
    public void tokenizeMessage_whenEmptyResult_thenThrowError() {

        // Given
        String eventName = "(MoirAI) Tokenize content";

        MessageContextInteractionEvent event = mock(MessageContextInteractionEvent.class);
        ReplyCallbackAction messageReplyAction = mock(ReplyCallbackAction.class);
        InteractionHook interactionHook = mock(InteractionHook.class);
        MessageCreateAction messageCreationMock = mock(MessageCreateAction.class);
        AuditableRestAction<Void> deleteAction = mock(AuditableRestAction.class);
        RestAction<Void> restAction = mock(RestAction.class);

        when(event.getMember()).thenReturn(member);
        when(event.getName()).thenReturn(eventName);
        when(event.getGuild()).thenReturn(guild);
        when(event.getJDA()).thenReturn(jda);
        when(event.getTarget()).thenReturn(message);
        when(event.getChannel()).thenReturn(channelUnion);

        when(event.reply(anyString())).thenReturn(messageReplyAction);
        when(messageReplyAction.setEphemeral(anyBoolean())).thenReturn(messageReplyAction);
        when(messageReplyAction.complete()).thenReturn(interactionHook);
        when(useCaseRunner.run(any())).thenReturn(Optional.empty());

        when(event.getChannel()).thenReturn(channelUnion);
        when(channelUnion.sendMessage(anyString())).thenReturn(messageCreationMock);
        when(messageCreationMock.complete()).thenReturn(message);
        when(message.delete()).thenReturn(deleteAction);
        when(channelUnion.sendTyping()).thenReturn(restAction);

        // When
        listener.onMessageContextInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any());
    }
}
