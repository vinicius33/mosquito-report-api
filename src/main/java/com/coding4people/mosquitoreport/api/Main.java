package com.coding4people.mosquitoreport.api;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.internal.FilteringJacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.JacksonFilteringFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.coding4people.mosquitoreport.api.buckets.BucketBinder;
import com.coding4people.mosquitoreport.api.controllers.AuthEmailController;
import com.coding4people.mosquitoreport.api.controllers.FocusController;
import com.coding4people.mosquitoreport.api.controllers.PictureController;
import com.coding4people.mosquitoreport.api.controllers.PostFocusController;
import com.coding4people.mosquitoreport.api.controllers.ProfileController;
import com.coding4people.mosquitoreport.api.controllers.SignUpController;
import com.coding4people.mosquitoreport.api.controllers.ThumbsUpController;
import com.coding4people.mosquitoreport.api.exceptionmappers.BadRequestExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmappers.ForbiddenExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmappers.InternalServerErrorExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmappers.JsonMappingExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmappers.JsonParseExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmappers.NotFoundExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmappers.ValidationExceptionMapper;
import com.coding4people.mosquitoreport.api.factories.FactoryBinder;
import com.coding4people.mosquitoreport.api.filters.CORSFilter;
import com.coding4people.mosquitoreport.api.indexers.IndexerBinder;
import com.coding4people.mosquitoreport.api.repositories.RepositoryBinder;

public class Main {
    public static final String BASE_URI = "http://0.0.0.0:9000/";

    public static ResourceConfig createApp() {
        return commonConfig()
                // Controllers
                .register(AuthEmailController.class)
                .register(FocusController.class)
                .register(PictureController.class)
                .register(PostFocusController.class)
                .register(ProfileController.class)
                .register(SignUpController.class)
                .register(ThumbsUpController.class)
                
                // Exception mappers
                .register(BadRequestExceptionMapper.class)
                .register(ForbiddenExceptionMapper.class)
                .register(InternalServerErrorExceptionMapper.class)
                .register(JsonMappingExceptionMapper.class)
                .register(JsonParseExceptionMapper.class)
                .register(NotFoundExceptionMapper.class)
                .register(ValidationExceptionMapper.class)
                
                // Filters
                .register(CORSFilter.class)
                
                // Binders
                .register(new BucketBinder())
                .register(new FactoryBinder())
                .register(new IndexerBinder())
                .register(new RepositoryBinder());
    }
    
    public static ResourceConfig commonConfig() {
        return new ResourceConfig()
                .property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true)
                .register(EntityFilteringFeature.class)
                .register(JacksonFilteringFeature.class)
                .register(FilteringJacksonJaxbJsonProvider.class)
                .register(MultiPartFeature.class);
    }

    public static void main(String[] args) throws IOException {
        try {
            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), createApp());

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));

            server.start();

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
