public class MainEx2 {
    public static void main(String[] args){
        int N = 10;
        Bank b = new Bank();
        Thread threads[] = new Thread[N];

        for(int i = 0 ; i < N; i++){
            threads[i] = new Thread(new Deposit(b));
        }

        for(int i = 0 ; i < N ; i++){
            threads[i].start();
        }

        try{
            for(int i = 0 ; i < N; i++){
                threads[i].join();
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        System.out.println(b.balance());
    }
}
