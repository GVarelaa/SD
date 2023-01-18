import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface ControloEntrada {
    void podeAbrirEntrada();
    void saiuPassageiro();
    void podeFecharEntrada();
    void entrouPassageiro(String bilhete);
}


public class ControloEntradaTeste implements ControloEntrada{
    private int pessoasNoFunicular;
    private final int N;
    private ReentrantLock lock;
    private Condition cond1;
    private Condition cond2;

    public ControloEntradaTeste(int pessoasNoFunicular) {
        this.pessoasNoFunicular = pessoasNoFunicular;
        this.N = 10;
        this.lock = new ReentrantLock();
        this.cond1 = this.lock.newCondition();
        this.cond2 = this.lock.newCondition();
    }

    @Override
    public void podeAbrirEntrada() {
        this.lock.lock();
        while(this.pessoasNoFunicular > 0){
            this.cond1.await();
        }
        this.lock.unlock();
    }

    @Override
    public void saiuPassageiro() {
        this.lock.lock();

        this.pessoasNoFunicular--;
        if(this.pessoasNoFunicular == 0) this.cond1.signalAll();

        this.lock.unlock();
    }

    @Override
    public void podeFecharEntrada() {
        this.lock.lock();
        while(this.pessoasNoFunicular < N){
            this.cond2.await();
        }
        this.lock.unlock();
    }

    @Override
    public void entrouPassageiro(String bilhete) {
        this.lock.lock();

        this.pessoasNoFunicular++;
        if(this.pessoasNoFunicular == N) this.cond2.signalAll();

        this.lock.unlock();
    }
}
