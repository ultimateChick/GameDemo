package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdhandler.ICmdHandler;
import org.tinygame.herostory.cmdhandler.UserEntryCmdHandler;
import org.tinygame.herostory.cmdhandler.UserMoveToCmdHandler;
import org.tinygame.herostory.cmdhandler.WhoElseIsHereCmdHandler;
import org.tinygame.herostory.factory.CmdHandlerFactory;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 重构笔记：
 * 1.抽取广播和用户管理为工具类，使得Handler职责更加明确
 * 2.抽取对msg的处理，使其成为根据不同msg类型形成的不同msgHandler，再对这些handler进行接口规范
 * 3.抽取对msg的类型判断，使其成为cmd的工厂，只需要传入msg就可以在ChannelHandler的业务中得到不同的builder进行
 */

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Broadcaster.addChannel(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        Broadcaster.removeChannel(ctx.channel());

        // 先拿到用户 Id
        Integer userId = (Integer)ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (null == userId) {
            return;
        }

        UserManager.removeUserById(userId);

        GameMsgProtocol.UserQuitResult.Builder resultBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
        resultBuilder.setQuitUserId(userId);

        GameMsgProtocol.UserQuitResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {

        System.out.println("收到客户短消息， msgClazz = " + msg.getClass().getName() + ", msg = " + msg);

        //其实handle方法要的是cmd，我们可以通过对msg的判断来决定到底是什么cmd
        //if-else可以完成，map也可以完成
        //用map的方式还可以结合反射，自动获取圈梁的msg、cmd、msgCode的映射
         GeneratedMessageV3 cmd = null;

         //希望有一个工厂，我们把msg投进去，就能返回一个正确的cmd，而不是在外面if-else

        ICmdHandler<? extends GeneratedMessageV3> handler = CmdHandlerFactory.getHandler(msg.getClass());

        if(null == handler) return;

        handler.handle(channelHandlerContext, cast(msg));

    }

    static private <T extends GeneratedMessageV3> T cast(Object msg){
        if (null == msg) return null;
        return (T)msg;
    }

}
