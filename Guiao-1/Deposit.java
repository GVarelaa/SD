public class Deposit implements Runnable{
    private Bank b;

    public Deposit(Bank b){
        this.b = b;
    }

    public void run(){
        for (int i = 0 ; i < 1000; i++){
            b.deposit(100);
        }
    }
}
