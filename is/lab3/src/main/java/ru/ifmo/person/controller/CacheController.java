package ru.ifmo.person.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import ru.ifmo.person.interceptor.CacheStatsInterceptor;

import java.util.HashMap;
import java.util.Map;

@Path("/cache")
@Produces(MediaType.APPLICATION_JSON)
public class CacheController {

    @Inject
    private SessionFactory sessionFactory;

    @GET
    @Path("/stats")
    public Response getCacheStats() {
        try {
            Statistics stats = sessionFactory.getStatistics();
            
            Map<String, Object> result = new HashMap<>();
            result.put("secondLevelCacheHitCount", stats.getSecondLevelCacheHitCount());
            result.put("secondLevelCacheMissCount", stats.getSecondLevelCacheMissCount());
            result.put("secondLevelCachePutCount", stats.getSecondLevelCachePutCount());
            
            long hits = stats.getSecondLevelCacheHitCount();
            long misses = stats.getSecondLevelCacheMissCount();
            double hitRatio = (hits + misses) > 0 ? (double) hits / (hits + misses) * 100 : 0.0;
            result.put("hitRatio", hitRatio);
            
            result.put("queryCacheHitCount", stats.getQueryCacheHitCount());
            result.put("queryCacheMissCount", stats.getQueryCacheMissCount());
            result.put("queryCachePutCount", stats.getQueryCachePutCount());
            
            result.put("entityLoadCount", stats.getEntityLoadCount());
            result.put("entityFetchCount", stats.getEntityFetchCount());
            result.put("entityInsertCount", stats.getEntityInsertCount());
            result.put("entityUpdateCount", stats.getEntityUpdateCount());
            result.put("entityDeleteCount", stats.getEntityDeleteCount());
            
            result.put("loggingEnabled", CacheStatsInterceptor.isLoggingEnabled());
            
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/logging/enable")
    @Consumes(MediaType.WILDCARD)
    public Response enableLogging() {
        try {
            CacheStatsInterceptor.enableLogging();
            return Response.ok(Map.of("message", "Cache logging enabled", "enabled", true)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/logging/disable")
    @Consumes(MediaType.WILDCARD)
    public Response disableLogging() {
        try {
            CacheStatsInterceptor.disableLogging();
            return Response.ok(Map.of("message", "Cache logging disabled", "enabled", false)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/evict-all")
    @Consumes(MediaType.WILDCARD)
    public Response evictAll() {
        try {
            sessionFactory.getCache().evictAllRegions();
            return Response.ok(Map.of("message", "All cache regions evicted")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/regions")
    public Response getCacheRegions() {
        try {
            Statistics stats = sessionFactory.getStatistics();
            String[] regionNames = stats.getSecondLevelCacheRegionNames();
            
            Map<String, Map<String, Object>> regions = new HashMap<>();
            for (String regionName : regionNames) {
                Map<String, Object> regionStats = new HashMap<>();
                regionStats.put("hitCount", stats.getCacheRegionStatistics(regionName).getHitCount());
                regionStats.put("missCount", stats.getCacheRegionStatistics(regionName).getMissCount());
                regionStats.put("putCount", stats.getCacheRegionStatistics(regionName).getPutCount());
                regionStats.put("elementCountInMemory", stats.getCacheRegionStatistics(regionName).getElementCountInMemory());
                regionStats.put("sizeInMemory", stats.getCacheRegionStatistics(regionName).getSizeInMemory());
                regions.put(regionName, regionStats);
            }
            
            return Response.ok(regions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/clear-stats")
    @Consumes(MediaType.WILDCARD)
    public Response clearStatistics() {
        try {
            sessionFactory.getStatistics().clear();
            return Response.ok(Map.of("message", "Cache statistics cleared")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }
}
