package pns.alltypes.netty.httpclient.request;

import java.util.UUID;

/**
 * The Class AbstractRequestMessage.
 * @author arung
 */
public class AbstractRequestMessage implements RequestMessage {

    /** The request id. */
    protected final String requestId = UUID.randomUUID().toString();

    @Override
    public String getRequestId() {

        return requestId;
    }

}
