import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private static final ConcurrentLinkedDeque<SocketChannel> channels
            = new ConcurrentLinkedDeque<>();
    private final Callback callback;

    public ClientHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected");
        channels.add((SocketChannel) ctx.channel());
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String s) {
        System.out.printf("Received message: %s\n", s);
        callback.call(s);
        channels.stream()
                .forEach(channel -> channel.writeAndFlush(s));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected");
        channels.remove((SocketChannel) ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}