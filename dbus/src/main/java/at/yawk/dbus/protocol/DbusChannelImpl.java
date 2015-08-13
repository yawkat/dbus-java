package at.yawk.dbus.protocol;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class DbusChannelImpl implements DbusChannel {
    private final Channel channel;
    private final SwappableMessageConsumer protocol;

    @Override
    public void setMessageConsumer(MessageConsumer consumer) {
        protocol.setConsumer(consumer);
    }

    @Override
    public void write(DbusMessage message) {
        channel.writeAndFlush(message);
    }

    @Override
    public void disconnect() {
        channel.disconnect();
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public CompletableFuture<?> closeStage() {
        return nettyFutureToStage(channel.closeFuture());
    }

    private static <V> CompletableFuture<V> nettyFutureToStage(Future<V> future) {
        CompletableFuture<V> stage = new CompletableFuture<>();
        future.addListener(new GenericFutureListener<Future<V>>() {
            @Override
            public void operationComplete(Future<V> future) throws Exception {
                try {
                    stage.complete(future.get());
                } catch (ExecutionException e) {
                    stage.completeExceptionally(e.getCause());
                }
            }
        });
        return stage;
    }
}