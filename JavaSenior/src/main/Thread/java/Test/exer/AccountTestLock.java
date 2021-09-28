package Test.exer;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 银行有一个账户。
 * 有两个储户分别向同一个账户存3000元，每次存1000，存3次。每次存完打印账户余额。
 * <p>
 * 分析：
 * 1.是否是多线程问题？ 是，两个储户线程
 * 2.是否有共享数据？ 有，账户（或账户余额）
 * 3.是否有线程安全问题？有
 * 4.需要考虑如何解决线程安全问题？同步机制：有三种方式。
 *
 * @author shkstart
 * @create 2019-02-15 下午 3:54
 */
class AccountLock {
    private double balance;

    public AccountLock(double balance) {
        this.balance = balance;
    }

    //存钱
    public void deposit(double amt) {
        if (amt > 0) {
            balance += amt;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(Thread.currentThread().getName() + ":存钱成功。余额为：" + balance);
        }
    }
}

class CustomerLock extends Thread {

    private AccountLock acct;
    private static final ReentrantLock lock = new ReentrantLock();

    public CustomerLock(AccountLock acct) {
        this.acct = acct;
    }

    @Override
    public void run() {
        try {
            lock.lock();
            for (int i = 0; i < 10; i++) {
                acct.deposit(1000);
            }
        } finally {
            lock.unlock();
        }
    }
}


public class AccountTestLock {

    public static void main(String[] args) {
        AccountLock acct = new AccountLock(0);
        CustomerLock c1 = new CustomerLock(acct);
        CustomerLock c2 = new CustomerLock(acct);

        c1.setName("甲");
        c2.setName("乙");

        c1.start();
        c2.start();
    }
}

