package Design_Patterns;

public class BankTest {
}

class Bank {
    private Bank() {
    }

    ;
    private static Bank instatnce = null;

    public static Bank getInstance() {
        //方式一：效率稍差
//        synchronized (Bank.class) {
//            if (instatnce == null) {
//                instatnce = new Bank();
//            }
//            return instatnce;
//        }

        //方式二：效率更高
        if (instatnce == null) {
            synchronized (Bank.class) {
                if (instatnce == null) {
                    instatnce = new Bank();
                }
            }
        }
        return instatnce;
    }
}