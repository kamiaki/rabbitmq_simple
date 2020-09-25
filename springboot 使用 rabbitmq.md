# springboot 使用 rabbitmq

## SpringBoot整合RabbitMQ

添加依赖：

```
<!-- 添加springboot对amqp的支持 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
```

在application.yml文件中添加配置

```
spring:
  application:
    name: async-task
  rabbitmq:
    host: 192.168.255.255
    port: 5672
    username: xxx
    password: 123456
```

## Direct模式

Direct是RabbitMQ默认的交换机模式,也是最简单的模式.即创建消息队列的时候,指定一个路由键（RoutingKey）.当发送者发送消息的时候,指定对应的Key.当Key和消息队列的RoutingKey一致的时候,消息将会被发送到该消息队列中.

```
@Configuration
public class RabbitMQConfig{
    // 交换机有四种类型,分别为Direct,topic,headers,Fanout.

    // Direct 模式创建队列
    // 创建队列
    @Bean
    public Queue testQueue() {
        return new Queue("queueName");
    }

    // 创建一个交换机
    @Bean
    public DirectExchange testExchange() {
        return new DirectExchange("exchangeName");
    }

    // 把队列和交换机绑定在一起
    @Bean
    public Binding testBinding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("routingKey");
    }
}

// 消息生产者
@Component
public class HelloSender {
    @Autowired
    private RabbitTemplate template;
    
    public void send() {
    template.convertAndSend("exchangeName", "routingKey", "hello,rabbit~");
    }
}

// 定义消费者
@Component
public class TestConsumer {

    @RabbitListener(queues = "queueName")
    public void process(String data){
        log.info("------------data: {}",data);
    }
}
```

AmqpTemplate  发送的消息数据还可以是对象，但对象必须序列化

```
// 消息生产者
@Component
public class HelloSender {
    @Autowired
    private RabbitTemplate template;
    
    public void send() {
    User user = new User("name","password");
    template.convertAndSend("exchangeName", "routingKey",user);
    }
}


// 定义消费者
@Component
public class TestConsumer {

    @RabbitListener(queues = "queueName")
    public void process(User user){
        log.info("------------user: {}",user);
    }
```

@RabbitListener 可以作用在类上，需要和 @RabbitHandler 配合使用

```
@Component
@RabbitListener(queues = "queueName")
public class TestConsumer {

    @RabbitHandler
    public void process(String data){
        log.info("------------data: {}",data);
    }

    @RabbitHandler
    public void process(UserInfo userInfo){
        log.info("------------userInfo: {}",userInfo);
    }
    
    public void test(){
        log.info("------------");
    }
}
```

上面的TestConsumer 消费者接收所有路由键为 routingKey 的消息，队列中的消息会转发到被@RabbitHandler修饰的方法然后被消费，不同的消息类型被转发到对应的方法中。test()方法不会消费消息
 RabbitMq 服务启动后会创建一个默认的DirectExchange，这个交换机只接收 路由键routingKey 和 队列名称相同的消息，所以direct模式可以简化：

```
@Configuration
public class RabbitMQConfig{
    // Direct 模式创建队列
    // 创建队列
    @Bean
    public Queue testQueue() {
        return new Queue("routingKey");  // 队列名和routingKey相同
    }
}

// 消息生产者
@Component
public class HelloSender {
    @Autowired
    private RabbitTemplate template;
    
    public void send() {
    template.convertAndSend("routingKey", "hello,rabbit~"); // 没有交换机名称，消息会被发送到默认交换机，然后被转发到 名称和routingKey相同的队列上
    }
}

// 定义消费者
@Component
public class TestConsumer {

    @RabbitListener(queues = "routingKey")
    public void process(String data){
        log.info("------------data: {}",data);
    }
}
```

## Topic模式

topic转发信息主要是依据通配符,队列和交换机的绑定主要是依据一种模式(通配符+字符串),而当发送消息的时候,只有指定的Key和该模式相匹配的时候,消息才会被发送到该消息队列中.
 通配符：* 表示一个词，# 表示零个或多个词
 **注意： 通配符是针对交换机的！！！**也就是说消息进入交换机时才进行通配符匹配，匹配完了以后才进入固定的队列

```
@Configuration
public class RabbitMQConfig{

   // 交换机有四种类型,分别为Direct,topic,headers,Fanout.

   // topit 模式

   @Bean(name="queueName1")
   public Queue queueMessage1() {
       return new Queue("queueName1");      // 定义第一个队列，名称为 queueName1
   }
   @Bean(name="queueName2")
   public Queue queueMessage2() {
       return new Queue("queueName2");     // 定义第二个队列，名称为 queueName2
   }
   @Bean
   public TopicExchange exchange() {
       return new TopicExchange("exchangeName");  // 定义交换机
   }
   // 定义绑定关系，通过交换机 将名称为queueName1 的队列绑定到交换机上， routingKey 为 topic.key1
   @Bean
   public Binding bindingExchangeMessage(@Qualifier("queueName1") Queue queue, TopicExchange exchange) {
       return BindingBuilder.bind(queue).to(exchange).with("topic.key1");
   }
   // 定义另一个绑定关系，通过交换机 将名称为queueName2 的队列绑定到交换机上 ，routingKey 是符合 通配符topic.#  的路由键
   // 如：topic.xx、topic.yy 等
   @Bean
   public Binding bindingExchangeMessages(@Qualifier("queueName2") Queue queue, TopicExchange exchange) {
       return BindingBuilder.bind(queue).to(exchange).with("topic.#");//*表示一个词,#表示零个或多个词
   }
```

消费者

```
    @RabbitListener(queues="queueName1")    //监听器监听指定的Queue
    public void process1(String str) {    
        System.out.println("message:"+str);
    }
    @RabbitListener(queues="queueName2")    //监听器监听指定的Queue
    public void process2(String str) {
        System.out.println("messages:"+str);
    }
```

然后发送消息

```
// 2个消费者都会收到消息
template.convertAndSend("exchangeName", "topic.key1", "data info");
// 只有第2个消费者收到消息
template.convertAndSend("exchangeName", "topic.key2", "data info");
// 只有第2个消费者收到消息
template.convertAndSend("exchangeName", "topic.key3", "data info");
```

## Fanout模式

fanout是路由广播的形式,将会把消息发给绑定它的全部队列,即便设置了key,也会被忽略.
 因此我们发送到交换机的消息会使得绑定到该交换机的每一个Queue接收到消息,这个时候就算指定了路由键（routingKey）,或者规则(即上文中convertAndSend方法的参数2),也会被忽略!

```
// fanout模式
    @Bean(name="Amessage")
    public Queue AMessage() {
        return new Queue("fanout.A");
    }


    @Bean(name="Bmessage")
    public Queue BMessage() {
        return new Queue("fanout.B");
    }

    @Bean(name="Cmessage")
    public Queue CMessage() {
        return new Queue("fanout.C");
    }

    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanoutExchange");//配置广播路由器
    }

    @Bean
    Binding bindingExchangeA(@Qualifier("Amessage") Queue AMessage,FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(AMessage).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeB(@Qualifier("Bmessage") Queue BMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(BMessage).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeC(@Qualifier("Cmessage") Queue CMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(CMessage).to(fanoutExchange);
    }
```

然后发送消息

```
template.convertAndSend("fanoutExchange", "", "data info"); // 第二个参数会被忽略
```

消费者

```
    @RabbitListener(queues="fanout.A")
    public void processA(String str1) {
        System.out.println("ReceiveA:"+str1);
    }
    @RabbitListener(queues="fanout.B")
    public void processB(String str) {
        System.out.println("ReceiveB:"+str);
    }
    @RabbitListener(queues="fanout.C")
    public void processC(String str) {
        System.out.println("ReceiveC:"+str);
    }
```

结果三个都收到消息

 

![img](upload-images.jianshu.io/upload_images/6907979-796ccbfa3fd93257.png?imageMogr2/auto-orient/strip|imageView2/2/w/523/format/webp)

image.png

## RabbitMQ 实现延迟队列

rabbitMQ可以通过死信机制来实现延迟队列的功能，一些概念：
 1、TTL ，即 Time-To-Live，存活时间，消息和队列都可以设置存活时间
 2、Dead Letter，即死信，若给消息设置了存活时间，当超过存活时间后消息还没有被消费，则该消息变成了死信
 3、Dead Letter Exchanges（DLX），即死信交换机
 4、Dead Letter Routing Key （DLK），死信路由键
 直接上代码：

```
@Configuration
public class RabbitMQConfig { 

    // 创建一个立即消费队列
    @Bean(QueueName.ImmediateQueue)
    public Queue immediateQueue() {
        return new Queue(QueueName.ImmediateQueue);
    }

    @Bean(ExchangeName.IMMEDIATE)
    public DirectExchange immediateExchange() {
        return new DirectExchange(ExchangeName.IMMEDIATE);
    }

    // 把 立即消费的队列 和 立即消费的exchange 绑定在一起
    @Bean
    public Binding immediateBinding(@Qualifier(QueueName.ImmediateQueue) Queue queue, @Qualifier(ExchangeName.IMMEDIATE) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RoutingKey.IMMEDIATE_ROUTING_KEY);
    }


    // 创建一个延时队列
    @Bean(QueueName.DelayQueue)
    public Queue delayQueue() {
        Map<String, Object> params = new HashMap<>();

        // x-dead-letter-exchange 声明了队列里的死信转发到的DLX名称，
        params.put("x-dead-letter-exchange", ExchangeName.IMMEDIATE);

        // x-dead-letter-routing-key 声明了这些死信在转发时携带的 routing-key
        params.put("x-dead-letter-routing-key", RoutingKey.IMMEDIATE_ROUTING_KEY);

        // 设置队列中消息的过期时间，单位 毫秒
        params.put("x-message-ttl", 5 * 1000);

        return new Queue(QueueName.DelayQueue, true, false, false, params);
    }

    @Bean(ExchangeName.DELAY)
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(ExchangeName.DEAD_LETTER);
    }

    // 把 延迟消费的队列 和 延迟消费的exchange 绑定在一起
    @Bean
    public Binding delayBinding(@Qualifier(QueueName.DelayQueue) Queue queue, @Qualifier(ExchangeName.DELAY) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RoutingKey.DELAY_KEY);
    }
}

@Component
public class TestConsumer {
    @RabbitListener(queues = QueueName.ImmediateQueue)
    public void process2(UserInfo userInfo){
        log.info("------------userInfo: {}",userInfo);
    }
}

// 常量
public interface ExchangeName {
    String IMMEDIATE = "immediate";
    String DELAY = "delay";
}
public interface QueueName {
    String ImmediateQueue = "ImmediateQueue";
    String DelayQueue = "DelayQueue";
}
public interface RoutingKey {
    String IMMEDIATE_ROUTING_KEY = "immediate.key";
    String DELAY_KEY = "delay.key";
}
```

![img](upload-images.jianshu.io/upload_images/6907979-96b460f7bd546fcd.png?imageMogr2/auto-orient/strip|imageView2/2/w/864/format/webp)

image.png

过程：
 1、先创建一个普通队列，即上面的 ImmediateQueue，创建一个普通交换机 immediateExchange，绑定两者。
 2、创建一个延迟队列，即创建时设置了参数：x-dead-letter-exchange，x-dead-letter-routing-key，x-message-ttl，该队列就相当于是一个延迟队列了
 3、创建延迟交换机（其实也是普通交换机）,和延迟队列绑定
 4、给ImmediateQueue创建监听消费者，注意，延迟队列不要设置监听消费者，不然延迟队列就变成普通队列了，不起作用
 到此延迟队列已完成，直接发送消息到延迟交换机即可

```
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone("15800000000");
        userInfo.setUserName("aaaaaaa");

        log.info("开始发送消息");
        template.convertAndSend(ExchangeName.DELAY, RoutingKey.DELAY_KEY, userInfo);
```

原理：发送消息到延迟交换机，延迟交换机将消息转发到延迟队列，因为延迟队列没有监听消费者，所以消息不会被消费，直到消息超过存活时间（即 延迟）变成死信，这时延迟队列会将死信转发到死信交换机，即上面的immediateExchange（因为延迟队列绑定的死信交换机x-dead-letter-exchange指向了immediateExchange），immediateExchange将消息转发给ImmediateQueue，然后被监听消费者消费

可以看出过了5秒才消费消息

**注意：** 一个延迟队列只能设置一个存活时间，即该延迟队列里面的所有消息的存活时间都必须一致，如果需要设置不一样的存活时间，只能再创建一个延迟队列。原因是延迟队列并不会去扫描队列里面所有消息的存活时间，只会判断队列头的第一个消息是否过期，若过期了就转发消息，否则一直等待，即使队列后面已经有消息先过期，也只能等前面的消息被转发后，该消息才被转发。

## 消息确认机制

消息确认分为两部分： 生产确认 和 消费确认。
 生产确认： 生产者生产消息后，将消息发送到交换机，触发确认回调；交换机将消息转发到绑定队列，若失败则触发返回回调。
 消费确认： 默认情况下消息被消费者从队列中获取后即发送确认，不管消费者处理消息时是否失败，不需要额外代码，但是不能保证消息被正确消费。我们增加手动确认，则需要代码中明确进行消息确认。
 在配置文件中添加：

```
spring:
 application:
   name: async-task
 rabbitmq:
   host: 192.168.0.0
   port: 5672
   username: username
   password: password
   publisher-confirms: true  # 开启发送确认
   publisher-returns: true   # 开启发送失败退回
   template:
     mandatory: true
   listener:
     type: simple
     simple:
       acknowledge-mode: manual # 开启消息消费手动确认
```

在RabbitMQConfig 中添加如下配置

```
    @Bean
    public RabbitTemplate getTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // 消息发送到交换器Exchange后触发回调
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //  可以进行消息入库操作
                log.info("消息唯一标识 correlationData = {}", correlationData);
                log.info("确认结果 ack = {}", ack);
                log.info("失败原因 cause = {}", cause);
            }
        });

        // 配置这个，下面的ReturnCallback 才会起作用
        template.setMandatory(true);
        // 如果消息从交换器发送到对应队列失败时触发（比如 根据发送消息时指定的routingKey找不到队列时会触发）
        template.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //  可以进行消息入库操作
                log.info("消息主体 message = {}", message);
                log.info("回复码 replyCode = {}", replyCode);
                log.info("回复描述 replyText = {}", replyText);
                log.info("交换机名字 exchange = {}", exchange);
                log.info("路由键 routingKey = {}", routingKey);
            }
        });

        return template;
    }
```

成功确认：
 void basicAck(long deliveryTag, boolean multiple) throws IOException;
 deliveryTag:该消息的index
 multiple：是否批量. true：将一次性ack所有小于deliveryTag的消息。
 消费者成功处理后，调用channel.basicAck(message.getMessageProperties().getDeliveryTag(), false)方法对消息进行确认。

失败确认：
 void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException;
 deliveryTag:该消息的index。
 multiple：是否批量. true：将一次性拒绝所有小于deliveryTag的消息。
 requeue：是否重新入队列。

拒绝
 void basicReject(long deliveryTag, boolean requeue) throws IOException;
 deliveryTag:该消息的index。
 requeue：被拒绝的是否重新入队列。

channel.basicNack 与 channel.basicReject 的区别在于basicNack可以批量拒绝多条消息，而basicReject一次只能拒绝一条消息

消费者消息确认代码：

```
@Component
public class OrderConsumer {

    @Autowired
    private TaskOrderFeign orderFeign;

    @RabbitListener(queues = QueueName.OrderCancelQueue)
    public void process(OrderDTO orderDTO, Message message, Channel channel){

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("---消费消息---------deliveryTag = {} ,  orderDTO: {}",deliveryTag ,orderDTO);

            // 取消订单
            orderDTO.setState(OrderStateEnum.DELAY_CANCEL.code);
            Response response = orderFeign.cancelOrder(orderDTO);

            //TODO 判断结果，是否需要重试

            // 成功确认消息
            channel.basicAck(deliveryTag, true);

        } catch (IOException e) {
            log.error("确认消息时抛出异常 ， e = {}", PrintUtil.print(e));
            // 重新确认
            // 成功确认消息
            try {
                Thread.sleep(50);
                channel.basicAck(deliveryTag, true);
            } catch (IOException | InterruptedException e1) {
                log.error("确认消息时抛出异常 ， e = {}", PrintUtil.print(e));
                // 可以考虑入库
            }

        } catch (Exception e) {

            log.error("取消订单失败 ， e = {}", PrintUtil.print(e));

            try {
                // 失败确认
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException e1) {
                log.error("消息失败确认失败 ， e1 = {}", PrintUtil.print(e1));
            }
        }
    }
}
```