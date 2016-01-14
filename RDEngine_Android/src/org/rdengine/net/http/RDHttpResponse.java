package org.rdengine.net.http;

import org.json.JSONObject;

/**
 * @author rainer.yang@gmail.com
 */
public class RDHttpResponse
{

    public static final String JSON_ERR_CODE = "errcode";
    public static final String JSON_ERR_MSG = "errmsg";

    private RDHttpRequest request = null;

    public RDHttpRequest getRequest()
    {
        return request;
    }

    public void setRequest(RDHttpRequest request)
    {
        this.request = request;
    }

    /**
     * response data readed from local cache
     */
    private boolean isFromCache = false;

    public boolean isFromCache()
    {
        return isFromCache;
    }

    public void setFromCache(boolean fromCache)
    {
        this.isFromCache = fromCache;
    }

    /**
     * parse customer protocol and return key<errcode> in json
     */
    private int errcode = 0;

    public int getErrcode()
    {
        parseJson();
        return errcode;

    }

    public void setErrcode(int errcode)
    {
        this.errcode = errcode;
    }

    /**
     * parse customer protocol and return key<errmsg> in json
     */
    private String errMsg = "";

    public String getErrMsg()
    {
        parseJson();
        return errMsg;
    }

    public void setErrMsg(String errMsg)
    {
        this.errMsg = errMsg;
    }

    private byte[] responseData = null;

    public byte[] getResponseData()
    {
        return responseData;
    }

    public void setResponseData(byte[] responseData)
    {
        this.responseData = responseData;
    }

    private JSONObject responseJson = null;

    public JSONObject getResponseJson()
    {
        parseJson();
        return responseJson;
    }

    private void parseJson()
    {

        if (errcode == RDHttpConnection.ERR_OK && responseJson == null && responseData != null)
        {
            try
            {
                responseJson = new JSONObject(new String(responseData, "utf-8"));
                if (responseJson != null)
                {
                    setErrcode(responseJson.optInt(JSON_ERR_CODE, errcode));
                    setErrMsg(responseJson.optString(JSON_ERR_MSG, errMsg));
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private long postFinished = 0;

    public long getPostFinished()
    {
        return postFinished;
    }

    public void setPostFinished(long postFinished)
    {
        this.postFinished = postFinished;
        if (request.processCallback != null)
        {
            request.processCallback.onProcessing(this);
        }
    }

    private long postTotal = 0;

    public long getPostTotal()
    {
        return postTotal;
    }

    public void setPostTotal(long postTotal)
    {
        this.postTotal = postTotal;
        if (request.processCallback != null)
        {
            request.processCallback.onProcessing(this);
        }
    }

    private long responseFinished = 0;

    public long getResponseFinished()
    {
        return responseFinished;
    }

    public void setResponseFinished(long responseFinished)
    {
        this.responseFinished = responseFinished;
        if (request.processCallback != null)
        {
            request.processCallback.onProcessing(this);
        }
    }

    private long responseTotal = 0;

    public long getResponseTotal()
    {
        return responseTotal;
    }

    public void setResponseTotal(long responseTotal)
    {
        this.responseTotal = responseTotal;
        if (request.processCallback != null)
        {
            request.processCallback.onProcessing(this);
        }
    }

    public RDHttpResponse()
    {

    }

}
