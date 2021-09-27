Java `transient`关键字用于类属性/变量，表示该类的序列化过程在为该类的任何实例创建持久字节流时应该忽略此类变量。

`transient`变量是不能序列化的变量。根据Java语言规范[[jls-8.3.1.30](https://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.3.1.3)] -“变量可以标记为`transient`，以表明它们不是对象的持久状态的一部分。”

讨论有关在`serialization`上下文中使用transient`关键字的各种概念。

## 1. Java transient关键字是什么？

java中的修饰符transient可以应用于类的字段成员，以关闭这些字段成员的序列化。每个标记为`transient`的字段将不会序列化。使用`transient`关键字向java虚拟机表明，`transient`变量不是对象的持久状态的一部分。

让我们写一个非常基本的例子来理解上面的类比到底是什么意思。我将创建一个`Employee`类并定义3个属性，即`firstName`，`lastName`和`confidentialInfo`。由于某些原因，我们不想存储/保存`confidentialInfo`，因此我们将该字段标记为`transient`。

```java
public class Employee implements Serializable {
    private static final long serialVersionUID = 2624368016355021172L;

    private String           firstName;
    private String           lastName;
    private transient String confidentialInfo;

    // Getter and Setter
}
```

现在让我们序列化一个`Employee`类的实例

```csharp
public class TransSerializationTest {
    public static void main(String[] args) {
        try {
            Employee emp = new Employee();
            emp.setFirstName("ag");
            emp.setLastName("qg");
            emp.setConfidentialInfo("password");

            System.out.println("Read before Serialization:");
            System.out.println("firstName: " + emp.getFirstName());
            System.out.println("lastName: " + emp.getLastName());
            System.out.println("confidentialInfo: " + emp.getConfidentialInfo());

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("E:/chenss.txt"));
            //Serialize the object
            oos.writeObject(emp);
            oos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
```

现在我们反序列化到Java对象中，并校验`confidentialInfo`对象是否被保存下来？

```csharp
public class TransDeSerializationTest {
    public static void main(String[] args) {
        try {
            ObjectInputStream ooi = new ObjectInputStream(new FileInputStream("E:/chenss.txt"));
            //Read the object back
            Employee readEmpInfo = (Employee) ooi.readObject();
            System.out.println("Read From Serialization:");
            System.out.println("firstName: " + readEmpInfo.getFirstName());
            System.out.println("lastName: " + readEmpInfo.getLastName());
            System.out.println("confidentialInfo: " + readEmpInfo.getConfidentialInfo());
            ooi.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
```

运行结果输出：

```csharp
Read From Serialization:
ag
qg
null
```

很明显，`confidentialInfo`在序列化的时候没有被保存到持久对象，这也正是我们在java中使用`transient`关键字的原因。

## 2. 什么时候应该在java中使用transient关键字？

现在对于`transient`关键字非常了解了。通过确定需要使用`transient`关键字的场景来扩展理解。

1. 首先，非常符合逻辑的情况是，您可能有**从类实例中的其他字段派生/计算的字段**。它们应该每次都以编程方式计算，而不是通过序列化来持久化状态。一个例子是基于时间戳的值；例如一个人的年龄或时间戳与当前时间戳之间的持续时间。在这两种情况下，您将根据当前系统时间而不是在序列化实例时计算变量的值。
2. 第二个逻辑示例可以是不应该以任何形式(数据库或字节流)泄漏到JVM外部的**任何安全信息**。
3. 另一个例子是JDK或应用程序代码中**没有标记为`Serializable`的字段**。不实现可序列化接口且在任何可序列化类中引用的类不能序列化；并将抛出异常`java.io.NotSerializableException`。在序列化主类之前，这些不可序列化的引用应该标记为`transient`。
4. 最后，有时**序列化某些字段是没有意义的**。例如，在任何类中，如果您添加了一个`logger`引用，那么序列化该`logger`实例有什么用呢？绝对用不上。逻辑上，你只会序列化表示实例状态的信息。`Loggers`从不共享实例的状态。它们只是用于编程/调试目的的实用程序。类似的例子可以参照线程类的引用。线程表示进程在任何给定时间点的状态，并且不需要用实例存储线程状态；仅仅因为它们不构成类实例的状态。

以上四个用例是您应该在引用变量中使用`transient`关键字的时候。如果您有更多可以使用`transient`的逻辑情况，请与我分享，我会在这里更新列表，让每个人都能从你的知识中受益。

> 阅读更多：[实现可序列化接口的简单指南](https://howtodoinjava.com/java/serialization/a-mini-guide-for-implementing-serializable-interface-in-java/)

## 3. Transient 和 final

说的是在`final`关键字中使用`transient`，因为它在不同的情况下有不同的行为，而java中的其他关键字通常不是这样。

为了使这个概念更实际，我对`Employee`类进行了如下修改:

```java
public class Employee implements Serializable {
    private static final long serialVersionUID = 2624368016355021172L;

    private String           firstName;
    private String           lastName;
    //final field 1
    public final transient String confidentialInfo = "password";
    //final field 2
    private final transient Logger logger = Logger.getLogger("demo");

    //Getter and Setter
}
```

现在当我重新运行序列化（写/读）的时候，会有如下的输出内容：

```csharp
Read From Serialization:
firstName: ag
lastName: qg
confidentialInfo: password
logger: null
```

很奇怪。我们已将`confidentialInfo`标记为`transient`；字段仍然被序列化了。而对于类似的声明，`logger`却没有被序列化。为什么?

原因是，无论何时将任何`final`字段/引用计算为“常量表达式”，JVM都会对其进行序列化，忽略`transient`关键字的存在。

在上面的例子中，值`password`是一个常量表达式，`logger` `demo`的实例是引用。因此，根据规则，`confidentialInfo`被持久化，而`logger`没有被持久化。

您是否在想，如果我从两个字段中删除`transient`呢？那么，实现可序列化引用的字段将保持不变。因此，如果在上面的代码中删除`transient`，`String`(实现`Serializable`)将被持久化；而`Logger`(不实现`Serializable`)将不会被持久化，并且将会抛出异常`java.io.NotSerializableException`。

*如果希望持久保存不可序列化字段的状态，那么可以使用`readObject()`和`writeObject()`方法。`writeObject()`/`readObject()`通常在内部链接到序列化/反序列化机制中，因此会自动调用。*

> 阅读更多：[java中的SerialVersionUID和相关的快速事实](https://howtodoinjava.com/java/serialization/serialversionuid/)

## 4. 案例研究：HashMap如何使用transient关键字？

到目前为止，我们一直在讨论与`transient`关键字相关的概念，这些概念基本上都是理论性的。让我们了解一下在`HashMap`类中逻辑地使用`transient`的正确用法。它将使您更好地了解java中`transient`关键字的实际用法。

在理解使用`transient`创建的解决方案之前，让我们先确定问题本身。

`HashMap`用于存储键-值对，这一点我们都知道。我们还知道`HashMap`中键的位置是根据键实例的哈希码计算的。现在，当我们序列化一个`HashMap`时，这意味着`HashMap`中的所有键以及与键相关的所有值也将被序列化。序列化之后，当我们反序列化HashMap实例时，所有关键实例也将被反序列化。我们知道在这个序列化/反序列化过程中，可能会丢失信息(用于计算`hashcode`)，最重要的是它本身是一个新实例。

在java中，任何两个实例(甚至是相同类的实例)都不能有相同的`hashcode`。这是一个大问题，因为应该根据新的`hashcode`放置键的位置不正确。当检索键的值时，您将在这个新的`HashMap`中引用错误的索引。

> 阅读更多：[使用java中的hashCode和equals方法](https://howtodoinjava.com/java/basics/java-hashcode-equals-methods/)

因此，当一个哈希表被序列化时，它意味着哈希索引，和表的顺序不再有效，不应该被保留。这是问题陈述。

现在看看如何在`HashMap`类中解决这个问题。如果通过`HashMap.java`的源代码。你会发现下面的声明:

```java
    transient Node<K,V>[] table;
    transient Set<Map.Entry<K,V>> entrySet;
    transient int size;
    transient int modCount;
```

所有重要字段都标记为`transient`(所有字段实际上都是在运行时计算/更改的)，因此它们不是序列化`HashMap`实例的一部分。为了再次填充这个重要的信息，HashMap类使用`writeObject()`和`readObject()`方法，如下所示:

```java
private void writeObject(java.io.ObjectOutputStream s)
    throws IOException {
    int buckets = capacity();
    // Write out the threshold, loadfactor, and any hidden stuff
    s.defaultWriteObject();
    s.writeInt(buckets);
    s.writeInt(size);
    internalWriteEntries(s);
}
void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
    Node<K,V>[] tab;
    if (size > 0 && (tab = table) != null) {
        for (Node<K,V> e : tab) {
            for (; e != null; e = e.next) {
                s.writeObject(e.key);
                s.writeObject(e.value);
            }
        }
    }
}
```



```java
private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " +
                                             loadFactor);
        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                                             mappings);
        else if (mappings > 0) { // (if zero, use defaults)
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);

            // Check Map.Entry[].class since it's the nearest public type to
            // what we're actually creating.
            SharedSecrets.getJavaObjectInputStreamAccess().checkArray(s, Map.Entry[].class, cap);
            @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                    K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }
```

使用上面的代码，HashMap仍然允许像通常那样处理非`transient`字段，但是它们在字节数组的末尾一个接一个地写存储的键-值对。在反序列化时，它允许默认情况下处理的非`transient`变量，然后逐个读取键-值对。对于每个键，哈希值和索引将被再次计算，并被插入到表中的正确位置，以便再次检索时不会出现任何错误。

上面使用`transient`关键字就是一个很好的例子。您应该记住它，并在下一次java面试问题中提到它。

> 相关帖子：[HashMap在Java中是如何工作的？](https://howtodoinjava.com/java/collections/how-hashmap-works-in-java/)

## 5. 摘要说明

1. 修饰符`transient`可以应用于类的字段成员，以关闭这些字段成员的序列化。
2. 你可以在需要对现有状态字段进行保护或计算的字段的类中使用`transient`关键字。当序列化那些字段(如日志记录器和线程)毫无意义时，可以使用它。
3. 序列化不关心访问修饰符，如`private`；所有非`transient`字段都被认为是对象持久状态的一部分，并且都符合持久状态的条件。
4. 无论何时将任何`final`字段/引用计算为“常量表达式”，JVM都会对其进行序列化，忽略`transient`关键字的存在。
5. `HashMap`类是java中`transient`关键字的一个很好的用例。