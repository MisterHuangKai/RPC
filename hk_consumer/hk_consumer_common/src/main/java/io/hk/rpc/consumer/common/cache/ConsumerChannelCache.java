package io.hk.rpc.consumer.common.cache;

import java.nio.channels.Channel;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 在服务消费者端,缓存,连接服务提供者成功的Channel
 */
public class ConsumerChannelCache {

    private static volatile Set<Channel> channelCache = new CopyOnWriteArraySet<>();

    public static void add(Channel channel) {
        channelCache.add(channel);
    }

    public static void remove(Channel channel) {
        channelCache.remove(channel);
    }

    public static Set<Channel> getChannelCache() {
        return channelCache;
    }

}
