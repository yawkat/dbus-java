package at.yawk.dbus.client;

import at.yawk.dbus.client.error.RemoteException;
import at.yawk.dbus.client.request.Request;
import at.yawk.dbus.client.request.RequestExecutor;
import at.yawk.dbus.client.request.Response;
import at.yawk.dbus.protocol.MatchRule;
import at.yawk.dbus.protocol.object.DbusObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.testng.Assert;
import org.testng.internal.EclipseInterface;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class CollectingExecutor implements RequestExecutor {
    private final Function<Request, Response> delegate;
    private final List<Request> requests = new ArrayList<>();

    void assertEquals(Request... requests) {
        boolean match = requests.length == this.requests.size();
        if (match) {
            for (int i = 0; i < requests.length; i++) {
                match &= RequestImpl.requestsEqual(requests[i], this.requests.get(i));
            }
        }
        if (!match) {
            Assert.fail(EclipseInterface.ASSERT_LEFT +
                        Arrays.stream(requests).map(RequestImpl::requestToString)
                                .collect(Collectors.joining(", ", "[", "]")) +
                        EclipseInterface.ASSERT_MIDDLE +
                        this.requests.stream().map(RequestImpl::requestToString)
                                .collect(Collectors.joining(", ", "[", "]")) +
                        EclipseInterface.ASSERT_RIGHT);
        }
    }

    @Override
    public Response execute(Request request) throws Exception {
        requests.add(request);
        return delegate.apply(request);
    }

    @Override
    public Response execute(Request request, long timeout, TimeUnit unit) throws Exception {
        // timeout is ignored in this test class
        return execute(request);
    }

    @Override
    public Runnable listen(String bus, MatchRule rule, Consumer<List<DbusObject>> listener) throws RemoteException {
        throw new UnsupportedOperationException();
    }
}
