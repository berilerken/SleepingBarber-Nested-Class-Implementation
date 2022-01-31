// Java implementation of a producer and consumer
// that use semaphores to control synchronization.

import java.util.concurrent.Semaphore;
import java.util.Random;


class Ex5_SleepingBarber {

    static class BarberShop {
        // # of free waiting room seats
        int FreeWRseats;
        Random rand = new Random();

        // semCon initialized with 0 permits
        // to ensure put() executes first
        Semaphore semCustomer = new Semaphore(0);
        Semaphore MUTEX = new Semaphore(1);
        Semaphore semBarber = new Semaphore(1);
        Semaphore semBarberFinished= new Semaphore(0);
        int custcount=0;

        public BarberShop(int nc) {
            FreeWRseats= nc;
        }
    }

     static class Customer implements Runnable {
        int custid;
        int custcount=0;
        Random rand= new Random();
        BarberShop BS;
        public Customer(BarberShop BS) {
            this.BS= BS;
        }
        @Override
        public void run() {
            try {BS.MUTEX.acquire();} catch (Exception e) {}
            custcount= custcount+1;
            custid=custcount;
            BS.MUTEX.release();
            try { Thread.sleep(rand.nextInt(1000));}
            catch (Exception e) { }
            System.out.println("Customer:"+custid+" enters shop");
            try {BS.MUTEX.acquire();} catch (Exception e) {}
            if(BS.FreeWRseats<=0) {
                BS.MUTEX.release();
                System.out.println("Customer:"+custid+" leaves shop");
                return; // customer exits the BarberShop
            }
            System.out.println("Customer:"+custid+" takes seat");
            BS.FreeWRseats = BS.FreeWRseats - 1;
            BS.MUTEX.release();
            BS.semCustomer.release();
            System.out.println("Customer:"+custid+" waiting for Barber");
            try {BS.semBarber.acquire();} catch (Exception e) {}
            try {BS.MUTEX.acquire();} catch (Exception e) {}
            BS.FreeWRseats = BS.FreeWRseats + 1;
            BS.MUTEX.release();
            System.out.println("Customer:"+custid+" getting haircut");
            try {BS.semBarberFinished.acquire();} catch (Exception e) {}
            System.out.println("Customer:"+custid+" haircut finished");
        }
    }

    static class Barber implements Runnable {
        BarberShop BS;
        Random rand = new Random();
        public Barber(BarberShop BS) {
            this.BS= BS;
        }

        public void run() {
            while (true) {
                try {BS.semCustomer.acquire(); } catch (Exception e) {}
                System.out.println("Barber ready for customer");
                BS.semBarber.release();
                try {Thread.sleep(rand.nextInt(1000));} catch (Exception e) { }
                BS.semBarberFinished.release();
            }
        }
    }
    public static void main(String args[]) //#barbers  #chairs  #customers
    {
        int num_barbers, num_chairs, num_customers;
        num_barbers= Integer.parseInt(args[0]);
        num_chairs= Integer.parseInt(args[1]);
        num_customers = Integer.parseInt(args[2]);
        // creating buffer queue
        BarberShop BS1 = new BarberShop(num_chairs);

        // starting customer thread
        for(int i=0;i<num_customers;i++)
            new Thread(new Customer(BS1)).start();

        // starting barber thread
        for(int i=0;i<num_barbers;i++)
            new Thread(new Barber(BS1)).start();
    }
}