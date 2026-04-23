package me.moirai.storyengine.infrastructure.inbound.websocket;

import java.security.Principal;
import java.util.List;

import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.core.port.inbound.notification.GetActiveBroadcastNotifications;
import me.moirai.storyengine.core.port.inbound.notification.GetActiveSystemNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;

@Controller
public class NotificationWebSocketController {

    private final QueryRunner queryRunner;

    public NotificationWebSocketController(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    @SubscribeMapping("/notifications/broadcast")
    public List<NotificationDetails> onBroadcastSubscribe(Principal principal) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var moiraiPrincipal = (MoiraiPrincipal) auth.getPrincipal();

        return queryRunner.run(new GetActiveBroadcastNotifications(moiraiPrincipal.username()));
    }

    @SubscribeMapping("/notifications/system")
    public List<NotificationDetails> onSystemSubscribe(Principal principal) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var moiraiPrincipal = (MoiraiPrincipal) auth.getPrincipal();

        return queryRunner.run(new GetActiveSystemNotifications(moiraiPrincipal.username()));
    }
}
