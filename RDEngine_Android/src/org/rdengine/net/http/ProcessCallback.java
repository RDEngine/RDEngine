package org.rdengine.net.http;

public interface ProcessCallback
{
    public boolean onPreRequest(RDHttpResponse response);

    public boolean onProcessing(RDHttpResponse response);

}
