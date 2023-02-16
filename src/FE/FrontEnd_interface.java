package FE;

public interface FrontEnd_interface {
    void informRmHasBug(int RmNumber);

    void informRmIsDown(int RmNumber);

    int sendRequestToSequencer(Request myRequest);

    void retryRequest(Request myRequest);
}
