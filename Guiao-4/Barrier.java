import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier {
    private int n;
    private int counter;
    private int round;
    private ReentrantLock lock = new ReentrantLock();
    private Condition waitingForThreads = lock.newCondition();

    public Barrier(int n){
        this.n = n;
        this.counter = 0;
        this.round = 0;
    }

    public void awaitEx1() throws InterruptedException{
        this.lock.lock();
        this.counter++;

        if(this.counter < this.n){
            while(this.counter < this.n){ // sincroniza-las, enquanto não tiveram todas entrado, esperam
                this.waitingForThreads.await();
            }
        }
        else this.waitingForThreads.signalAll(); // apenas a última thread notificar para poupar trabalho as outras

        this.lock.unlock();
    }

    public void awaitEx2() throws InterruptedException{ // barreira reutilizavel
        this.lock.lock();
        this.counter++;

        if(this.counter < this.n ){
            int myRound = this.round;
            while(this.round == myRound){ // sincroniza-las, enquanto não tiveram todas entrado, esperam
                this.waitingForThreads.await();
            }
        }
        else {
            this.round++;
            this.counter = 0;
            this.waitingForThreads.signalAll();
        } // apenas a última thread notificar para poupar trabalho às outras

        this.lock.unlock();
    }
}



class BarrierMain{
    public static void main(String[] args) {
        int nThreads = 10;
        Barrier b = new Barrier(nThreads);
        Thread[] threads = new Thread[nThreads];

        for(int i = 0; i < nThreads; i++){

        }
    }

}