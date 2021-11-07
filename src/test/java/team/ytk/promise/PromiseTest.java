/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package team.ytk.promise;

import io.github.vipcxj.jasync.spec.JPromise;
import io.smallrye.mutiny.Uni;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import team.ytk.promise.Promise.RunOn;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PromiseTest {

    @BeforeAll
    void setPromiseRunMode(Vertx vertx) {
        Promise.setVertx(vertx);
    }

    @Test
    void resolveInteger() {
        Assertions.assertEquals(Promise.resolve(1).block(), 1);
    }

    @Test
    void resolveString() {
        Assertions.assertEquals(Promise.resolve("1").block(), "1");
    }

    @Test
    void resolveBoolean() {
        Assertions.assertEquals(Promise.resolve(true).block(), true);
    }

    @Test
    void resolveBigDecimal() {
        Assertions.assertEquals(Promise.resolve(new BigDecimal("1")).block(), new BigDecimal("1"));
    }

    @Test
    void resolve() {
        Assertions.assertEquals(Promise.resolve().block(), null);
    }

    @Test
    void resolveFutureSuccess(Vertx vertx) {
        Assertions.assertEquals(Promise.resolve(Future.succeededFuture(null)).block(), null);
    }

    @Test
    void resolveFutureError() {
        try {
            Assertions.assertEquals(
                Promise.resolve(Future.failedFuture(new RuntimeException("reject"))).block(),
                null
            );
        } catch (RuntimeException error) {
            Assertions.assertEquals(error.getMessage(), "reject");
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void resolveUniSuccess() {
        Assertions.assertEquals(Promise.resolve(Uni.createFrom().nullItem()).block(), null);
    }

    @Test
    void resolveUniError() {
        try {
            Assertions.assertEquals(
                Promise.resolve(Uni.createFrom().failure(new RuntimeException("reject"))).block(),
                null
            );
        } catch (RuntimeException error) {
            Assertions.assertEquals(error.getMessage(), "reject");
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void resolveConsumerHandler() {
        Promise.<Long>resolve(doneCallback -> Vertx.vertx().setTimer(3000, doneCallback)).block();

        Long start = System.currentTimeMillis();
        Long[] timerIds = { 999l };
        Long timerId = Promise
            .<Long>resolve(
                doneCallback -> {
                    timerIds[0] = Vertx.vertx().setTimer(3000, doneCallback);
                }
            )
            .block();
        Long end = System.currentTimeMillis();
        System.out.println(start + ":" + end + ":" + timerId);
        Assertions.assertTrue(end - start > 2900 && timerIds[0] == timerId);
    }

    @Test
    void resolveCompositeFutureAll(Vertx vertx, VertxTestContext testContext) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.complete(timerId));

        Long start = System.currentTimeMillis();

        List<Long> timerIds = Promise
            .<Long>resolve(CompositeFuture.all(sleep1.future(), sleep2.future()))
            .block();
        Long end = System.currentTimeMillis();
        System.out.println(start + ":" + end + ":" + timerIds.get(0) + ":" + timerIds.get(1));
        try {
            Assertions.assertTrue(end - start > 4900 && timerIds.get(0) != timerIds.get(1));
            testContext.completeNow();
        } catch (Exception error) {
            testContext.failNow(error);
        }
    }

    @Test
    void resolveCompositeFutureAny(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.complete(timerId));

        Long start = System.currentTimeMillis();

        List<Long> timerIds = Promise
            .<Long>resolve(CompositeFuture.any(sleep1.future(), sleep2.future()))
            .block();
        Long end = System.currentTimeMillis();
        System.out.println(start + ":" + end + ":" + timerIds.get(0) + ":" + timerIds.get(1));
        Assertions.assertTrue(
            end - start > 2900 && end - start < 3500 && timerIds.get(0) != null && timerIds.get(1) == null
        );
    }

    @Test
    void resolvePromiseAll(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.complete(timerId));

        Long start = System.currentTimeMillis();

        List<Object> timerIds = Promise
            .all(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
            .block();
        Long end = System.currentTimeMillis();
        System.out.println(start + ":" + end + ":" + timerIds.get(0) + ":" + timerIds.get(1));
        Assertions.assertTrue(end - start > 4900 && timerIds.get(0) != timerIds.get(1));
    }

    @Test
    void resolvePromiseAllError(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.fail("error"));

        Long start = System.currentTimeMillis();

        List<Object> timerIds = new ArrayList<Object>();
        try {
            timerIds =
                Promise.all(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future())).block();
            Assertions.assertTrue(false);
        } catch (Exception error) {
            Long end = System.currentTimeMillis();
            Assertions.assertTrue(
                end - start > 2900 &&
                (int) timerIds.size() == 0 &&
                error.getCause().getMessage().equals("error")
            );
        }
    }

    @Test
    void resolvePromiseAllSettle(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.complete(timerId));

        Long start = System.currentTimeMillis();

        List<Object> timerIds = Promise
            .allSettled(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
            .block();
        Long end = System.currentTimeMillis();
        System.out.println(start + ":" + end + ":" + timerIds.get(0) + ":" + timerIds.get(1));
        Assertions.assertTrue(end - start > 4900 && timerIds.get(0) != timerIds.get(1));
    }

    @Test
    void resolvePromiseAllSettleError() {
        Vertx vertx = Vertx.vertx();
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.fail("error"));

        Long start = System.currentTimeMillis();

        List<Object> timerIds = Promise
            .allSettled(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
            .block();
        Long end = System.currentTimeMillis();
        Assertions.assertTrue(
            end - start > 5000 &&
            (int) timerIds.size() == 2 &&
            (long) timerIds.get(0) == 0 &&
            ((NoStackTraceThrowable) timerIds.get(1)).getMessage().equals("error")
        );
    }

    @Test
    void resolvePromiseRace() {
        Vertx vertx = Vertx.vertx();
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.complete(timerId));

        Long start = System.currentTimeMillis();

        Long timerId = (Long) Promise
            .race(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
            .block();
        Long end = System.currentTimeMillis();
        System.out.println("!!!!" + start + ":" + end + ":" + timerId);
        Assertions.assertTrue(end - start > 2900 && end - start < 3500 && timerId == 0);
    }

    @Test
    void resolvePromiseRaceNoError(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.fail("error"));

        Long start = System.currentTimeMillis();

        try {
            Object timerId = Promise
                .race(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
                .block();
            Long end = System.currentTimeMillis();
            Assertions.assertTrue(end - start > 2900 && end - start < 3500 && timerId instanceof Long);
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void resolvePromiseRaceToError(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(5000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(3000, timerId -> sleep2.fail("error"));

        Long start = System.currentTimeMillis();

        try {
            Object timerId = Promise
                .race(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
                .block();
            Assertions.assertTrue(false);
        } catch (Exception error) {
            Long end = System.currentTimeMillis();
            Assertions.assertTrue(
                end - start > 2900 && end - start < 3500 && error.getCause().getMessage().equals("error")
            );
        }
    }

    @Test
    void resolvePromiseAnyNoError(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.fail("error"));

        Long start = System.currentTimeMillis();

        try {
            Object timerId = Promise
                .any(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
                .block();
            Long end = System.currentTimeMillis();
            Assertions.assertTrue(end - start > 2900 && end - start < 3500 && timerId instanceof Long);
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void resolvePromiseAny2NoError(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(5000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(3000, timerId -> sleep2.fail("error"));

        Long start = System.currentTimeMillis();

        try {
            Object timerId = Promise
                .any(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
                .block();
            Long end = System.currentTimeMillis();
            Assertions.assertTrue(end - start > 4900 && end - start < 5100 && timerId instanceof Long);
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void resolvePromiseAnyError(Vertx vertx) {
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(5000, timerId -> sleep1.fail("error"));
        vertx.setTimer(3000, timerId -> sleep2.fail("error"));

        Long start = System.currentTimeMillis();

        try {
            Object timerId = Promise
                .any(Promise.resolve(sleep1.future()), Promise.resolve(sleep2.future()))
                .block();
            Assertions.assertTrue(false);
        } catch (Exception error) {
            Long end = System.currentTimeMillis();
            Assertions.assertTrue(
                end - start > 4900 && end - start < 5100 && error.getCause().getMessage().equals("error")
            );
        }
    }

    @Test
    void deferResolve() {
        try {
            Long start = System.currentTimeMillis();
            JPromise<Long> p = Promise.deferResolve(() -> System.currentTimeMillis());
            Thread.sleep(1000);
            Long end = p.block();
            Assertions.assertTrue(end - start > 900);
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void deferResolvePromise(Vertx vertx) {
        try {
            io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
            vertx.setTimer(5000, timerId -> sleep1.complete(timerId));

            Long start = System.currentTimeMillis();
            JPromise<Long> p = Promise.deferPromiseResolve(() -> Promise.resolve(sleep1.future()));
            Thread.sleep(1000);
            p.block();
            Long end = System.currentTimeMillis();
            Assertions.assertTrue(end - start > 4900 && end - start < 5500);
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void reject() {
        try {
            Promise.reject().block();
        } catch (RuntimeException error) {
            Assertions.assertEquals(error.getMessage(), "reject");
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    void rejectCustomError() {
        try {
            Promise.reject(new NullPointerException()).block();
        } catch (NullPointerException error) {
            Assertions.assertTrue(true);
        } catch (Exception error) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    @SneakyThrows
    void then(VertxTestContext testContext) {
        Vertx vertx = Vertx.vertx();
        io.vertx.core.Promise<Long> sleep1 = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep2 = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep1.complete(timerId));
        vertx.setTimer(5000, timerId -> sleep2.complete(timerId));

        Long start = System.currentTimeMillis();
        Promise
            .resolve(sleep1.future())
            .then(
                timerId -> {
                    return Promise.resolve(sleep2.future());
                }
            )
            .then(
                timerId -> {
                    Long end = System.currentTimeMillis();

                    try {
                        Assertions.assertTrue(end - start > 4900 && end - start < 5100 && timerId == 1);
                        testContext.completeNow();
                    } catch (Exception error) {
                        testContext.failNow(error);
                    }

                    return Promise.resolve();
                }
            )
            .async();
    }

    @Test
    @SneakyThrows
    void thenTryCatchFinally(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint(1);

        io.vertx.core.Promise<Long> sleep3s = io.vertx.core.Promise.promise();
        io.vertx.core.Promise<Long> sleep5s = io.vertx.core.Promise.promise();
        vertx.setTimer(3000, timerId -> sleep3s.complete(timerId)); //睡眠3s后返回timerId
        vertx.setTimer(5000, timerId -> sleep5s.complete(timerId)); //睡眠5s后返回timerId

        Promise
            .resolve(sleep3s.future())
            .then(
                () -> {
                    return Promise.reject("在这里抛了个错。。。");
                }
            )
            .then(sleep3sTimerId -> Promise.resolve(sleep5s.future()))
            .then(
                sleep5sTimerId -> {
                    System.out.println("sleep5sTimerId is:" + sleep5sTimerId);
                    return Promise.resolve();
                }
            )
            .doCatch(
                Exception.class,
                error -> {
                    checkpoint.flag();
                    System.out.println("抓到了一个错误:" + error.getMessage());
                }
            )
            .doFinally(
                () -> {
                    System.out.println("finally");
                    if (testContext.unsatisfiedCheckpointCallSites().size() == 1) {
                        testContext.failNow("没有成功catch");
                    } else {
                        testContext.completeNow();
                    }

                    return Promise.resolve();
                }
            )
            .async();
    }

    @Test
    void resolveRunOnCurrentThread(Vertx vertx, VertxTestContext testContext) {
        Promise
            .deferResolve(
                RunOn.CONTENT_THREAD,
                () -> {
                    if (Thread.currentThread().getName().indexOf("main") == -1) {
                        testContext.failNow("运行的线程错误");
                    } else {
                        testContext.completeNow();
                    }

                    System.out.println(Thread.currentThread().getName());
                    return null;
                }
            )
            .block();
    }

    @Test
    void resolveRunOnEventLoopThread(Vertx vertx, VertxTestContext testContext) {
        Promise
            .deferResolve(
                RunOn.VERTX_EVENT_LOOP_THREAD,
                () -> {
                    if (Thread.currentThread().getName().indexOf("eventloop") == -1) {
                        testContext.failNow("运行的线程错误");
                    } else {
                        testContext.completeNow();
                    }

                    System.out.println(Thread.currentThread().getName());
                    return null;
                }
            )
            .block();
    }

    @Test
    void resolveRunOnWorkerThread(Vertx vertx, VertxTestContext testContext) {
        Promise
            .deferResolve(
                RunOn.VERTX_WORKER_THREAD,
                () -> {
                    if (Thread.currentThread().getName().indexOf("worker") == -1) {
                        testContext.failNow("运行的线程错误");
                    } else {
                        testContext.completeNow();
                    }

                    System.out.println(Thread.currentThread().getName());
                    return null;
                }
            )
            .block();
    }

    @Test
    void resolveRunOnWorkerThreadThen(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint(2);
        Promise
            .resolve(RunOn.VERTX_WORKER_THREAD, 1)
            .then(
                resolver -> {
                    if (Thread.currentThread().getName().indexOf("worker") != -1) checkpoint.flag();
                    return Promise.resolve();
                }
            )
            .then(
                resolver -> {
                    if (Thread.currentThread().getName().indexOf("worker") != -1) checkpoint.flag();
                    return Promise.resolve();
                }
            )
            .block();
        if (testContext.unsatisfiedCheckpointCallSites().size() != 0) {
            testContext.failNow("线程运行错误");
        } else {
            testContext.completeNow();
        }
    }

    @Test
    void changeFutureThreadFail(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint(1);

        Future<Void> future = Future.future(
            handler -> {
                if (Thread.currentThread().getName().indexOf("main") != -1) checkpoint.flag();
                handler.complete();
            }
        );

        Promise.resolve(RunOn.VERTX_EVENT_LOOP_THREAD, future).block();

        if (testContext.unsatisfiedCheckpointCallSites().size() != 0) {
            testContext.failNow("线程运行错误");
        } else {
            testContext.completeNow();
        }
    }

    @Test
    void changeFutureThreadSuccess(Vertx vertx, VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint(1);

        Promise
            .deferPromiseResolve(
                RunOn.VERTX_WORKER_THREAD,
                () -> {
                    Future<Void> future = Future.future(
                        handler -> {
                            if (Thread.currentThread().getName().indexOf("worker") != -1) checkpoint.flag();
                            handler.complete();
                        }
                    );
                    return Promise.resolve(future);
                }
            )
            .block();

        if (testContext.unsatisfiedCheckpointCallSites().size() != 0) {
            testContext.failNow("线程运行错误");
        } else {
            testContext.completeNow();
        }
    }
}
