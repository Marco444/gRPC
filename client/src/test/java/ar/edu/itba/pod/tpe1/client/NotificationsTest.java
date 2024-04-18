package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationsTest {

    private static Logger logger = LoggerFactory.getLogger(Client.class);

    ManagedChannel channel;
    NotificationsClient notificationsClient;

    @Before
    public void setUp(){
        channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();
        notificationsClient = new NotificationsClient(channel);
    }

    @Test
    public void testRegisterAndUnregister() {


        // Thread 1
        Thread thread1 = new Thread(() -> {
            logger.info("Start register thread, register airline");
            notificationsClient.registerNotifications("AmericanAirlines");
            logger.info("Airline registered");
        });

        // Thread 2
        Thread thread2 = new Thread(() -> {
            logger.info("Start unregister thread");
            try {
                logger.info("Sleep register thread");
                Thread.sleep(500); // Sleep for 5 seconds
                logger.info("Wake up register thread, try unregister airline");
                notificationsClient.unregisterNotifications("AmericanAirlines");
                logger.info("Airline unregistered");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start both threads
        logger.info("Start both threads");
        thread1.start();
        thread2.start();
    }
}
