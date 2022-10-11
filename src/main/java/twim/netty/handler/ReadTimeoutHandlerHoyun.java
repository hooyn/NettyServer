package twim.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import twim.netty.server.NettySocketServerHandler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReadTimeoutHandlerHoyun extends ChannelInboundHandlerAdapter {
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private long timeoutNanos;

    private long lastReadTime;

    private volatile ScheduledFuture<?> timeout;

    private volatile int state; // 0 - none, 1 - Initialized, 2 - Destroyed;

    private volatile boolean reading;
    private boolean closed;

    /**
     * Creates a new instance.
     *
     * @param timeoutSeconds
     *        read timeout in seconds
     */
    public ReadTimeoutHandlerHoyun(int timeoutSeconds) {
        this(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Creates a new instance.
     *
     * @param timeout
     *        read timeout
     * @param unit
     *        the {@link TimeUnit} of {@code timeout}
     */
    public ReadTimeoutHandlerHoyun(long timeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        if (timeout <= 0) {
            timeoutNanos = 0;
        } else {
            timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
        }
    }

    public ReadTimeoutHandlerHoyun(long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            // channelActvie() event has been fired already, which means this.channelActive() will
            // not be invoked. We have to initialize here instead.
            initialize(ctx);
        } else {
            // channelActive() event has not been fired yet.  this.channelActive() will be invoked
            // and initialization will occur there.
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        destroy();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // Initialize early if channel is active already.
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // This method will be invoked only if this handler was added
        // before channelActive() event is fired.  If a user adds this handler
        // after the channelActive() event, initialize() will be called by beforeAdd().
        initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        reading = true;
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        lastReadTime = System.nanoTime();
        reading = false;
        ctx.fireChannelReadComplete();
    }

    private void initialize(ChannelHandlerContext ctx) {
        // Avoid the case where destroy() is called before scheduling timeouts.
        // See: https://github.com/netty/netty/issues/143
        switch (state) {
            case 1:
            case 2:
                return;
        }

        state = 1;

        lastReadTime = System.nanoTime();
        if (timeoutNanos > 0) {
            timeout = ctx.executor().schedule(
                    new ReadTimeoutHandlerHoyun.ReadTimeoutTask(ctx),
                    timeoutNanos, TimeUnit.NANOSECONDS);
        }
    }

    private void destroy() {
        state = 2;

        if (timeout != null) {
            timeout.cancel(false);
            timeout = null;
        } else {
            //서버 연결 종료 로직

        }
    }

    /**
     * Is called when a read timeout was detected.
     */
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        if (!closed) {
            log.error("********** TIME OUT: " + Thread.currentThread().getId() + " **********");
            NettySocketServerHandler.banList.add(Thread.currentThread().getId());

            //ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE); -> 새로운 쓰레드를 생성하는 것을 막기 위해 주석처리
            //ctx.fireChannelRead(""); -> Main 으로 메세지 보내기 가능
        }
    }

    private final class ReadTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }

            long nextDelay = timeoutNanos;
            if (!reading) {
                nextDelay -= System.nanoTime() - lastReadTime;
            }

            if (nextDelay <= 0) {
                // Read timed out - set a new timeout and notify the callback.
                //timeout = ctx.executor().schedule(this, timeoutNanos, TimeUnit.NANOSECONDS);
                try {
                    readTimedOut(ctx);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Read occurred before the timeout - set a new timeout with shorter delay.
                timeout = ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }
}
