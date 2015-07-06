


package pns.alltypes.netty.httpclient.request;

import java.util.UUID;

/**
 * The Class AbstractRequestMessage.
 * @author arung
 */
public class AbstractRequestMessage implements RequestMessage {

    /** The request id. */
    protected final String requestId = UUID.randomUUID().toString();

    /*
     * (non-Javadoc)
     * @see com.slingmedia.neat.model.RequestMessage#getRequestId()
     */
    @Override
    public String getRequestId() {

        return requestId;
    }

}
