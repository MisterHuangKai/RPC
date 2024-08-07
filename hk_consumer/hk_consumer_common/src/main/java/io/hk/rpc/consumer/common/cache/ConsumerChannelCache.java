package io.hk.rpc.consumer.common.cache;

import io.hk.rpc.constants.RpcConstants;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在服务消费者端,缓存,连接服务提供者成功的Channel
 */
public class ConsumerChannelCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerChannelCache.class);

    private static volatile Set<Channel> channelCache = new CopyOnWriteArraySet<>();
    /**
     * 作业63-x: 维护心跳待响应次数
     */
    private static volatile Map<String, AtomicInteger> waitingPongTimesMap = new ConcurrentHashMap<>();

    public static Set<Channel> getChannelCache() {
        return channelCache;
    }

    public static void add(Channel channel) {
        channelCache.add(channel);
        // 作业63-x
        waitingPongTimesMap.put(getKey(channel), new AtomicInteger(0));
    }

    public static void remove(Channel channel) {
        channelCache.remove(channel);
        // 作业63-x
        waitingPongTimesMap.remove(getKey(channel));
    }

    // ============ 作业63-x: 关于心跳待响应次数的检查、增加、减少 ============

    /**
     * 生成key: ip_port
     * <p>作业63-x
     */
    private static String getKey(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        return socketAddress.getAddress().getHostAddress().concat("_").concat(String.valueOf(socketAddress.getPort()));
    }

    /**
     * 收到服务提供者pong后,对应channel等待数-1
     * <p>作业63-x
     */
    public static int decreaseWaitTimes(Channel channel) {
        AtomicInteger count = waitingPongTimesMap.get(getKey(channel));
        if (count != null) {
            return count.decrementAndGet();
        }
        return 0;
    }

    /**
     * 给服务提供者发送ping后,对应channel等待数+1
     * <p>作业63-x
     */
    public static int increaseWaitTimes(Channel channel) {
        AtomicInteger count = waitingPongTimesMap.get(getKey(channel));
        if (count != null) {
            return count.incrementAndGet();
        }
        return 0;
    }

    /**
     * 检查是否超过3次心跳没有响应
     * <p>作业63-x
     */
    public static boolean isWaitTimesOverflow(Channel channel) {
        AtomicInteger count = waitingPongTimesMap.get(getKey(channel));
        if (count != null) {
            return count.get() >= RpcConstants.MAX_WAITING_PONG_TIMES;
        }
        return false;
    }

}
