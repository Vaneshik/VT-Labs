package ru.ifmo.person.interceptor;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import java.io.Serializable;
import java.util.logging.Logger;

@CacheStatsLogging
@Interceptor
@jakarta.annotation.Priority(Interceptor.Priority.APPLICATION)
public class CacheStatsInterceptor implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CacheStatsInterceptor.class.getName());
    
    private static volatile boolean loggingEnabled = true;

    @Inject
    private SessionFactory sessionFactory;

    @AroundInvoke
    public Object logCacheStats(InvocationContext ctx) throws Exception {
        if (!loggingEnabled || sessionFactory == null) {
            return ctx.proceed();
        }

        Statistics stats = sessionFactory.getStatistics();
        
        long hitsBefore = stats.getSecondLevelCacheHitCount();
        long missesBefore = stats.getSecondLevelCacheMissCount();
        long putsBefore = stats.getSecondLevelCachePutCount();

        Object result = ctx.proceed();

        long hitsAfter = stats.getSecondLevelCacheHitCount();
        long missesAfter = stats.getSecondLevelCacheMissCount();
        long putsAfter = stats.getSecondLevelCachePutCount();

        long hitsDelta = hitsAfter - hitsBefore;
        long missesDelta = missesAfter - missesBefore;
        long putsDelta = putsAfter - putsBefore;

        if (hitsDelta > 0 || missesDelta > 0 || putsDelta > 0) {
            String methodName = ctx.getMethod().getDeclaringClass().getSimpleName() + "." + ctx.getMethod().getName();
            double hitRatio = (hitsAfter + missesAfter) > 0 
                ? (double) hitsAfter / (hitsAfter + missesAfter) * 100 
                : 0.0;

            LOGGER.info(String.format(
                "L2 Cache Stats [%s] - Hits: +%d (%d total), Misses: +%d (%d total), Puts: +%d (%d total), Hit Ratio: %.2f%%",
                methodName, hitsDelta, hitsAfter, missesDelta, missesAfter, putsDelta, putsAfter, hitRatio
            ));
        }

        return result;
    }

    public static void enableLogging() {
        loggingEnabled = true;
        LOGGER.info("Cache statistics logging enabled");
    }

    public static void disableLogging() {
        loggingEnabled = false;
        LOGGER.info("Cache statistics logging disabled");
    }

    public static boolean isLoggingEnabled() {
        return loggingEnabled;
    }
}
