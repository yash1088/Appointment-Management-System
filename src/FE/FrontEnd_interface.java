package FE;
/**
 * @author Pratik Gondaliya
 * @created 30/03/2022
 */
public interface FrontEnd_interface {
    void informRmHasBug(int RmNumber);

    void informRmIsDown(int RmNumber);

    int sendRequestToSequencer(Request myRequest);

    void retryRequest(Request myRequest);
}
