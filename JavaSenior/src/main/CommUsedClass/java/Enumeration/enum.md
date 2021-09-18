**Java使用`enum`定义枚举类型，它被编译器编译为`final class Xxx extends Enum { … }`；**

例如，我们定义的`Color`枚举类：

```java
public enum Color {
    RED, GREEN, BLUE;
}
```

编译器编译出的`class`大概就像这样：

```java
public final class Color extends Enum { // 继承自Enum，标记为final class
    // 每个实例均为全局唯一:
    public static final Color RED = new Color();
    public static final Color GREEN = new Color();
    public static final Color BLUE = new Color();
    // private构造方法，确保外部无法调用new操作符:
    private Color() {}
}
```



**通过`name()`获取常量定义的字符串，注意不要使用`toString()`；**

默认情况下，对枚举常量调用`toString()`会返回和`name()`一样的字符串。但是，`toString()`可以被覆写，而`name()`则不行

 注意：判断枚举常量的名字，要始终使用name()方法，绝不能调用toString()！



通过`ordinal()`返回常量定义的顺序（无实质意义）；



**可以为`enum`编写构造方法、字段和方法**

**`enum`的构造方法要声明为`private`，字段强烈建议声明为`final`；**

 注意：枚举类的字段也可以是非final类型，即可以在运行期修改，但是不推荐这样做！

```java
public class Main {
    public static void main(String[] args) {
        Weekday day = Weekday.SUN;
        if (day.dayValue == 6 || day.dayValue == 0) {
            System.out.println("Today is " + day + ". Work at home!");
        } else {
            System.out.println("Today is " + day + ". Work at office!");
        }
    }
}

enum Weekday {
    MON(1, "星期一"), TUE(2, "星期二"), WED(3, "星期三"), THU(4, "星期四"), FRI(5, "星期五"), SAT(6, "星期六"), SUN(0, "星期日");

    public final int dayValue;
    private final String chinese;
    
    private Weekday(int dayValue, String chinese) {
        this.dayValue = dayValue;
        this.chinese = chinese;
    }
    
    @Override
    public String toString() {
        return this.chinese;
    }

}
```



**`enum`适合用在`switch`语句中。**

```java
 public class Main {
    public static void main(String[] args) {
        Weekday day = Weekday.SUN;
        switch(day) {
        case MON:
        case TUE:
        case WED:
        case THU:
        case FRI:
            System.out.println("Today is " + day + ". Work at office!");
            break;
        case SAT:
        case SUN:
            System.out.println("Today is " + day + ". Work at home!");
            break;
        default:
            throw new RuntimeException("cannot process " + day);
        }
    }
}

enum Weekday {
    MON, TUE, WED, THU, FRI, SAT, SUN;
}
```







