/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
        if (response.isError()) {
            throw new RemoteException(response.getErrorName(), response.getReply());
        }
    };

    void validate(Response response) throws Exception;
}
