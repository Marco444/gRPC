package ar.edu.itba.pod.tpe1.client;

import airport.AirportService;
import counter.CounterReservationServiceGrpc;
import counter.CounterReservationServiceOuterClass;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.util.List;

public class CounterReservationClient {

    private final CounterReservationServiceGrpc.CounterReservationServiceBlockingStub blockingStub;

    public CounterReservationClient(Channel channel) {
        this.blockingStub = CounterReservationServiceGrpc.newBlockingStub(channel);
    }

    public void listSectors() {
        CounterReservationServiceOuterClass.SectorRequest request = CounterReservationServiceOuterClass.SectorRequest.newBuilder().build();
        try {
            CounterReservationServiceOuterClass.SectorResponse response = blockingStub.listSectors(request);
            if (response.getSectorsList().isEmpty()) {
                System.out.println("No sectors available at the airport.");
                return;
            }
            System.out.println("Sectors   Counters");
            System.out.println("###################");
            response.getSectorsList().forEach(sector -> {
                String ranges = sector.getRangesList().stream()
                        .map(range -> String.format("(%d-%d)", range.getStart(), range.getEnd()))
                        .reduce((a, b) -> a + " " + b).orElse("-");
                System.out.printf("%s         %s\n", sector.getName(), ranges);
            });
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }

    public void queryCounterRange(String sectorName, int fromVal, int toVal) {
        CounterReservationServiceOuterClass.CounterRangeRequest request = CounterReservationServiceOuterClass.CounterRangeRequest.newBuilder()
                .setSectorName(sectorName)
                .setFromVal(fromVal)
                .setToVal(toVal)
                .build();
        try {
            CounterReservationServiceOuterClass.CounterRangeResponse response = blockingStub.queryCounterRange(request);
            if (response.getCountersList().isEmpty()) {
                System.out.println("No counters found in the specified range.");
                return;
            }
            System.out.println("Counters  Airline          Flights             People");
            System.out.println("##########################################################");
            response.getCountersList().forEach(counter -> {
                String flights = String.join("|", counter.getFlightsList());
                String line = String.format("(%d-%d)     %s %s   %d",
                        counter.getStart(), counter.getEnd(), counter.getAirline(),
                        flights.isEmpty() ? "-" : flights, counter.getPeopleWaiting());
                System.out.println(line);
            });
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }

    public void assignCounters(String sectorName, List<String> flights, String airlineName, int counterCount) {
        CounterReservationServiceOuterClass.AssignCounterRequest request = CounterReservationServiceOuterClass.AssignCounterRequest.newBuilder()
                .setSectorName(sectorName)
                .addAllFlights(flights)
                .setAirlineName(airlineName)
                .setCounterCount(counterCount)
                .build();
        try {
            CounterReservationServiceOuterClass.AssignCounterResponse response = blockingStub.assignCounters(request);
            if(response.getIsPending()) {
                String airlines = String.join("|", flights);
                System.out.println(response.getCounterFrom() + "counters (" + response.getCounterFrom() + "-" + response.getCounterFrom() + counterCount + ") in Sector C are now checking in passengers from" +
                        airlineName + airlines + "flights\n");
            } else {
                System.out.println(counterCount + " counters in Sector " + sectorName +" is pending with " + response.getPendingAhead() + " other pendings ahead\n");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }

    public void freeCounters(String sectorName, int fromVal, String airlineName) {
        CounterReservationServiceOuterClass.FreeCounterRequest request = CounterReservationServiceOuterClass.FreeCounterRequest.newBuilder()
                .setSectorName(sectorName)
                .setFromVal(fromVal)
                .setAirlineName(airlineName)
                .build();
        try {
            CounterReservationServiceOuterClass.BasicResponse response = blockingStub.freeCounters(request);
            System.out.println(response.getMessage());
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }

    public void checkInCounters(String sectorName, int fromVal, String airlineName) {
        CounterReservationServiceOuterClass.CheckInCounterRequest request = CounterReservationServiceOuterClass.CheckInCounterRequest.newBuilder()
                .setSectorName(sectorName)
                .setFromVal(fromVal)
                .setAirlineName(airlineName)
                .build();
        try {
            CounterReservationServiceOuterClass.BasicResponse response = blockingStub.checkInCounters(request);
            System.out.println(response.getMessage());
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }

    public void listPendingAssignments(String sectorName) {
        CounterReservationServiceOuterClass.PendingAssignmentsRequest request = CounterReservationServiceOuterClass.PendingAssignmentsRequest.newBuilder()
                .setSectorName(sectorName)
                .build();
        try {
            CounterReservationServiceOuterClass.PendingAssignmentsResponse response = blockingStub.listPendingAssignments(request);
            if (response.getAssignmentsList().isEmpty()) {
                System.out.println("No pending assignments.");
                return;
            }
            System.out.println("Counters  Airline          Flights");
            System.out.println("##################################################");
            response.getAssignmentsList().forEach(assignment -> {
                String flights = String.join("|", assignment.getFlightsList());
                System.out.printf("%d         %s        %s\n",
                        assignment.getCounterCount(), assignment.getAirlineName(), flights);
            });
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }
}
