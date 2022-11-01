import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Agreement{
    private int n;

    private int[] values;
    private int counter;

    private int round;
    private ReentrantLock lock = new ReentrantLock();

    private Condition waitingForThreads = lock.newCondition();


    public Agreement(int N){
        this.n = N;
        this.counter = 0;
        this.values = new int[N];
        this.round = 0;
    }

    public int proposeEx1(int choice) throws InterruptedException{
        try{
            this.lock.lock();

            this.values[counter] = choice;

            this.counter++;

            if(this.counter < this.n){
                while(this.counter < this.n){
                    this.waitingForThreads.await();
                }
            }
            else this.waitingForThreads.signalAll();

            int max = Integer.MIN_VALUE;

            for(int i = 0; i < this.n; i++){
                if (this.values[i] > max) {
                    max = this.values[i];
                }
            }

            return max;
        }
        finally {
            this.lock.unlock();
        }

    }

    public int proposeEx2(int choice) throws InterruptedException{
        try{
            this.lock.lock();

            this.values[counter] = choice;

            this.counter++;

            if(this.counter < this.n){
                int myRound = this.round;
                while(myRound == this.round){
                    this.waitingForThreads.await();
                }
            }
            else{
                this.round++;
                this.counter = 0;
                this.waitingForThreads.signalAll();
            }

            int max = Integer.MIN_VALUE;

            for(int i = 0; i < this.n; i++){
                if (this.values[i] > max) {
                    max = this.values[i];
                }
            }

            return max;
        }
        finally {
            this.lock.unlock();
        }

    }
}

class AgreementMain{
    public static void main(String[] args) {

    }
}