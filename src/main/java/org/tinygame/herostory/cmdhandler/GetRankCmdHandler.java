package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import org.tinygame.herostory.model.RankEntity;
import org.tinygame.herostory.msg.GameMsgProtocol;
import org.tinygame.herostory.rank.GetRankService;

import java.util.Collections;

public class GetRankCmdHandler implements ICmdHandler<GameMsgProtocol.GetRankCmd> {
    //虽然redis很快，但也算是IO，用异步处理器来做
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.GetRankCmd tCmd) {

        GetRankService.getInstance().getRank(rankEntities -> {

            if (null == rankEntities) {
                rankEntities = Collections.emptyList();
            }

            GameMsgProtocol.GetRankResult.Builder rankResultBuilder = GameMsgProtocol.GetRankResult.newBuilder();


            for (RankEntity rankEntity : rankEntities) {

                //build每个rankEntity
                GameMsgProtocol.GetRankResult.RankItem.Builder rankEntityBuilder = GameMsgProtocol.GetRankResult.RankItem.newBuilder();
                rankEntityBuilder.setRankId(rankEntity.rankId);
                rankEntityBuilder.setUserId(rankEntity.userId);
                rankEntityBuilder.setUserName(rankEntity.userName);
                rankEntityBuilder.setHeroAvatar(rankEntity.heroAvatar);
                rankEntityBuilder.setWin(rankEntity.win);
                rankResultBuilder.addRankItem(rankEntityBuilder);

            }

            GameMsgProtocol.GetRankResult rankResult = rankResultBuilder.build();

            ctx.writeAndFlush(rankResult);

            return null;
        });
    }

}
