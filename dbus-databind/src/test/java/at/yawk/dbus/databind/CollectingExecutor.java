package at.yawk.dbus.databind;

import at.yawk.dbus.databind.request.Request;
import at.yawk.dbus.databind.request.RequestExecutor;
import at.yawk.dbus.databind.request.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.testng.Assert;
import org.testng.internal.EclipseInterface;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class CollectingExecutor implements RequestExecutor {
    private final RequestExecutor delegate;
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
        return delegate.execute(request);
    }
}
