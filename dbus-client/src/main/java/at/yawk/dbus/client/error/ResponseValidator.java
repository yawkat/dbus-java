package at.yawk.dbus.client.error;

import at.yawk.dbus.client.request.Response;

/**
 * @author yawkat
 */
public interface ResponseValidator {
    /**
     * {@link ResponseValidator} that handles any error in the response and throws a {@link RemoteException}.
     */
    ResponseValidator HANDLE_ERROR = response -> {
        if (response.getError() != null) {
            throw new RemoteException(response.getError());
        }
    };

    void validate(Response response) throws Exception;
}
