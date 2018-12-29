package org.activiti.cloud.services.graphql.ws.transport;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisherAware;
import reactor.core.publisher.WorkQueueProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.WaitStrategy;

/**
 * Implementation of an {@link org.springframework.core.task.AsyncTaskExecutor} that is backed by a Reactor {@link
 * WorkQueueProcessor}.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 * @since 1.1, 2.5
 */
public class WorkQueueAsyncTaskExecutor extends AbstractAsyncTaskExecutor implements ApplicationEventPublisherAware {

    private final Logger log = LoggerFactory.getLogger(WorkQueueAsyncTaskExecutor.class);

    private WaitStrategy                      waitStrategy;
    private WorkQueueProcessor<Runnable> workQueue;

    public WorkQueueAsyncTaskExecutor() {
        this(Schedulers.parallel());
    }

    public WorkQueueAsyncTaskExecutor(Scheduler timer) {
        super(timer);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.workQueue = WorkQueueProcessor.<Runnable> builder()
                                           .share(isShared())
                                           .name(getName())
                                           .bufferSize(getBacklog())
                                           .waitStrategy((null != waitStrategy ? waitStrategy : WaitStrategy.blocking()))
                                           .build();
        
        if (isAutoStartup()) {
            start();
        }
    }

    /**
     * Get the {@link reactor.util.concurrent.WaitStrategy} this {@link reactor.util.concurrent.RingBuffer} is using.
     *
     * @return the {@link reactor.util.concurrent.WaitStrategy}
     */
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    /**
     * Set the {@link reactor.util.concurrent.WaitStrategy} to use when creating the internal {@link
     * reactor.util.concurrent.RingBuffer}.
     *
     * @param waitStrategy
     *      the {@link reactor.util.concurrent.WaitStrategy}
     */
    public void setWaitStrategy(WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
    }

    @Override
    protected WorkQueueProcessor<Runnable> getProcessor() {
        return workQueue;
    }

}