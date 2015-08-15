package at.yawk.dbus.protocol;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
@RequiredArgsConstructor
class DbusChannelImpl implements DbusChannel {
    private final Channel channel;
    private final SwappableMessageConsumer protocol;

    private final AtomicInteger nextSerial = new AtomicInteger(1);

    @Override
    public int createSerial() {
        int serial;
        do {
            serial = nextSerial.getAndIncrement();
        } while (serial == 0);
        return serial;
    }

    @Override
    public void setMessageConsumer(MessageConsumer consumer) {
        protocol.setConsumer(consumer);
    }

    @Override
    public void write(DbusMessage message) {
        if (message.getHeader().getSerial() == 0) {
            message.getHeader().setSerial(createSerial());
        }
        channel.writeAndFlush(message, channel.voidPromise());
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