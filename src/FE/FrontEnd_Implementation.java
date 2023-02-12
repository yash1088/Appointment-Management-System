package FE;

import FE.SendData.SendData_interfacePOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Pratik Gondaliya
 * @created 06/04/2022
 */


public class FrontEnd_Implementation extends SendData_interfacePOA {
    private static long DYNAMIC_TIMEOUT = 10000;
    private static int Rm1BugCount = 0;
    private static int Rm2BugCount = 0;
    private static int Rm3BugCount = 0;
    private static int Rm4BugCount = 0;
    int i=1;

    private static int Rm1NoResponseCount = 0;
    private static int Rm2NoResponseCount = 0;
    private static int Rm3NoResponseCount = 0;
    private static int Rm4NoResponseCount = 0;

    private long responseTime = DYNAMIC_TIMEOUT;
    private long startTime;
    private CountDownLatch latch;
    private final FrontEnd_interface inter;
    private final List<Response> responses = new ArrayList<>();
    private ORB orb;

    public FrontEnd_Implementation(FrontEnd_interface inter) {
        super();
        this.inter = inter;
    }

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public void waitingForReply() {
        try {
            System.out.println("Waiting for Responses...ResponsesRemained:- " + latch.getCount());
            boolean timeoutReached = latch.await(DYNAMIC_TIMEOUT, TimeUnit.MILLISECONDS);
            if (timeoutReached) {
                setDynamicTimout();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String checkResponse(Request Request) {
        String resp;
        switch ((int) latch.getCount()) {
            case 0:
            case 1:
            case 2:
            case 3:
                resp = findMajorityResponse(Request);
                break;
            case 4:
                resp = "Request has been failed: No response found from any server";
                System.out.println(resp);
                if (Request.haveRetries()) {
                    Request.countRetry();
                    resp = retryRequest(Request);
                }
                rmDown(1);
                rmDown(2);
                rmDown(3);
                rmDown(4);
                break;
            default:
                resp = "Request Fail: " + Request.noRequestSendError();
                break;
        }
        System.out.println("Following response has been sended to client:- " + resp);
        return resp;
    }

    private String findMajorityResponse(Request Request) {
        Response res1 = null;
        Response res2 = null;
        Response res3 = null;
        Response res4 = null;
        for (Response response :
                responses) {
            if (response.getSequenceID() == Request.getSequenceNumber()) {
                switch (response.getRmNumber()) {
                    case 1:
                        res1 = response;
                        break;
                    case 2:
                        res2 = response;
                        break;
                    case 3:
                        res3 = response;
                        break;
                    case 4:
                        res4 = response;
                        break;
                }
            }
        }
        System.out.println("Response from RM1:- " + ((res1 != null) ? res1.getResponse() : "null"));
        System.out.println("Response from RM2:- " + ((res2 != null) ? res2.getResponse() : "null"));
        System.out.println("Response from RM3:- " + ((res3 != null) ? res3.getResponse() : "null"));
        System.out.println("Response from RM4:- " + ((res4 != null) ? res4.getResponse() : "null"));
        //System.out.println("Respnse-4:................"+res4.getResponse());

        if (res1 == null) {
            rmDown(1);
        } else {
            Rm1NoResponseCount = 0;
            if (res1.equals(res2)) {
                if (!res1.equals(res3) && res3 != null) {
                    rmBugFound(3);
                }
                return res2.getResponse();
            } else if (res1.equals(res3)) {
                if (!res1.equals(res2) && res2 != null) {
                    rmBugFound(2);
                }
                return res1.getResponse();
            } else {
                if (res2 == null && res3 == null) {
                    return res1.getResponse();
                } else {
                }
            }
        }
        if (res2 == null) {
            rmDown(2);
        } else {
            Rm2NoResponseCount = 0;
            if (res2.equals(res3)) {
                if (!res2.equals(res1) && res1 != null) {
                    rmBugFound(1);
                }
                return res2.getResponse();
            } else if (res2.equals(res1)) {
                if (!res2.equals(res3) && res3 != null) {
                    rmBugFound(3);
                }
                return res2.getResponse();
            } else {
                if (res1 == null && res3 == null) {
                    return res2.getResponse();
                } else {
                }
            }
        }
        if (res3 == null) {
            rmDown(3);
        } else {
            Rm3NoResponseCount = 0;
            if (res3.equals(res4)) {
                if (!res3.equals(res4) && res1 != null) {
                    rmBugFound(1);
                }
                return res2.getResponse();
            } else if (res3.equals(res4) && res2 != null) {
                if (!res3.equals(res2)) {
                    rmBugFound(2);
                }
                return res3.getResponse();
            } else {
                if (res1 == null && res2 == null) {
                    return res3.getResponse();
                } else {
                }
            }
        }

        if (res4 == null) {
            rmDown(4);
        } else {
            Rm4NoResponseCount = 0;
            if (res4.equals(res3)) {
                if (!res4.equals(res3) && res1 != null) {
                    rmBugFound(1);
                }
                return res2.getResponse();
            } else if (res3.equals(res1) && res2 != null) {
                if (!res3.equals(res2)) {
                    rmBugFound(2);
                }
                return res3.getResponse();
            } else {
                if (res1 == null && res2 == null) {
                    return res3.getResponse();
                } else {
                }
            }
        }
        return "Request failed:response not found";
    }

    private void rmBugFound(int rmNumber) {
        switch (rmNumber) {
            case 1:
                Rm1BugCount++;
                if (Rm1BugCount == 3) {
                    Rm1BugCount = 0;
                    inter.informRmHasBug(rmNumber);
                }
                break;
            case 2:
                Rm2BugCount++;
                if (Rm2BugCount == 3) {
                    Rm2BugCount = 0;
                    inter.informRmHasBug(rmNumber);
                }
                break;

            case 3:
                Rm3BugCount++;
                if (Rm3BugCount == 3) {
                    Rm3BugCount = 0;
                    inter.informRmHasBug(rmNumber);
                }
                break;

            case 4:
                Rm4BugCount++;
                if (Rm4BugCount == 3) {
                    Rm4BugCount = 0;
                    inter.informRmHasBug(rmNumber);
                }
                break;
        }
        System.out.println("RM1 - bugs:" + Rm1BugCount);
        System.out.println("RM2 - bugs:" + Rm2BugCount);
        System.out.println("RM3 - bugs:" + Rm3BugCount);
        System.out.println("RM4 - bugs:" + Rm4BugCount);
    }

    private void rmDown(int rmNumber) {
        DYNAMIC_TIMEOUT = 10000;
        switch (rmNumber) {
            case 1:
                Rm1NoResponseCount++;
                System.out.println("Null Count Incremented for RM1");
                if (Rm1NoResponseCount == 3) {
                    Rm1NoResponseCount = 0;
                    inter.informRmIsDown(rmNumber);
                }
                break;
            case 2:
                Rm2NoResponseCount++;
                System.out.println("Null Count Incremented for RM2");

                if (Rm2NoResponseCount == 3) {
                    Rm2NoResponseCount = 0;
                    inter.informRmIsDown(rmNumber);
                }
                break;

            case 3:
                Rm3NoResponseCount++;
                System.out.println("Null Count Incremented for RM3");

                if (Rm3NoResponseCount == 3) {
                    Rm3NoResponseCount = 0;
                    inter.informRmIsDown(rmNumber);
                }
                break;

            case 4:
                Rm4NoResponseCount++;
                System.out.println("Null Count Incremented for RM4");

                if (Rm4NoResponseCount == 4) {
                    Rm4NoResponseCount = 0;
                    inter.informRmIsDown(rmNumber);
                }
                break;
        }
        System.out.println("RM1 - noResponse:" + Rm1NoResponseCount);
        System.out.println("RM2 - noResponse:" + Rm2NoResponseCount);
        System.out.println("RM3 - noResponse:" + Rm3NoResponseCount);
        System.out.println("RM4 - noResponse:" + Rm4NoResponseCount);
    }

    private void setDynamicTimout() {
        if (responseTime < 4000) {
            DYNAMIC_TIMEOUT = (DYNAMIC_TIMEOUT + (responseTime * 3)) / 2;
        } else {
            DYNAMIC_TIMEOUT = 10000;
        }
        System.out.println("FrontEnd Implementation:setDynamicTimout" + DYNAMIC_TIMEOUT);
    }

    private void notifyOKCommandReceived() {
        latch.countDown();
        System.out.println("NotifyOKCommandReceived Response Received: Remaining responses:- " + latch.getCount());
    }

    public void addReceivedResponse(Response res) {
        long endTime = System.nanoTime();
        responseTime = (endTime - startTime) / 1000000;
        System.out.println("Current Response time is: " + responseTime);
        responses.add(res);
        System.out.println("SIZE OF RES..........."+responses.size()+"\n");
        notifyOKCommandReceived();
    }

    private int sendRequestToSequencer(Request Request) {
        startTime = System.nanoTime();
        int sequenceNumber = inter.sendRequestToSequencer(Request);
        Request.setSequenceNumber(sequenceNumber);
        latch = new CountDownLatch(4);
        waitingForReply();
        return sequenceNumber;
    }

    private String retryRequest(Request Request) {
        System.out.println("FrontEnd :retryRequest:" + Request.toString());
        startTime = System.nanoTime();
        inter.retryRequest(Request);
        latch = new CountDownLatch(4);
        waitingForReply();
        return checkResponse(Request);
    }

    public String sayHello() {
        return null;
    }

    public void shutdown() {
    }

    public boolean authenticate(String client_userName, String client_password) {
        return false;
    }

    public String printMsg(String msg) {
        return null;
    }

    public String add_appointment(String appointmentType, String appointmentID, int slot_numbers, String admin_Id) {
        Request request = new Request("ADD_APPOINTMENT", admin_Id);
        request.setAppointmentID(appointmentID);
        request.setAppointmentType(appointmentType);
        request.setBookingCapacity(slot_numbers);
        request.setSequenceNumber(sendRequestToSequencer(request));

        //System.out.println("/////////////////////////////////////////"+slot_numbers);
        System.out.println("Add Appointment called>>>" + request.toString());
        return checkResponse(request);
    }

    public String list_appointment_availability(String appointment_type,String admin_ID) {
        Request request = new Request("LIST_AVAILABLE_APPOINTMENT", admin_ID);
        request.setAppointmentType(appointment_type);
        request.setSequenceNumber(sendRequestToSequencer(request));
        System.out.println("List Appointment Availability called" + request.toString());
        return checkResponse(request);
    }

    public String book_appointment(String patient_Id, String appointmentID, String appointmentType) {
        Request request = new Request("BOOK_APPOINTMENT", patient_Id);
        request.setAppointmentID(appointmentID);
        request.setAppointmentType(appointmentType);
        request.setSequenceNumber(sendRequestToSequencer(request));
        System.out.println("Book Appointment called" + request.toString());
        return checkResponse(request);
    }

    public String get_appointment_schedule(String patient_Id) {
        Request request = new Request("GET_APPOINTMENT_SCHEDULE", patient_Id);
        request.setSequenceNumber(sendRequestToSequencer(request));
        System.out.println("Patient Scheduled Appointment called" + request.toString());
        return checkResponse(request);
    }

    public String cancel_appointment(String patient_Id, String appointmentID, String appointmentType) {
        Request request = new Request("CANCEL_APPOINTMENT", patient_Id);
        request.setAppointmentID(appointmentID);
        request.setAppointmentType(appointmentType);
        request.setSequenceNumber(sendRequestToSequencer(request));
        System.out.println("Cancel Appointment called" + request.toString());
        return checkResponse(request);
    }
    public String remove_appointment(String appointmentID, String appointmentType, String patientID) {
        Request request = new Request("REMOVE_APPOINTMENT", patientID);
        request.setAppointmentID(appointmentID);
        request.setAppointmentType(appointmentType);
        request.setSequenceNumber(sendRequestToSequencer(request));
        System.out.println("Remove Appointment called" + request.toString());
        return checkResponse(request);
    }
    public String swap_appointment(String patientID, String old_appointmentID, String old_appointmentType, String new_appointmentID, String new_appointmentType) {
        Request myRequest = new Request("SWAP_APPOINTMENT", patientID);
        myRequest.setAppointmentID(new_appointmentID);
        myRequest.setAppointmentType(new_appointmentType);
        myRequest.setOldAppointmentID(old_appointmentID);
        myRequest.setOldAppointmentType(old_appointmentType);
        myRequest.setSequenceNumber(sendRequestToSequencer(myRequest));
        System.out.println("Swap Appointment called" + myRequest.toString());
        return checkResponse(myRequest);
    }

    public String fault(String admin_ID) {
        Request request;
        i++;
        if(admin_ID.length()==8){
           request  = new Request("FAULT", admin_ID+i);
            request.setSequenceNumber(sendRequestToSequencer(request));
            System.out.println("Fault Called:- " + request.toString());
            return checkResponse(request);
        }
        else {
            request  = new Request("FAULT", admin_ID.substring(0,8)+i);

            request.setMessageType("22");
            request.setSequenceNumber(sendRequestToSequencer(request));
            System.out.println("Fault Called 22" + request.toString());
            return checkResponse(request);
        }


    }

    @Override
    public String crash(String adminID) {
        return null;
    }

    @Override
    public String general(String adminID) {
        return null;
    }
}
