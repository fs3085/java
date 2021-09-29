**Java 中 int 和 Integer 的区别**

**1.** int 是基本数据类型，int 变量存储的是数值。Integer 是引用类型，实际是一个对象，Integer 存储的是引用对象的地址。

**2.**

```java
Integer i = new Integer(100);
Integer j = new Integer(100);
System.out.print(i == j); //false
```

因为 new 生成的是两个对象，其内存地址不同。

**3.**

int 和 Integer 所占内存比较：

Integer 对象会占用更多的内存。Integer 是一个对象，需要存储对象的元数据。但是 int 是一个原始类型的数据，所以占用的空间更少。

**4.** 非 new 生成的 Integer 变量与 **new Integer()** 生成的变量比较，结果为 false。

```java
/**
 * 比较非new生成的Integer变量与new生成的Integer变量
 */
public class Test {
    public static void main(String[] args) {
        Integer i= new Integer(200);
        Integer j = 200;
        System.out.print(i == j);
        //输出：false
    }
}
```

因为非 new 生成的 Integer 变量指向的是 java 常量池中的对象，而 **new Integer()** 生成的变量指向堆中新建的对象，两者在内存中的地址不同。所以输出为 false。

**5.** 两个非 new 生成的 Integer 对象进行比较，如果两个变量的值在区间 **[-128,127]** 之间，比较结果为 true；否则，结果为 false。

```java
/**
 * 比较两个非new生成的Integer变量
 */
public class Test {
    public static void main(String[] args) {
        Integer i1 = 127;
        Integer ji = 127;
        System.out.println(i1 == ji);//输出：true
        Integer i2 = 128;
        Integer j2 = 128;
        System.out.println(i2 == j2);//输出：false
    }
}
```

java 在编译 **Integer i1 = 127** 时，会翻译成 **Integer i1 = Integer.valueOf(127)**。

**6.** Integer 变量(无论是否是 new 生成的)与 int 变量比较，只要两个变量的值是相等的，结果都为 true。

```java
/**
 * 比较Integer变量与int变量
 */
public class Test {
    public static void main(String[] args) {
        Integer i1 = 200;
        Integer i2 = new Integer(200);
        int j = 200;
        System.out.println(i1 == j);//输出：true
        System.out.println(i2 == j);//输出：true
    }
}
```

包装类 Integer 变量在与基本数据类型 int 变量比较时，Integer 会自动拆包装为 int，然后进行比较，实际上就是两个 int 变量进行比较，值相等，所以为 true。



Ingeter是int的包装类。

int的初值为0。

Ingeter的初值为null。

```java
//声明一个Integer对象
Integer num = 10;
//以上的声明就是用到了自动的装箱：解析为
Integer num= Integer.valueOf(10);
int n= num; 
执行上面那句代码的时候，系统为我们执行了拆箱： 
int n= num.intValue();
```

1、**装箱：**Integer.valueOf函数

```java
private static final Integer[] SMALL_VALUES = new Integer[256]; 
public static Integer valueOf(int i) {
return  i >= 128 || i < -128 ? new Integer(i) : SMALL_VALUES[i + 128];
}
```

它会首先判断i的大小：如果i小于-128或者大于等于128，就创建一个Integer对象，否则返回一个已经存在的Integer数组一个位置的引用：SMALL_VALUES[i + 128]，它是一个静态的Integer数组对象，其中存储了-127到128。可以看出，在-127到128之间的使用自动装箱产生的Integer对象都不会产生新的Integer对象，并且在这个范围内值相同的Integer对象的引用也相同。

然后我们来看看Integer的构造函数：

```java
 private final int value;
 public Integer(int value) {
     this.value = value;
 }
 public Integer(String string) throws NumberFormatException {
     this(parseInt(string));
 }
```

它里面定义了一个value变量，创建一个Integer对象，就会给这个变量初始化。第二个传入的是一个String变量，它会先把它转换成一个int值，然后进行初始化。

2、**拆箱：**intValue函数

```java
 @Override
 public int intValue() {
    return value;
 }
```

这个很简单，直接返回value值即可。

一般进行数值计算或者使用到Integer对象的值得时候，需要自动拆包

