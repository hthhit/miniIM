package server.handler;

import server.session.ServerSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class MyWebSocketFrameHandler extends
		SimpleChannelInboundHandler<WebSocketFrame> {

	private ServerAccessHandler accessHandler = null;
	private ServerSession session;
	
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame)
			throws Exception {
		if (frame instanceof CloseWebSocketFrame) {
			MyWebSocketSeverHandler tmp = (MyWebSocketSeverHandler) ctx.pipeline().get("shaker");
			tmp.getHandshaker().close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            ctx.write(frame.retain());
            return;
        }
		if(frame instanceof TextWebSocketFrame){
			String request  = ((TextWebSocketFrame) frame).text();
			
			if(accessHandler ==null){
				accessHandler = new ServerAccessHandler();
			}
			
			if(!accessHandler.getAccess()){
				accessHandler.handle(request);
				ctx.channel().writeAndFlush(new TextWebSocketFrame(accessHandler.getResult()));
			}else{
				if(!session.isHasinit()){
					ctx.channel().writeAndFlush(new TextWebSocketFrame(session.init(request, accessHandler.getSecretKeySpec())));
				}
				System.out.println(request);
			}
		}else{
			String message = "unsupported frame type:" + frame.getClass().getName();
			throw new UnsupportedOperationException(message);
		}
		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		System.out.println("active");
		session = new ServerSession();
		
	}

}
