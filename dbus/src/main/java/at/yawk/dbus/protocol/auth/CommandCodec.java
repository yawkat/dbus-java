package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.*;
import at.yawk.dbus.protocol.auth.command.Error;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class CommandCodec extends ByteToMessageCodec<Command> {
    private static final Charset CHARSET = StandardCharsets.US_ASCII;
    private static final byte[] CRLF = new byte[]{ '\r', '\n' };
    private static final Map<String, Function<List<String>, Command>> FACTORIES = new HashMap<>();

    static {
        FACTORIES.put(AgreeUnixFd.NAME, AgreeUnixFd::parse);
        FACTORIES.put(Auth.NAME, Auth::parse);
        FACTORIES.put(Begin.NAME, Begin::parse);
        FACTORIES.put(Cancel.NAME, Cancel::parse);
        FACTORIES.put(Data.NAME, Data::parse);
        FACTORIES.put(Error.NAME, Error::parse);
        FACTORIES.put(NegotiateUnixFd.NAME, NegotiateUnixFd::parse);
        FACTORIES.put(Ok.NAME, Ok::parse);
        FACTORIES.put(Rejected.NAME, Rejected::parse);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        out.writeBytes(msg.getSerialized().getBytes(CHARSET));
        out.writeBytes(CRLF);
        log.trace("Sent message {}", msg.getSerialized());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int crlfPos = in.forEachByte(new CRLFFinder());
        if (crlfPos == -1) {
            return;
        }

        int start = in.readerIndex();
        int end = crlfPos - 1; // remove trailing crlf
        String commandString = in.toString(start, end - start, CHARSET);
        log.trace("Received message {}", commandString);
        in.readerIndex(crlfPos + 1);

        int sep = commandString.indexOf(' ');
        String commandName = sep == -1 ? commandString : commandString.substring(0, sep);
        Function<List<String>, Command> factory = FACTORIES.get(commandName);
        if (factory != null) {
            List<String> args;
            if (sep == -1) {
                args = Collections.emptyList();
            } else {
                args = Arrays.asList(commandString.substring(sep + 1).split(" "));
            }
            out.add(factory.apply(args));
        }
    }

    private static class CRLFFinder implements ByteBufProcessor {
        boolean hasCr = false;

        @Override
        public boolean process(byte value) throws Exception {
            if (hasCr) {
                if (value != '\n') {
                    throw new DecoderException("CR not followed by LF");
                } else {
                    return false;
                }
            } else {
                hasCr = value == '\r';
                return true;
            }
        }
    }
}
