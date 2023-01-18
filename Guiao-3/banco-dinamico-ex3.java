import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// create e closeAccount sao de escrita
class Bank {

    private static class Account {
        private int balance;
        private ReentrantLock lock = new ReentrantLock();

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

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantReadWriteLock lockBank = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.WriteLock writeLock = lockBank.writeLock(); // (?)
    private ReentrantReadWriteLock.ReadLock readLock = lockBank.readLock(); // (?)


    // garantir exclusivadade mutua simples era adicionar lock a cada metodo a nivel do banco
    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);

        try{
            this.writeLock.lock();

            int id = nextId;
            nextId += 1;
            map.put(id, c);

            return id;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    // regra geral usar two phase locking
    // quanto mais lento for o deposito mais tempo fico com o lock do banco no outro lado (closeAccount)
    public int closeAccount(int id) {
        Account c;
        try{
            this.writeLock.lock();
            c = map.remove(id);
            if (c == null)
                return 0;

            c.lock.lock();
        }
        finally {
            this.writeLock.unlock();
        }

        try{
            return c.balance();
        }
        finally {
            c.lock.unlock();
        }

    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        try{
            this.readLock.lock();

            c = map.get(id);
            if (c == null)
                return 0;

            c.lock.lock();
        }
        finally {
            this.readLock.unlock();
        }

        try{
            return c.balance();
        }
        finally {
            c.lock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        this.readLock.lock();
        Account c;
        try{
            c = map.get(id); // operacao a nivel do banco

            if (c == null)
                return false;

            c.lock.lock(); // este metodo é o primeiro a ver o lock da conta
                           // lock na conta antes do unlock no banco para garantir que nenhuma operaçao
                           // (que vai ter o lock do banco a seguir) consegue aceder à conta antes desta
        }
        finally {
            this.readLock.unlock();

        }

        try{
            return c.deposit(value); // operacao a nivel da conta
        }
        finally {
            c.lock.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        try{
            this.readLock.lock();
            c = map.get(id);
            if (c == null)
                return false;

            c.lock.lock();
        }
        finally {
            this.readLock.unlock();
        }

        try{
            return c.withdraw(value);
        }
        finally {
            c.lock.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        try{
            this.readLock.lock();

            cfrom = map.get(from);
            cto = map.get(to);

            if (cfrom == null || cto ==  null)
                return false;

            cfrom.lock.lock();
            cto.lock.lock();
        }
        finally {
            this.readLock.unlock();
        }

        try{
            return cfrom.withdraw(value) && cto.deposit(value);
        }
        finally {
            cfrom.lock.unlock();
            cto.lock.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) { // lock ao banco, lock as contas, unlock ao banco, unlock as contas
        int total = 0;
        for (int i : ids) {
            Account c = map.get(i);
            if (c == null)
                return 0;
            total += c.balance();
        }
        return total;
    }

}