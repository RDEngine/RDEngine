package org.rdengine.net.http;

public interface RDProcessCallback
{
    public boolean onPreRequest(RDHttpResponse response);

    public boolean onProcessing(RDHttpResponse response);

}
