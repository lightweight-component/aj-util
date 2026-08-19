# 编程语言的风格约束

对于 C 语系的编程语言，包括 C/Java/Js/Ts 统一采用下面的语言风格生成代码。

## 单行 if/for/while 可以不用尖括号

例如：
```js
  if (readonly.value) {
    return;
  }
```

要求是：

```js
  if (readonly.value) 
    return;
```

`else`语句也是如此。

## if/for/while 语块之间有一个空行

例如：
```javascript
  if (readonly.value) 
    return;
  if (type === "start" && Object.values(workflow.value.states).some((node) => node.type === "start")) {
    window.alert("流程只能包含一个开始节点");
    return;
  }
```

要求是：

```javascript
  if (readonly.value) 
    return;
    
  if (type === "start" && Object.values(workflow.value.states).some((node) => node.type === "start")) {
    window.alert("流程只能包含一个开始节点");
    return;
  }
```

## 对于可以省略变量类型的语言，尽量加上类型提示

例如 ts、swift，不应省略，以便于人类阅读，例如`let index = 1;`应该是` let index:number = 1;`

## `return` 所在的行，上一行应该有一个空行


# 注释

整理整个项目 Java 代码的注释，要求：

- 包括类、字段、方法，私有方法也加入注释，单测的方法不需要加入注释（当然已经有的则保留）
- 方法内部不需要加入注释，如果已经有则不处理
- 如果已经有注释则保留，如果没有则加入。不需要统一语言，如果原来是中文的就保留中文，原来是英文的就保留英文
- 注释要求符合 JavaDoc 注释规范，包括方法的描述、入参、出参、异常、返回值等。如果已经有的注释，保持不变，缺少的则补充
- 形成 skill 并保存到源码 git 中分享，让 AI 学习如何使用这个库

某些注释转换为多行注释（这样才符合 javadoc），举例：

```java
// 路径遍历关键字列表
private static final Set<String> illegalFileStrList;
```

转换为：

```java
/**
 * 路径遍历关键字列表
 */
private static final Set<String> illegalFileStrList;
```

字段与下一行的注释之间有一行空格，例如：

```java
/**
 * Defines the file dir.
 */
private static final String fileDir = ConfigConstants.getFileDir();
/**
 * Defines the url param ftp username.
 */
private static final String URL_PARAM_FTP_USERNAME = "ftp.username";
```
转换为：


```java
/**
 * Defines the file dir.
 */
private static final String fileDir = ConfigConstants.getFileDir();

/**
 * Defines the url param ftp username.
 */
private static final String URL_PARAM_FTP_USERNAME = "ftp.username";
```


# 单元测试

针对该项目的 java 源码，补充单元测试，要求：

- 为了可以单测，把所有`private`方法去掉，改为`default`方法。去掉 private 就可以了 不用显式声明 default
- 每个方法安排一个至少一个单元测试，测试方法的正常情况和异常情况，非必要可以不安排异常测试。如果已经有单测了则不需要额外增加
- 如果要实现测试太困难的话，可以放弃，比如没有测试数据或者设计假对象太麻烦了
- 不用太追求单测覆盖率，否则会导致测试代码量增加，长篇大论
- getter/setter 这些方法当然不用测试，除非里面有比较复杂的逻辑。一些异常类或者低价值的也不用单测
- 所有测试类的名称都要以`Test`开头，例如`TestQrCode`
- 所有测试类不加 public 关键字（类和方法），默认 default 即可

