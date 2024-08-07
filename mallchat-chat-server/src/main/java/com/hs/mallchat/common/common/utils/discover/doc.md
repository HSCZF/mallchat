### URL解析

#### 1、用到了4种设计模式

* 把标题的解析方式做成解析器。每个解析器串成一个链条。通用的解析器优先级更高。链条中直到其中某个解析器解析出标题，就返回。
  解析器串起的链条就是责任链模式，创建责任链的地方就是工厂模式。而不同的类实现不同的url解析方法，这就是策略模式，而抽象类里面的逻辑，又像是模板方法模式，一口气就能实现四种模式。

#### 2、各模式的作用

* 责任链模式
    * 定义: 使多个对象都有机会处理请求，从而避免请求的发送者和接收者之间的耦合关系。将这些对象连成一条链，并沿着这条链传递该请求，直到有一个对象处理它为止。
    * PrioritizedUrlDiscover()内部维护了一个 List<UrlDiscover> 的集合，并在构造函数中添加了其他 UrlDiscover 的实例（如
      WxUrlDiscover 和 CommonUrlDiscover）。
    * 调用 PrioritizedUrlDiscover 的方法，它会按顺序执行责任链，直到解析出url标题。
* 工厂模式
    * 定义: 提供了一个创建一系列相关或相互依赖对象的接口，而无需指定它们具体的类。
    * 在 PrioritizedUrlDiscover 的构造函数中，通过直接实例化 WxUrlDiscover 和 CommonUrlDiscover 并将它们添加到 List<UrlDiscover> 中。
    * 这种方式可以看作是简单工厂模式的一种应用，即在构造函数中创建并返回多个 UrlDiscover 的实例。
    * 如果未来需要支持更多的 UrlDiscover 实现类，可以通过扩展工厂类来实现，而不需要修改现有的代码。
* 策略模式
    * 定义: 定义了一系列的算法,把它们一个个封装起来, 并且使它们可相互替换。本模式使得算法可独立于使用它的客户而变化。
    * PrioritizedUrlDiscover 通过组合不同的 UrlDiscover 实例来实现不同的策略
    * 不同的 UrlDiscover 实现类（如 CommonUrlDiscover, WxUrlDiscover）代表了不同的策略，可以在运行时动态选择使用哪个策略。
    * 这种方式使得系统可以根据不同的条件或需求选择最合适的 UrlDiscover 实现。
    * prioritizedUrl是我们的策略类，同时也是组装责任链的工厂。如果你调用它，它会按顺序执行责任链，直到解析出url标题。
* 模板方法模式
    * 定义: 定义了一个算法的骨架，而将一些步骤延迟到子类中。使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤。 
    * 公共的逻辑放在抽象类AbstractUrlDiscover，getUrlContentMap和getContent
    * getTitle，getDescription，getImage，放在子类实现