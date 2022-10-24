import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

class Bank {

    private static class Account {
        private int balance;
        ReentrantLock lock = new ReentrantLock();
        Account(int balance) { this.balance = balance; }

        int balance() { return balance; }

        boolean deposit(int value) {
            balance += value;
            return true;
        }

        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    // Bank slots and vector of accounts
    private int slots;
    private Account[] av;
    public Bank(int n)
    {
        slots=n;

        av=new Account[slots];
        for (int i=0; i<slots; i++) av[i]=new Account(0);

        this.lock = new ReentrantLock();
    }

    // Account balance
    public int balance(int id) {
        try{
            this.av[id].lock.lock();

            if (id < 0 || id >= slots)
                return 0;

            return av[id].balance();
        }
        finally {
            this.av[id].lock.unlock();
        }
    }

    // Deposit
    boolean deposit(int id, int value) {
        try{
            this.av[id].lock.lock();

            if (id < 0 || id >= slots)
                return false;

            return av[id].deposit(value);
        }
        finally {
            this.av[id].lock.unlock();
        }
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        try{
            this.av[id].lock.lock();

            if (id < 0 || id >= slots)
                return false;
            return av[id].withdraw(value);
        }
        finally {
            this.av[id].lock.unlock();
        }

    }

    public boolean transfer (int from, int to, int value){
            try{
                if(from > to) {
                    this.av[from].lock.lock();          //lock 1
                    this.withdraw(from, value);

                    this.av[to].lock.lock();              //lock 2
                    this.av[from].lock.unlock();             //unlock 1

                    this.deposit(to, value);
                    this.av[to].lock.unlock();               //unlock 2

                } else {
                    this.av[to].lock.lock();
                    this.av[from].lock.lock();
                }



                if (from < 0 || from >= slots)
                    return false;

                if (to < 0 || to >= slots)
                    return false;

                this.withdraw(from, value);
                this.deposit(to, value);

                return true;
            }
            finally {
                this.av[from].lock.unlock();
                this.av[to].lock.unlock();
            }
    }

    public int totalBalance(){
        try{
            int balance = 0;

            for(int i = 0; i < this.slots; i++){
                this.av[i].lock.lock();
                // ir libertando à medida
            }

            for(int i = 0; i < slots; i++) balance += av[i].balance();

            return balance;
        }
        finally {
            for(int i = 0 ; i < this.slots; i++)
                this.av[i].lock.unlock();
        }
    }
}

class Mover implements Runnable {
    Bank b;
    int s; // Number of accounts

    public Mover(Bank b, int s) { this.b=b; this.s=s; }

    public void run() {
        final int moves=100000;
        int from, to;
        Random rand = new Random();

        for (int m=0; m<moves; m++)
        {
            from=rand.nextInt(s); // Get one
            while ((to=rand.nextInt(s))==from); // Slow way to get distinct
            b.transfer(from,to,1);
        }
    }
}

class BankTest {
    public static void main(String[] args) throws InterruptedException {
        final int N=10;

        Bank b = new Bank(N);

        for (int i=0; i<N; i++)
            b.deposit(i,1000);

        System.out.println(b.totalBalance());

        Thread t1 = new Thread(new Mover(b,10));
        Thread t2 = new Thread(new Mover(b,10));

        t1.start(); t2.start(); t1.join(); t2.join();

        System.out.println(b.totalBalance());
    }
}
