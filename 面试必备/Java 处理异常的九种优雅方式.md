在本文中，介绍了 9 个处理异常的最佳方法与实践，以举例与代码展示结合的方式，让开发者更好的理解这 9 种方式，并指导读者在不同情况下选择不同的异常处理方式。

以下为译文：

Java 中的异常处理不是一个简单的话题。初学者很难理解，甚至有经验的开发人员也会花几个小时来讨论应该如何抛出或处理这些异常。这就是为什么大多数开发团队都有自己的异常处理的规则和方法。如果你是一个团队的新手，你可能会惊讶于这些方法与你之前使用过的那些方法有多么不同。然而，有几种异常处理的最佳方法被大多数开发团队所使用。

下面是帮助改进异常处理的 9 个最重要的方法。

#### 1. 在 finally 中清理资源或者使用 Try-With-Resource 语句

通常情况下，你在try中使用了一个资源，比如 InputStream，之后需要关闭它。在这种情况下，一个常见的错误是在 try 的末尾关闭了资源。

```java
public void doNotCloseResourceInTry() {
    FileInputStream inputStream = null;
    try {
        File file = new File("./tmp.txt");
        inputStream = new FileInputStream(file);
        // use the inputStream to read a file
        // do NOT do this
        inputStream.close();
    } catch (FileNotFoundException e) {
        log.error(e);
    } catch (IOException e) {
        log.error(e);
    }
}
```

问题是，只要不抛出异常，这种方法就可以很好地运行。try 内的所有语句都将被执行，资源也会被关闭。但是你在 try 里调用了一个或多个可能抛出异常的方法，或者自己抛出异常。这意味着可能无法到达 try 的末尾。因此将不会关闭这些资源。所以应该将清理资源的代码放入finally中，或者使用 Try-With-Resource 语句。

##### 使用 finally

相比于 try，无论是在成功执行 try 里的代码后，或是在 catch 中处理了一个异常后，finally 里的内容是一定会被执行的。因此，可以确保清理所有已打开的资源。

```java
public void closeResourceInFinally() {
    FileInputStream inputStream = null;
    try {
        File file = new File("./tmp.txt");
        inputStream = new FileInputStream(file);
        // use the inputStream to read a file
    } catch (FileNotFoundException e) {
        log.error(e);
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}
```

##### Java 7 的 Try-With-Resource 语句

另一个选择是 Try-With-Resource 语句，如果你的资源实现了 AutoCloseable 接口，就可以使用它，这正是大多数 Java 标准资源所做的。当你在 try 子句中打开资源时，它将在 try 被执行后自动关闭，或者处理一个异常。

```java
public void automaticallyCloseResource() {
    File file = new File("./tmp.txt");
    try (FileInputStream inputStream = new FileInputStream(file);) {
        // use the inputStream to read a file
    } catch (FileNotFoundException e) {
        log.error(e);
    } catch (IOException e) {
        log.error(e);
    }
}
```

#### 2. 给出准确的异常处理信息

你抛出的异常越具体越好。一定要记住，一个不太了解你代码的同事，也许几个月后，需要调用你的方法，并且处理这个异常。

因此，请确保提供尽可能多的信息，这会使你的 API 更容易理解。因此，你方法的调用者将能够更好地处理异常，或者通过额外的检查来避免它。

所以，要尽量能更好地描述你的异常处理信息，比如用 NumberFormatException 代替IllegalArgumentException，避免抛出一个不具体的异常。

```java
public void doNotDoThis() throws Exception {
    ...
}
public void doThis() throws NumberFormatException {
    ...
}
```

#### 3. 记录你所指定的异常

当你在方法中指定一个异常时，你应该在 Javadoc 中记录下它。这与前面提到的方法有着相同的目标：为调用者提供尽可能多的信息，这样他们就可以避免异常或者更容易地处理异常。因此，请确保在 Javadoc 中添加一个@throws 声明，并描述可能导致的异常情况。

```java
/**
 * This method does something extremely useful ...
 *
 * @param input
 * @throws MyBusinessException if ... happens
 */
public void doSomething(String input) throws MyBusinessException {
    ...
}
```

#### 4. 使用描述性消息抛出异常

这一最佳实践的理念与前两个相似。但这一次，你不用给调用方法的人提供信息。异常消息会被所有人读取，同时必须了解在日志文件或监视工具中报告异常时发生了什么。因此，应该尽可能准确地描述问题，并提供相关的信息来了解异常事件。

别误会，你不需要写一段文字，而是应该用 1-2 个简短的句子解释异常的原因。这可以帮助开发团队理解问题的严重性，同时也使你能够更容易地分析任何服务事件。

如果抛出一个特定的异常，它的类名很可能已经描述了这种类型的错误。所以，你不需要提供很多额外的信息。一个很好的例子就是，当你以错误的格式使用字符串时，如 NumberFormatException，它就会被类 java.lang.Long的构造函数抛出。

```java
try {
     new Long( "xyz" );
}  catch (NumberFormatException e) {
     log.error(e);
}
```

#### 5. 最先捕获特定的异常

大多数 IDE 都可以帮助你做到这点，当你试图捕获不确定的异常时，它会报告一个不可到达的代码块。

问题是只有第一个匹配到异常的 catch 语句才会被执行，所以，如果你最先发现 IllegalArgumentException，你将永远不会到达 catch 里处理更具体的 NumberFormatException，因为它是 IllegalArgumentException 的一个子类。所以要首先捕获特定的异常类，并在末尾添加一些处理不是很具体异常的 catch 语句。

你可以在下面的代码片段中看到这样一个 try-catch 语句的示例。第一个 catch 处理所有NumberFormatExceptions 异常，第二个 catch 处理 NumberFormatException 异常以外的illegalargumentexception 异常。

```java
public void catchMostSpecificExceptionFirst() {
    try {
        doSomething("A message");
    } catch (NumberFormatException e) {
        log.error(e);
    } catch (IllegalArgumentException e) {
        log.error(e)
    }
}
```

#### 6. 不要在catch中使用Throwable

Throwable 是 exceptions 和 errors 的父类。当然，你可以在 catch 子句中使用它，但其实你不应该这样做。

如果你在 catch 子句中使用 Throwable，它将不仅捕获所有的异常，还会捕获所有错误。JVM 会抛出错误，这是应用程序不打算处理的严重问题。典型的例子是 OutOfMemoryError 或 StackOverflowError。这两种情况都是由应用程序控制之外的情况引起的，无法处理。

所以，最好不要在 catch 中使用 Throwable，除非你完全确定自己处于一个特殊的情况下，并且你需要处理一个错误。

```java
public void doNotCatchThrowable() {
    try {
        // do something
    } catch (Throwable t) {
        // don't do this!
    }
}
```

##### 7. 不要忽略Exceptions

你是否曾经分析过只有用例的第一部分才被执行的 bug 报告吗?

这通常是由一个被忽略的异常引起的。开发人员可能非常确信它不会被抛出，并添加一个无法处理或无法记录它的catch 语句。当你发现它的时候，你很可能就会明白一句著名的话“This will never happen”。

```java
public void doNotIgnoreExceptions() {
    try {
        // do something
    } catch (NumberFormatException e) {
        // this will never happen
    }
}
```

是的，你可能在分析一个不可能发生的问题。

所以，请千万不要忽略一个例外。你不会知道代码在将来会发生什么变化。有些人可能会删除阻止异常事件的验证，而没有意识到这造成了问题。或者抛出异常的代码被更改，现在抛出了同一个类的多个异常，而调用的代码并不能阻止所有这些异常。

你至少应该写一个日志信息，告诉每个人，需要检查一下这个问题。

```java
public void logAnException() {
    try {
        // do something
    } catch (NumberFormatException e) {
        log.error("This should never happen: " + e);
    }
}
```

##### 8. 不要记录和抛出一个异常

这可能是最常被忽略的。你可以在许多代码片段或者库文件里发现，有异常会被捕获、记录和重新抛出。

```java
try {
    new Long("xyz");
} catch (NumberFormatException e) {
    log.error(e);
    throw e;
}
```

当它发生时记录一个异常，然后重新抛出它，以便调用者能够适当地处理它，这可能会很直观。但是它会为同一个异常写多个错误消息。

```java
17:44:28,945 ERROR TestExceptionHandling:65 - java.lang.NumberFormatException: For input string: "xyz"
Exception in thread "main" java.lang.NumberFormatException: For input string: "xyz"
at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
at java.lang.Long.parseLong(Long.java:589)
at java.lang.Long.(Long.java:965)
at com.stackify.example.TestExceptionHandling.logAndThrowException(TestExceptionHandling.java:63)
at com.stackify.example.TestExceptionHandling.main(TestExceptionHandling.java:58)
```

不添加任何额外的信息。正如在上述第4个中所解释的那样，异常消息应该描述异常事件。堆栈会告诉你在哪个类、方法和行中异常被抛出。

如果你需要添加额外的信息，应该捕获异常并将其包装在一个自定义的信息中。但要确保遵循下面的第 9 条。

```java
public void wrapException(String input) throws MyBusinessException {
    try {
        // do something
    } catch (NumberFormatException e) {
        throw new MyBusinessException("A message that describes the error.", e);
    }
}
```

因此，只需要捕获一个你想要处理的异常，在方法中指定它，并让调用者处理它。

##### 9. 包装异常

有时最好捕获一个标准异常并将其封装到一个定制的异常中。此类异常的典型例子是应用程序或框架特定的业务异常。这允许你添加额外的信息，并且也可以为异常类实现一个特殊的处理。

当你这样做时，确保引用原始的异常处理。Exception 类提供了一些特定的构造函数方法，这些方法可以接受Throwable 作为参数。否则，你将丢失原始异常的堆栈跟踪和消息，这将使你很难分析导致异常的事件。

```java
public void wrapException(String input) throws MyBusinessException {
    try {
        // do something
    } catch (NumberFormatException e) {
        throw new MyBusinessException("A message that describes the error.", e);
    }
}
```

#### 总结

正如你所看到的，在抛出或捕获异常时，有许多不同的事情需要考虑。以上大多数方法都可以提高代码可读性或API 可用性。

异常通常是一个错误处理机制和一个通信媒介。因此，你应该确保同事一起讨论想要应用的最佳实践和方法，以便每个人都理解通用概念并以相同的方式使用它们。