 import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


class WarehouseEx2 {
  private Map<String, Product> map =  new HashMap<String, Product>();
  private ReentrantLock lock = new ReentrantLock();
  private Condition c = lock.newCondition();
  private class Product {
    int quantity = 0;
    ReentrantLock lock = new ReetrantLock();
    Condition c = lock.newCondition();
  }

  private Product get(String item) {
    Product p = map.get(item);
    if (p != null) return p;
    p = new Product();
    map.put(item, p);
    return p;
  }

  public void supply(String item, int quantity) {
    try{
      this.lock.lock();

      Product p = get(item);

      p.lock.lock()
    }
    finally {
      this.lock.unlock();
    }

    try{
      p.quantity += quantity;
    }
    finally {
      p.lock.unlock();
    }
  }

  // Errado se faltar algum produto...
  public void consume(Set<String> items) {
      this.lock.lock();
      int i = 0;

      for (String s : items){
        Product p = this.get(s);
        quantity =

        while(p.quantity < 0){
          p.c.await();
        }

        p.quantity--;
      }

  }

}
