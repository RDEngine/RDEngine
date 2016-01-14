package org.rdengine.net.http;

public class NetworkState
{

    private static NetworkState instance;

    public synchronized static NetworkState ins()
    {
        if (instance == null)
        {
            instance = new NetworkState();
        }
        return instance;
    }

    private NetworkState()
    {

    }

    public boolean getNetworkAvailable()
    {
        return true;
    }

}
