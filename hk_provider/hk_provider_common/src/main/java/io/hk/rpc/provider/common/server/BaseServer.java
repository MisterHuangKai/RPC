package io.hk.rpc.provider.common.server;

import io.hk.rpc.codec.RpcDecoder;
import io.hk.rpc.codec.RpcEncoder;
import io.hk.rpc.provider.common.handler.RpcProviderHandler;
import io.hk.rpc.registry.api.RegistryService;
import io.hk.rpc.registry.api.config.RegistryConfig;
import io.hk.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础服务
 */
public class BaseServer implements Server {

    private final Logger logger = LoggerFactory.getLogger(BaseServer.class);

    /**
     * 主机域名或者IP地址
     */
    protected String host = "127.0.0.1";
    /**
     * 端口号
     */
    protected int port = 27110;
    /**
     * 反射类型
     */
    private String reflectType;
    //
    protected String serverRegistryHost;
    //
    protected int serverRegistryPort;
    /**
     * 服务注册与发现
     */
    protected RegistryService registryService;

    // 存储的是实体类关系
    protected Map<String, Object> handlerMap = new HashMap<>();

    public BaseServer(String serverAddress, String registryAddress, String registryType, String reflectType) {
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress, registryType);
    }

    private RegistryService getRegistryService(String registryAddress, String registryType) {
        // todo 后续扩展支持SPI
        RegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, "1"));
        } catch (Exception e) {
            logger.error("RPC Server init error.", e);
        }
        return registryService;
    }

    @Override
    public void startNettyServer() {
        // 启动netty服务的经典代码模板
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcProviderHandler(reflectType, handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("Server started on {}:{}", host, port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("RPC Server start error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
